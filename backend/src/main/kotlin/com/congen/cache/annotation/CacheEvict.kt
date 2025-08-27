package com.congen.cache.annotation

import com.congen.cache.CacheInvalidationStrategy

/**
 * Annotation for marking DAL methods that should invalidate cache entries.
 *
 * This annotation is used on write operations (insert, update, delete) to
 * automatically invalidate related cache entries when data is modified.
 *
 * ## Usage Examples
 *
 * ```kotlin
 * // Standard invalidation
 * @CacheEvict
 * fun insertExercise(name: String, ...): Mono<Exercise>
 *
 * // User-specific invalidation
 * @CacheEvict(invalidationStrategy = CacheInvalidationStrategy.USER_DATA)
 * fun updateUser(keycloakId: String, ...): Mono<User>
 *
 * // Name-based invalidation for entities with name primary keys
 * @CacheEvict(invalidationStrategy = CacheInvalidationStrategy.ENTITY_BY_NAME)
 * fun updateExerciseByName(name: String, ...): Mono<Exercise>
 * ```
 *
 * @property invalidationStrategy The strategy for invalidating related cache entries
 * @property entityName The name of the entity for invalidation (optional)
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheEvict(
    val invalidationStrategy: CacheInvalidationStrategy = CacheInvalidationStrategy.STANDARD,
    val entityName: String = ""
)
