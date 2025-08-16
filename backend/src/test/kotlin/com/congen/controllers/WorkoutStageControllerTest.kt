package com.congen.controllers

import com.congen.createGdprComplianceServiceSpy
import com.congen.exceptions.DatabaseException
import com.congen.exceptions.DatabaseQueryException
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockWorkoutStage
import com.congen.model.ProgrammedWorkout
import com.congen.model.WorkoutStage
import com.congen.service.GdprComplianceService
import com.congen.service.ProgramService
import com.congen.service.ProgrammedWorkoutService
import com.congen.service.WorkoutStageService
import com.congen.util.KeycloakUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.util.stream.Stream

/**
 * Unit tests for WorkoutStageController.
 *
 * These tests verify the REST API endpoints for workout stage operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class WorkoutStageControllerTest {
    private lateinit var workoutStageService: WorkoutStageService
    private lateinit var programService: ProgramService
    private lateinit var programmedWorkoutService: ProgrammedWorkoutService
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var gdprComplianceService: GdprComplianceService
    private lateinit var workoutStageController: WorkoutStageController

    private val currentUserId = "test-keycloak-user-id"

    companion object {
        private const val WORKOUT_STAGE_ID_1 = 1L
        private const val WORKOUT_STAGE_ID_2 = 2L
        private const val PROGRAMMED_WORKOUT_ID = 5L
        private const val PROGRAM_ID = 10L
        private const val STAGE_TYPE_ID_1 = 1
        private const val STAGE_TYPE_ID_2 = 2
        private const val POSITION_1 = 1
        private const val POSITION_2 = 2
        private const val WARMUP_NAME = "Warm-up"
        private const val MAIN_WORK_NAME = "Main Work"
        private const val COOLDOWN_NAME = "Cool-down"
        private const val NON_EXISTENT_ID = 999L

        @JvmStatic
        fun errorScenarios(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    "GET by ID should handle database errors",
                    { controller: WorkoutStageController, service: WorkoutStageService ->
                        whenever(service.selectWorkoutStageById(WORKOUT_STAGE_ID_1))
                            .thenReturn(Mono.error(DatabaseQueryException("Database connection failed")))
                        controller.get(WORKOUT_STAGE_ID_1)
                    },
                    { service: WorkoutStageService ->
                        verify(service).selectWorkoutStageById(WORKOUT_STAGE_ID_1)
                    },
                    // expect error to propagate
                    true
                ),
                Arguments.of(
                    "POST should handle database errors",
                    { controller: WorkoutStageController, service: WorkoutStageService ->
                        whenever(
                            service.insertWorkoutStage(
                                programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                                stageTypeId = STAGE_TYPE_ID_1,
                                position = POSITION_1,
                                name = WARMUP_NAME
                            )
                        ).thenReturn(Mono.error(DatabaseQueryException("Database connection failed")))
                        controller.save(PROGRAMMED_WORKOUT_ID, STAGE_TYPE_ID_1, POSITION_1, WARMUP_NAME)
                    },
                    { service: WorkoutStageService ->
                        verify(service).insertWorkoutStage(
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
                    { controller: WorkoutStageController, service: WorkoutStageService ->
                        whenever(
                            service.updateWorkoutStage(
                                id = WORKOUT_STAGE_ID_1,
                                programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                                stageTypeId = STAGE_TYPE_ID_2,
                                position = POSITION_2,
                                name = MAIN_WORK_NAME
                            )
                        ).thenReturn(Mono.error(DatabaseQueryException("Database connection failed")))
                        controller.update(WORKOUT_STAGE_ID_1, PROGRAMMED_WORKOUT_ID, STAGE_TYPE_ID_2, POSITION_2, MAIN_WORK_NAME)
                    },
                    { service: WorkoutStageService ->
                        verify(service).updateWorkoutStage(
                            id = WORKOUT_STAGE_ID_1,
                            programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                            stageTypeId = STAGE_TYPE_ID_2,
                            position = POSITION_2,
                            name = MAIN_WORK_NAME
                        )
                    },
                    // expect error to propagate
                    true
                ),
                Arguments.of(
                    "DELETE should handle database errors",
                    { controller: WorkoutStageController, service: WorkoutStageService ->
                        whenever(service.deleteWorkoutStage(WORKOUT_STAGE_ID_1))
                            .thenReturn(Mono.error(DatabaseQueryException("Database connection failed")))
                        controller.delete(WORKOUT_STAGE_ID_1)
                    },
                    { service: WorkoutStageService ->
                        verify(service).deleteWorkoutStage(WORKOUT_STAGE_ID_1)
                    },
                    // expect error to propagate
                    true
                )
            )

        @JvmStatic
        fun notFoundScenarios(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    "GET by ID should return not found when workout stage not found",
                    { controller: WorkoutStageController, service: WorkoutStageService ->
                        whenever(service.selectWorkoutStageById(NON_EXISTENT_ID))
                            .thenReturn(Mono.error(NoResultsFoundException("Not found")))
                        controller.get(NON_EXISTENT_ID)
                    },
                    { service: WorkoutStageService ->
                        verify(service).selectWorkoutStageById(NON_EXISTENT_ID)
                    }
                ),
                Arguments.of(
                    "UPDATE should return not found when workout stage not found",
                    { controller: WorkoutStageController, service: WorkoutStageService ->
                        whenever(
                            service.updateWorkoutStage(
                                id = NON_EXISTENT_ID,
                                programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                                stageTypeId = STAGE_TYPE_ID_2,
                                position = POSITION_2,
                                name = MAIN_WORK_NAME
                            )
                        ).thenReturn(Mono.error(NoResultsFoundException("Not found")))
                        controller.update(NON_EXISTENT_ID, PROGRAMMED_WORKOUT_ID, STAGE_TYPE_ID_2, POSITION_2, MAIN_WORK_NAME)
                    },
                    { service: WorkoutStageService ->
                        verify(service).updateWorkoutStage(
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
                    { controller: WorkoutStageController, service: WorkoutStageService ->
                        whenever(service.deleteWorkoutStage(NON_EXISTENT_ID))
                            .thenReturn(Mono.error(NoResultsFoundException("Not found")))
                        controller.delete(NON_EXISTENT_ID)
                    },
                    { service: WorkoutStageService ->
                        verify(service).deleteWorkoutStage(NON_EXISTENT_ID)
                    }
                )
            )
    }

    @BeforeEach
    fun setUp() {
        workoutStageService = mock()
        programService = mock()
        programmedWorkoutService = mock()
        keycloakUtil = mock()
        gdprComplianceService = createGdprComplianceServiceSpy()
        workoutStageController = WorkoutStageController(workoutStageService, programService, programmedWorkoutService, keycloakUtil, gdprComplianceService)

        doReturn(Mono.just(true)).whenever(gdprComplianceService).hasUserConsent(any<String>())

        // Mock KeycloakUtil methods for all tests
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(currentUserId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
    }

    @Test
    fun `GET by ID should return workout stage when found`() {
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
        whenever(workoutStageService.selectWorkoutStageById(WORKOUT_STAGE_ID_1)).thenReturn(Mono.just(workoutStage))
        whenever(workoutStageService.isOwner(WORKOUT_STAGE_ID_1, currentUserId)).thenReturn(Mono.just(true))

        val result = workoutStageController.get(WORKOUT_STAGE_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(workoutStage))
            .verifyComplete()
        verify(workoutStageService).selectWorkoutStageById(WORKOUT_STAGE_ID_1)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("notFoundScenarios")
    @Suppress("UnusedParameter")
    fun `should handle not found scenarios`(
        _testName: String,
        testAction: (WorkoutStageController, WorkoutStageService) -> Mono<ResponseEntity<WorkoutStage>>,
        verification: (WorkoutStageService) -> Unit
    ) {
        // Mock the security checks for all not found scenarios
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(currentUserId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        whenever(workoutStageService.isOwner(any(), any())).thenReturn(Mono.just(true))
        whenever(programmedWorkoutService.selectProgrammedWorkoutById(any())).thenReturn(
            Mono.just(
                ProgrammedWorkout(
                    id = PROGRAMMED_WORKOUT_ID,
                    programId = PROGRAM_ID,
                    dayNumber = 1,
                    name = "Test Workout",
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
            )
        )
        whenever(programService.isOwner(any(), any())).thenReturn(Mono.just(true))
        whenever(programmedWorkoutService.isOwner(any(), any())).thenReturn(Mono.just(true))

        val result = testAction(workoutStageController, workoutStageService)
        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verification(workoutStageService)
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("errorScenarios")
    @Suppress("UnusedParameter")
    fun `should handle database errors`(
        _testName: String,
        testAction: (WorkoutStageController, WorkoutStageService) -> Mono<ResponseEntity<WorkoutStage>>,
        verification: (WorkoutStageService) -> Unit,
        expectError: Boolean
    ) {
        // Mock the security checks for all error scenarios
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(currentUserId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        whenever(workoutStageService.isOwner(any(), any())).thenReturn(Mono.just(true))
        whenever(programmedWorkoutService.selectProgrammedWorkoutById(any())).thenReturn(
            Mono.just(
                ProgrammedWorkout(
                    id = PROGRAMMED_WORKOUT_ID,
                    programId = PROGRAM_ID,
                    dayNumber = 1,
                    name = "Test Workout",
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
            )
        )
        whenever(programService.isOwner(any(), any())).thenReturn(Mono.just(true))
        whenever(programmedWorkoutService.isOwner(any(), any())).thenReturn(Mono.just(true))

        val result = testAction(workoutStageController, workoutStageService)
        if (expectError) {
            StepVerifier.create(result)
                .expectError(DatabaseQueryException::class.java)
                .verify()
        } else {
            StepVerifier.create(result)
                .expectNext(ResponseEntity.notFound().build())
                .verifyComplete()
        }
        verification(workoutStageService)
    }

    @Test
    fun `GET all should return all workout stages for admin user`() {
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
        val userId = "123"
        val roles = setOf("admin")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(workoutStageService.selectWorkoutStages()).thenReturn(Mono.just(workoutStages))

        val result = workoutStageController.getAll()
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(workoutStages))
            .verifyComplete()
        verify(workoutStageService).selectWorkoutStages()
    }

    @Test
    fun `GET all should return user owned workout stages for regular user`() {
        val now = Instant.now()
        val userWorkoutStages =
            listOf(
                mockWorkoutStage(
                    id = WORKOUT_STAGE_ID_1,
                    programmedWorkoutId = PROGRAMMED_WORKOUT_ID,
                    stageTypeId = STAGE_TYPE_ID_1,
                    position = POSITION_1,
                    name = WARMUP_NAME,
                    createdAt = now,
                    updatedAt = now
                )
            )
        val userId = "123"
        val roles = setOf("user")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(workoutStageService.selectWorkoutStagesByUserId(userId)).thenReturn(Mono.just(userWorkoutStages))

        val result = workoutStageController.getAll()
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userWorkoutStages))
            .verifyComplete()
        verify(workoutStageService).selectWorkoutStagesByUserId(userId)
    }

    @Test
    fun `GET all should return all workout stages for service user`() {
        val now = Instant.now()
        val allWorkoutStages =
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
        val userId = "123"
        val roles = setOf("service")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(workoutStageService.selectWorkoutStages()).thenReturn(Mono.just(allWorkoutStages))

        val result = workoutStageController.getAll()
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(allWorkoutStages))
            .verifyComplete()
        verify(workoutStageService).selectWorkoutStages()
    }

    @Test
    fun `GET all should return empty list when regular user has no owned workout stages`() {
        val emptyList = emptyList<WorkoutStage>()
        val userId = "123"
        val roles = setOf("user")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(workoutStageService.selectWorkoutStagesByUserId(userId)).thenReturn(Mono.just(emptyList))

        val result = workoutStageController.getAll()
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(emptyList))
            .verifyComplete()
        verify(workoutStageService).selectWorkoutStagesByUserId(userId)
    }

    @Test
    fun `GET all should propagate errors from getAll`() {
        val userId = "123"
        val roles = setOf("user")
        val databaseError = RuntimeException("Database error")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(workoutStageService.selectWorkoutStagesByUserId(userId)).thenReturn(Mono.error(databaseError))

        val result = workoutStageController.getAll()
        StepVerifier.create(result)
            .expectError(databaseError::class.java)
            .verify()
        verify(workoutStageService).selectWorkoutStagesByUserId(userId)
    }

    @Test
    fun `GET all should handle database errors`() {
        val userId = "1"
        val roles = setOf("admin")
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(workoutStageService.selectWorkoutStages())
            .thenReturn(Mono.error(DatabaseException("Database connection failed")))
        val result = workoutStageController.getAll()
        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
        verify(workoutStageService).selectWorkoutStages()
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

        val programmedWorkout =
            ProgrammedWorkout(
                id = PROGRAMMED_WORKOUT_ID,
                programId = PROGRAM_ID,
                dayNumber = 1,
                name = "Test Workout",
                createdAt = now,
                updatedAt = now
            )

        whenever(programmedWorkoutService.selectProgrammedWorkoutById(PROGRAMMED_WORKOUT_ID)).thenReturn(Mono.just(programmedWorkout))
        whenever(programService.isOwner(PROGRAM_ID, currentUserId)).thenReturn(Mono.just(true))
        whenever(workoutStageService.selectWorkoutStagesByProgrammedWorkoutId(PROGRAMMED_WORKOUT_ID)).thenReturn(Mono.just(workoutStages))

        val result = workoutStageController.getByProgrammedWorkoutId(PROGRAMMED_WORKOUT_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(workoutStages))
            .verifyComplete()
        verify(workoutStageService).selectWorkoutStagesByProgrammedWorkoutId(PROGRAMMED_WORKOUT_ID)
    }

    @Test
    fun `GET by programmed workout ID should handle database errors`() {
        // Mock the security checks
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(currentUserId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        whenever(programmedWorkoutService.selectProgrammedWorkoutById(PROGRAMMED_WORKOUT_ID)).thenReturn(
            Mono.just(
                ProgrammedWorkout(
                    id = PROGRAMMED_WORKOUT_ID,
                    programId = PROGRAM_ID,
                    dayNumber = 1,
                    name = "Test Workout",
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
            )
        )
        whenever(programService.isOwner(PROGRAM_ID, currentUserId)).thenReturn(Mono.just(true))
        whenever(workoutStageService.selectWorkoutStagesByProgrammedWorkoutId(PROGRAMMED_WORKOUT_ID))
            .thenReturn(Mono.error(DatabaseException("Database connection failed")))

        val result = workoutStageController.getByProgrammedWorkoutId(PROGRAMMED_WORKOUT_ID)
        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
        verify(workoutStageService).selectWorkoutStagesByProgrammedWorkoutId(PROGRAMMED_WORKOUT_ID)
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

        val programmedWorkout =
            ProgrammedWorkout(
                id = PROGRAMMED_WORKOUT_ID,
                programId = PROGRAM_ID,
                dayNumber = 1,
                name = "Test Workout",
                createdAt = now,
                updatedAt = now
            )

        whenever(programmedWorkoutService.selectProgrammedWorkoutById(PROGRAMMED_WORKOUT_ID)).thenReturn(Mono.just(programmedWorkout))
        whenever(programService.isOwner(PROGRAM_ID, currentUserId)).thenReturn(Mono.just(true))
        whenever(
            workoutStageService.insertWorkoutStage(
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
        verify(workoutStageService).insertWorkoutStage(
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

        whenever(workoutStageService.isOwner(WORKOUT_STAGE_ID_1, currentUserId)).thenReturn(Mono.just(true))
        whenever(programmedWorkoutService.isOwner(PROGRAMMED_WORKOUT_ID, currentUserId)).thenReturn(Mono.just(true))
        whenever(
            workoutStageService.updateWorkoutStage(
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
        verify(workoutStageService).updateWorkoutStage(
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

        whenever(workoutStageService.isOwner(WORKOUT_STAGE_ID_1, currentUserId)).thenReturn(Mono.just(true))
        whenever(workoutStageService.deleteWorkoutStage(WORKOUT_STAGE_ID_1)).thenReturn(Mono.just(deletedStage))

        val result = workoutStageController.delete(WORKOUT_STAGE_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(deletedStage))
            .verifyComplete()
        verify(workoutStageService).deleteWorkoutStage(WORKOUT_STAGE_ID_1)
    }

    @Test
    fun `getAll returns all items for admin`() {
        val userId = "1"
        val roles = setOf("admin")
        val stages =
            listOf(
                mockWorkoutStage(id = 1L),
                mockWorkoutStage(id = 2L)
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(workoutStageService.selectWorkoutStages()).thenReturn(Mono.just(stages))

        val result = workoutStageController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.size == 2)
                assert(response.body!!.containsAll(stages))
            }
            .verifyComplete()
    }

    @Test
    fun `getAll returns all items for service`() {
        val userId = "1"
        val roles = setOf("service")
        val stages =
            listOf(
                mockWorkoutStage(id = 1L),
                mockWorkoutStage(id = 2L)
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(workoutStageService.selectWorkoutStages()).thenReturn(Mono.just(stages))

        val result = workoutStageController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.size == 2)
                assert(response.body!!.containsAll(stages))
            }
            .verifyComplete()
    }

    @Test
    fun `getAll returns only owned items for regular user`() {
        val userId = "1"
        val roles = setOf("user")
        val stages =
            listOf(
                mockWorkoutStage(id = 1L),
                mockWorkoutStage(id = 2L)
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(workoutStageService.selectWorkoutStagesByUserId(userId)).thenReturn(Mono.just(stages))

        val result = workoutStageController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.size == 2)
                assert(response.body!!.containsAll(stages))
            }
            .verifyComplete()
    }

    @Test
    fun `getAll returns empty for regular user with no owned items`() {
        val userId = "3"
        val roles = setOf("user")
        val stages = emptyList<WorkoutStage>()
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(workoutStageService.selectWorkoutStagesByUserId(userId)).thenReturn(Mono.just(stages))

        val result = workoutStageController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.isEmpty())
            }
            .verifyComplete()
    }
}
