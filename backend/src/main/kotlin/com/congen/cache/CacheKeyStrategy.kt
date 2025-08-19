package com.congen.cache

/**
 * Enumeration of cache key generation strategies.
 *
 * This enum defines different strategies for generating cache keys based on
 * the type of data being cached and its access patterns.
 *
 * ## Key Strategies
 *
 * - **STANDARD**: Basic entity:methodName:params pattern
 * - **ENTITY_BY_NAME**: For entities with name-based primary keys
 * - **USER_SPECIFIC**: For user-specific data with user ID in key
 * - **RELATIONSHIP**: For relationship tables with multiple entity references
 * - **LIST_QUERY**: For list queries that return collections
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
enum class CacheKeyStrategy {
    /** Basic entity:methodName:params pattern */
    STANDARD,

    /** For entities with name-based primary keys (e.g., exercises, equipment) */
    ENTITY_BY_NAME,

    /** For user-specific data with user ID in key */
    USER_SPECIFIC,

    /** For relationship tables with multiple entity references */
    RELATIONSHIP,

    /** For list queries that return collections */
    LIST_QUERY
}
