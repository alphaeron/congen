package com.congen.controllers

import com.congen.dal.WorkoutStageTypeDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.WorkoutStageType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class WorkoutStageTypeControllerTest {
    @Mock
    private lateinit var workoutStageTypeDAL: WorkoutStageTypeDAL

    @InjectMocks
    private lateinit var workoutStageTypeController: WorkoutStageTypeController

    private lateinit var testWorkoutStageType: WorkoutStageType

    @BeforeEach
    fun setUp() {
        testWorkoutStageType =
            WorkoutStageType(
                id = 1,
                name = "Warm-up"
            )
    }

    @Test
    fun `get should return workout stage type by id`() {
        // Given
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeById(1))
            .thenReturn(Mono.just(testWorkoutStageType))

        // When
        val result = workoutStageTypeController.get(1)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testWorkoutStageType))
            .verifyComplete()

        verify(workoutStageTypeDAL).selectWorkoutStageTypeById(1)
    }

    @Test
    fun `get should return 404 when workout stage type not found`() {
        // Given
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeById(1))
            .thenReturn(Mono.error(NoResultsFoundException("Not found")))

        // When
        val result = workoutStageTypeController.get(1)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(workoutStageTypeDAL).selectWorkoutStageTypeById(1)
    }

    @Test
    fun `get should handle service error`() {
        // Given
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeById(1))
            .thenReturn(Mono.error(RuntimeException("Database error")))

        // When
        val result = workoutStageTypeController.get(1)

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `getByName should return workout stage type by name`() {
        // Given
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByName("Warm-up"))
            .thenReturn(Mono.just(testWorkoutStageType))

        // When
        val result = workoutStageTypeController.getByName("Warm-up")

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testWorkoutStageType))
            .verifyComplete()

        verify(workoutStageTypeDAL).selectWorkoutStageTypeByName("Warm-up")
    }

    @Test
    fun `getByName should return 404 when workout stage type not found`() {
        // Given
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByName("Warm-up"))
            .thenReturn(Mono.error(NoResultsFoundException("Not found")))

        // When
        val result = workoutStageTypeController.getByName("Warm-up")

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(workoutStageTypeDAL).selectWorkoutStageTypeByName("Warm-up")
    }

    @Test
    fun `getByName should handle service error`() {
        // Given
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByName("Warm-up"))
            .thenReturn(Mono.error(RuntimeException("Database error")))

        // When
        val result = workoutStageTypeController.getByName("Warm-up")

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `getAll should return all workout stage types`() {
        // Given
        val workoutStageTypes =
            listOf(
                testWorkoutStageType,
                WorkoutStageType(id = 2, name = "Main"),
                WorkoutStageType(id = 3, name = "Accessory"),
                WorkoutStageType(id = 4, name = "Cool-down")
            )
        whenever(workoutStageTypeDAL.selectWorkoutStageTypes())
            .thenReturn(Mono.just(workoutStageTypes))

        // When
        val result = workoutStageTypeController.getAll()

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(workoutStageTypes))
            .verifyComplete()

        verify(workoutStageTypeDAL).selectWorkoutStageTypes()
    }

    @Test
    fun `getAll should handle service error`() {
        // Given
        whenever(workoutStageTypeDAL.selectWorkoutStageTypes())
            .thenReturn(Mono.error(RuntimeException("Database error")))

        // When
        val result = workoutStageTypeController.getAll()

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(workoutStageTypeDAL).selectWorkoutStageTypes()
    }
}
