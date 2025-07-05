package com.congen.controllers

import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.model.ExerciseRotationHistory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ExerciseRotationHistoryControllerTest {
    private lateinit var exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL
    private lateinit var exerciseRotationHistoryController: ExerciseRotationHistoryController

    @BeforeEach
    fun setUp() {
        exerciseRotationHistoryDAL = mock()
        exerciseRotationHistoryController = ExerciseRotationHistoryController(exerciseRotationHistoryDAL)
    }

    @Test
    fun `should save exercise rotation history`() {
        val userId = 123L
        val exerciseName = "Bench Press"
        val category = "primary"
        val expectedRecord =
            ExerciseRotationHistory(
                id = 1L,
                userId = userId,
                exerciseName = exerciseName,
                category = category
            )

        whenever(exerciseRotationHistoryDAL.insert(any())).thenReturn(Mono.just(expectedRecord))

        val result = exerciseRotationHistoryController.save(userId, exerciseName, category)

        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK && response.body == expectedRecord
            }
            .verifyComplete()
    }

    @Test
    fun `should get exercise rotation history by id`() {
        val id = 1L
        val expectedRecord =
            ExerciseRotationHistory(
                id = id,
                userId = 123L,
                exerciseName = "Bench Press",
                category = "primary"
            )

        whenever(exerciseRotationHistoryDAL.selectById(id)).thenReturn(Mono.just(expectedRecord))

        val result = exerciseRotationHistoryController.get(id)

        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK && response.body == expectedRecord
            }
            .verifyComplete()
    }

    @Test
    fun `should get exercise rotation history by user id`() {
        val userId = 123L
        val expectedRecords =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    userId = userId,
                    exerciseName = "Bench Press",
                    category = "primary"
                ),
                ExerciseRotationHistory(
                    id = 2L,
                    userId = userId,
                    exerciseName = "Squat",
                    category = "secondary"
                )
            )

        whenever(exerciseRotationHistoryDAL.selectByUserId(userId)).thenReturn(Mono.just(expectedRecords))

        val result = exerciseRotationHistoryController.getByUserId(userId)

        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK && response.body == expectedRecords
            }
            .verifyComplete()
    }

    @Test
    fun `should get exercise rotation history by user id and category`() {
        val userId = 123L
        val category = "primary"
        val expectedRecords =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    userId = userId,
                    exerciseName = "Bench Press",
                    category = category
                )
            )

        whenever(exerciseRotationHistoryDAL.selectByUserIdAndCategory(userId, category)).thenReturn(Mono.just(expectedRecords))

        val result = exerciseRotationHistoryController.getByUserIdAndCategory(userId, category)

        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK && response.body == expectedRecords
            }
            .verifyComplete()
    }

    @Test
    fun `should get all exercise rotation history records`() {
        val expectedRecords =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    userId = 123L,
                    exerciseName = "Bench Press",
                    category = "primary"
                ),
                ExerciseRotationHistory(
                    id = 2L,
                    userId = 456L,
                    exerciseName = "Squat",
                    category = "secondary"
                )
            )

        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(expectedRecords))

        val result = exerciseRotationHistoryController.getAll()

        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK && response.body == expectedRecords
            }
            .verifyComplete()
    }

    @Test
    fun `should update exercise rotation history`() {
        val id = 1L
        val userId = 123L
        val exerciseName = "Bench Press"
        val category = "secondary"
        val expectedRecord =
            ExerciseRotationHistory(
                id = id,
                userId = userId,
                exerciseName = exerciseName,
                category = category
            )

        whenever(exerciseRotationHistoryDAL.update(any())).thenReturn(Mono.just(expectedRecord))

        val result = exerciseRotationHistoryController.update(id, userId, exerciseName, category)

        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK && response.body == expectedRecord
            }
            .verifyComplete()
    }

    @Test
    fun `should delete exercise rotation history by id`() {
        val id = 1L
        val expectedRecord =
            ExerciseRotationHistory(
                id = id,
                userId = 123L,
                exerciseName = "Bench Press",
                category = "primary"
            )

        whenever(exerciseRotationHistoryDAL.deleteById(id)).thenReturn(Mono.just(expectedRecord))

        val result = exerciseRotationHistoryController.delete(id)

        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK && response.body == expectedRecord
            }
            .verifyComplete()
    }

    @Test
    fun `should delete exercise rotation history by user id`() {
        val userId = 123L
        val deletedCount = 2

        whenever(exerciseRotationHistoryDAL.deleteByUserId(userId)).thenReturn(Mono.just(deletedCount))

        val result = exerciseRotationHistoryController.deleteByUserId(userId)

        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK && response.body == deletedCount
            }
            .verifyComplete()
    }
}
