package com.congen.service

import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.mockExerciseRotationHistory
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ExerciseRotationHistoryServiceTest {
    private lateinit var exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL
    private lateinit var exerciseRotationHistoryService: ExerciseRotationHistoryService

    private val history = mockExerciseRotationHistory()
    private val historyList = listOf(history, mockExerciseRotationHistory(id = 2L, userId = 2))

    @BeforeEach
    fun setUp() {
        exerciseRotationHistoryDAL = mock()
        exerciseRotationHistoryService = ExerciseRotationHistoryService(exerciseRotationHistoryDAL)
    }

    @Test
    fun `selectById returns record when found`() {
        whenever(exerciseRotationHistoryDAL.selectById(1L)).thenReturn(Mono.just(history))
        val result = exerciseRotationHistoryService.selectById(1L)
        StepVerifier.create(result)
            .expectNext(history)
            .verifyComplete()
        verify(exerciseRotationHistoryDAL).selectById(1L)
    }

    @Test
    fun `selectById returns error when not found`() {
        whenever(exerciseRotationHistoryDAL.selectById(1L)).thenReturn(Mono.error(RuntimeException("Not found")))
        val result = exerciseRotationHistoryService.selectById(1L)
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
        verify(exerciseRotationHistoryDAL).selectById(1L)
    }

    @Test
    fun `selectAll returns list of records`() {
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(historyList))
        val result = exerciseRotationHistoryService.selectAll()
        StepVerifier.create(result)
            .expectNext(historyList)
            .verifyComplete()
        verify(exerciseRotationHistoryDAL).selectAll()
    }

    @Test
    fun `selectByIsAccessory returns filtered records`() {
        whenever(exerciseRotationHistoryDAL.selectByIsAccessory(true)).thenReturn(Mono.just(listOf(history)))
        val result = exerciseRotationHistoryService.selectByIsAccessory(true)
        StepVerifier.create(result)
            .expectNext(listOf(history))
            .verifyComplete()
        verify(exerciseRotationHistoryDAL).selectByIsAccessory(true)
    }

    @Test
    fun `insert returns inserted record`() {
        whenever(exerciseRotationHistoryDAL.insert(1, "Bench Press", false)).thenReturn(Mono.just(history))
        val result = exerciseRotationHistoryService.insert(1, "Bench Press", false)
        StepVerifier.create(result)
            .expectNext(history)
            .verifyComplete()
        verify(exerciseRotationHistoryDAL).insert(1, "Bench Press", false)
    }

    @Test
    fun `update returns updated record`() {
        whenever(exerciseRotationHistoryDAL.update(1L, 1, "Bench Press", false)).thenReturn(Mono.just(history))
        val result = exerciseRotationHistoryService.update(1L, 1, "Bench Press", false)
        StepVerifier.create(result)
            .expectNext(history)
            .verifyComplete()
        verify(exerciseRotationHistoryDAL).update(1L, 1, "Bench Press", false)
    }

    @Test
    fun `deleteById returns deleted record`() {
        whenever(exerciseRotationHistoryDAL.deleteById(1L)).thenReturn(Mono.just(history))
        val result = exerciseRotationHistoryService.deleteById(1L)
        StepVerifier.create(result)
            .expectNext(history)
            .verifyComplete()
        verify(exerciseRotationHistoryDAL).deleteById(1L)
    }

    @Test
    fun `deleteByUserId returns number of deleted records`() {
        whenever(exerciseRotationHistoryDAL.deleteByUserId(1L)).thenReturn(Mono.just(2))
        val result = exerciseRotationHistoryService.deleteByUserId(1L)
        StepVerifier.create(result)
            .expectNext(2)
            .verifyComplete()
        verify(exerciseRotationHistoryDAL).deleteByUserId(1L)
    }

    @Test
    fun `isOwner returns true when user is owner`() {
        val historyId = 1L
        val ownerUserId = 42
        val userId = "42"
        val history = mockExerciseRotationHistory(id = historyId, userId = ownerUserId)
        whenever(exerciseRotationHistoryDAL.selectById(historyId)).thenReturn(Mono.just(history))

        val result = exerciseRotationHistoryService.isOwner(historyId, userId)
        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `isOwner returns false when user is not owner`() {
        val historyId = 1L
        val ownerUserId = 99
        val userId = "42"
        val history = mockExerciseRotationHistory(id = historyId, userId = ownerUserId)
        whenever(exerciseRotationHistoryDAL.selectById(historyId)).thenReturn(Mono.just(history))

        val result = exerciseRotationHistoryService.isOwner(historyId, userId)
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `isOwner returns false when record not found`() {
        val historyId = 1L
        val userId = "42"
        whenever(exerciseRotationHistoryDAL.selectById(historyId)).thenReturn(Mono.error(RuntimeException("Not found")))

        val result = exerciseRotationHistoryService.isOwner(historyId, userId)
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `isOwner handles userId as string vs int`() {
        val historyId = 1L
        val ownerUserId = 42
        val userId = "42"
        val history = mockExerciseRotationHistory(id = historyId, userId = ownerUserId)
        whenever(exerciseRotationHistoryDAL.selectById(historyId)).thenReturn(Mono.just(history))

        val result = exerciseRotationHistoryService.isOwner(historyId, userId)
        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `selectByUserId returns records for user only`() {
        val userId = 42
        val expected = listOf(history)
        whenever(exerciseRotationHistoryDAL.selectByUserId(userId, null)).thenReturn(Mono.just(expected))
        val result = exerciseRotationHistoryService.selectByUserId(userId)
        StepVerifier.create(result).expectNext(expected).verifyComplete()
        verify(exerciseRotationHistoryDAL).selectByUserId(userId, null)
    }

    @Test
    fun `selectByUserId returns records for user and isAccessory true`() {
        val userId = 42
        val isAccessory = true
        val expected = listOf(history)
        whenever(exerciseRotationHistoryDAL.selectByUserId(userId, isAccessory)).thenReturn(Mono.just(expected))
        val result = exerciseRotationHistoryService.selectByUserId(userId, isAccessory)
        StepVerifier.create(result).expectNext(expected).verifyComplete()
        verify(exerciseRotationHistoryDAL).selectByUserId(userId, isAccessory)
    }

    @Test
    fun `selectByUserId returns records for user and isAccessory false`() {
        val userId = 42
        val isAccessory = false
        val expected = listOf(history)
        whenever(exerciseRotationHistoryDAL.selectByUserId(userId, isAccessory)).thenReturn(Mono.just(expected))
        val result = exerciseRotationHistoryService.selectByUserId(userId, isAccessory)
        StepVerifier.create(result).expectNext(expected).verifyComplete()
        verify(exerciseRotationHistoryDAL).selectByUserId(userId, isAccessory)
    }
}
