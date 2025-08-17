package com.congen.cache

/**
 * Enumeration of cache invalidation strategies.
 *
 * This enum defines different strategies for invalidating cache entries when
 * data is modified through write operations.
 *
 * ## Invalidation Strategies
 *
 * - **STANDARD**: Standard entity invalidation on write operations
 * - **ENTITY_BY_NAME**: Name-based entity invalidation for entities with name primary keys
 * - **USER_DATA**: User-specific invalidation for user-related data
 * - **RELATIONSHIP**: Relationship invalidation for relationship tables
 * - **LIST_QUERIES**: Invalidate all list queries for an entity
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
enum class CacheInvalidationStrategy {
    /** Standard entity invalidation on write operations */
    STANDARD,
    
    /** Name-based entity invalidation for entities with name primary keys */
    ENTITY_BY_NAME,
    
    /** User-specific invalidation for user-related data */
    USER_DATA,
    
    /** Relationship invalidation for relationship tables */
    RELATIONSHIP,
    
    /** Invalidate all list queries for an entity */
    LIST_QUERIES
}
