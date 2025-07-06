package com.congen.controllers

import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.model.ExerciseRotationHistory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
        val userId = 1
        val exerciseName = "Bench Press"
        val isAccessory = false
        val expectedRecord =
            ExerciseRotationHistory(
                id = 1L,
                userId = userId,
                exerciseName = exerciseName,
                isAccessory = isAccessory
            )

        whenever(exerciseRotationHistoryDAL.insert(userId, exerciseName, isAccessory)).thenReturn(Mono.just(expectedRecord))

        val result = exerciseRotationHistoryController.save(userId, exerciseName, isAccessory)

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
                userId = 1,
                exerciseName = "Bench Press",
                isAccessory = false
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
    fun `should get exercise rotation history by isAccessory`() {
        val isAccessory = false
        val expectedRecords =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    userId = 1,
                    exerciseName = "Bench Press",
                    isAccessory = isAccessory
                ),
                ExerciseRotationHistory(
                    id = 2L,
                    userId = 2,
                    exerciseName = "Squat",
                    isAccessory = isAccessory
                )
            )

        whenever(exerciseRotationHistoryDAL.selectByIsAccessory(isAccessory)).thenReturn(Mono.just(expectedRecords))

        val result = exerciseRotationHistoryController.getByIsAccessory(isAccessory)

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
                    userId = 1,
                    exerciseName = "Bench Press",
                    isAccessory = false
                ),
                ExerciseRotationHistory(
                    id = 2L,
                    userId = 2,
                    exerciseName = "Squat",
                    isAccessory = true
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
        val userId = 1
        val exerciseName = "Bench Press"
        val isAccessory = true
        val expectedRecord =
            ExerciseRotationHistory(
                id = id,
                userId = userId,
                exerciseName = exerciseName,
                isAccessory = isAccessory
            )

        whenever(exerciseRotationHistoryDAL.update(id, userId, exerciseName, isAccessory)).thenReturn(Mono.just(expectedRecord))

        val result = exerciseRotationHistoryController.update(id, userId, exerciseName, isAccessory)

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
                userId = 1,
                exerciseName = "Bench Press",
                isAccessory = false
            )

        whenever(exerciseRotationHistoryDAL.deleteById(id)).thenReturn(Mono.just(expectedRecord))

        val result = exerciseRotationHistoryController.delete(id)

        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK && response.body == expectedRecord
            }
            .verifyComplete()
    }
}
