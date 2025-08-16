package com.congen.controllers

import com.congen.createGdprComplianceServiceSpy
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.exceptions.DatabaseQueryException
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.ProgrammedExercise
import com.congen.service.GdprComplianceService
import com.congen.service.ProgrammedExerciseService
import com.congen.service.WorkoutStageService
import com.congen.util.KeycloakUtil
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for ProgrammedExerciseController.
 *
 * These tests verify the REST API endpoints for programmed exercise operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension::class)
class ProgrammedExerciseControllerTest {
    @Mock
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL

    private lateinit var programmedExerciseController: ProgrammedExerciseController
    private lateinit var programmedExerciseService: ProgrammedExerciseService
    private lateinit var workoutStageService: WorkoutStageService
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var gdprComplianceService: GdprComplianceService

    private val now = Instant.now()
    private val objectMapper = ObjectMapper()

    private lateinit var testProgrammedExercise: ProgrammedExercise

    companion object {
        private const val EXERCISE_ID_1 = 1L
        private const val EXERCISE_ID_2 = 2L
        private const val WORKOUT_STAGE_ID = 5L
        private const val BENCH_PRESS = "Bench Press"
        private const val SQUAT = "Back Squat"
        private const val POSITION_1 = 1
        private const val POSITION_2 = 2
        private const val NOTES = "Focus on controlled descent"
        private const val UPDATED_NOTES = "Updated notes"
        private const val NON_EXISTENT_ID = 999L
        private const val EMPTY_EXERCISE_NAME = ""
    }

    @BeforeEach
    fun setUp() {
        programmedExerciseService = mock()
        workoutStageService = mock()
        keycloakUtil = mock()
        gdprComplianceService = createGdprComplianceServiceSpy()
        programmedExerciseController = ProgrammedExerciseController(programmedExerciseService, workoutStageService, keycloakUtil, gdprComplianceService)

        // Mock KeycloakUtil methods for all tests
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just("test-keycloak-user-id"))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        
        // Mock GDPR compliance service for all tests
        doReturn(Mono.just(true)).whenever(gdprComplianceService).hasUserConsent(any<String>())

        testProgrammedExercise =
            ProgrammedExercise(
                id = EXERCISE_ID_1,
                workoutStageId = WORKOUT_STAGE_ID,
                exerciseName = BENCH_PRESS,
                position = POSITION_1,
                notes = NOTES,
                createdAt = now,
                updatedAt = now
            )
    }

    @Test
    fun `save should create new programmed exercise successfully`() {
        whenever(workoutStageService.isOwner(WORKOUT_STAGE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.insertProgrammedExercise(any(), any(), any(), any()))
            .thenReturn(Mono.just(testProgrammedExercise))
        val result =
            programmedExerciseController.save(
                workoutStageId = WORKOUT_STAGE_ID,
                exerciseName = BENCH_PRESS,
                position = POSITION_1,
                notes = NOTES
            )
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgrammedExercise))
            .verifyComplete()
        verify(programmedExerciseService).insertProgrammedExercise(WORKOUT_STAGE_ID, BENCH_PRESS, POSITION_1, NOTES)
    }

    @Test
    fun `save should handle null notes`() {
        val exerciseWithNullNotes = testProgrammedExercise.copy(notes = null)
        whenever(workoutStageService.isOwner(WORKOUT_STAGE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.insertProgrammedExercise(WORKOUT_STAGE_ID, BENCH_PRESS, POSITION_1, null))
            .thenReturn(Mono.just(exerciseWithNullNotes))
        val result =
            programmedExerciseController.save(
                workoutStageId = WORKOUT_STAGE_ID,
                exerciseName = BENCH_PRESS,
                position = POSITION_1,
                notes = null
            )
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseWithNullNotes))
            .verifyComplete()
        verify(programmedExerciseService).insertProgrammedExercise(WORKOUT_STAGE_ID, BENCH_PRESS, POSITION_1, null)
    }

    @Test
    fun `save should handle validation errors`() {
        whenever(workoutStageService.isOwner(WORKOUT_STAGE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.insertProgrammedExercise(any(), any(), any(), any()))
            .thenReturn(Mono.error(DatabaseQueryException("Validation error")))
        val result =
            programmedExerciseController.save(
                workoutStageId = WORKOUT_STAGE_ID,
                exerciseName = EMPTY_EXERCISE_NAME,
                position = POSITION_1,
                notes = NOTES
            )
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
        verify(programmedExerciseService).insertProgrammedExercise(WORKOUT_STAGE_ID, EMPTY_EXERCISE_NAME, POSITION_1, NOTES)
    }

    @Test
    fun `save should handle database errors`() {
        whenever(workoutStageService.isOwner(WORKOUT_STAGE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.insertProgrammedExercise(any(), any(), any(), any()))
            .thenReturn(Mono.error(DatabaseQueryException("Database connection failed")))
        val result =
            programmedExerciseController.save(
                workoutStageId = WORKOUT_STAGE_ID,
                exerciseName = BENCH_PRESS,
                position = POSITION_1,
                notes = NOTES
            )
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
        verify(programmedExerciseService).insertProgrammedExercise(WORKOUT_STAGE_ID, BENCH_PRESS, POSITION_1, NOTES)
    }

    @Test
    fun `get should return programmed exercise when found`() {
        whenever(programmedExerciseService.isOwner(EXERCISE_ID_1, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.selectProgrammedExerciseById(EXERCISE_ID_1))
            .thenReturn(Mono.just(testProgrammedExercise))
        val result = programmedExerciseController.get(EXERCISE_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgrammedExercise))
            .verifyComplete()
        verify(programmedExerciseService).selectProgrammedExerciseById(EXERCISE_ID_1)
    }

    @Test
    fun `get should return not found when programmed exercise not found`() {
        whenever(programmedExerciseService.isOwner(NON_EXISTENT_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.selectProgrammedExerciseById(NON_EXISTENT_ID))
            .thenReturn(Mono.error(NoResultsFoundException("Not found")))
        val result = programmedExerciseController.get(NON_EXISTENT_ID)
        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(programmedExerciseService).selectProgrammedExerciseById(NON_EXISTENT_ID)
    }

    @Test
    fun `get should handle database errors`() {
        whenever(programmedExerciseService.isOwner(EXERCISE_ID_1, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.selectProgrammedExerciseById(EXERCISE_ID_1))
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = programmedExerciseController.get(EXERCISE_ID_1)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
        verify(programmedExerciseService).selectProgrammedExerciseById(EXERCISE_ID_1)
    }

    @Test
    fun `getByStage should return programmed exercises for stage`() {
        val exercises =
            listOf(
                testProgrammedExercise,
                testProgrammedExercise.copy(
                    id = EXERCISE_ID_2,
                    exerciseName = SQUAT,
                    position = POSITION_2
                )
            )
        whenever(workoutStageService.isOwner(WORKOUT_STAGE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.selectProgrammedExercisesByWorkoutStageId(WORKOUT_STAGE_ID))
            .thenReturn(Mono.just(exercises))
        val result = programmedExerciseController.getByStage(WORKOUT_STAGE_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exercises))
            .verifyComplete()
        verify(programmedExerciseService).selectProgrammedExercisesByWorkoutStageId(WORKOUT_STAGE_ID)
    }

    @Test
    fun `getByStage should handle database errors`() {
        whenever(workoutStageService.isOwner(WORKOUT_STAGE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.selectProgrammedExercisesByWorkoutStageId(WORKOUT_STAGE_ID))
            .thenReturn(Mono.error(DatabaseQueryException("Database connection failed")))
        val result = programmedExerciseController.getByStage(WORKOUT_STAGE_ID)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
        verify(programmedExerciseService).selectProgrammedExercisesByWorkoutStageId(WORKOUT_STAGE_ID)
    }

    @Test
    fun `getAll returns all items for admin`() {
        val userId = "1"
        val roles = setOf("admin")
        val exercises =
            listOf(
                testProgrammedExercise,
                testProgrammedExercise.copy(id = EXERCISE_ID_2, workoutStageId = 2L)
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programmedExerciseService.selectProgrammedExercises()).thenReturn(Mono.just(exercises))

        val result = programmedExerciseController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.size == 2)
                assert(response.body!!.containsAll(exercises))
            }
            .verifyComplete()
    }

    @Test
    fun `getAll returns all items for service`() {
        val userId = "1"
        val roles = setOf("service")
        val exercises =
            listOf(
                testProgrammedExercise,
                testProgrammedExercise.copy(id = EXERCISE_ID_2, workoutStageId = 2L)
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programmedExerciseService.selectProgrammedExercises()).thenReturn(Mono.just(exercises))

        val result = programmedExerciseController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.size == 2)
                assert(response.body!!.containsAll(exercises))
            }
            .verifyComplete()
    }

    @Test
    fun `getAll returns only owned items for regular user`() {
        val userId = "1"
        val roles = setOf("user")
        val ownedExercises = listOf(testProgrammedExercise)
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programmedExerciseService.selectProgrammedExercisesByUserId(userId)).thenReturn(Mono.just(ownedExercises))

        val result = programmedExerciseController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.size == 1)
                assert(response.body!![0].id == EXERCISE_ID_1)
            }
            .verifyComplete()
    }

    @Test
    fun `getAll returns empty for regular user with no owned items`() {
        val userId = "3"
        val roles = setOf("user")
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programmedExerciseService.selectProgrammedExercisesByUserId(userId)).thenReturn(Mono.just(emptyList()))

        val result = programmedExerciseController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.isEmpty())
            }
            .verifyComplete()
    }

    @Test
    fun `getAll propagates error for regular user`() {
        val userId = "1"
        val roles = setOf("user")
        val ex = RuntimeException("db error")
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programmedExerciseService.selectProgrammedExercisesByUserId(userId)).thenReturn(Mono.error(ex))

        val result = programmedExerciseController.getAll()
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `update should update programmed exercise successfully`() {
        val updatedExercise = testProgrammedExercise.copy(notes = UPDATED_NOTES)
        whenever(workoutStageService.isOwner(WORKOUT_STAGE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.isOwner(EXERCISE_ID_1, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.updateProgrammedExercise(any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(updatedExercise))
        val result =
            programmedExerciseController.update(
                id = EXERCISE_ID_1,
                workoutStageId = WORKOUT_STAGE_ID,
                exerciseName = BENCH_PRESS,
                position = POSITION_2,
                notes = UPDATED_NOTES
            )
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(updatedExercise))
            .verifyComplete()
        verify(programmedExerciseService).updateProgrammedExercise(EXERCISE_ID_1, WORKOUT_STAGE_ID, BENCH_PRESS, POSITION_2, UPDATED_NOTES)
    }

    @Test
    fun `update should handle null notes`() {
        val updatedExercise =
            testProgrammedExercise.copy(
                exerciseName = SQUAT,
                position = POSITION_2,
                notes = null,
                updatedAt = now
            )
        whenever(workoutStageService.isOwner(WORKOUT_STAGE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.isOwner(EXERCISE_ID_1, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.updateProgrammedExercise(EXERCISE_ID_1, WORKOUT_STAGE_ID, SQUAT, POSITION_2, null))
            .thenReturn(Mono.just(updatedExercise))
        val result =
            programmedExerciseController.update(
                id = EXERCISE_ID_1,
                workoutStageId = WORKOUT_STAGE_ID,
                exerciseName = SQUAT,
                position = POSITION_2,
                notes = null
            )
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(updatedExercise))
            .verifyComplete()
        verify(programmedExerciseService).updateProgrammedExercise(EXERCISE_ID_1, WORKOUT_STAGE_ID, SQUAT, POSITION_2, null)
    }

    @Test
    fun `update should return not found when programmed exercise not found`() {
        whenever(workoutStageService.isOwner(WORKOUT_STAGE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.isOwner(NON_EXISTENT_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.updateProgrammedExercise(any(), any(), any(), any(), any()))
            .thenReturn(Mono.error(NoResultsFoundException("Not found")))
        val result =
            programmedExerciseController.update(
                id = NON_EXISTENT_ID,
                workoutStageId = WORKOUT_STAGE_ID,
                exerciseName = BENCH_PRESS,
                position = POSITION_1,
                notes = NOTES
            )
        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(programmedExerciseService).updateProgrammedExercise(NON_EXISTENT_ID, WORKOUT_STAGE_ID, BENCH_PRESS, POSITION_1, NOTES)
    }

    @Test
    fun `update should handle database errors`() {
        whenever(workoutStageService.isOwner(WORKOUT_STAGE_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.isOwner(EXERCISE_ID_1, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.updateProgrammedExercise(any(), any(), any(), any(), any()))
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result =
            programmedExerciseController.update(
                id = EXERCISE_ID_1,
                workoutStageId = WORKOUT_STAGE_ID,
                exerciseName = BENCH_PRESS,
                position = POSITION_1,
                notes = NOTES
            )
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
        verify(programmedExerciseService).updateProgrammedExercise(EXERCISE_ID_1, WORKOUT_STAGE_ID, BENCH_PRESS, POSITION_1, NOTES)
    }

    @Test
    fun `delete should delete programmed exercise successfully`() {
        whenever(programmedExerciseService.isOwner(EXERCISE_ID_1, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.deleteProgrammedExercise(EXERCISE_ID_1))
            .thenReturn(Mono.just(testProgrammedExercise))
        val result = programmedExerciseController.delete(EXERCISE_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgrammedExercise))
            .verifyComplete()
        verify(programmedExerciseService).deleteProgrammedExercise(EXERCISE_ID_1)
    }

    @Test
    fun `delete should return not found when programmed exercise not found`() {
        whenever(programmedExerciseService.isOwner(NON_EXISTENT_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.deleteProgrammedExercise(NON_EXISTENT_ID))
            .thenReturn(Mono.error(NoResultsFoundException("Not found")))
        val result = programmedExerciseController.delete(NON_EXISTENT_ID)
        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(programmedExerciseService).deleteProgrammedExercise(NON_EXISTENT_ID)
    }

    @Test
    fun `delete should handle database errors`() {
        whenever(programmedExerciseService.isOwner(EXERCISE_ID_1, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedExerciseService.deleteProgrammedExercise(EXERCISE_ID_1))
            .thenReturn(Mono.error(DatabaseQueryException("Database connection failed")))
        val result = programmedExerciseController.delete(EXERCISE_ID_1)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
        verify(programmedExerciseService).deleteProgrammedExercise(EXERCISE_ID_1)
    }
}
