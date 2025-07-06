package com.congen.controllers

import com.congen.dal.WorkoutStageDAL
import com.congen.model.WorkoutStage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class WorkoutStageControllerTest {
    @Mock
    private lateinit var workoutStageDAL: WorkoutStageDAL

    @InjectMocks
    private lateinit var workoutStageController: WorkoutStageController

    private lateinit var testWorkoutStage: WorkoutStage

    @BeforeEach
    fun setUp() {
        testWorkoutStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )
    }

    @Test
    fun `save should create new workout stage successfully`() {
        // Given
        whenever(workoutStageDAL.insertWorkoutStage(5L, 1L, 1))
            .thenReturn(Mono.just(testWorkoutStage))

        // When
        val result =
            workoutStageController.save(
                programmedWorkoutId = 5L,
                stageTypeId = 1L,
                position = 1
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testWorkoutStage))
            .verifyComplete()

        verify(workoutStageDAL).insertWorkoutStage(5L, 1L, 1)
    }

    @Test
    fun `save should handle service error`() {
        // Given
        whenever(workoutStageDAL.insertWorkoutStage(5L, 1, 1))
            .thenReturn(Mono.error(RuntimeException("Database error")))

        // When
        val result =
            workoutStageController.save(
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1
            )

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(workoutStageDAL).insertWorkoutStage(5L, 1, 1)
    }

    @Test
    fun `get should return workout stage by id`() {
        // Given
        whenever(workoutStageDAL.selectWorkoutStageById(1L))
            .thenReturn(Mono.just(testWorkoutStage))

        // When
        val result = workoutStageController.get(1L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testWorkoutStage))
            .verifyComplete()

        verify(workoutStageDAL).selectWorkoutStageById(1L)
    }

    @Test
    fun `get should return 404 when workout stage not found`() {
        // Given
        whenever(workoutStageDAL.selectWorkoutStageById(1L))
            .thenReturn(Mono.error(RuntimeException("Not found")))

        // When
        val result = workoutStageController.get(1L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(workoutStageDAL).selectWorkoutStageById(1L)
    }

    @Test
    fun `getAll should return all workout stages`() {
        // Given
        val workoutStages =
            listOf(
                testWorkoutStage,
                testWorkoutStage.copy(id = 2L, position = 2)
            )
        whenever(workoutStageDAL.selectWorkoutStages())
            .thenReturn(Mono.just(workoutStages))

        // When
        val result = workoutStageController.getAll()

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<WorkoutStage>>)
            .expectNext(workoutStages)
            .verifyComplete()

        verify(workoutStageDAL).selectWorkoutStages()
    }

    @Test
    fun `getByProgrammedWorkoutId should return workout stages for workout`() {
        // Given
        val workoutStages =
            listOf(
                testWorkoutStage,
                testWorkoutStage.copy(id = 2L, position = 2)
            )
        whenever(workoutStageDAL.selectWorkoutStagesByProgrammedWorkoutId(5L))
            .thenReturn(Mono.just(workoutStages))

        // When
        val result = workoutStageController.getByProgrammedWorkoutId(5L)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<WorkoutStage>>)
            .expectNext(workoutStages)
            .verifyComplete()

        verify(workoutStageDAL).selectWorkoutStagesByProgrammedWorkoutId(5L)
    }

    @Test
    fun `update should update workout stage successfully`() {
        // Given
        val updatedWorkoutStage =
            testWorkoutStage.copy(
                stageTypeId = 2,
                position = 2
            )
        whenever(workoutStageDAL.updateWorkoutStage(1L, 5L, 2, 2))
            .thenReturn(Mono.just(updatedWorkoutStage))

        // When
        val result =
            workoutStageController.update(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 2,
                position = 2
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(updatedWorkoutStage))
            .verifyComplete()

        verify(workoutStageDAL).updateWorkoutStage(1L, 5L, 2, 2)
    }

    @Test
    fun `update should return 404 when workout stage not found`() {
        // Given
        whenever(workoutStageDAL.updateWorkoutStage(1L, 5L, 2, 2))
            .thenReturn(Mono.error(RuntimeException("Not found")))

        // When
        val result =
            workoutStageController.update(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 2,
                position = 2
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(workoutStageDAL).updateWorkoutStage(1L, 5L, 2, 2)
    }

    @Test
    fun `delete should delete workout stage successfully`() {
        // Given
        whenever(workoutStageDAL.deleteWorkoutStage(1L))
            .thenReturn(Mono.just(testWorkoutStage))

        // When
        val result = workoutStageController.delete(1L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testWorkoutStage))
            .verifyComplete()

        verify(workoutStageDAL).deleteWorkoutStage(1L)
    }

    @Test
    fun `delete should return 404 when workout stage not found`() {
        // Given
        whenever(workoutStageDAL.deleteWorkoutStage(1L))
            .thenReturn(Mono.error(RuntimeException("Not found")))

        // When
        val result = workoutStageController.delete(1L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(workoutStageDAL).deleteWorkoutStage(1L)
    }
}
