package com.congen.config

import org.junit.jupiter.api.Test
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for CacheWarmupConfig.
 *
 * Tests the configuration properties loading and default values
 * for the cache warmup functionality.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@TestConfiguration
@EnableConfigurationProperties(CacheWarmupConfig::class)
class CacheWarmupConfigTest {
    @Test
    fun `should load default configuration properties correctly`() {
        // Given
        val context = AnnotationConfigApplicationContext()
        context.register(CacheWarmupConfigTest::class.java)
        context.refresh()

        val cacheWarmupConfig = context.getBean(CacheWarmupConfig::class.java)

        // Then
        assertTrue(cacheWarmupConfig.enabled)
        assertTrue(cacheWarmupConfig.warmupReferenceData)
        assertTrue(cacheWarmupConfig.warmupLists)
        assertTrue(cacheWarmupConfig.warmupRelationships)

        // Verify default popular exercises
        assertTrue(cacheWarmupConfig.popularExercises.contains("Bench Press"))
        assertTrue(cacheWarmupConfig.popularExercises.contains("Squat"))
        assertTrue(cacheWarmupConfig.popularExercises.contains("Deadlift"))

        // Verify default popular equipment
        assertTrue(cacheWarmupConfig.popularEquipment.contains("Barbell"))
        assertTrue(cacheWarmupConfig.popularEquipment.contains("Dumbbell"))

        // Verify default popular muscles
        assertTrue(cacheWarmupConfig.popularMuscles.contains("Chest"))
        assertTrue(cacheWarmupConfig.popularMuscles.contains("Back"))
        assertTrue(cacheWarmupConfig.popularMuscles.contains("Legs"))

        context.close()
    }
}

/**
 * Unit tests for CacheWarmupConfig with disabled settings.
 *
 * Tests the configuration when cache warmup is disabled.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@TestConfiguration
@EnableConfigurationProperties(CacheWarmupConfig::class)
class CacheWarmupConfigDisabledTest {
    @Test
    fun `should load disabled configuration properties correctly`() {
        // Given
        val context = AnnotationConfigApplicationContext()
        context.register(CacheWarmupConfigDisabledTest::class.java)

        // Set disabled properties
        context.environment.setActiveProfiles("test")
        context.environment.propertySources.addFirst(
            org.springframework.core.env.MapPropertySource(
                "test-properties",
                mapOf(
                    "congen.cache.warmup.enabled" to "false",
                    "congen.cache.warmup.warmup-reference-data" to "false",
                    "congen.cache.warmup.warmup-lists" to "false",
                    "congen.cache.warmup.warmup-relationships" to "false"
                )
            )
        )

        context.refresh()

        val cacheWarmupConfig = context.getBean(CacheWarmupConfig::class.java)

        // Then
        assertFalse(cacheWarmupConfig.enabled)
        assertFalse(cacheWarmupConfig.warmupReferenceData)
        assertFalse(cacheWarmupConfig.warmupLists)
        assertFalse(cacheWarmupConfig.warmupRelationships)

        context.close()
    }
}

/**
 * Unit tests for CacheWarmupConfig with default values.
 *
 * Tests the configuration when no properties are specified,
 * ensuring default values are used correctly.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@TestConfiguration
@EnableConfigurationProperties(CacheWarmupConfig::class)
class CacheWarmupConfigDefaultTest {
    @Test
    fun `should use default values when no properties specified`() {
        // Given
        val context = AnnotationConfigApplicationContext()
        context.register(CacheWarmupConfigDefaultTest::class.java)
        context.refresh()

        val cacheWarmupConfig = context.getBean(CacheWarmupConfig::class.java)

        // Then
        assertTrue(cacheWarmupConfig.enabled)
        assertTrue(cacheWarmupConfig.warmupReferenceData)
        assertTrue(cacheWarmupConfig.warmupLists)
        assertTrue(cacheWarmupConfig.warmupRelationships)

        // Verify default popular exercises
        assertTrue(cacheWarmupConfig.popularExercises.contains("Bench Press"))
        assertTrue(cacheWarmupConfig.popularExercises.contains("Squat"))
        assertTrue(cacheWarmupConfig.popularExercises.contains("Deadlift"))

        // Verify default popular equipment
        assertTrue(cacheWarmupConfig.popularEquipment.contains("Barbell"))
        assertTrue(cacheWarmupConfig.popularEquipment.contains("Dumbbell"))

        // Verify default popular muscles
        assertTrue(cacheWarmupConfig.popularMuscles.contains("Chest"))
        assertTrue(cacheWarmupConfig.popularMuscles.contains("Back"))
        assertTrue(cacheWarmupConfig.popularMuscles.contains("Legs"))

        context.close()
    }
}
