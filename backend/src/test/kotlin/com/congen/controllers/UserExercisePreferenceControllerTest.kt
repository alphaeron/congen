package com.congen.controllers

import com.congen.dal.UserExercisePreferenceDAL
import com.congen.exceptions.DatabaseQueryException
import com.congen.mockUserExercisePreference
import com.congen.service.GdprComplianceService
import com.congen.util.KeycloakUtil
import com.congen.createGdprComplianceServiceSpy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for UserExercisePreferenceController.
 *
 * These tests verify the REST API endpoints for user exercise preference operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@TestPropertySource(
    properties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"]
)
class UserExercisePreferenceControllerTest {
    private lateinit var userExercisePreferenceDAL: UserExercisePreferenceDAL
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var gdprComplianceService: GdprComplianceService
    private lateinit var userExercisePreferenceController: UserExercisePreferenceController

    companion object {
        private const val USER_ID = "b226d772-c063-4974-ae08-ab64134abbcf"
        private const val EXERCISE_NAME = "Bench Press"
        private const val SHOULD_AVOID = true
        private const val SQUAT = "Squat"
    }

    @BeforeEach
    fun setUp() {
        userExercisePreferenceDAL = mock()
        keycloakUtil = mock()
        gdprComplianceService = createGdprComplianceServiceSpy()
        userExercisePreferenceController = UserExercisePreferenceController(userExercisePreferenceDAL, keycloakUtil, gdprComplianceService)

        // Mock KeycloakUtil methods for all tests
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(USER_ID))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))

        // Mock GDPR compliance service for all tests
        whenever(gdprComplianceService.hasUserConsent(any<String>())).thenReturn(Mono.just(true))
    }

    @Test
    fun `save should return created user exercise preference`() {
        val now = Instant.now()
        val userExercisePreference =
            mockUserExercisePreference(
                userId = USER_ID,
                exerciseName = EXERCISE_NAME,
                shouldAvoid = SHOULD_AVOID,
                createdAt = now
            )
        whenever(userExercisePreferenceDAL.insertUserExercisePreference(USER_ID, EXERCISE_NAME, SHOULD_AVOID))
            .thenReturn(Mono.just(userExercisePreference))
        val result = userExercisePreferenceController.save(USER_ID, EXERCISE_NAME, SHOULD_AVOID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userExercisePreference))
            .verifyComplete()
        verify(userExercisePreferenceDAL).insertUserExercisePreference(USER_ID, EXERCISE_NAME, SHOULD_AVOID)
    }

    @Test
    fun `getByUser should return user exercise preferences when found`() {
        val now = Instant.now()
        val userExercisePreference =
            mockUserExercisePreference(
                userId = USER_ID,
                exerciseName = EXERCISE_NAME,
                shouldAvoid = SHOULD_AVOID,
                createdAt = now
            )
        val userExercisePreferences =
            listOf(
                userExercisePreference,
                mockUserExercisePreference(
                    userId = USER_ID,
                    exerciseName = SQUAT,
                    shouldAvoid = SHOULD_AVOID,
                    createdAt = now
                )
            )
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(USER_ID))
            .thenReturn(Mono.just(userExercisePreferences))
        val result = userExercisePreferenceController.getByUser(USER_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userExercisePreferences))
            .verifyComplete()
        verify(userExercisePreferenceDAL).selectUserExercisePreferencesByUser(USER_ID)
    }

    @Test
    fun `delete should return deleted user exercise preference`() {
        val now = Instant.now()
        val userExercisePreference =
            mockUserExercisePreference(
                userId = USER_ID,
                exerciseName = EXERCISE_NAME,
                shouldAvoid = SHOULD_AVOID,
                createdAt = now
            )
        whenever(userExercisePreferenceDAL.deleteUserExercisePreference(USER_ID, EXERCISE_NAME))
            .thenReturn(Mono.just(userExercisePreference))
        val result = userExercisePreferenceController.delete(USER_ID, EXERCISE_NAME)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userExercisePreference))
            .verifyComplete()
        verify(userExercisePreferenceDAL).deleteUserExercisePreference(USER_ID, EXERCISE_NAME)
    }

    @Test
    fun `should handle DAL error gracefully for save`() {
        whenever(userExercisePreferenceDAL.insertUserExercisePreference(USER_ID, EXERCISE_NAME, SHOULD_AVOID))
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = userExercisePreferenceController.save(USER_ID, EXERCISE_NAME, SHOULD_AVOID)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for getByUser`() {
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(USER_ID))
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = userExercisePreferenceController.getByUser(USER_ID)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for delete`() {
        whenever(userExercisePreferenceDAL.deleteUserExercisePreference(USER_ID, EXERCISE_NAME))
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = userExercisePreferenceController.delete(USER_ID, EXERCISE_NAME)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }
}
