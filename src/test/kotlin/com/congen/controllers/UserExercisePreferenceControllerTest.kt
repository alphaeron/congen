package com.congen.controllers

import com.congen.dal.UserExercisePreferenceDAL
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
import java.time.LocalDateTime

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

    @BeforeEach
    fun setUp() {
        userExercisePreferenceDAL = mock()
        userExercisePreferenceController = UserExercisePreferenceController(userExercisePreferenceDAL)
    }

    @Test
    fun `save should return created user exercise preference`() {
        val userId = 1
        val exerciseName = "Bench Press"
        val shouldAvoid = false
        val now = LocalDateTime.now()
        val userExercisePreference = UserExercisePreference(
            userId = userId,
            exerciseName = exerciseName,
            shouldAvoid = shouldAvoid,
            createdAt = now
        )
        whenever(userExercisePreferenceDAL.insertUserExercisePreference(userId, exerciseName, shouldAvoid)).thenReturn(Mono.just(userExercisePreference))

        val result = userExercisePreferenceController.save(userId, exerciseName, shouldAvoid)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserExercisePreference>)
            .expectNext(userExercisePreference)
            .verifyComplete()

        verify(userExercisePreferenceDAL).insertUserExercisePreference(userId, exerciseName, shouldAvoid)
    }

    @Test
    fun `getByUser should return user exercise preferences when found`() {
        val userId = 1
        val now = LocalDateTime.now()
        val userExercisePreferences = listOf(
            UserExercisePreference(
                userId = userId,
                exerciseName = "Bench Press",
                shouldAvoid = false,
                createdAt = now
            ),
            UserExercisePreference(
                userId = userId,
                exerciseName = "Squat",
                shouldAvoid = false,
                createdAt = now
            )
        )

        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.just(userExercisePreferences))

        val result = userExercisePreferenceController.getByUser(userId)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userExercisePreferences))
            .verifyComplete()

        verify(userExercisePreferenceDAL).selectUserExercisePreferencesByUser(userId)
    }

    @Test
    fun `delete should return deleted user exercise preference`() {
        val userId = 1
        val exerciseName = "Bench Press"
        val shouldAvoid = false
        val now = LocalDateTime.now()
        val userExercisePreference = UserExercisePreference(
            userId = userId,
            exerciseName = exerciseName,
            shouldAvoid = shouldAvoid,
            createdAt = now
        )
        whenever(userExercisePreferenceDAL.deleteUserExercisePreference(userId, exerciseName)).thenReturn(Mono.just(userExercisePreference))

        val result = userExercisePreferenceController.delete(userExercisePreference)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserExercisePreference>)
            .expectNext(userExercisePreference)
            .verifyComplete()

        verify(userExercisePreferenceDAL).deleteUserExercisePreference(userId, exerciseName)
    }

    @Test
    fun `should handle DAL error gracefully for save`() {
        val userId = 1
        val exerciseName = "Bench Press"
        val shouldAvoid = false

        whenever(userExercisePreferenceDAL.insertUserExercisePreference(userId, exerciseName, shouldAvoid)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userExercisePreferenceController.save(userId, exerciseName, shouldAvoid)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserExercisePreference>)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for getByUser`() {
        val userId = 1

        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userExercisePreferenceController.getByUser(userId)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for delete`() {
        val userId = 1
        val exerciseName = "Bench Press"
        val shouldAvoid = false
        val now = LocalDateTime.now()
        val userExercisePreference = UserExercisePreference(
            userId = userId,
            exerciseName = exerciseName,
            shouldAvoid = shouldAvoid,
            createdAt = now
        )

        whenever(userExercisePreferenceDAL.deleteUserExercisePreference(userId, exerciseName)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userExercisePreferenceController.delete(userExercisePreference)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserExercisePreference>)
            .expectError(RuntimeException::class.java)
            .verify()
    }
}
