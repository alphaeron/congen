package com.congen.cache.annotation

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL

/**
 * Annotation for marking DAL methods that should be cached.
 *
 * This annotation enables declarative caching for DAL methods using Spring AOP.
 * It provides control over TTL, cache key generation, and invalidation patterns.
 *
 * ## Usage Examples
 *
 * ```kotlin
 * // Basic caching with default TTL
 * @Cacheable
 * fun selectExerciseByName(exerciseName: String): Mono<Exercise>
 *
 * // Custom TTL and key strategy
 * @Cacheable(
 *     ttl = CacheTTL.LONG_TERM,
 *     keyStrategy = CacheKeyStrategy.ENTITY_BY_NAME
 * )
 * fun selectExerciseByName(exerciseName: String): Mono<Exercise>
 *
 * // User-specific caching
 * @Cacheable(
 *     ttl = CacheTTL.USER_DATA,
 *     keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
 *     invalidationStrategy = CacheInvalidationStrategy.USER_DATA
 * )
 * fun selectUserByKeycloakId(keycloakId: String): Mono<User>
 * ```
 *
 * @property ttl The time-to-live duration for cached values
 * @property keyStrategy The strategy for generating cache keys
 * @property invalidationStrategy The strategy for invalidating related cache entries
 * @property entityName The name of the entity for key generation (optional)
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class Cacheable(
    val ttl: CacheTTL = CacheTTL.MEDIUM_TERM,
    val keyStrategy: CacheKeyStrategy = CacheKeyStrategy.STANDARD,
    val invalidationStrategy: CacheInvalidationStrategy = CacheInvalidationStrategy.STANDARD,
    val entityName: String = ""
)
