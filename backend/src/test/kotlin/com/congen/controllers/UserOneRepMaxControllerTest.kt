package com.congen.controllers

import com.congen.dal.UserOneRepMaxDAL
import com.congen.exceptions.DatabaseException
import com.congen.model.UserOneRepMax
import com.congen.service.UserOneRepMaxService
import com.congen.util.ValidationUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

@ExtendWith(MockitoExtension::class)
@TestPropertySource(
    properties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"]
)
class UserOneRepMaxControllerTest {
    @Mock
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL

    @Mock
    private lateinit var userOneRepMaxService: UserOneRepMaxService

    @Mock
    private lateinit var validationUtil: ValidationUtil

    private lateinit var controller: UserOneRepMaxController

    private val testUserId = 1
    private val testExerciseName = "Bench Press"
    private val testOneRepMax =
        UserOneRepMax(
            userId = testUserId,
            exerciseName = testExerciseName,
            oneRepMax = BigDecimal("100.0"),
            updatedAt = Instant.now()
        )

    @BeforeEach
    fun setUp() {
        controller = UserOneRepMaxController(userOneRepMaxDAL, userOneRepMaxService, validationUtil)
    }

    @Test
    fun `getOneRepMaxesByUserId should return list of one rep maxes`() {
        // Given
        val oneRepMaxes = listOf(testOneRepMax)
        whenever(userOneRepMaxService.getAllByUser(testUserId, null))
            .thenReturn(Mono.just(oneRepMaxes))

        // When
        val result = controller.getOneRepMaxesByUserId(testUserId, null)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(oneRepMaxes))
            .verifyComplete()

        verify(userOneRepMaxService).getAllByUser(testUserId, null)
    }

    @Test
    fun `getOneRepMaxesByUserId should pass unit parameter to service`() {
        // Given
        val oneRepMaxes = listOf(testOneRepMax)
        whenever(userOneRepMaxService.getAllByUser(testUserId, "lbs"))
            .thenReturn(Mono.just(oneRepMaxes))

        // When
        val result = controller.getOneRepMaxesByUserId(testUserId, "lbs")

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(oneRepMaxes))
            .verifyComplete()

        verify(userOneRepMaxService).getAllByUser(testUserId, "lbs")
    }

    @Test
    fun `getOneRepMaxByUserAndExercise should return one rep max when found`() {
        // Given
        whenever(userOneRepMaxService.getByUserAndExercise(testUserId, testExerciseName, null))
            .thenReturn(Mono.just(testOneRepMax))

        // When
        val result = controller.getOneRepMaxByUserAndExercise(testUserId, testExerciseName, null)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testOneRepMax))
            .verifyComplete()

        verify(userOneRepMaxService).getByUserAndExercise(testUserId, testExerciseName, null)
    }

    @Test
    fun `getOneRepMaxByUserAndExercise should return not found when not found`() {
        // Given
        whenever(userOneRepMaxService.getByUserAndExercise(testUserId, testExerciseName, null))
            .thenReturn(Mono.error(DatabaseException("Not found")))

        // When
        val result = controller.getOneRepMaxByUserAndExercise(testUserId, testExerciseName, null)

        // Then
        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()

        verify(userOneRepMaxService).getByUserAndExercise(testUserId, testExerciseName, null)
    }

    @Test
    fun `getOneRepMaxByUserAndExercise should pass unit parameter to service`() {
        // Given
        whenever(userOneRepMaxService.getByUserAndExercise(testUserId, testExerciseName, "kg"))
            .thenReturn(Mono.just(testOneRepMax))

        // When
        val result = controller.getOneRepMaxByUserAndExercise(testUserId, testExerciseName, "kg")

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testOneRepMax))
            .verifyComplete()

        verify(userOneRepMaxService).getByUserAndExercise(testUserId, testExerciseName, "kg")
    }

    @Test
    fun `upsertOneRepMax should return created one rep max`() {
        // Given
        val oneRepMax = BigDecimal("100.0")
        whenever(userOneRepMaxService.upsertOneRepMax(testUserId, testExerciseName, oneRepMax, null))
            .thenReturn(Mono.just(testOneRepMax))

        // When
        val result = controller.upsertOneRepMax(testUserId, testExerciseName, oneRepMax, null)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testOneRepMax))
            .verifyComplete()

        verify(userOneRepMaxService).upsertOneRepMax(testUserId, testExerciseName, oneRepMax, null)
    }

    @Test
    fun `upsertOneRepMax should pass unit parameter to service`() {
        // Given
        val oneRepMax = BigDecimal("100.0")
        whenever(userOneRepMaxService.upsertOneRepMax(testUserId, testExerciseName, oneRepMax, "lbs"))
            .thenReturn(Mono.just(testOneRepMax))

        // When
        val result = controller.upsertOneRepMax(testUserId, testExerciseName, oneRepMax, "lbs")

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testOneRepMax))
            .verifyComplete()

        verify(userOneRepMaxService).upsertOneRepMax(testUserId, testExerciseName, oneRepMax, "lbs")
    }

    @Test
    fun `deleteOneRepMax should return deleted one rep max when deleted`() {
        // Given
        whenever(userOneRepMaxService.deleteOneRepMax(testUserId, testExerciseName))
            .thenReturn(Mono.just(testOneRepMax))

        // When
        val result = controller.deleteOneRepMax(testUserId, testExerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testOneRepMax))
            .verifyComplete()

        verify(userOneRepMaxService).deleteOneRepMax(testUserId, testExerciseName)
    }

    @Test
    fun `deleteOneRepMax should return not found when not found`() {
        // Given
        whenever(userOneRepMaxService.deleteOneRepMax(testUserId, testExerciseName))
            .thenReturn(Mono.error(DatabaseException("Not found")))

        // When
        val result = controller.deleteOneRepMax(testUserId, testExerciseName)

        // Then
        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()

        verify(userOneRepMaxService).deleteOneRepMax(testUserId, testExerciseName)
    }
}
