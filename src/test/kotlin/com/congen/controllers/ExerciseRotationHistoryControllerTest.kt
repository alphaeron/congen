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
import java.time.Instant

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

    companion object {
        private const val ID_1 = 1L
        private const val ID_2 = 2L
        private const val USER_ID = 1
        private const val BENCH_PRESS = "Bench Press"
        private const val SQUAT = "Squat"
        private const val BARBELL_BENCH_PRESS = "Barbell Bench Press"
        private const val NON_EXISTENT_ID = 999L
    }

    @BeforeEach
    fun setUp() {
        exerciseRotationHistoryDAL = mock()
        exerciseRotationHistoryController = ExerciseRotationHistoryController(exerciseRotationHistoryDAL)
    }

    @Test
    fun `should get all exercise rotation histories`() {
        val now = Instant.now()
        val exerciseRotationHistories =
            listOf(
                ExerciseRotationHistory(
                    id = ID_1,
                    userId = USER_ID,
                    exerciseName = BENCH_PRESS,
                    isAccessory = false,
                    createdAt = now
                ),
                ExerciseRotationHistory(
                    id = ID_2,
                    userId = USER_ID,
                    exerciseName = SQUAT,
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
        val now = Instant.now()
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = ID_1,
                userId = USER_ID,
                exerciseName = BENCH_PRESS,
                isAccessory = false,
                createdAt = now
            )
        whenever(exerciseRotationHistoryDAL.selectById(ID_1)).thenReturn(Mono.just(exerciseRotationHistory))
        val result = exerciseRotationHistoryController.get(ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistory))
            .verifyComplete()
    }

    @Test
    fun `should return empty when exercise rotation history not found`() {
        whenever(exerciseRotationHistoryDAL.selectById(NON_EXISTENT_ID)).thenReturn(Mono.empty())
        val result = exerciseRotationHistoryController.get(NON_EXISTENT_ID)
        StepVerifier.create(result)
            .expectComplete()
            .verify()
    }

    @Test
    fun `should create exercise rotation history`() {
        val now = Instant.now()
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = ID_1,
                userId = USER_ID,
                exerciseName = BENCH_PRESS,
                isAccessory = false,
                createdAt = now
            )
        whenever(exerciseRotationHistoryDAL.insert(USER_ID, BENCH_PRESS, false)).thenReturn(Mono.just(exerciseRotationHistory))
        val result = exerciseRotationHistoryController.save(USER_ID, BENCH_PRESS, false)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistory))
            .verifyComplete()
    }

    @Test
    fun `should update exercise rotation history`() {
        val now = Instant.now()
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = ID_1,
                userId = USER_ID,
                exerciseName = BARBELL_BENCH_PRESS,
                isAccessory = true,
                createdAt = now
            )
        whenever(exerciseRotationHistoryDAL.update(ID_1, USER_ID, BARBELL_BENCH_PRESS, true)).thenReturn(Mono.just(exerciseRotationHistory))
        val result = exerciseRotationHistoryController.update(ID_1, USER_ID, BARBELL_BENCH_PRESS, true)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistory))
            .verifyComplete()
    }

    @Test
    fun `should return empty when updating non-existent exercise rotation history`() {
        whenever(exerciseRotationHistoryDAL.update(NON_EXISTENT_ID, USER_ID, BENCH_PRESS, false)).thenReturn(Mono.empty())
        val result = exerciseRotationHistoryController.update(NON_EXISTENT_ID, USER_ID, BENCH_PRESS, false)
        StepVerifier.create(result)
            .expectComplete()
            .verify()
    }

    @Test
    fun `should delete exercise rotation history`() {
        val now = Instant.now()
        val exerciseRotationHistory =
            ExerciseRotationHistory(
                id = ID_1,
                userId = USER_ID,
                exerciseName = BENCH_PRESS,
                isAccessory = false,
                createdAt = now
            )
        whenever(exerciseRotationHistoryDAL.deleteById(ID_1)).thenReturn(Mono.just(exerciseRotationHistory))
        val result = exerciseRotationHistoryController.delete(ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(exerciseRotationHistory))
            .verifyComplete()
    }

    @Test
    fun `should return empty when deleting non-existent exercise rotation history`() {
        whenever(exerciseRotationHistoryDAL.deleteById(NON_EXISTENT_ID)).thenReturn(Mono.empty())
        val result = exerciseRotationHistoryController.delete(NON_EXISTENT_ID)
        StepVerifier.create(result)
            .expectComplete()
            .verify()
    }

    @Test
    fun `should get exercise rotation histories by accessory type`() {
        val now = Instant.now()
        val isAccessory = false
        val exerciseRotationHistories =
            listOf(
                ExerciseRotationHistory(
                    id = ID_1,
                    userId = USER_ID,
                    exerciseName = BENCH_PRESS,
                    isAccessory = isAccessory,
                    createdAt = now
                ),
                ExerciseRotationHistory(
                    id = ID_2,
                    userId = USER_ID,
                    exerciseName = SQUAT,
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
