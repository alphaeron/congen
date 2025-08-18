package com.congen.cache

import com.congen.cache.annotation.Cacheable
import com.congen.cache.annotation.CacheEvict
import com.congen.dal.ExerciseDAL
import com.congen.model.Exercise
import com.congen.model.MovementType
import com.congen.cache.ReactiveMemcachedCache
import com.congen.exceptions.CacheMissException
import com.fasterxml.jackson.databind.ObjectMapper
import org.aspectj.lang.reflect.MethodSignature
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.lang.reflect.Method
import java.time.Duration
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

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
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper()
        dalCachingAspect = DALCachingAspect(reactiveCache, cacheKeyGenerator)
    }

    @Test
    fun `should create aspect successfully`() {
        // Given & When & Then
        assert(dalCachingAspect != null)
    }

    @Test
    fun `should handle cache miss gracefully with reactive result`() {
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
        val method = createMockMethod()
        
        // Mock key generation
        Mockito.`when`(cacheKeyGenerator.generateKey(method, arrayOf(exerciseName), createCacheableAnnotation()))
            .thenReturn(cacheKey)
        
        // Mock successful cache set operation
        Mockito.`when`(reactiveCache.set(cacheKey, exercise, Duration.ofHours(24)))
            .thenReturn(Mono.just(true))
        
        // When
        val result = dalCachingAspect.cacheMethod(
            createJoinPoint(exerciseName, exercise, method),
            createCacheableAnnotation()
        )
        
        // Then
        assert(result != null)
        StepVerifier.create(result as Mono<*>)
            .expectNext(exercise)
            .verifyComplete()
        
        // Verify cache was set
        Mockito.verify(reactiveCache).set(cacheKey, exercise, Duration.ofHours(24))
    }

    @Test
    fun `should handle cache miss gracefully with non-reactive result`() {
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
        val method = createMockMethod()
        
        // Mock key generation
        Mockito.`when`(cacheKeyGenerator.generateKey(method, arrayOf(exerciseName), createCacheableAnnotation()))
            .thenReturn(cacheKey)
        
        // Mock successful cache set operation
        Mockito.`when`(reactiveCache.set(cacheKey, exercise, Duration.ofHours(24)))
            .thenReturn(Mono.just(true))
        
        // When
        val result = dalCachingAspect.cacheMethod(
            createJoinPointNonReactive(exerciseName, exercise, method),
            createCacheableAnnotation()
        )
        
        // Then
        assert(result != null)
        assert(result == exercise)
        
        // Verify cache was set
        Mockito.verify(reactiveCache).set(cacheKey, exercise, Duration.ofHours(24))
    }

    @Test
    fun `should handle null result gracefully`() {
        // Given
        val exerciseName = "non-existent"
        val cacheKey = "exercise:byName:non-existent"
        val method = createMockMethod()
        
        // Mock key generation
        Mockito.`when`(cacheKeyGenerator.generateKey(method, arrayOf(exerciseName), createCacheableAnnotation()))
            .thenReturn(cacheKey)
        
        // When
        val result = dalCachingAspect.cacheMethod(
            createJoinPointWithNull(exerciseName, method),
            createCacheableAnnotation()
        )
        
        // Then
        assert(result != null)
        StepVerifier.create(result as Mono<*>)
            .expectComplete()
            .verify()
        
        // Test passes if we reach here without cache operations
    }

    @Test
    fun `should handle cache set failure gracefully`() {
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
        val method = createMockMethod()
        
        // Mock key generation
        Mockito.`when`(cacheKeyGenerator.generateKey(method, arrayOf(exerciseName), createCacheableAnnotation()))
            .thenReturn(cacheKey)
        
        // Mock failed cache set operation
        Mockito.`when`(reactiveCache.set(cacheKey, exercise, Duration.ofHours(24)))
            .thenReturn(Mono.just(false))
        
        // When
        val result = dalCachingAspect.cacheMethod(
            createJoinPoint(exerciseName, exercise, method),
            createCacheableAnnotation()
        )
        
        // Then
        assert(result != null)
        StepVerifier.create(result as Mono<*>)
            .expectNext(exercise)
            .verifyComplete()
        
        // Verify cache set was attempted
        Mockito.verify(reactiveCache).set(cacheKey, exercise, Duration.ofHours(24))
    }

    @Test
    fun `should handle cache set error gracefully`() {
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
        val method = createMockMethod()
        
        // Mock key generation
        Mockito.`when`(cacheKeyGenerator.generateKey(method, arrayOf(exerciseName), createCacheableAnnotation()))
            .thenReturn(cacheKey)
        
        // Mock cache set error
        Mockito.`when`(reactiveCache.set(cacheKey, exercise, Duration.ofHours(24)))
            .thenReturn(Mono.error(RuntimeException("Cache error")))
        
        // When
        val result = dalCachingAspect.cacheMethod(
            createJoinPoint(exerciseName, exercise, method),
            createCacheableAnnotation()
        )
        
        // Then
        assert(result != null)
        StepVerifier.create(result as Mono<*>)
            .expectNext(exercise)
            .verifyComplete()
        
        // Verify cache set was attempted
        Mockito.verify(reactiveCache).set(cacheKey, exercise, Duration.ofHours(24))
    }

    @Test
    fun `should handle method execution error`() {
        // Given
        val exerciseName = "bench-press"
        val cacheKey = "exercise:byName:bench-press"
        val method = createMockMethod()
        
        // Mock key generation
        Mockito.`when`(cacheKeyGenerator.generateKey(method, arrayOf(exerciseName), createCacheableAnnotation()))
            .thenReturn(cacheKey)
        
        // When & Then
        val exception = assertThrows<RuntimeException> {
            dalCachingAspect.cacheMethod(
                createJoinPointWithError(exerciseName, RuntimeException("Method error"), method),
                createCacheableAnnotation()
            )
        }
        
        assertEquals("Method error", exception.message)
        
        // Test passes if we reach here without cache operations
    }

    @Test
    fun `should handle different TTL values`() {
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
        val method = createMockMethod()
        val shortTtlAnnotation = createCacheableAnnotation(CacheTTL.SHORT_TERM)
        
        // Mock key generation
        Mockito.`when`(cacheKeyGenerator.generateKey(method, arrayOf(exerciseName), shortTtlAnnotation))
            .thenReturn(cacheKey)
        
        // Mock successful cache set operation
        Mockito.`when`(reactiveCache.set(cacheKey, exercise, Duration.ofMinutes(30)))
            .thenReturn(Mono.just(true))
        
        // When
        val result = dalCachingAspect.cacheMethod(
            createJoinPoint(exerciseName, exercise, method),
            shortTtlAnnotation
        )
        
        // Then
        assert(result != null)
        StepVerifier.create(result as Mono<*>)
            .expectNext(exercise)
            .verifyComplete()
        
        // Verify cache was set with correct TTL
        Mockito.verify(reactiveCache).set(cacheKey, exercise, Duration.ofMinutes(30))
    }

    @Test
    fun `should handle cache eviction successfully`() {
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
        val method = createMockMethod()
        
        // Mock successful cache deletion
        Mockito.`when`(reactiveCache.delete("exercise:byName:bench-press"))
            .thenReturn(Mono.just(true))
        
        // Mock key generation with specific parameters
        Mockito.`when`(cacheKeyGenerator.generateInvalidationKeys("exercise", CacheInvalidationStrategy.ENTITY_BY_NAME, arrayOf(exerciseName)))
            .thenReturn(invalidationKeys)
        
        // When
        val result = dalCachingAspect.evictCache(
            createJoinPointForEviction(exerciseName, exercise, method),
            createCacheEvictAnnotation()
        )
        
        // Then
        assert(result != null)
        assert(result == exercise)
        
        // Verify that cache entries were invalidated
        Mockito.verify(reactiveCache).delete("exercise:byName:bench-press")
    }

    @Test
    fun `should handle cache eviction with multiple keys`() {
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
        
        val invalidationKeys = listOf("exercise:byName:bench-press", "exercise:all", "exercise:list")
        val method = createMockMethod()
        
        // Mock successful cache deletion for all keys
        Mockito.`when`(reactiveCache.delete("exercise:byName:bench-press"))
            .thenReturn(Mono.just(true))
        Mockito.`when`(reactiveCache.delete("exercise:all"))
            .thenReturn(Mono.just(true))
        Mockito.`when`(reactiveCache.delete("exercise:list"))
            .thenReturn(Mono.just(true))
        
        // Mock key generation
        Mockito.`when`(cacheKeyGenerator.generateInvalidationKeys("exercise", CacheInvalidationStrategy.ENTITY_BY_NAME, arrayOf(exerciseName)))
            .thenReturn(invalidationKeys)
        
        // When
        val result = dalCachingAspect.evictCache(
            createJoinPointForEviction(exerciseName, exercise, method),
            createCacheEvictAnnotation()
        )
        
        // Then
        assert(result != null)
        assert(result == exercise)
        
        // Verify that all cache entries were invalidated
        Mockito.verify(reactiveCache).delete("exercise:byName:bench-press")
        Mockito.verify(reactiveCache).delete("exercise:all")
        Mockito.verify(reactiveCache).delete("exercise:list")
    }

    @Test
    fun `should handle cache eviction with pattern keys`() {
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
        
        val invalidationKeys = listOf("exercise:*")
        val method = createMockMethod()
        
        // Mock key generation
        Mockito.`when`(cacheKeyGenerator.generateInvalidationKeys("exercise", CacheInvalidationStrategy.ENTITY_BY_NAME, arrayOf(exerciseName)))
            .thenReturn(invalidationKeys)
        
        // When
        val result = dalCachingAspect.evictCache(
            createJoinPointForEviction(exerciseName, exercise, method),
            createCacheEvictAnnotation()
        )
        
        // Then
        assert(result != null)
        assert(result == exercise)
        
        // Verify that pattern-based invalidation was logged but not executed
        // (pattern-based invalidation is not yet implemented)
        Mockito.verify(reactiveCache, Mockito.never()).delete("exercise:*")
    }

    @Test
    fun `should handle cache eviction error gracefully`() {
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
        
        val invalidationKeys = listOf("exercise:byName:bench-press")
        val method = createMockMethod()
        
        // Mock cache deletion error
        Mockito.`when`(reactiveCache.delete("exercise:byName:bench-press"))
            .thenReturn(Mono.error(RuntimeException("Cache deletion error")))
        
        // Mock key generation
        Mockito.`when`(cacheKeyGenerator.generateInvalidationKeys("exercise", CacheInvalidationStrategy.ENTITY_BY_NAME, arrayOf(exerciseName)))
            .thenReturn(invalidationKeys)
        
        // When
        val result = dalCachingAspect.evictCache(
            createJoinPointForEviction(exerciseName, exercise, method),
            createCacheEvictAnnotation()
        )
        
        // Then
        assert(result != null)
        assert(result == exercise)
        
        // Verify that cache deletion was attempted
        Mockito.verify(reactiveCache).delete("exercise:byName:bench-press")
    }

    @Test
    fun `should handle cache eviction with method execution error`() {
        // Given
        val exerciseName = "bench-press"
        val invalidationKeys = listOf("exercise:byName:bench-press")
        val method = createMockMethod()
        
        // Mock key generation
        Mockito.`when`(cacheKeyGenerator.generateInvalidationKeys("exercise", CacheInvalidationStrategy.ENTITY_BY_NAME, arrayOf(exerciseName)))
            .thenReturn(invalidationKeys)
        
        // When & Then
        val exception = assertThrows<RuntimeException> {
            dalCachingAspect.evictCache(
                createJoinPointForEvictionWithError(exerciseName, RuntimeException("Method error"), method),
                createCacheEvictAnnotation()
            )
        }
        
        assertEquals("Method error", exception.message)
        
        // Test passes if we reach here without cache operations
    }

    @Test
    fun `should extract entity name from annotation`() {
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
        
        val invalidationKeys = listOf("custom:byName:bench-press")
        val method = createMockMethod()
        val customCacheEvict = CacheEvict(
            invalidationStrategy = CacheInvalidationStrategy.ENTITY_BY_NAME,
            entityName = "custom"
        )
        
        // Mock key generation with custom entity name
        Mockito.`when`(cacheKeyGenerator.generateInvalidationKeys("custom", CacheInvalidationStrategy.ENTITY_BY_NAME, arrayOf(exerciseName)))
            .thenReturn(invalidationKeys)
        
        // Mock successful cache deletion
        Mockito.`when`(reactiveCache.delete("custom:byName:bench-press"))
            .thenReturn(Mono.just(true))
        
        // When
        val result = dalCachingAspect.evictCache(
            createJoinPointForEviction(exerciseName, exercise, method),
            customCacheEvict
        )
        
        // Then
        assert(result != null)
        assert(result == exercise)
        
        // Verify that cache entries were invalidated with custom entity name
        Mockito.verify(reactiveCache).delete("custom:byName:bench-press")
    }

    private fun createMockMethod(): Method {
        return ExerciseDAL::class.java.getMethod("selectExerciseByName", String::class.java)
    }

    private fun createJoinPoint(exerciseName: String, exercise: Exercise, method: Method): org.aspectj.lang.ProceedingJoinPoint {
        val joinPoint = Mockito.mock(org.aspectj.lang.ProceedingJoinPoint::class.java)
        val methodSignature = Mockito.mock(MethodSignature::class.java)
        
        Mockito.lenient().`when`(joinPoint.args).thenReturn(arrayOf(exerciseName))
        Mockito.lenient().`when`(joinPoint.signature).thenReturn(methodSignature)
        Mockito.lenient().`when`(methodSignature.method).thenReturn(method)
        Mockito.lenient().`when`(methodSignature.declaringType).thenReturn(ExerciseDAL::class.java)
        Mockito.lenient().`when`(methodSignature.name).thenReturn("selectExerciseByName")
        Mockito.lenient().`when`(joinPoint.proceed()).thenReturn(Mono.just(exercise))
        
        return joinPoint
    }

    private fun createJoinPointWithNull(exerciseName: String, method: Method): org.aspectj.lang.ProceedingJoinPoint {
        val joinPoint = Mockito.mock(org.aspectj.lang.ProceedingJoinPoint::class.java)
        val methodSignature = Mockito.mock(MethodSignature::class.java)
        
        Mockito.lenient().`when`(joinPoint.args).thenReturn(arrayOf(exerciseName))
        Mockito.lenient().`when`(joinPoint.signature).thenReturn(methodSignature)
        Mockito.lenient().`when`(methodSignature.method).thenReturn(method)
        Mockito.lenient().`when`(methodSignature.declaringType).thenReturn(ExerciseDAL::class.java)
        Mockito.lenient().`when`(methodSignature.name).thenReturn("selectExerciseByName")
        Mockito.lenient().`when`(joinPoint.proceed()).thenReturn(Mono.empty<Exercise>())
        
        return joinPoint
    }

    private fun createJoinPointNonReactive(exerciseName: String, exercise: Exercise, method: Method): org.aspectj.lang.ProceedingJoinPoint {
        val joinPoint = Mockito.mock(org.aspectj.lang.ProceedingJoinPoint::class.java)
        val methodSignature = Mockito.mock(MethodSignature::class.java)
        
        Mockito.lenient().`when`(joinPoint.args).thenReturn(arrayOf(exerciseName))
        Mockito.lenient().`when`(joinPoint.signature).thenReturn(methodSignature)
        Mockito.lenient().`when`(methodSignature.method).thenReturn(method)
        Mockito.lenient().`when`(methodSignature.declaringType).thenReturn(ExerciseDAL::class.java)
        Mockito.lenient().`when`(methodSignature.name).thenReturn("selectExerciseByName")
        Mockito.lenient().`when`(joinPoint.proceed()).thenReturn(exercise)
        
        return joinPoint
    }

    private fun createJoinPointWithError(exerciseName: String, error: Exception, method: Method): org.aspectj.lang.ProceedingJoinPoint {
        val joinPoint = Mockito.mock(org.aspectj.lang.ProceedingJoinPoint::class.java)
        val methodSignature = Mockito.mock(MethodSignature::class.java)
        
        Mockito.lenient().`when`(joinPoint.args).thenReturn(arrayOf(exerciseName))
        Mockito.lenient().`when`(joinPoint.signature).thenReturn(methodSignature)
        Mockito.lenient().`when`(methodSignature.method).thenReturn(method)
        Mockito.lenient().`when`(methodSignature.declaringType).thenReturn(ExerciseDAL::class.java)
        Mockito.lenient().`when`(methodSignature.name).thenReturn("selectExerciseByName")
        Mockito.lenient().`when`(joinPoint.proceed()).thenThrow(error)
        
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

    private fun createJoinPointForEviction(exerciseName: String, exercise: Exercise, method: Method): org.aspectj.lang.ProceedingJoinPoint {
        val joinPoint = Mockito.mock(org.aspectj.lang.ProceedingJoinPoint::class.java)
        val methodSignature = Mockito.mock(MethodSignature::class.java)
        
        Mockito.`when`(joinPoint.args).thenReturn(arrayOf(exerciseName))
        Mockito.`when`(joinPoint.signature).thenReturn(methodSignature)
        Mockito.`when`(methodSignature.method).thenReturn(method)
        Mockito.`when`(joinPoint.proceed()).thenReturn(exercise)
        
        return joinPoint
    }

    private fun createJoinPointForEvictionWithError(exerciseName: String, error: Exception, method: Method): org.aspectj.lang.ProceedingJoinPoint {
        val joinPoint = Mockito.mock(org.aspectj.lang.ProceedingJoinPoint::class.java)
        val methodSignature = Mockito.mock(MethodSignature::class.java)
        
        Mockito.`when`(joinPoint.args).thenReturn(arrayOf(exerciseName))
        Mockito.`when`(joinPoint.signature).thenReturn(methodSignature)
        Mockito.`when`(methodSignature.method).thenReturn(method)
        Mockito.`when`(joinPoint.proceed()).thenThrow(error)
        
        return joinPoint
    }
}
