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

/**
 * Unit tests for UserOneRepMaxController.
 *
 * These tests verify the HTTP request handling for UserOneRepMax operations,
 * including CRUD endpoints and error handling.
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
    fun `save should return saved user one rep max`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("100.0"),
            )

        whenever(userOneRepMaxDAL.insertUserOneRepMax(userOneRepMax)).thenReturn(Mono.just(userOneRepMax))

        // When
        val result = userOneRepMaxController.save(userOneRepMax)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserOneRepMax>)
            .expectNext(userOneRepMax)
            .verifyComplete()

        verify(userOneRepMaxDAL).insertUserOneRepMax(userOneRepMax)
    }

    @Test
    fun `getByUserAndExercise should return user one rep max when found`() {
        // Given
        val userId = 1
        val exerciseName = "Bench Press"
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = BigDecimal("100.0"),
            )

        whenever(userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)).thenReturn(Mono.just(userOneRepMax))

        // When
        val result = userOneRepMaxController.getByUserAndExercise(userId, exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userOneRepMax))
            .verifyComplete()

        verify(userOneRepMaxDAL).selectUserOneRepMax(userId, exerciseName)
    }

    @Test
    fun `getByUserAndExercise should return not found when user one rep max not found`() {
        // Given
        val userId = 1
        val exerciseName = "Bench Press"

        whenever(userOneRepMaxDAL.selectUserOneRepMax(userId, exerciseName)).thenReturn(Mono.error(NoResultsFoundException("Not found")))

        // When
        val result = userOneRepMaxController.getByUserAndExercise(userId, exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(userOneRepMaxDAL).selectUserOneRepMax(userId, exerciseName)
    }

    @Test
    fun `getAllByUser should return all user one rep maxes for user`() {
        // Given
        val userId = 1
        val userOneRepMaxes =
            listOf(
                UserOneRepMax(
                    userId = userId,
                    exerciseName = "Bench Press",
                    oneRepMax = BigDecimal("100.0"),
                ),
                UserOneRepMax(
                    userId = userId,
                    exerciseName = "Squat",
                    oneRepMax = BigDecimal("150.0"),
                ),
            )

        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)).thenReturn(Mono.just(userOneRepMaxes))

        // When
        val result = userOneRepMaxController.getAllByUser(userId)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userOneRepMaxes))
            .verifyComplete()

        verify(userOneRepMaxDAL).selectUserOneRepMaxByUser(userId)
    }

    @Test
    fun `update should return updated user one rep max when found`() {
        // Given
        val userOneRepMax =
            UserOneRepMax(
                userId = 1,
                exerciseName = "Bench Press",
                oneRepMax = BigDecimal("110.0"),
            )

        whenever(userOneRepMaxDAL.updateUserOneRepMax(userOneRepMax)).thenReturn(Mono.just(userOneRepMax))

        // When
        val result = userOneRepMaxController.update(userOneRepMax)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserOneRepMax>)
            .expectNext(userOneRepMax)
            .verifyComplete()

        verify(userOneRepMaxDAL).updateUserOneRepMax(userOneRepMax)
    }

    @Test
    fun `delete should return deleted user one rep max when found`() {
        // Given
        val userId = 1
        val exerciseName = "Bench Press"
        val userOneRepMax =
            UserOneRepMax(
                userId = userId,
                exerciseName = exerciseName,
                oneRepMax = BigDecimal("100.0"),
            )

        whenever(userOneRepMaxDAL.deleteUserOneRepMax(userId, exerciseName)).thenReturn(Mono.just(userOneRepMax))

        // When
        val result = userOneRepMaxController.delete(userId, exerciseName)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserOneRepMax>)
            .expectNext(userOneRepMax)
            .verifyComplete()

        verify(userOneRepMaxDAL).deleteUserOneRepMax(userId, exerciseName)
    }
}
