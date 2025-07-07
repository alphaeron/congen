package com.congen.controllers

import com.congen.dal.WorkoutStageTypeDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.WorkoutStageType
import com.congen.model.WorkoutStageTypeEnum
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for WorkoutStageTypeController.
 *
 * These tests verify the REST API endpoints for workout stage type operations,
 * including read operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class WorkoutStageTypeControllerTest {
    private lateinit var workoutStageTypeDAL: WorkoutStageTypeDAL
    private lateinit var workoutStageTypeController: WorkoutStageTypeController

    @BeforeEach
    fun setUp() {
        workoutStageTypeDAL = mock()
        workoutStageTypeController = WorkoutStageTypeController(workoutStageTypeDAL)
    }

    @Test
    fun `get should return workout stage type when found`() {
        val now = Instant.now()
        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.WARMUP,
                createdAt = now
            )
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeById(1)).thenReturn(Mono.just(workoutStageType))

        val result = workoutStageTypeController.get(1)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(workoutStageType))
            .verifyComplete()

        verify(workoutStageTypeDAL).selectWorkoutStageTypeById(1)
    }

    @Test
    fun `get should return not found when workout stage type not found`() {
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeById(999)).thenReturn(Mono.error(NoResultsFoundException("Not found")))

        val result = workoutStageTypeController.get(999)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(workoutStageTypeDAL).selectWorkoutStageTypeById(999)
    }

    @Test
    fun `getByName should return workout stage type when found`() {
        val now = Instant.now()
        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.WARMUP,
                createdAt = now
            )
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(WorkoutStageTypeEnum.WARMUP)).thenReturn(Mono.just(workoutStageType))

        val result = workoutStageTypeController.getByName("Warmup")

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(workoutStageType))
            .verifyComplete()

        verify(workoutStageTypeDAL).selectWorkoutStageTypeByEnum(WorkoutStageTypeEnum.WARMUP)
    }

    @Test
    fun `getByName should return not found when workout stage type not found`() {
        whenever(
            workoutStageTypeDAL.selectWorkoutStageTypeByEnum(WorkoutStageTypeEnum.WARMUP)
        ).thenReturn(Mono.error(NoResultsFoundException("Not found")))

        val result = workoutStageTypeController.getByName("Warmup")

        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(workoutStageTypeDAL).selectWorkoutStageTypeByEnum(WorkoutStageTypeEnum.WARMUP)
    }

    @Test
    fun `getAll should return all workout stage types`() {
        val now = Instant.now()
        val workoutStageTypes =
            listOf(
                WorkoutStageType(
                    id = 1,
                    name = WorkoutStageTypeEnum.WARMUP,
                    createdAt = now
                ),
                WorkoutStageType(
                    id = 2,
                    name = WorkoutStageTypeEnum.PRIMARY,
                    createdAt = now
                )
            )
        whenever(workoutStageTypeDAL.selectWorkoutStageTypes()).thenReturn(Mono.just(workoutStageTypes))

        val result = workoutStageTypeController.getAll()

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(workoutStageTypes))
            .verifyComplete()

        verify(workoutStageTypeDAL).selectWorkoutStageTypes()
    }

    @Test
    fun `should handle DAL error gracefully for getAll`() {
        whenever(workoutStageTypeDAL.selectWorkoutStageTypes()).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = workoutStageTypeController.getAll()

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }
}
