package com.congen.config

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertEquals
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
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [CacheWarmupConfig::class])
class CacheWarmupConfigTest {

    @Autowired
    private lateinit var cacheWarmupConfig: CacheWarmupConfig

    @Test
    fun `should load default configuration properties correctly`() {
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
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [CacheWarmupConfig::class])
@TestPropertySource(properties = [
    "congen.cache.warmup.enabled=false",
    "congen.cache.warmup.warmup-reference-data=false",
    "congen.cache.warmup.warmup-lists=false",
    "congen.cache.warmup.warmup-relationships=false",
    "congen.cache.warmup.warmup-programs=false"
])
class CacheWarmupConfigDisabledTest {

    @Autowired
    private lateinit var cacheWarmupConfig: CacheWarmupConfig

    @Test
    fun `should load disabled configuration properties correctly`() {
        // Then
        assertFalse(cacheWarmupConfig.enabled)
        assertFalse(cacheWarmupConfig.warmupReferenceData)
        assertFalse(cacheWarmupConfig.warmupLists)
        assertFalse(cacheWarmupConfig.warmupRelationships)
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
@ExtendWith(SpringExtension::class)
@SpringBootTest(classes = [CacheWarmupConfig::class])
class CacheWarmupConfigDefaultTest {

    @Autowired
    private lateinit var cacheWarmupConfig: CacheWarmupConfig

    @Test
    fun `should use default values when no properties specified`() {
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
    }
}
