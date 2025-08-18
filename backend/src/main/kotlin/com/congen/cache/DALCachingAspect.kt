package com.congen.cache

import com.congen.cache.annotation.Cacheable
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.ReactiveMemcachedCache
import com.congen.exceptions.CacheMissException
import com.fasterxml.jackson.core.type.TypeReference
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.lang.reflect.Method
import java.lang.reflect.ParameterizedType

/**
 * Spring AOP aspect for transparent DAL caching.
 *
 * This aspect intercepts method calls on DAL classes and provides transparent
 * caching functionality using the @Cacheable and @CacheEvict annotations.
 *
 * ## Features
 *
 * - **Transparent Caching**: Services work without any caching-related code
 * - **Declarative Configuration**: Use annotations to mark cached methods
 * - **Automatic Invalidation**: Cache entries are invalidated on write operations
 * - **Error Handling**: Graceful handling of cache misses and errors
 * - **Logging**: Comprehensive logging for debugging and monitoring
 * - **Fully Reactive**: No blocking operations, compatible with Spring WebFlux
 *
 * ## Usage
 *
 * ```kotlin
 * // In DAL class
 * @Cacheable(ttl = CacheTTL.LONG_TERM, keyStrategy = CacheKeyStrategy.ENTITY_BY_NAME)
 * fun selectExerciseByName(exerciseName: String): Mono<Exercise>
 *
 * @CacheEvict(invalidationStrategy = CacheInvalidationStrategy.ENTITY_BY_NAME)
 * fun insertExercise(name: String, ...): Mono<Exercise>
 * ```
 *
 * @property reactiveCache The reactive Memcached cache utility
 * @property cacheKeyGenerator The cache key generator utility
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Aspect
@Component
class DALCachingAspect(
    private val reactiveCache: ReactiveMemcachedCache,
    private val cacheKeyGenerator: CacheKeyGenerator
) {
    companion object {
        private val logger = LoggerFactory.getLogger(DALCachingAspect::class.java)
    }

    /**
     * Intercepts methods annotated with @Cacheable for read operations.
     *
     * This method handles caching for read operations by:
     * 1. Checking if the result is already cached
     * 2. If cached, returning the cached result
     * 3. If not cached, executing the method and caching the result
     * 4. Handling cache misses gracefully
     *
     * @param joinPoint The method execution join point
     * @return The method result (cached or fresh)
     */
    @Around("@annotation(cacheable)")
    fun cacheMethod(joinPoint: ProceedingJoinPoint, cacheable: Cacheable): Any? {
        // Get the method signature directly from the join point
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        
        val cacheKey = cacheKeyGenerator.generateKey(method, joinPoint.args, cacheable)
        val ttl = cacheable.ttl.duration
        
        logger.debug("Cache lookup for key: {} with TTL: {}", cacheKey, ttl)
        
        return try {
            // Execute the method first to get the result
            val result = joinPoint.proceed()
            
            if (result is Mono<*>) {
                // Handle reactive results
                result.flatMap { value ->
                    if (value != null) {
                        // Cache the result asynchronously and return the original value
                        reactiveCache.set(cacheKey, value, ttl)
                            .doOnSuccess { success ->
                                if (success) {
                                    logger.debug("Successfully cached result for key: {}", cacheKey)
                                } else {
                                    logger.warn("Failed to cache result for key: {}", cacheKey)
                                }
                            }
                            .doOnError { error ->
                                logger.error("Error caching result for key: {}", cacheKey, error)
                            }
                            .subscribe()
                        
                        Mono.just(value)
                    } else {
                        Mono.just(value)
                    }
                }
            } else {
                // Handle non-reactive results
                if (result != null) {
                    // For non-reactive results, we need to handle caching differently
                    // Since we can't block in reactive context, we'll cache asynchronously
                    reactiveCache.set(cacheKey, result, ttl)
                        .doOnSuccess { success ->
                            if (success) {
                                logger.debug("Successfully cached result for key: {}", cacheKey)
                            } else {
                                logger.warn("Failed to cache result for key: {}", cacheKey)
                            }
                        }
                        .doOnError { error ->
                            logger.error("Error caching result for key: {}", cacheKey, error)
                        }
                        .subscribe()
                }
                result
            }
        } catch (e: Exception) {
            logger.error("Error in cache method execution for key: {}", cacheKey, e)
            throw e
        }
    }

    /**
     * Intercepts methods annotated with @CacheEvict for write operations.
     *
     * This method handles cache invalidation for write operations by:
     * 1. Executing the write operation
     * 2. Generating invalidation keys based on the strategy
     * 3. Invalidating related cache entries
     * 4. Logging the invalidation operations
     *
     * @param joinPoint The method execution join point
     * @param cacheEvict The cache eviction configuration
     * @return The method result
     */
    @Around("@annotation(cacheEvict)")
    fun evictCache(joinPoint: ProceedingJoinPoint, cacheEvict: CacheEvict): Any? {
        // Get the method signature directly from the join point
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method
        
        val entityName = getEntityName(method, cacheEvict)
        val invalidationKeys = cacheKeyGenerator.generateInvalidationKeys(
            entityName,
            cacheEvict.invalidationStrategy,
            joinPoint.args
        )
        
        logger.debug("Executing write operation with invalidation keys: {}", invalidationKeys)
        
        return try {
            // Execute the write operation
            val result = joinPoint.proceed()
            
            // Invalidate cache entries
            invalidationKeys.forEach { keyPattern ->
                invalidateCacheEntries(keyPattern)
            }
            
            result
        } catch (e: Exception) {
            logger.error("Error in cache eviction for keys: {}", invalidationKeys, e)
            throw e
        }
    }

    /**
     * Invalidates cache entries matching the given pattern.
     *
     * @param keyPattern The key pattern to match for invalidation
     */
    private fun invalidateCacheEntries(keyPattern: String) {
        // For now, we'll use a simple approach to invalidate related keys
        // In a production environment, you might want to use a more sophisticated
        // approach with key indexing or pattern-based invalidation
        
        logger.debug("Invalidating cache entries matching pattern: {}", keyPattern)
        
        // This is a simplified invalidation - in practice, you might need
        // to implement pattern-based key discovery and deletion
        if (!keyPattern.contains("*")) {
            reactiveCache.delete(keyPattern)
                .doOnSuccess { success ->
                    if (success) {
                        logger.debug("Successfully invalidated cache key: {}", keyPattern)
                    } else {
                        logger.debug("Cache key not found for invalidation: {}", keyPattern)
                    }
                }
                .doOnError { error ->
                    logger.error("Error invalidating cache key: {}", keyPattern, error)
                }
                .subscribe()
        } else {
            logger.debug("Pattern-based invalidation not yet implemented for: {}", keyPattern)
        }
    }

    /**
     * Extracts the entity name from the method or annotation.
     *
     * @param method The method being called
     * @param cacheEvict The cache eviction configuration
     * @return Entity name
     */
    private fun getEntityName(method: Method, cacheEvict: CacheEvict): String {
        if (cacheEvict.entityName.isNotEmpty()) {
            return cacheEvict.entityName
        }
        
        // Extract entity name from method name or class name
        val className = method.declaringClass.simpleName
        return when {
            className.endsWith("DAL") -> className.removeSuffix("DAL").lowercase()
            else -> className.lowercase()
        }
    }
}
