package com.congen.service

import com.congen.dal.ProgramDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.model.Program
import com.congen.model.ProgrammedWorkout
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

class ProgrammedWorkoutServiceTest {
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    private lateinit var programDAL: ProgramDAL
    private lateinit var programmedWorkoutService: ProgrammedWorkoutService

    private val now = Instant.now()
    private val testProgram =
        Program(
            id = 1L,
            userId = "b226d772-c063-4974-ae08-ab64134abbcf",
            name = "Test Program",
            currentWeekNumber = 1,
            isActive = true,
            createdAt = now,
            updatedAt = now
        )
    private val workout =
        ProgrammedWorkout(
            id = 1L,
            programId = 1L,
            dayNumber = 1,
            name = "Workout 1",
            createdAt = now,
            updatedAt = now
        )
    private val workoutList = listOf(workout, workout.copy(id = 2L, dayNumber = 2, name = "Workout 2"))

    @BeforeEach
    fun setUp() {
        programmedWorkoutDAL = mock()
        programDAL = mock()
        programmedWorkoutService = ProgrammedWorkoutService(programmedWorkoutDAL, programDAL)
    }

    @Test
    fun `selectProgrammedWorkoutById returns record when found`() {
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(1L)).thenReturn(Mono.just(workout))
        val result = programmedWorkoutService.selectProgrammedWorkoutById(1L)
        StepVerifier.create(result)
            .expectNext(workout)
            .verifyComplete()
        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(1L)
    }

    @Test
    fun `selectProgrammedWorkoutById returns error when not found`() {
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(1L)).thenReturn(Mono.error(RuntimeException("Not found")))
        val result = programmedWorkoutService.selectProgrammedWorkoutById(1L)
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(1L)
    }

    @Test
    fun `selectProgrammedWorkoutsByProgramId returns list of records`() {
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutsByProgramId(1L)).thenReturn(Mono.just(workoutList))
        val result = programmedWorkoutService.selectProgrammedWorkoutsByProgramId(1L)
        StepVerifier.create(result)
            .expectNext(workoutList)
            .verifyComplete()
        verify(programmedWorkoutDAL).selectProgrammedWorkoutsByProgramId(1L)
    }

    @Test
    fun `selectProgrammedWorkouts returns list of all records`() {
        whenever(programmedWorkoutDAL.selectProgrammedWorkouts()).thenReturn(Mono.just(workoutList))
        val result = programmedWorkoutService.selectProgrammedWorkouts()
        StepVerifier.create(result)
            .expectNext(workoutList)
            .verifyComplete()
        verify(programmedWorkoutDAL).selectProgrammedWorkouts()
    }

    @Test
    fun `insertProgrammedWorkout returns inserted record`() {
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(1L, 1, "Workout 1")).thenReturn(Mono.just(workout))
        val result = programmedWorkoutService.insertProgrammedWorkout(1L, 1, "Workout 1")
        StepVerifier.create(result)
            .expectNext(workout)
            .verifyComplete()
        verify(programmedWorkoutDAL).insertProgrammedWorkout(1L, 1, "Workout 1")
    }

    @Test
    fun `updateProgrammedWorkout returns updated record`() {
        whenever(programmedWorkoutDAL.updateProgrammedWorkout(1L, 1L, 1, "Workout 1")).thenReturn(Mono.just(workout))
        val result = programmedWorkoutService.updateProgrammedWorkout(1L, 1L, 1, "Workout 1")
        StepVerifier.create(result)
            .expectNext(workout)
            .verifyComplete()
        verify(programmedWorkoutDAL).updateProgrammedWorkout(1L, 1L, 1, "Workout 1")
    }

    @Test
    fun `hasUserExistingWorkouts returns true`() {
        whenever(programmedWorkoutDAL.hasUserExistingWorkouts("b226d772-c063-4974-ae08-ab64134abbcf")).thenReturn(Mono.just(true))
        val result = programmedWorkoutService.hasUserExistingWorkouts("b226d772-c063-4974-ae08-ab64134abbcf")
        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
        verify(programmedWorkoutDAL).hasUserExistingWorkouts("b226d772-c063-4974-ae08-ab64134abbcf")
    }

    @Test
    fun `deleteProgrammedWorkout returns deleted record`() {
        whenever(programmedWorkoutDAL.deleteProgrammedWorkout(1L)).thenReturn(Mono.just(workout))
        val result = programmedWorkoutService.deleteProgrammedWorkout(1L)
        StepVerifier.create(result)
            .expectNext(workout)
            .verifyComplete()
        verify(programmedWorkoutDAL).deleteProgrammedWorkout(1L)
    }

    @Test
    fun `isOwner returns true when user is owner`() {
        val programmedWorkoutId = 1L
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val workout = this.workout.copy(id = programmedWorkoutId, programId = 1L)
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(programmedWorkoutId)).thenReturn(Mono.just(workout))
        whenever(programDAL.selectProgramById(1L)).thenReturn(Mono.just(testProgram))

        val result = programmedWorkoutService.isOwner(programmedWorkoutId, userId)
        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(programmedWorkoutId)
        verify(programDAL).selectProgramById(1L)
    }

    @Test
    fun `isOwner returns false when user is not owner`() {
        val programmedWorkoutId = 1L
        val userId = "different-user-id"
        val workout = this.workout.copy(id = programmedWorkoutId, programId = 1L)
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(programmedWorkoutId)).thenReturn(Mono.just(workout))
        whenever(programDAL.selectProgramById(1L)).thenReturn(Mono.just(testProgram))

        val result = programmedWorkoutService.isOwner(programmedWorkoutId, userId)
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(programmedWorkoutId)
        verify(programDAL).selectProgramById(1L)
    }

    @Test
    fun `isOwner returns false when programmed workout not found`() {
        val programmedWorkoutId = 1L
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        whenever(
            programmedWorkoutDAL.selectProgrammedWorkoutById(programmedWorkoutId)
        ).thenReturn(Mono.error(RuntimeException("Not found")))

        val result = programmedWorkoutService.isOwner(programmedWorkoutId, userId)
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(programmedWorkoutId)
    }

    @Test
    fun `isOwner returns false when program not found`() {
        val programmedWorkoutId = 1L
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val workout = this.workout.copy(id = programmedWorkoutId, programId = 1L)
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(programmedWorkoutId)).thenReturn(Mono.just(workout))
        whenever(programDAL.selectProgramById(1L)).thenReturn(Mono.error(RuntimeException("Program not found")))

        val result = programmedWorkoutService.isOwner(programmedWorkoutId, userId)
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(programmedWorkoutId)
        verify(programDAL).selectProgramById(1L)
    }

    @Test
    fun `selectProgrammedWorkoutsByUserId returns list of user owned workouts`() {
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val userWorkouts =
            listOf(
                workout.copy(id = 1L, programId = 1L, dayNumber = 1, name = "User Workout 1"),
                workout.copy(id = 2L, programId = 1L, dayNumber = 2, name = "User Workout 2")
            )
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutsByUserId(userId)).thenReturn(Mono.just(userWorkouts))

        val result = programmedWorkoutService.selectProgrammedWorkoutsByUserId(userId)

        StepVerifier.create(result).expectNext(userWorkouts).verifyComplete()
        verify(programmedWorkoutDAL).selectProgrammedWorkoutsByUserId(userId)
    }

    @Test
    fun `selectProgrammedWorkoutsByUserId returns empty list when user has no workouts`() {
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val emptyList = emptyList<ProgrammedWorkout>()
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutsByUserId(userId)).thenReturn(Mono.just(emptyList))

        val result = programmedWorkoutService.selectProgrammedWorkoutsByUserId(userId)

        StepVerifier.create(result).expectNext(emptyList).verifyComplete()
        verify(programmedWorkoutDAL).selectProgrammedWorkoutsByUserId(userId)
    }

    @Test
    fun `selectProgrammedWorkoutsByUserId propagates database errors`() {
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val databaseError = RuntimeException("Database connection failed")
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutsByUserId(userId)).thenReturn(Mono.error(databaseError))

        val result = programmedWorkoutService.selectProgrammedWorkoutsByUserId(userId)

        StepVerifier.create(result).expectError(databaseError::class.java).verify()
        verify(programmedWorkoutDAL).selectProgrammedWorkoutsByUserId(userId)
    }
}
