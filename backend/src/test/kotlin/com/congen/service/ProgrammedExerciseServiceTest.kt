package com.congen.service

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.mockProgrammedExercise
import com.congen.model.ProgrammedExercise
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class ProgrammedExerciseServiceTest {
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL
    private lateinit var programmedExerciseService: ProgrammedExerciseService

    private val exercise = mockProgrammedExercise()
    private val exerciseList = listOf(exercise, mockProgrammedExercise(id = 2L, workoutStageId = 2L))

    @BeforeEach
    fun setUp() {
        programmedExerciseDAL = mock()
        programmedExerciseService = ProgrammedExerciseService(programmedExerciseDAL)
    }

    @Test
    fun `selectProgrammedExerciseById returns record when found`() {
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(1L)).thenReturn(Mono.just(exercise))
        val result = programmedExerciseService.selectProgrammedExerciseById(1L)
        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()
        verify(programmedExerciseDAL).selectProgrammedExerciseById(1L)
    }

    @Test
    fun `selectProgrammedExerciseById returns error when not found`() {
        whenever(programmedExerciseDAL.selectProgrammedExerciseById(1L)).thenReturn(Mono.error(RuntimeException("Not found")))
        val result = programmedExerciseService.selectProgrammedExerciseById(1L)
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
        verify(programmedExerciseDAL).selectProgrammedExerciseById(1L)
    }

    @Test
    fun `selectProgrammedExercisesByWorkoutStageId returns list of records`() {
        whenever(programmedExerciseDAL.selectProgrammedExercisesByWorkoutStageId(1L)).thenReturn(Mono.just(exerciseList))
        val result = programmedExerciseService.selectProgrammedExercisesByWorkoutStageId(1L)
        StepVerifier.create(result)
            .expectNext(exerciseList)
            .verifyComplete()
        verify(programmedExerciseDAL).selectProgrammedExercisesByWorkoutStageId(1L)
    }

    @Test
    fun `selectProgrammedExercises returns list of all records`() {
        whenever(programmedExerciseDAL.selectProgrammedExercises()).thenReturn(Mono.just(exerciseList))
        val result = programmedExerciseService.selectProgrammedExercises()
        StepVerifier.create(result)
            .expectNext(exerciseList)
            .verifyComplete()
        verify(programmedExerciseDAL).selectProgrammedExercises()
    }

    @Test
    fun `insertProgrammedExercise returns inserted record`() {
        whenever(programmedExerciseDAL.insertProgrammedExercise(1L, "Bench Press", 1, null)).thenReturn(Mono.just(exercise))
        val result = programmedExerciseService.insertProgrammedExercise(1L, "Bench Press", 1, null)
        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()
        verify(programmedExerciseDAL).insertProgrammedExercise(1L, "Bench Press", 1, null)
    }

    @Test
    fun `updateProgrammedExercise returns updated record`() {
        whenever(programmedExerciseDAL.updateProgrammedExercise(1L, 1L, "Bench Press", 1, null)).thenReturn(Mono.just(exercise))
        val result = programmedExerciseService.updateProgrammedExercise(1L, 1L, "Bench Press", 1, null)
        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()
        verify(programmedExerciseDAL).updateProgrammedExercise(1L, 1L, "Bench Press", 1, null)
    }

    @Test
    fun `deleteProgrammedExercise returns deleted record`() {
        whenever(programmedExerciseDAL.deleteProgrammedExercise(1L)).thenReturn(Mono.just(exercise))
        val result = programmedExerciseService.deleteProgrammedExercise(1L)
        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()
        verify(programmedExerciseDAL).deleteProgrammedExercise(1L)
    }

    @Test
    fun `selectProgrammedExerciseByStageIdAndExerciseName returns record when found`() {
        whenever(programmedExerciseDAL.selectProgrammedExerciseByStageIdAndExerciseName(1L, "Bench Press")).thenReturn(Mono.just(exercise))
        val result = programmedExerciseService.selectProgrammedExerciseByStageIdAndExerciseName(1L, "Bench Press")
        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()
        verify(programmedExerciseDAL).selectProgrammedExerciseByStageIdAndExerciseName(1L, "Bench Press")
    }

    @Test
    fun `getUserIdFromProgrammedExercise returns user id`() {
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(1L)).thenReturn(Mono.just("42"))
        val result = programmedExerciseService.getUserIdFromProgrammedExercise(1L)
        StepVerifier.create(result)
            .expectNext("42")
            .verifyComplete()
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(1L)
    }

    @Test
    fun `isOwner returns true when user is owner`() {
        val programmedExerciseId = 1L
        val ownerUserId = "42"
        val userId = "42"
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExerciseId)).thenReturn(Mono.just(ownerUserId))

        val result = programmedExerciseService.isOwner(programmedExerciseId, userId)
        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(programmedExerciseId)
    }

    @Test
    fun `isOwner returns false when user is not owner`() {
        val programmedExerciseId = 1L
        val ownerUserId = "99"
        val userId = "42"
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExerciseId)).thenReturn(Mono.just(ownerUserId))

        val result = programmedExerciseService.isOwner(programmedExerciseId, userId)
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(programmedExerciseId)
    }

    @Test
    fun `isOwner returns false when programmed exercise not found`() {
        val programmedExerciseId = 1L
        val userId = "42"
        whenever(
            programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExerciseId)
        ).thenReturn(Mono.error(RuntimeException("Not found")))

        val result = programmedExerciseService.isOwner(programmedExerciseId, userId)
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(programmedExerciseId)
    }

    @Test
    fun `isOwner handles userId as string vs int`() {
        val programmedExerciseId = 1L
        val ownerUserId = "42"
        val userId = "42"
        whenever(programmedExerciseDAL.getUserIdFromProgrammedExercise(programmedExerciseId)).thenReturn(Mono.just(ownerUserId))

        val result = programmedExerciseService.isOwner(programmedExerciseId, userId)
        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
        verify(programmedExerciseDAL).getUserIdFromProgrammedExercise(programmedExerciseId)
    }

    @Test
    fun `selectProgrammedExercisesByUserId returns list of records`() {
        val userId = "42"
        whenever(programmedExerciseDAL.selectProgrammedExercisesByUserId(userId)).thenReturn(Mono.just(exerciseList))
        val result = programmedExerciseService.selectProgrammedExercisesByUserId(userId)
        StepVerifier.create(result)
            .expectNext(exerciseList)
            .verifyComplete()
        verify(programmedExerciseDAL).selectProgrammedExercisesByUserId(userId)
    }

    @Test
    fun `selectProgrammedExercisesByUserId returns empty list`() {
        val userId = "99"
        whenever(programmedExerciseDAL.selectProgrammedExercisesByUserId(userId)).thenReturn(Mono.just(emptyList()))
        val result = programmedExerciseService.selectProgrammedExercisesByUserId(userId)
        StepVerifier.create(result)
            .expectNext(emptyList<ProgrammedExercise>())
            .verifyComplete()
        verify(programmedExerciseDAL).selectProgrammedExercisesByUserId(userId)
    }

    @Test
    fun `selectProgrammedExercisesByUserId propagates error`() {
        val userId = "42"
        val ex = RuntimeException("db error")
        whenever(programmedExerciseDAL.selectProgrammedExercisesByUserId(userId)).thenReturn(Mono.error(ex))
        val result = programmedExerciseService.selectProgrammedExercisesByUserId(userId)
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
        verify(programmedExerciseDAL).selectProgrammedExercisesByUserId(userId)
    }
}
