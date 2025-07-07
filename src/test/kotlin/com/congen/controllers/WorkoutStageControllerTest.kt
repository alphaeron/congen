package com.congen.controllers

import com.congen.dal.WorkoutStageDAL
import com.congen.mockWorkoutStage
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

    companion object {
        private const val WORKOUT_STAGE_ID_1 = 1L
        private const val WORKOUT_STAGE_ID_2 = 2L
        private const val PROGRAMMED_WORKOUT_ID = 5L
        private const val STAGE_TYPE_ID_1 = 1
        private const val STAGE_TYPE_ID_2 = 2
        private const val POSITION_1 = 1
        private const val POSITION_2 = 2
        private const val WARMUP_NAME = "Warmup"
        private const val MAIN_WORK_NAME = "Main Work"
    }

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        workoutStageController = WorkoutStageController(workoutStageDAL)
    }

    @Test
    fun `GET by ID should return workout stage`() {
        val now = Instant.now()
        val workoutStage =
            mockWorkoutStage(
                id = WORKOUT_STAGE_ID_1,
                programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                stageTypeId = STAGE_TYPE_ID_1,
                position = POSITION_1,
                name = WARMUP_NAME,
                createdAt = now,
                updatedAt = now
            )
        whenever(workoutStageDAL.selectWorkoutStageById(WORKOUT_STAGE_ID_1)).thenReturn(Mono.just(workoutStage))
        val result = workoutStageController.get(WORKOUT_STAGE_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(workoutStage))
            .verifyComplete()
        verify(workoutStageDAL).selectWorkoutStageById(WORKOUT_STAGE_ID_1)
    }

    @Test
    fun `GET by programmed workout ID should return list of workout stages`() {
        val now = Instant.now()
        val workoutStages =
            listOf(
                mockWorkoutStage(
                    id = WORKOUT_STAGE_ID_1,
                    programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                    stageTypeId = STAGE_TYPE_ID_1,
                    position = POSITION_1,
                    name = WARMUP_NAME,
                    createdAt = now,
                    updatedAt = now
                ),
                mockWorkoutStage(
                    id = WORKOUT_STAGE_ID_2,
                    programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                    stageTypeId = STAGE_TYPE_ID_2,
                    position = POSITION_2,
                    name = MAIN_WORK_NAME,
                    createdAt = now,
                    updatedAt = now
                )
            )
        whenever(workoutStageDAL.selectWorkoutStagesByProgrammedWorkoutId(PROGRAMMED_WORKOUT_ID)).thenReturn(Mono.just(workoutStages))
        val result = workoutStageController.getByProgrammedWorkoutId(PROGRAMMED_WORKOUT_ID)
        assertEquals(HttpStatus.OK, result.statusCode)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<WorkoutStage>>)
            .expectNext(workoutStages)
            .verifyComplete()
        verify(workoutStageDAL).selectWorkoutStagesByProgrammedWorkoutId(PROGRAMMED_WORKOUT_ID)
    }

    @Test
    fun `POST workout_stage should create new workout stage`() {
        val now = Instant.now()
        val createdStage =
            mockWorkoutStage(
                id = WORKOUT_STAGE_ID_1,
                programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                stageTypeId = STAGE_TYPE_ID_1,
                position = POSITION_1,
                name = WARMUP_NAME,
                createdAt = now,
                updatedAt = now
            )
        whenever(
            workoutStageDAL.insertWorkoutStage(
                programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                stageTypeId = STAGE_TYPE_ID_1,
                position = POSITION_1,
                name = WARMUP_NAME
            )
        ).thenReturn(Mono.just(createdStage))
        val result = workoutStageController.save(PROGRAMMED_WORKOUT_ID, STAGE_TYPE_ID_1, POSITION_1, WARMUP_NAME)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(createdStage))
            .verifyComplete()
        verify(workoutStageDAL).insertWorkoutStage(
            programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
            stageTypeId = STAGE_TYPE_ID_1,
            position = POSITION_1,
            name = WARMUP_NAME
        )
    }

    @Test
    fun `update by ID should update workout stage`() {
        val now = Instant.now()
        val updatedStage =
            mockWorkoutStage(
                id = WORKOUT_STAGE_ID_1,
                programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                stageTypeId = STAGE_TYPE_ID_2,
                position = POSITION_2,
                name = MAIN_WORK_NAME,
                createdAt = now,
                updatedAt = now
            )
        whenever(
            workoutStageDAL.updateWorkoutStage(
                id = WORKOUT_STAGE_ID_1,
                programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                stageTypeId = STAGE_TYPE_ID_2,
                position = POSITION_2,
                name = MAIN_WORK_NAME
            )
        ).thenReturn(Mono.just(updatedStage))
        val result = workoutStageController.update(WORKOUT_STAGE_ID_1, PROGRAMMED_WORKOUT_ID, STAGE_TYPE_ID_2, POSITION_2, MAIN_WORK_NAME)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(updatedStage))
            .verifyComplete()
        verify(workoutStageDAL).updateWorkoutStage(
            id = WORKOUT_STAGE_ID_1,
            programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
            stageTypeId = STAGE_TYPE_ID_2,
            position = POSITION_2,
            name = MAIN_WORK_NAME
        )
    }

    @Test
    fun `DELETE by ID should delete workout stage`() {
        val now = Instant.now()
        val deletedStage =
            mockWorkoutStage(
                id = WORKOUT_STAGE_ID_1,
                programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                stageTypeId = STAGE_TYPE_ID_1,
                position = POSITION_1,
                name = WARMUP_NAME,
                createdAt = now,
                updatedAt = now
            )
        whenever(workoutStageDAL.deleteWorkoutStage(WORKOUT_STAGE_ID_1)).thenReturn(Mono.just(deletedStage))
        val result = workoutStageController.delete(WORKOUT_STAGE_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(deletedStage))
            .verifyComplete()
        verify(workoutStageDAL).deleteWorkoutStage(WORKOUT_STAGE_ID_1)
    }
}
