package com.congen.cache

import com.congen.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.lang.reflect.Method

/**
 * Utility for generating cache keys based on different strategies.
 *
 * This class provides methods to generate consistent cache keys for different
 * types of data and access patterns.
 *
 * ## Key Generation Patterns
 *
 * - **Standard**: entityName:methodName:param1:param2
 * - **Entity by Name**: entityName:byName:entityName
 * - **User Specific**: entityName:user:userId:methodName:params
 * - **Relationship**: entity1_entity2:methodName:param1:param2
 * - **List Query**: entityName:list:methodName:params
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class CacheKeyGenerator {
    /**
     * Generates a cache key based on the method, parameters, and cache configuration.
     *
     * @param method The method being called
     * @param args The method arguments
     * @param cacheable The cache configuration annotation
     * @return Generated cache key
     */
    fun generateKey(
        method: Method,
        args: Array<Any?>,
        cacheable: Cacheable
    ): String {
        val entityName = getEntityName(method, cacheable)

        return when (cacheable.keyStrategy) {
            CacheKeyStrategy.STANDARD -> generateStandardKey(entityName, method, args)
            CacheKeyStrategy.ENTITY_BY_NAME -> generateEntityByNameKey(entityName, args)
            CacheKeyStrategy.USER_SPECIFIC -> generateUserSpecificKey(entityName, method, args)
            CacheKeyStrategy.RELATIONSHIP -> generateRelationshipKey(entityName, method, args)
            CacheKeyStrategy.LIST_QUERY -> generateListQueryKey(entityName, method, args)
        }
    }

    /**
     * Generates a standard cache key.
     *
     * @param entityName The name of the entity
     * @param method The method being called
     * @param args The method arguments
     * @return Standard cache key
     */
    private fun generateStandardKey(
        entityName: String,
        method: Method,
        args: Array<Any?>
    ): String {
        val methodName = method.name
        val params = args.joinToString(":") { it?.toString() ?: "null" }
        return "$entityName:$methodName:$params"
    }

    /**
     * Generates a cache key for entities with name-based primary keys.
     *
     * @param entityName The name of the entity
     * @param args The method arguments
     * @return Entity by name cache key
     */
    private fun generateEntityByNameKey(
        entityName: String,
        args: Array<Any?>
    ): String {
        val entityNameParam = args.firstOrNull()?.toString() ?: "unknown"
        return "$entityName:byName:$entityNameParam"
    }

    /**
     * Generates a user-specific cache key.
     *
     * @param entityName The name of the entity
     * @param method The method being called
     * @param args The method arguments
     * @return User-specific cache key
     */
    private fun generateUserSpecificKey(
        entityName: String,
        method: Method,
        args: Array<Any?>
    ): String {
        val userId = args.firstOrNull()?.toString() ?: "unknown"
        val methodName = method.name
        val remainingParams = args.drop(1).joinToString(":") { it?.toString() ?: "null" }
        return "$entityName:user:$userId:$methodName:$remainingParams"
    }

    /**
     * Generates a cache key for relationship tables.
     *
     * @param entityName The name of the entity
     * @param method The method being called
     * @param args The method arguments
     * @return Relationship cache key
     */
    private fun generateRelationshipKey(
        entityName: String,
        method: Method,
        args: Array<Any?>
    ): String {
        val methodName = method.name
        val params = args.joinToString(":") { it?.toString() ?: "null" }
        return "$entityName:$methodName:$params"
    }

    /**
     * Generates a cache key for list queries.
     *
     * @param entityName The name of the entity
     * @param method The method being called
     * @param args The method arguments
     * @return List query cache key
     */
    private fun generateListQueryKey(
        entityName: String,
        method: Method,
        args: Array<Any?>
    ): String {
        val methodName = method.name
        val params = args.joinToString(":") { it?.toString() ?: "null" }
        return "$entityName:list:$methodName:$params"
    }

    /**
     * Extracts the entity name from the method or annotation.
     *
     * @param method The method being called
     * @param cacheable The cache configuration annotation
     * @return Entity name
     */
    private fun getEntityName(
        method: Method,
        cacheable: Cacheable
    ): String {
        if (cacheable.entityName.isNotEmpty()) {
            return cacheable.entityName
        }

        // Extract entity name from method name or class name
        val className = method.declaringClass.simpleName
        return when {
            className.endsWith("DAL") -> className.removeSuffix("DAL").lowercase()
            else -> className.lowercase()
        }
    }

    /**
     * Generates invalidation keys for cache eviction.
     *
     * @param entityName The name of the entity
     * @param invalidationStrategy The invalidation strategy
     * @param args The method arguments
     * @return List of keys to invalidate
     */
    fun generateInvalidationKeys(
        entityName: String,
        invalidationStrategy: com.congen.cache.CacheInvalidationStrategy,
        args: Array<Any?>
    ): List<String> {
        return when (invalidationStrategy) {
            com.congen.cache.CacheInvalidationStrategy.STANDARD -> listOf("$entityName:*")
            com.congen.cache.CacheInvalidationStrategy.ENTITY_BY_NAME -> {
                val entityNameParam = args.firstOrNull()?.toString()
                if (entityNameParam != null) {
                    listOf("$entityName:byName:$entityNameParam", "$entityName:*")
                } else {
                    listOf("$entityName:*")
                }
            }
            com.congen.cache.CacheInvalidationStrategy.USER_DATA -> {
                val userId = args.firstOrNull()?.toString()
                if (userId != null) {
                    listOf("$entityName:user:$userId:*", "$entityName:*")
                } else {
                    listOf("$entityName:*")
                }
            }
            com.congen.cache.CacheInvalidationStrategy.RELATIONSHIP -> {
                val params = args.joinToString(":") { it?.toString() ?: "null" }
                listOf("$entityName:*", "$entityName:*:$params")
            }
            com.congen.cache.CacheInvalidationStrategy.LIST_QUERIES -> {
                listOf("$entityName:list:*", "$entityName:*")
            }
        }
    }
}
