package com.congen.controllers

import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.model.ExerciseRotationHistory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

/**
 * Unit tests for ExerciseRotationHistoryController.
 *
 * These tests verify the REST API endpoints for exercise rotation history operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class ExerciseRotationHistoryControllerTest {
    private lateinit var exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL
    private lateinit var exerciseRotationHistoryController: ExerciseRotationHistoryController

    @BeforeEach
    fun setUp() {
        exerciseRotationHistoryDAL = mock()
        exerciseRotationHistoryController = ExerciseRotationHistoryController(exerciseRotationHistoryDAL)
    }

    @Test
    fun `should get all exercise rotation histories`() {
        val now = LocalDateTime.now()
        val exerciseRotationHistories =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    userId = 1,
                    exerciseName = "Bench Press",
                    isAccessory = false,
                    createdAt = now
                ),
                ExerciseRotationHistory(
                    id = 2L,
                    userId = 1,
                    exerciseName = "Squat",
                    isAccessory = true,
                    createdAt = now
                )
            )

        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(exerciseRotationHistories))

        val result = exerciseRotationHistoryController.getAll()

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistories))
            .verifyComplete()
    }

    @Test
    fun `should get exercise rotation history by id`() {
        val now = LocalDateTime.now()
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = 1L,
                userId = 1,
                exerciseName = "Bench Press",
                isAccessory = false,
                createdAt = now
            )

        whenever(exerciseRotationHistoryDAL.selectById(1L)).thenReturn(Mono.just(exerciseRotationHistory))

        val result = exerciseRotationHistoryController.get(1L)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistory))
            .verifyComplete()
    }

    @Test
    fun `should return empty when exercise rotation history not found`() {
        whenever(exerciseRotationHistoryDAL.selectById(999L)).thenReturn(Mono.empty())

        val result = exerciseRotationHistoryController.get(999L)

        StepVerifier.create(result)
            .expectComplete()
            .verify()
    }

    @Test
    fun `should create exercise rotation history`() {
        val now = LocalDateTime.now()
        val userId = 1
        val exerciseName = "Bench Press"
        val isAccessory = false
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = 1L,
                userId = userId,
                exerciseName = exerciseName,
                isAccessory = isAccessory,
                createdAt = now
            )

        whenever(exerciseRotationHistoryDAL.insert(userId, exerciseName, isAccessory))
            .thenReturn(Mono.just(exerciseRotationHistory))

        val result = exerciseRotationHistoryController.save(userId, exerciseName, isAccessory)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistory))
            .verifyComplete()
    }

    @Test
    fun `should update exercise rotation history`() {
        val now = LocalDateTime.now()
        val id = 1L
        val userId = 1
        val exerciseName = "Barbell Bench Press"
        val isAccessory = true
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = id,
                userId = userId,
                exerciseName = exerciseName,
                isAccessory = isAccessory,
                createdAt = now
            )

        whenever(exerciseRotationHistoryDAL.update(id, userId, exerciseName, isAccessory))
            .thenReturn(Mono.just(exerciseRotationHistory))

        val result = exerciseRotationHistoryController.update(id, userId, exerciseName, isAccessory)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistory))
            .verifyComplete()
    }

    @Test
    fun `should return empty when updating non-existent exercise rotation history`() {
        whenever(exerciseRotationHistoryDAL.update(999L, 1, "Bench Press", false))
            .thenReturn(Mono.empty())

        val result = exerciseRotationHistoryController.update(999L, 1, "Bench Press", false)

        StepVerifier.create(result)
            .expectComplete()
            .verify()
    }

    @Test
    fun `should delete exercise rotation history`() {
        val now = LocalDateTime.now()
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = 1L,
                userId = 1,
                exerciseName = "Bench Press",
                isAccessory = false,
                createdAt = now
            )

        whenever(exerciseRotationHistoryDAL.deleteById(1L)).thenReturn(Mono.just(exerciseRotationHistory))

        val result = exerciseRotationHistoryController.delete(1L)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistory))
            .verifyComplete()
    }

    @Test
    fun `should return empty when deleting non-existent exercise rotation history`() {
        whenever(exerciseRotationHistoryDAL.deleteById(999L)).thenReturn(Mono.empty())

        val result = exerciseRotationHistoryController.delete(999L)

        StepVerifier.create(result)
            .expectComplete()
            .verify()
    }

    @Test
    fun `should get exercise rotation histories by accessory type`() {
        val now = LocalDateTime.now()
        val isAccessory = false
        val exerciseRotationHistories =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    userId = 1,
                    exerciseName = "Bench Press",
                    isAccessory = isAccessory,
                    createdAt = now
                ),
                ExerciseRotationHistory(
                    id = 2L,
                    userId = 1,
                    exerciseName = "Squat",
                    isAccessory = isAccessory,
                    createdAt = now
                )
            )

        whenever(exerciseRotationHistoryDAL.selectByIsAccessory(isAccessory)).thenReturn(Mono.just(exerciseRotationHistories))

        val result = exerciseRotationHistoryController.getByIsAccessory(isAccessory)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistories))
            .verifyComplete()
    }

    @Test
    fun `should return empty list when no exercise rotation histories for accessory type`() {
        whenever(exerciseRotationHistoryDAL.selectByIsAccessory(true)).thenReturn(Mono.just(emptyList()))

        val result = exerciseRotationHistoryController.getByIsAccessory(true)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(emptyList<ExerciseRotationHistory>()))
            .verifyComplete()
    }

    @Test
    fun `should handle DAL error gracefully`() {
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = exerciseRotationHistoryController.getAll()

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }
}
