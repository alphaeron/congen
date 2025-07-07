package com.congen.controllers

import com.congen.dal.UserOneRepMaxDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockUserOneRepMax
import com.congen.model.UserOneRepMax
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

/**
 * Unit tests for UserOneRepMaxController.
 *
 * These tests verify the REST API endpoints for user one rep max operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UserOneRepMaxControllerTest {
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL
    private lateinit var userOneRepMaxController: UserOneRepMaxController

    companion object {
        private const val USER_ID = 1
        private const val EXERCISE_NAME = "Bench Press"
        private const val SQUAT = "Squat"
        private const val MISSING_EXERCISE = "Non-existent Exercise"
        private const val ONE_REP_MAX_225 = "225.5"
        private const val ONE_REP_MAX_250 = "250.0"
        private const val ONE_REP_MAX_315 = "315.0"
    }

    @BeforeEach
    fun setUp() {
        userOneRepMaxDAL = mock()
        userOneRepMaxController = UserOneRepMaxController(userOneRepMaxDAL)
    }

    @Test
    fun `save should return created user one rep max`() {
        val now = Instant.now()
        val oneRepMax = BigDecimal(ONE_REP_MAX_225)
        val userOneRepMax = mockUserOneRepMax(userId = USER_ID, exerciseName = EXERCISE_NAME, oneRepMax = oneRepMax, updatedAt = now)
        whenever(userOneRepMaxDAL.insertUserOneRepMax(USER_ID, EXERCISE_NAME, oneRepMax)).thenReturn(Mono.just(userOneRepMax))
        val result = userOneRepMaxController.save(USER_ID, EXERCISE_NAME, oneRepMax)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<UserOneRepMax>
        StepVerifier.create(body)
            .expectNext(userOneRepMax)
            .verifyComplete()
        verify(userOneRepMaxDAL).insertUserOneRepMax(USER_ID, EXERCISE_NAME, oneRepMax)
    }

    @Test
    fun `get should return user one rep max when found`() {
        val now = Instant.now()
        val oneRepMax = BigDecimal(ONE_REP_MAX_225)
        val userOneRepMax = mockUserOneRepMax(userId = USER_ID, exerciseName = EXERCISE_NAME, oneRepMax = oneRepMax, updatedAt = now)
        whenever(userOneRepMaxDAL.selectUserOneRepMax(USER_ID, EXERCISE_NAME)).thenReturn(Mono.just(userOneRepMax))
        val result = userOneRepMaxController.getByUserAndExercise(USER_ID, EXERCISE_NAME)
        StepVerifier.create(result).expectNext(ResponseEntity.ok(userOneRepMax)).verifyComplete()
        verify(userOneRepMaxDAL).selectUserOneRepMax(USER_ID, EXERCISE_NAME)
    }

    @Test
    fun `getByUserAndExercise should return not found when user one rep max not found`() {
        whenever(
            userOneRepMaxDAL.selectUserOneRepMax(USER_ID, MISSING_EXERCISE)
        ).thenReturn(Mono.error(NoResultsFoundException("Not found")))
        val result = userOneRepMaxController.getByUserAndExercise(USER_ID, MISSING_EXERCISE)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()
        verify(userOneRepMaxDAL).selectUserOneRepMax(USER_ID, MISSING_EXERCISE)
    }

    @Test
    fun `getAllByUser should return all user one rep maxes`() {
        val now = Instant.now()
        val oneRepMax = BigDecimal(ONE_REP_MAX_225)
        val squatOneRepMax = BigDecimal(ONE_REP_MAX_315)
        val userOneRepMaxes =
            listOf(
                mockUserOneRepMax(userId = USER_ID, exerciseName = EXERCISE_NAME, oneRepMax = oneRepMax, updatedAt = now),
                mockUserOneRepMax(userId = USER_ID, exerciseName = SQUAT, oneRepMax = squatOneRepMax, updatedAt = now)
            )
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(userOneRepMaxes))
        val result = userOneRepMaxController.getAllByUser(USER_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userOneRepMaxes))
            .verifyComplete()
        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(USER_ID)
    }

    @Test
    fun `update should return updated user one rep max`() {
        val now = Instant.now()
        val updatedOneRepMax = BigDecimal(ONE_REP_MAX_250)
        val userOneRepMax = mockUserOneRepMax(userId = USER_ID, exerciseName = EXERCISE_NAME, oneRepMax = updatedOneRepMax, updatedAt = now)
        whenever(userOneRepMaxDAL.updateUserOneRepMax(USER_ID, EXERCISE_NAME, updatedOneRepMax)).thenReturn(Mono.just(userOneRepMax))
        val result = userOneRepMaxController.update(USER_ID, EXERCISE_NAME, updatedOneRepMax)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<UserOneRepMax>
        StepVerifier.create(body)
            .expectNext(userOneRepMax)
            .verifyComplete()
        verify(userOneRepMaxDAL).updateUserOneRepMax(USER_ID, EXERCISE_NAME, updatedOneRepMax)
    }

    @Test
    fun `delete should return deleted user one rep max`() {
        val now = Instant.now()
        val oneRepMax = BigDecimal(ONE_REP_MAX_225)
        val userOneRepMax = mockUserOneRepMax(userId = USER_ID, exerciseName = EXERCISE_NAME, oneRepMax = oneRepMax, updatedAt = now)
        whenever(userOneRepMaxDAL.deleteUserOneRepMax(USER_ID, EXERCISE_NAME)).thenReturn(Mono.just(userOneRepMax))
        val result = userOneRepMaxController.delete(USER_ID, EXERCISE_NAME)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<UserOneRepMax>
        StepVerifier.create(body)
            .expectNext(userOneRepMax)
            .verifyComplete()
        verify(userOneRepMaxDAL).deleteUserOneRepMax(USER_ID, EXERCISE_NAME)
    }

    @Test
    fun `should handle DAL error gracefully for getByUserAndExercise`() {
        whenever(userOneRepMaxDAL.selectUserOneRepMax(USER_ID, EXERCISE_NAME)).thenReturn(Mono.error(RuntimeException("Database error")))
        val result = userOneRepMaxController.getByUserAndExercise(USER_ID, EXERCISE_NAME)
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for getAllByUser`() {
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.error(RuntimeException("Database error")))
        val result = userOneRepMaxController.getAllByUser(USER_ID)
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for save`() {
        val oneRepMax = BigDecimal(ONE_REP_MAX_225)
        whenever(
            userOneRepMaxDAL.insertUserOneRepMax(USER_ID, EXERCISE_NAME, oneRepMax)
        ).thenReturn(Mono.error(RuntimeException("Database error")))
        val result = userOneRepMaxController.save(USER_ID, EXERCISE_NAME, oneRepMax)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<UserOneRepMax>
        StepVerifier.create(body)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for update`() {
        val updatedOneRepMax = BigDecimal(ONE_REP_MAX_250)
        whenever(
            userOneRepMaxDAL.updateUserOneRepMax(USER_ID, EXERCISE_NAME, updatedOneRepMax)
        ).thenReturn(Mono.error(RuntimeException("Database error")))
        val result = userOneRepMaxController.update(USER_ID, EXERCISE_NAME, updatedOneRepMax)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<UserOneRepMax>
        StepVerifier.create(body)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for delete`() {
        whenever(userOneRepMaxDAL.deleteUserOneRepMax(USER_ID, EXERCISE_NAME)).thenReturn(Mono.error(RuntimeException("Database error")))
        val result = userOneRepMaxController.delete(USER_ID, EXERCISE_NAME)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<UserOneRepMax>
        StepVerifier.create(body)
            .expectError(RuntimeException::class.java)
            .verify()
    }
}
