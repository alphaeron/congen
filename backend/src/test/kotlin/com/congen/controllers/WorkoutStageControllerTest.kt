package com.congen.controllers

import com.congen.dal.WorkoutStageDAL
import com.congen.exceptions.DatabaseException
import com.congen.exceptions.DatabaseQueryException
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockWorkoutStage
import com.congen.model.WorkoutStage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.util.stream.Stream

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
        private const val NON_EXISTENT_ID = 999L

        @JvmStatic
        fun errorScenarios(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    "GET by ID should handle database errors",
                    { controller: WorkoutStageController, dal: WorkoutStageDAL ->
                        whenever(dal.selectWorkoutStageById(WORKOUT_STAGE_ID_1))
                            .thenReturn(Mono.error(DatabaseQueryException("Database connection failed")))
                        controller.get(WORKOUT_STAGE_ID_1)
                    },
                    { dal: WorkoutStageDAL ->
                        verify(dal).selectWorkoutStageById(WORKOUT_STAGE_ID_1)
                    },
                    // expect 404 response
                    false
                ),
                Arguments.of(
                    "POST should handle database errors",
                    { controller: WorkoutStageController, dal: WorkoutStageDAL ->
                        whenever(
                            dal.insertWorkoutStage(
                                programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                                stageTypeId = STAGE_TYPE_ID_1,
                                position = POSITION_1,
                                name = WARMUP_NAME
                            )
                        ).thenReturn(Mono.error(DatabaseQueryException("Database connection failed")))
                        controller.save(PROGRAMMED_WORKOUT_ID, STAGE_TYPE_ID_1, POSITION_1, WARMUP_NAME)
                    },
                    { dal: WorkoutStageDAL ->
                        verify(dal).insertWorkoutStage(
                            programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                            stageTypeId = STAGE_TYPE_ID_1,
                            position = POSITION_1,
                            name = WARMUP_NAME
                        )
                    },
                    // expect error to propagate
                    true
                ),
                Arguments.of(
                    "UPDATE should handle database errors",
                    { controller: WorkoutStageController, dal: WorkoutStageDAL ->
                        whenever(
                            dal.updateWorkoutStage(
                                id = WORKOUT_STAGE_ID_1,
                                programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                                stageTypeId = STAGE_TYPE_ID_2,
                                position = POSITION_2,
                                name = MAIN_WORK_NAME
                            )
                        ).thenReturn(Mono.error(DatabaseQueryException("Database connection failed")))
                        controller.update(WORKOUT_STAGE_ID_1, PROGRAMMED_WORKOUT_ID, STAGE_TYPE_ID_2, POSITION_2, MAIN_WORK_NAME)
                    },
                    { dal: WorkoutStageDAL ->
                        verify(dal).updateWorkoutStage(
                            id = WORKOUT_STAGE_ID_1,
                            programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                            stageTypeId = STAGE_TYPE_ID_2,
                            position = POSITION_2,
                            name = MAIN_WORK_NAME
                        )
                    },
                    // expect 404 response
                    false
                ),
                Arguments.of(
                    "DELETE should handle database errors",
                    { controller: WorkoutStageController, dal: WorkoutStageDAL ->
                        whenever(dal.deleteWorkoutStage(WORKOUT_STAGE_ID_1))
                            .thenReturn(Mono.error(DatabaseQueryException("Database connection failed")))
                        controller.delete(WORKOUT_STAGE_ID_1)
                    },
                    { dal: WorkoutStageDAL ->
                        verify(dal).deleteWorkoutStage(WORKOUT_STAGE_ID_1)
                    },
                    // expect 404 response
                    false
                )
            )

        @JvmStatic
        fun notFoundScenarios(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    "GET by ID should return not found when workout stage not found",
                    { controller: WorkoutStageController, dal: WorkoutStageDAL ->
                        whenever(dal.selectWorkoutStageById(NON_EXISTENT_ID))
                            .thenReturn(Mono.error(NoResultsFoundException("Not found")))
                        controller.get(NON_EXISTENT_ID)
                    },
                    { dal: WorkoutStageDAL ->
                        verify(dal).selectWorkoutStageById(NON_EXISTENT_ID)
                    }
                ),
                Arguments.of(
                    "UPDATE should return not found when workout stage not found",
                    { controller: WorkoutStageController, dal: WorkoutStageDAL ->
                        whenever(
                            dal.updateWorkoutStage(
                                id = NON_EXISTENT_ID,
                                programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                                stageTypeId = STAGE_TYPE_ID_2,
                                position = POSITION_2,
                                name = MAIN_WORK_NAME
                            )
                        ).thenReturn(Mono.error(NoResultsFoundException("Not found")))
                        controller.update(NON_EXISTENT_ID, PROGRAMMED_WORKOUT_ID, STAGE_TYPE_ID_2, POSITION_2, MAIN_WORK_NAME)
                    },
                    { dal: WorkoutStageDAL ->
                        verify(dal).updateWorkoutStage(
                            id = NON_EXISTENT_ID,
                            programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                            stageTypeId = STAGE_TYPE_ID_2,
                            position = POSITION_2,
                            name = MAIN_WORK_NAME
                        )
                    }
                ),
                Arguments.of(
                    "DELETE should return not found when workout stage not found",
                    { controller: WorkoutStageController, dal: WorkoutStageDAL ->
                        whenever(dal.deleteWorkoutStage(NON_EXISTENT_ID))
                            .thenReturn(Mono.error(NoResultsFoundException("Not found")))
                        controller.delete(NON_EXISTENT_ID)
                    },
                    { dal: WorkoutStageDAL ->
                        verify(dal).deleteWorkoutStage(NON_EXISTENT_ID)
                    }
                )
            )
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

    @ParameterizedTest(name = "{0}")
    @MethodSource("notFoundScenarios")
    @Suppress("UnusedParameter")
    fun `should handle not found scenarios`(
        _testName: String,
        testAction: (WorkoutStageController, WorkoutStageDAL) -> Mono<ResponseEntity<WorkoutStage>>,
        verification: (WorkoutStageDAL) -> Unit
    ) {
        val result = testAction(workoutStageController, workoutStageDAL)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()
        verification(workoutStageDAL)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("errorScenarios")
    @Suppress("UnusedParameter")
    fun `should handle database errors`(
        _testName: String,
        testAction: (WorkoutStageController, WorkoutStageDAL) -> Mono<ResponseEntity<WorkoutStage>>,
        verification: (WorkoutStageDAL) -> Unit,
        expectError: Boolean
    ) {
        val result = testAction(workoutStageController, workoutStageDAL)
        if (expectError) {
            StepVerifier.create(result)
                .expectError(DatabaseQueryException::class.java)
                .verify()
        } else {
            StepVerifier.create(result)
                .expectNext(ResponseEntity.notFound().build())
                .verifyComplete()
        }
        verification(workoutStageDAL)
    }

    @Test
    fun `GET all should return all workout stages`() {
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
        whenever(workoutStageDAL.selectWorkoutStages()).thenReturn(Mono.just(workoutStages))
        val result = workoutStageController.getAll()
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(workoutStages))
            .verifyComplete()
        verify(workoutStageDAL).selectWorkoutStages()
    }

    @Test
    fun `GET all should handle database errors`() {
        whenever(workoutStageDAL.selectWorkoutStages())
            .thenReturn(Mono.error(DatabaseException("Database connection failed")))
        val result = workoutStageController.getAll()
        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
        verify(workoutStageDAL).selectWorkoutStages()
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
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(workoutStages))
            .verifyComplete()
        verify(workoutStageDAL).selectWorkoutStagesByProgrammedWorkoutId(PROGRAMMED_WORKOUT_ID)
    }

    @Test
    fun `GET by programmed workout ID should handle database errors`() {
        whenever(workoutStageDAL.selectWorkoutStagesByProgrammedWorkoutId(PROGRAMMED_WORKOUT_ID))
            .thenReturn(Mono.error(DatabaseException("Database connection failed")))
        val result = workoutStageController.getByProgrammedWorkoutId(PROGRAMMED_WORKOUT_ID)
        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
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
