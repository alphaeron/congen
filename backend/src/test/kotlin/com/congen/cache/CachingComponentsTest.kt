package com.congen.cache

import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

/**
 * Unit test for the caching system components.
 *
 * This test verifies that the caching annotations and enums work correctly
 * and can be used in DAL classes.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class CachingComponentsTest {
    @Test
    fun `should create cacheable annotation with all parameters`() {
        val cacheable =
            Cacheable(
                ttl = CacheTTL.LONG_TERM,
                keyStrategy = CacheKeyStrategy.ENTITY_BY_NAME,
                invalidationStrategy = CacheInvalidationStrategy.STANDARD,
                entityName = "exercise"
            )

        assertEquals(CacheTTL.LONG_TERM, cacheable.ttl)
        assertEquals(CacheKeyStrategy.ENTITY_BY_NAME, cacheable.keyStrategy)
        assertEquals(CacheInvalidationStrategy.STANDARD, cacheable.invalidationStrategy)
        assertEquals("exercise", cacheable.entityName)
    }

    @Test
    fun `should create cache evict annotation with parameters`() {
        val cacheEvict =
            CacheEvict(
                invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
                entityName = "user"
            )

        assertEquals(CacheInvalidationStrategy.USER_DATA, cacheEvict.invalidationStrategy)
        assertEquals("user", cacheEvict.entityName)
    }

    @Test
    fun `should have correct TTL durations`() {
        assertEquals(24 * 60 * 60, CacheTTL.LONG_TERM.duration.seconds)
        assertEquals(60 * 60, CacheTTL.MEDIUM_TERM.duration.seconds)
        assertEquals(30 * 60, CacheTTL.SHORT_TERM.duration.seconds)
        assertEquals(5 * 60, CacheTTL.VERY_SHORT_TERM.duration.seconds)
        assertEquals(30 * 60, CacheTTL.USER_DATA.duration.seconds)
    }

    @Test
    fun `should have all key strategies defined`() {
        assertNotNull(CacheKeyStrategy.STANDARD)
        assertNotNull(CacheKeyStrategy.ENTITY_BY_NAME)
        assertNotNull(CacheKeyStrategy.USER_SPECIFIC)
        assertNotNull(CacheKeyStrategy.RELATIONSHIP)
        assertNotNull(CacheKeyStrategy.LIST_QUERY)
    }

    @Test
    fun `should have all invalidation strategies defined`() {
        assertNotNull(CacheInvalidationStrategy.STANDARD)
        assertNotNull(CacheInvalidationStrategy.ENTITY_BY_NAME)
        assertNotNull(CacheInvalidationStrategy.USER_DATA)
        assertNotNull(CacheInvalidationStrategy.RELATIONSHIP)
        assertNotNull(CacheInvalidationStrategy.LIST_QUERIES)
    }
}
