package com.congen.controllers

import com.congen.dal.WorkoutStageDAL
import com.congen.model.WorkoutStage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import kotlin.test.assertEquals

class WorkoutStageControllerTest {
    @Mock
    private lateinit var workoutStageDAL: WorkoutStageDAL

    private lateinit var workoutStageController: WorkoutStageController

    private val now = Instant.now()

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        workoutStageController = WorkoutStageController(workoutStageDAL)
    }

    @Test
    fun `GET by ID should return workout stage`() {
        val workoutStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1,
                name = "Warmup",
                createdAt = now,
                updatedAt = now
            )

        whenever(workoutStageDAL.selectWorkoutStageById(1L)).thenReturn(Mono.just(workoutStage))

        val result = workoutStageController.get(1L)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(workoutStage))
            .verifyComplete()

        verify(workoutStageDAL).selectWorkoutStageById(1L)
    }

    @Test
    fun `GET by programmed workout ID should return list of workout stages`() {
        val workoutStages =
            listOf(
                WorkoutStage(
                    id = 1L,
                    programmedWorkoutId = 5L,
                    stageTypeId = 1,
                    position = 1,
                    name = "Warmup",
                    createdAt = now,
                    updatedAt = now
                ),
                WorkoutStage(
                    id = 2L,
                    programmedWorkoutId = 5L,
                    stageTypeId = 2,
                    position = 2,
                    name = "Main Work",
                    createdAt = now,
                    updatedAt = now
                )
            )

        whenever(workoutStageDAL.selectWorkoutStagesByProgrammedWorkoutId(5L)).thenReturn(Mono.just(workoutStages))

        val result = workoutStageController.getByProgrammedWorkoutId(5L)

        assertEquals(HttpStatus.OK, result.statusCode)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<WorkoutStage>>)
            .expectNext(workoutStages)
            .verifyComplete()
        verify(workoutStageDAL).selectWorkoutStagesByProgrammedWorkoutId(5L)
    }

    @Test
    fun `POST workout_stage should create new workout stage`() {
        val createdStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1,
                name = "Warmup",
                createdAt = now,
                updatedAt = now
            )

        whenever(
            workoutStageDAL.insertWorkoutStage(
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1,
                name = "Warmup"
            )
        ).thenReturn(Mono.just(createdStage))

        val result = workoutStageController.save(5L, 1, 1, "Warmup")

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(createdStage))
            .verifyComplete()

        verify(workoutStageDAL).insertWorkoutStage(
            programmedWorkoutId = 5L,
            stageTypeId = 1,
            position = 1,
            name = "Warmup"
        )
    }

    @Test
    fun `update by ID should update workout stage`() {
        val updatedStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 2,
                position = 2,
                name = "Main Work",
                createdAt = now,
                updatedAt = now
            )

        whenever(
            workoutStageDAL.updateWorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 2,
                position = 2,
                name = "Main Work"
            )
        ).thenReturn(Mono.just(updatedStage))

        val result = workoutStageController.update(1L, 5L, 2, 2, "Main Work")

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(updatedStage))
            .verifyComplete()

        verify(workoutStageDAL).updateWorkoutStage(
            id = 1L,
            programmedWorkoutId = 5L,
            stageTypeId = 2,
            position = 2,
            name = "Main Work"
        )
    }

    @Test
    fun `DELETE by ID should delete workout stage`() {
        val deletedStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 5L,
                stageTypeId = 1,
                position = 1,
                name = "Warmup",
                createdAt = now,
                updatedAt = now
            )

        whenever(workoutStageDAL.deleteWorkoutStage(1L)).thenReturn(Mono.just(deletedStage))

        val result = workoutStageController.delete(1L)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(deletedStage))
            .verifyComplete()

        verify(workoutStageDAL).deleteWorkoutStage(1L)
    }
}
