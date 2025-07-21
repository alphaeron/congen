package com.congen.controllers

import com.congen.dal.UserExercisePreferenceDAL
import com.congen.exceptions.DatabaseQueryException
import com.congen.mockUserExercisePreference
import com.congen.model.UserExercisePreference
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
class UserExercisePreferenceControllerTest {
    private lateinit var userExercisePreferenceDAL: UserExercisePreferenceDAL
    private lateinit var userExercisePreferenceController: UserExercisePreferenceController

    companion object {
        private const val USER_ID = 1
        private const val EXERCISE_NAME = "Bench Press"
        private const val SQUAT = "Back Squat"
        private const val SHOULD_AVOID = false
    }

    @BeforeEach
    fun setUp() {
        userExercisePreferenceDAL = mock()
        userExercisePreferenceController = UserExercisePreferenceController(userExercisePreferenceDAL)
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
        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<UserExercisePreference>)
            .expectNext(userExercisePreference)
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
        val result = userExercisePreferenceController.delete(userExercisePreference)
        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<UserExercisePreference>)
            .expectNext(userExercisePreference)
            .verifyComplete()
        verify(userExercisePreferenceDAL).deleteUserExercisePreference(USER_ID, EXERCISE_NAME)
    }

    @Test
    fun `should handle DAL error gracefully for save`() {
        whenever(userExercisePreferenceDAL.insertUserExercisePreference(USER_ID, EXERCISE_NAME, SHOULD_AVOID))
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = userExercisePreferenceController.save(USER_ID, EXERCISE_NAME, SHOULD_AVOID)
        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<UserExercisePreference>)
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
        val now = Instant.now()
        val userExercisePreference =
            mockUserExercisePreference(
                userId = USER_ID,
                exerciseName = EXERCISE_NAME,
                shouldAvoid = SHOULD_AVOID,
                createdAt = now
            )
        whenever(userExercisePreferenceDAL.deleteUserExercisePreference(USER_ID, EXERCISE_NAME))
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = userExercisePreferenceController.delete(userExercisePreference)
        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<UserExercisePreference>)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }
}
