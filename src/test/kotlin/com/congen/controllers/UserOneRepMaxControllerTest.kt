package com.congen.controllers

import com.congen.dal.UserOneRepMaxDAL
import com.congen.exceptions.NoResultsFoundException
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
import java.time.LocalDateTime

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

    @BeforeEach
    fun setUp() {
        userOneRepMaxDAL = mock()
        userOneRepMaxController = UserOneRepMaxController(userOneRepMaxDAL)
    }

    @Test
    fun `save should return created user one rep max`() {
        val userId = 1
        val exerciseName = "Bench Press"
        val oneRepMax = BigDecimal("225.5")
        val now = LocalDateTime.now()
        val userOneRepMax = UserOneRepMax(
            userId = userId,
            exerciseName = exerciseName,
            oneRepMax = oneRepMax,
            updatedAt = now
        )
        val savedUserOneRepMax = userOneRepMax.copy(userId = userId)
        whenever(userOneRepMaxDAL.insertUserOneRepMax(userId, exerciseName, oneRepMax)).thenReturn(Mono.just(savedUserOneRepMax))

        val result = userOneRepMaxController.save(userId, exerciseName, oneRepMax)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserOneRepMax>)
            .expectNext(savedUserOneRepMax)
            .verifyComplete()

        verify(userOneRepMaxDAL).insertUserOneRepMax(userId, exerciseName, oneRepMax)
    }

    @Test
    fun `getByUserAndExercise should return user one rep max when found`() {
        val userId = 1
        val exerciseName = "Bench Press"
        val userOneRepMax = UserOneRepMax(
            userId = userId,
            exerciseName = exerciseName,
            oneRepMax = BigDecimal("225.5"),
            updatedAt = LocalDateTime.now()
        )

        whenever(userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)).thenReturn(Mono.just(userOneRepMax))

        val result = userOneRepMaxController.getByUserAndExercise(userId, exerciseName)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userOneRepMax))
            .verifyComplete()

        verify(userOneRepMaxDAL).selectUserOneRepMax(userId, exerciseName)
    }

    @Test
    fun `getByUserAndExercise should return not found when user one rep max not found`() {
        val userId = 1
        val exerciseName = "Non-existent Exercise"

        whenever(userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)).thenReturn(Mono.error(NoResultsFoundException("Not found")))

        val result = userOneRepMaxController.getByUserAndExercise(userId, exerciseName)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(userOneRepMaxDAL).selectUserOneRepMax(userId, exerciseName)
    }

    @Test
    fun `getAllByUser should return all user one rep maxes`() {
        val userId = 1
        val userOneRepMaxes = listOf(
            UserOneRepMax(
                userId = userId,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("225.5"),
                updatedAt = LocalDateTime.now()
            ),
            UserOneRepMax(
                userId = userId,
                exerciseName = "Squat",
                oneRepMax = BigDecimal("315.0"),
                updatedAt = LocalDateTime.now()
            )
        )

        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(userOneRepMaxes))

        val result = userOneRepMaxController.getAllByUser(userId)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userOneRepMaxes))
            .verifyComplete()

        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(userId)
    }

    @Test
    fun `update should return updated user one rep max`() {
        val userId = 1
        val exerciseName = "Bench Press"
        val oneRepMax = BigDecimal("250.0")
        val userOneRepMax = UserOneRepMax(
            userId = userId,
            exerciseName = exerciseName,
            oneRepMax = oneRepMax,
            updatedAt = LocalDateTime.now()
        )

        whenever(userOneRepMaxDAL.updateUserOneRepMax(userId, exerciseName, oneRepMax)).thenReturn(Mono.just(userOneRepMax))

        val result = userOneRepMaxController.update(userId, exerciseName, oneRepMax)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserOneRepMax>)
            .expectNext(userOneRepMax)
            .verifyComplete()

        verify(userOneRepMaxDAL).updateUserOneRepMax(userId, exerciseName, oneRepMax)
    }

    @Test
    fun `delete should return deleted user one rep max`() {
        val userId = 1
        val exerciseName = "Bench Press"
        val userOneRepMax = UserOneRepMax(
            userId = userId,
            exerciseName = exerciseName,
            oneRepMax = BigDecimal("225.5"),
            updatedAt = LocalDateTime.now()
        )

        whenever(userOneRepMaxDAL.deleteUserOneRepMax(userId, exerciseName)).thenReturn(Mono.just(userOneRepMax))

        val result = userOneRepMaxController.delete(userId, exerciseName)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserOneRepMax>)
            .expectNext(userOneRepMax)
            .verifyComplete()

        verify(userOneRepMaxDAL).deleteUserOneRepMax(userId, exerciseName)
    }

    @Test
    fun `should handle DAL error gracefully`() {
        val userId = 1
        val exerciseName = "Bench Press"

        whenever(userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userOneRepMaxController.getByUserAndExercise(userId, exerciseName)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for getAllByUser`() {
        val userId = 1

        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userOneRepMaxController.getAllByUser(userId)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for save`() {
        val userId = 1
        val exerciseName = "Bench Press"
        val oneRepMax = BigDecimal("225.5")

        whenever(userOneRepMaxDAL.insertUserOneRepMax(userId, exerciseName, oneRepMax)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userOneRepMaxController.save(userId, exerciseName, oneRepMax)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserOneRepMax>)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for update`() {
        val userId = 1
        val exerciseName = "Bench Press"
        val oneRepMax = BigDecimal("225.5")

        whenever(userOneRepMaxDAL.updateUserOneRepMax(userId, exerciseName, oneRepMax)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userOneRepMaxController.update(userId, exerciseName, oneRepMax)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserOneRepMax>)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for delete`() {
        val userId = 1
        val exerciseName = "Bench Press"

        whenever(userOneRepMaxDAL.deleteUserOneRepMax(userId, exerciseName)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userOneRepMaxController.delete(userId, exerciseName)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserOneRepMax>)
            .expectError(RuntimeException::class.java)
            .verify()
    }
}
