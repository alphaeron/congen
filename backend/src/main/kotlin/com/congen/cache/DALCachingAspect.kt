package com.congen.cache

import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.lang.reflect.Method

/**
 * AspectJ aspect for transparent DAL caching with compile-time weaving.
 *
 * This aspect intercepts method calls on DAL classes and provides transparent
 * caching functionality using the @Cacheable and @CacheEvict annotations.
 * AspectJ compile-time weaving provides better performance than Spring AOP.
 *
 * ## Features
 *
 * - **Transparent Caching**: Services work without any caching-related code
 * - **Declarative Configuration**: Use annotations to mark cached methods
 * - **Automatic Invalidation**: Cache entries are invalidated on write operations
 * - **Error Handling**: Graceful handling of cache misses and errors
 * - **Logging**: Comprehensive logging for debugging and monitoring
 * - **Fully Reactive**: No blocking operations, compatible with Spring WebFlux
 * - **Compile-time Weaving**: Better performance than runtime proxy-based AOP
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
 * @param reactiveCache The reactive Memcached cache utility
 * @param cacheKeyGenerator The cache key generator utility
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
     * @param cacheable The cacheable annotation configuration
     * @return The method result (cached or fresh)
     */
    @Around("@annotation(cacheable)")
    fun cacheMethod(
        joinPoint: ProceedingJoinPoint,
        cacheable: Cacheable
    ): Any? {
        // Get the method signature directly from the join point
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method

        val cacheKey = cacheKeyGenerator.generateKey(method, joinPoint.args, cacheable)
        val ttl = cacheable.ttl.duration

        logger.debug("Cache lookup for key: {} with TTL: {}", cacheKey, ttl)

        // Execute the method first to get the result
        val result = joinPoint.proceed()

        return if (result is Mono<*>) {
            // Handle reactive results
            result.flatMap { value ->
                if (value != null) {
                    // Cache the result and return the original value
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
                        .onErrorReturn(false) // Return false on error to continue the chain
                        .then(Mono.just(value))
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
                    .onErrorReturn(false) // Return false on error to continue the chain
                    .subscribe(
                        { success ->
                            logger.debug("Cache operation completed for key: {}", cacheKey)
                        },
                        { error ->
                            logger.error("Cache operation failed for key: {}", cacheKey, error)
                        }
                    )
            }
            result
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
    fun evictCache(
        joinPoint: ProceedingJoinPoint,
        cacheEvict: CacheEvict
    ): Any? {
        // Get the method signature directly from the join point
        val methodSignature = joinPoint.signature as MethodSignature
        val method = methodSignature.method

        val entityName = getEntityName(method, cacheEvict)
        val invalidationKeys =
            cacheKeyGenerator.generateInvalidationKeys(
                entityName,
                cacheEvict.invalidationStrategy,
                joinPoint.args
            )

        logger.debug("Executing write operation with invalidation keys: {}", invalidationKeys)

        // Execute the write operation
        val result = joinPoint.proceed()

        // Invalidate cache entries reactively
        // Convert the result to Mono and chain cache invalidation
        return if (result is Mono<*>) {
            result.flatMap { value ->
                // Chain all cache invalidations
                val invalidationMonos = invalidationKeys.map { keyPattern ->
                    invalidateCacheEntries(keyPattern)
                }
                
                // Wait for all invalidations to complete, then return the original value
                if (invalidationMonos.isNotEmpty()) {
                    // Chain all invalidations sequentially
                    invalidationMonos.fold(Mono.just(emptyList<String>())) { acc, mono ->
                        acc.flatMap { accResult ->
                            mono.map { monoResult ->
                                accResult + monoResult
                            }
                        }
                    }.then(Mono.just(value))
                } else {
                    Mono.just(value)
                }
            }
        } else {
            // For non-reactive results, invalidate cache asynchronously but don't block
            invalidationKeys.forEach { keyPattern ->
                invalidateCacheEntries(keyPattern)
                    .subscribe()
            }
            result
        }
    }

    /**
     * Invalidates cache entries matching the given pattern.
     *
     * @param keyPattern The key pattern to match for invalidation
     * @return Mono<List<String>> containing the list of deleted keys
     */
    private fun invalidateCacheEntries(keyPattern: String): Mono<List<String>> {
        logger.debug("Invalidating cache entries matching pattern: {}", keyPattern)

        return if (!keyPattern.contains("*")) {
            // Exact key match - delete the specific key
            reactiveCache.delete(keyPattern)
                .map { success ->
                    if (success) {
                        logger.debug("Successfully invalidated cache key: {}", keyPattern)
                        listOf(keyPattern)
                    } else {
                        logger.debug("Cache key not found for invalidation: {}", keyPattern)
                        emptyList<String>()
                    }
                }
                .doOnError { error ->
                    logger.error("Error invalidating cache key: {}", keyPattern, error)
                }
        } else {
            // Pattern-based invalidation - delete all keys matching the pattern
            reactiveCache.deletePattern(keyPattern)
                .doOnSuccess { deletedKeys: List<String> ->
                    if (deletedKeys.isNotEmpty()) {
                        logger.debug("Successfully invalidated {} cache keys matching pattern: {}", deletedKeys.size, keyPattern)
                        deletedKeys.forEach { key: String ->
                            logger.debug("Invalidated cache key: {}", key)
                        }
                    } else {
                        logger.debug("No cache keys found matching pattern: {}", keyPattern)
                    }
                }
                .doOnError { error: Throwable ->
                    logger.error("Error invalidating cache keys matching pattern: {}", keyPattern, error)
                }
        }
    }

    /**
     * Extracts the entity name from the method or annotation.
     *
     * @param method The method being called
     * @param cacheEvict The cache eviction configuration
     * @return Entity name
     */
    private fun getEntityName(
        method: Method,
        cacheEvict: CacheEvict
    ): String {
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
