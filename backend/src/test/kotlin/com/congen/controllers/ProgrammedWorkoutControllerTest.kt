package com.congen.controllers

import com.congen.createGdprComplianceServiceSpy
import com.congen.model.ProgrammedWorkout
import com.congen.service.GdprComplianceService
import com.congen.service.ProgramService
import com.congen.service.ProgrammedWorkoutService
import com.congen.util.KeycloakUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
 * Unit tests for ProgrammedWorkoutController.
 *
 * These tests verify the REST API endpoints for programmed workout operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class ProgrammedWorkoutControllerTest {
    private lateinit var programmedWorkoutService: ProgrammedWorkoutService
    private lateinit var programService: ProgramService
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var gdprComplianceService: GdprComplianceService
    private lateinit var programmedWorkoutController: ProgrammedWorkoutController

    companion object {
        private const val WORKOUT_ID_1 = 1L
        private const val WORKOUT_ID_2 = 2L
        private const val PROGRAM_ID = 5L
        private const val DAY_NUMBER_1 = 1
        private const val DAY_NUMBER_2 = 2
        private const val WORKOUT_NAME_1 = "Upper Body"
        private const val WORKOUT_NAME_2 = "Lower Body"
    }

    @BeforeEach
    fun setUp() {
        programmedWorkoutService = mock()
        programService = mock()
        keycloakUtil = mock()
        gdprComplianceService = createGdprComplianceServiceSpy()
        programmedWorkoutController = ProgrammedWorkoutController(programmedWorkoutService, programService, keycloakUtil, gdprComplianceService)

        // Mock KeycloakUtil methods for all tests
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just("test-keycloak-user-id"))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))

        // Mock GDPR compliance service for all tests
        doReturn(Mono.just(true)).whenever(gdprComplianceService).hasUserConsent(any<String>())
    }

    @Test
    fun `should get all programmed workouts for admin user`() {
        val now = Instant.now()
        val programmedWorkouts =
            listOf(
                ProgrammedWorkout(
                    id = WORKOUT_ID_1,
                    programId = PROGRAM_ID,
                    dayNumber = DAY_NUMBER_1,
                    name = WORKOUT_NAME_1,
                    createdAt = now,
                    updatedAt = now
                ),
                ProgrammedWorkout(
                    id = WORKOUT_ID_2,
                    programId = PROGRAM_ID,
                    dayNumber = DAY_NUMBER_2,
                    name = WORKOUT_NAME_2,
                    createdAt = now,
                    updatedAt = now
                )
            )
        val userId = "123"
        val roles = setOf("admin")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programmedWorkoutService.selectProgrammedWorkouts()).thenReturn(Mono.just(programmedWorkouts))

        val result = programmedWorkoutController.getAll()
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programmedWorkouts))
            .verifyComplete()
        verify(programmedWorkoutService).selectProgrammedWorkouts()
    }

    @Test
    fun `should get user owned programmed workouts for regular user`() {
        val now = Instant.now()
        val userWorkouts =
            listOf(
                ProgrammedWorkout(
                    id = WORKOUT_ID_1,
                    programId = PROGRAM_ID,
                    dayNumber = DAY_NUMBER_1,
                    name = WORKOUT_NAME_1,
                    createdAt = now,
                    updatedAt = now
                )
            )
        val userId = "123"
        val roles = setOf("user")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programmedWorkoutService.selectProgrammedWorkoutsByUserId(userId)).thenReturn(Mono.just(userWorkouts))

        val result = programmedWorkoutController.getAll()
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userWorkouts))
            .verifyComplete()
        verify(programmedWorkoutService).selectProgrammedWorkoutsByUserId(userId)
    }

    @Test
    fun `should get user owned programmed workouts for service user`() {
        val now = Instant.now()
        val allWorkouts =
            listOf(
                ProgrammedWorkout(
                    id = WORKOUT_ID_1,
                    programId = PROGRAM_ID,
                    dayNumber = DAY_NUMBER_1,
                    name = WORKOUT_NAME_1,
                    createdAt = now,
                    updatedAt = now
                ),
                ProgrammedWorkout(
                    id = WORKOUT_ID_2,
                    programId = PROGRAM_ID,
                    dayNumber = DAY_NUMBER_2,
                    name = WORKOUT_NAME_2,
                    createdAt = now,
                    updatedAt = now
                )
            )
        val userId = "123"
        val roles = setOf("service")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programmedWorkoutService.selectProgrammedWorkouts()).thenReturn(Mono.just(allWorkouts))

        val result = programmedWorkoutController.getAll()
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(allWorkouts))
            .verifyComplete()
        verify(programmedWorkoutService).selectProgrammedWorkouts()
    }

    @Test
    fun `should return empty list when regular user has no owned workouts`() {
        val emptyList = emptyList<ProgrammedWorkout>()
        val userId = "123"
        val roles = setOf("user")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programmedWorkoutService.selectProgrammedWorkoutsByUserId(userId)).thenReturn(Mono.just(emptyList))

        val result = programmedWorkoutController.getAll()
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(emptyList))
            .verifyComplete()
        verify(programmedWorkoutService).selectProgrammedWorkoutsByUserId(userId)
    }

    @Test
    fun `should propagate errors from getAll`() {
        val userId = "123"
        val roles = setOf("user")
        val databaseError = RuntimeException("Database error")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programmedWorkoutService.selectProgrammedWorkoutsByUserId(userId)).thenReturn(Mono.error(databaseError))

        val result = programmedWorkoutController.getAll()
        StepVerifier.create(result)
            .expectError(databaseError::class.java)
            .verify()
        verify(programmedWorkoutService).selectProgrammedWorkoutsByUserId(userId)
    }

    @Test
    fun `should get programmed workout by id`() {
        val now = Instant.now()
        val programmedWorkout =
            ProgrammedWorkout(
                id = WORKOUT_ID_1,
                programId = PROGRAM_ID,
                dayNumber = DAY_NUMBER_1,
                name = WORKOUT_NAME_1,
                createdAt = now,
                updatedAt = now
            )
        whenever(programmedWorkoutService.isOwner(WORKOUT_ID_1, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedWorkoutService.selectProgrammedWorkoutById(WORKOUT_ID_1)).thenReturn(Mono.just(programmedWorkout))
        val result = programmedWorkoutController.get(WORKOUT_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programmedWorkout))
            .verifyComplete()
        verify(programmedWorkoutService).selectProgrammedWorkoutById(WORKOUT_ID_1)
    }

    @Test
    fun `should return not found when programmed workout not found`() {
        whenever(programmedWorkoutService.isOwner(WORKOUT_ID_2, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(
            programmedWorkoutService.selectProgrammedWorkoutById(WORKOUT_ID_2)
        ).thenReturn(Mono.error(RuntimeException("Not found")))
        val result = programmedWorkoutController.get(WORKOUT_ID_2)
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
        verify(programmedWorkoutService).selectProgrammedWorkoutById(WORKOUT_ID_2)
    }

    @Test
    fun `should create programmed workout`() {
        val now = Instant.now()
        val programmedWorkout =
            ProgrammedWorkout(
                id = 0L,
                programId = PROGRAM_ID,
                dayNumber = DAY_NUMBER_1,
                name = WORKOUT_NAME_1,
                createdAt = now,
                updatedAt = now
            )
        val savedProgrammedWorkout = programmedWorkout.copy(id = WORKOUT_ID_1)
        whenever(programService.isOwner(PROGRAM_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(
            programmedWorkoutService.insertProgrammedWorkout(PROGRAM_ID, DAY_NUMBER_1, WORKOUT_NAME_1)
        ).thenReturn(Mono.just(savedProgrammedWorkout))
        val result = programmedWorkoutController.save(PROGRAM_ID, DAY_NUMBER_1, WORKOUT_NAME_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(savedProgrammedWorkout))
            .verifyComplete()
        verify(programmedWorkoutService).insertProgrammedWorkout(PROGRAM_ID, DAY_NUMBER_1, WORKOUT_NAME_1)
    }

    @Test
    fun `should update programmed workout`() {
        val now = Instant.now()
        val programmedWorkout =
            ProgrammedWorkout(
                id = WORKOUT_ID_1,
                programId = PROGRAM_ID,
                dayNumber = DAY_NUMBER_2,
                name = WORKOUT_NAME_2,
                createdAt = now,
                updatedAt = now
            )
        whenever(programService.isOwner(PROGRAM_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedWorkoutService.isOwner(WORKOUT_ID_1, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(
            programmedWorkoutService.updateProgrammedWorkout(WORKOUT_ID_1, PROGRAM_ID, DAY_NUMBER_2, WORKOUT_NAME_2)
        ).thenReturn(Mono.just(programmedWorkout))
        val result = programmedWorkoutController.update(WORKOUT_ID_1, PROGRAM_ID, DAY_NUMBER_2, WORKOUT_NAME_2)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programmedWorkout))
            .verifyComplete()
        verify(programmedWorkoutService).updateProgrammedWorkout(WORKOUT_ID_1, PROGRAM_ID, DAY_NUMBER_2, WORKOUT_NAME_2)
    }

    @Test
    fun `should return not found when updating non-existent programmed workout`() {
        whenever(programService.isOwner(PROGRAM_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedWorkoutService.isOwner(WORKOUT_ID_2, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(
            programmedWorkoutService.updateProgrammedWorkout(WORKOUT_ID_2, PROGRAM_ID, DAY_NUMBER_1, WORKOUT_NAME_1)
        ).thenReturn(Mono.error(RuntimeException("Not found")))
        val result = programmedWorkoutController.update(WORKOUT_ID_2, PROGRAM_ID, DAY_NUMBER_1, WORKOUT_NAME_1)
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
        verify(programmedWorkoutService).updateProgrammedWorkout(WORKOUT_ID_2, PROGRAM_ID, DAY_NUMBER_1, WORKOUT_NAME_1)
    }

    @Test
    fun `should delete programmed workout`() {
        val now = Instant.now()
        val programmedWorkout =
            ProgrammedWorkout(
                id = WORKOUT_ID_1,
                programId = PROGRAM_ID,
                dayNumber = DAY_NUMBER_1,
                name = WORKOUT_NAME_1,
                createdAt = now,
                updatedAt = now
            )
        whenever(programmedWorkoutService.isOwner(WORKOUT_ID_1, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedWorkoutService.deleteProgrammedWorkout(WORKOUT_ID_1)).thenReturn(Mono.just(programmedWorkout))
        val result = programmedWorkoutController.delete(WORKOUT_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programmedWorkout))
            .verifyComplete()
        verify(programmedWorkoutService).deleteProgrammedWorkout(WORKOUT_ID_1)
    }

    @Test
    fun `should return not found when deleting non-existent programmed workout`() {
        whenever(programmedWorkoutService.isOwner(WORKOUT_ID_2, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedWorkoutService.deleteProgrammedWorkout(WORKOUT_ID_2)).thenReturn(Mono.error(RuntimeException("Not found")))
        val result = programmedWorkoutController.delete(WORKOUT_ID_2)
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
        verify(programmedWorkoutService).deleteProgrammedWorkout(WORKOUT_ID_2)
    }

    @Test
    fun `should get programmed workouts by program`() {
        val now = Instant.now()
        val programmedWorkouts =
            listOf(
                ProgrammedWorkout(
                    id = WORKOUT_ID_1,
                    programId = PROGRAM_ID,
                    dayNumber = DAY_NUMBER_1,
                    name = WORKOUT_NAME_1,
                    createdAt = now,
                    updatedAt = now
                ),
                ProgrammedWorkout(
                    id = WORKOUT_ID_2,
                    programId = PROGRAM_ID,
                    dayNumber = DAY_NUMBER_2,
                    name = WORKOUT_NAME_2,
                    createdAt = now,
                    updatedAt = now
                )
            )
        whenever(programService.isOwner(PROGRAM_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedWorkoutService.selectProgrammedWorkoutsByProgramId(PROGRAM_ID)).thenReturn(Mono.just(programmedWorkouts))
        val result = programmedWorkoutController.getByProgramId(PROGRAM_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programmedWorkouts))
            .verifyComplete()
        verify(programmedWorkoutService).selectProgrammedWorkoutsByProgramId(PROGRAM_ID)
    }

    @Test
    fun `should return empty list when no programmed workouts for program`() {
        whenever(programService.isOwner(PROGRAM_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(programmedWorkoutService.selectProgrammedWorkoutsByProgramId(PROGRAM_ID)).thenReturn(Mono.just(emptyList()))
        val result = programmedWorkoutController.getByProgramId(PROGRAM_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(emptyList<ProgrammedWorkout>()))
            .verifyComplete()
        verify(programmedWorkoutService).selectProgrammedWorkoutsByProgramId(PROGRAM_ID)
    }

    @Test
    fun `should handle service error gracefully`() {
        val userId = "123"
        val roles = setOf("admin")

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programmedWorkoutService.selectProgrammedWorkouts()).thenReturn(Mono.error(RuntimeException("Database error")))
        val result = programmedWorkoutController.getAll()
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
        verify(programmedWorkoutService).selectProgrammedWorkouts()
    }
}
