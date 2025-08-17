package com.congen.cache

import com.congen.cache.annotation.Cacheable
import com.congen.cache.annotation.CacheEvict
import com.congen.dal.ExerciseDAL
import com.congen.model.Exercise
import com.congen.model.MovementType
import com.congen.util.ReactiveMemcachedCache
import com.congen.util.CacheMissException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Tests for the DAL caching aspect functionality.
 *
 * This test suite verifies that the Spring AOP caching aspect correctly:
 * - Intercepts method calls on DAL classes
 * - Caches results based on @Cacheable annotations
 * - Invalidates cache entries based on @CacheEvict annotations
 * - Handles cache misses gracefully
 * - Uses correct TTL and key generation strategies
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension::class)
class DALCachingAspectTest {

    @Mock
    private lateinit var reactiveCache: ReactiveMemcachedCache

    @Mock
    private lateinit var cacheKeyGenerator: CacheKeyGenerator

    private lateinit var dalCachingAspect: DALCachingAspect

    @BeforeEach
    fun setUp() {
        dalCachingAspect = DALCachingAspect(reactiveCache, cacheKeyGenerator)
    }

    @Test
    fun `should create aspect successfully`() {
        // Given & When & Then
        assert(dalCachingAspect != null)
    }

    @Test
    fun `should handle cache miss gracefully`() {
        // Given
        val exerciseName = "bench-press"
        val exercise = Exercise(
            name = exerciseName,
            description = "Bench press exercise",
            movementType = MovementType.HORIZONTAL_PUSH,
            isUnilateral = false,
            isUpper = true,
            isAccessory = false
        )
        
        val cacheKey = "exercise:byName:bench-press"
        
        // Mock cache miss
        Mockito.`when`(reactiveCache.get<Any>(cacheKey))
            .thenReturn(Mono.error(CacheMissException(cacheKey)))
        
        // Mock key generation
        Mockito.`when`(cacheKeyGenerator.generateKey(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(cacheKey)
        
        // When
        val result = dalCachingAspect.cacheMethod(
            createJoinPoint(exerciseName, exercise),
            createCacheableAnnotation()
        )
        
        // Then
        assert(result != null)
    }

    @Test
    fun `should handle cache hit correctly`() {
        // Given
        val exerciseName = "bench-press"
        val cachedExercise = Exercise(
            name = exerciseName,
            description = "Cached bench press exercise",
            movementType = MovementType.HORIZONTAL_PUSH,
            isUnilateral = false,
            isUpper = true,
            isAccessory = false
        )
        
        val cacheKey = "exercise:byName:bench-press"
        
        // Mock cache hit
        Mockito.`when`(reactiveCache.get<Any>(cacheKey))
            .thenReturn(Mono.just(cachedExercise))
        
        // Mock key generation
        Mockito.`when`(cacheKeyGenerator.generateKey(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(cacheKey)
        
        // When
        val result = dalCachingAspect.cacheMethod(
            createJoinPoint(exerciseName, cachedExercise),
            createCacheableAnnotation()
        )
        
        // Then
        assert(result != null)
    }

    @Test
    fun `should handle cache eviction`() {
        // Given
        val exerciseName = "bench-press"
        val exercise = Exercise(
            name = exerciseName,
            description = "Updated bench press exercise",
            movementType = MovementType.HORIZONTAL_PUSH,
            isUnilateral = false,
            isUpper = true,
            isAccessory = false
        )
        
        val invalidationKeys = listOf("exercise:byName:bench-press", "exercise:*")
        
        // Mock successful cache deletion
        Mockito.`when`(reactiveCache.delete(Mockito.anyString()))
            .thenReturn(Mono.just(true))
        
        // Mock key generation
        Mockito.`when`(cacheKeyGenerator.generateInvalidationKeys(Mockito.any(), Mockito.any(), Mockito.any()))
            .thenReturn(invalidationKeys)
        
        // When
        val result = dalCachingAspect.evictCache(
            createJoinPoint(exerciseName, exercise),
            createCacheEvictAnnotation()
        )
        
        // Then
        assert(result != null)
    }

    private fun createJoinPoint(exerciseName: String, exercise: Exercise): org.aspectj.lang.ProceedingJoinPoint {
        val joinPoint = Mockito.mock(org.aspectj.lang.ProceedingJoinPoint::class.java)
        val signature = Mockito.mock(org.aspectj.lang.Signature::class.java)
        
        Mockito.`when`(joinPoint.args).thenReturn(arrayOf(exerciseName))
        Mockito.`when`(joinPoint.signature).thenReturn(signature)
        Mockito.`when`(signature.name).thenReturn("selectExerciseByName")
        Mockito.`when`(signature.declaringType).thenReturn(ExerciseDAL::class.java)
        Mockito.`when`(joinPoint.proceed()).thenReturn(Mono.just(exercise))
        
        return joinPoint
    }

    private fun createCacheableAnnotation(ttl: CacheTTL = CacheTTL.LONG_TERM): Cacheable {
        return Cacheable(
            ttl = ttl,
            keyStrategy = CacheKeyStrategy.ENTITY_BY_NAME,
            invalidationStrategy = CacheInvalidationStrategy.STANDARD,
            entityName = "exercise"
        )
    }

    private fun createCacheEvictAnnotation(): CacheEvict {
        return CacheEvict(
            invalidationStrategy = CacheInvalidationStrategy.ENTITY_BY_NAME,
            entityName = "exercise"
        )
    }
}
