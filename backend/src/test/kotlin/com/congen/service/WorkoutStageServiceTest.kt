package com.congen.service

import com.congen.dal.ProgramDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.mockProgram
import com.congen.mockProgrammedWorkout
import com.congen.mockWorkoutStage
import com.congen.model.WorkoutStage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class WorkoutStageServiceTest {
    private lateinit var workoutStageDAL: WorkoutStageDAL
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    private lateinit var programDAL: ProgramDAL
    private lateinit var workoutStageService: WorkoutStageService

    private val workoutStage: WorkoutStage = mockWorkoutStage()
    private val workoutStageList: List<WorkoutStage> = listOf(workoutStage, mockWorkoutStage(id = 2L, programmedWorkoutId = 2L))

    @BeforeEach
    fun setUp() {
        workoutStageDAL = mock()
        programmedWorkoutDAL = mock()
        programDAL = mock()
        workoutStageService = WorkoutStageService(workoutStageDAL, programmedWorkoutDAL, programDAL)
    }

    @Test
    fun `selectWorkoutStageById should return workout stage`() {
        whenever(workoutStageDAL.selectWorkoutStageById(workoutStage.id)).thenReturn(Mono.just(workoutStage))
        val result = workoutStageService.selectWorkoutStageById(workoutStage.id)
        StepVerifier.create(result).expectNext(workoutStage).verifyComplete()
        verify(workoutStageDAL).selectWorkoutStageById(workoutStage.id)
    }

    @Test
    fun `selectWorkoutStageById should propagate error`() {
        val error = RuntimeException("not found")
        whenever(workoutStageDAL.selectWorkoutStageById(workoutStage.id)).thenReturn(Mono.error(error))
        val result = workoutStageService.selectWorkoutStageById(workoutStage.id)
        StepVerifier.create(result).expectErrorMatches { it === error }.verify()
    }

    @Test
    fun `selectWorkoutStagesByProgrammedWorkoutId should return list of workout stages`() {
        whenever(
            workoutStageDAL.selectWorkoutStagesByProgrammedWorkoutId(workoutStage.programmedWorkoutId)
        ).thenReturn(Mono.just(workoutStageList))
        val result = workoutStageService.selectWorkoutStagesByProgrammedWorkoutId(workoutStage.programmedWorkoutId)
        StepVerifier.create(result).expectNext(workoutStageList).verifyComplete()
        verify(workoutStageDAL).selectWorkoutStagesByProgrammedWorkoutId(workoutStage.programmedWorkoutId)
    }

    @Test
    fun `selectWorkoutStages should return all workout stages`() {
        whenever(workoutStageDAL.selectWorkoutStages()).thenReturn(Mono.just(workoutStageList))
        val result = workoutStageService.selectWorkoutStages()
        StepVerifier.create(result).expectNext(workoutStageList).verifyComplete()
        verify(workoutStageDAL).selectWorkoutStages()
    }

    @Test
    fun `insertWorkoutStage should return inserted workout stage`() {
        val insertStage = mockWorkoutStage(id = 0L)
        whenever(
            workoutStageDAL.insertWorkoutStage(
                insertStage.programmedWorkoutId,
                insertStage.stageTypeId,
                insertStage.position,
                insertStage.name
            )
        ).thenReturn(Mono.just(insertStage))
        val result =
            workoutStageService.insertWorkoutStage(
                insertStage.programmedWorkoutId,
                insertStage.stageTypeId,
                insertStage.position,
                insertStage.name
            )
        StepVerifier.create(result).expectNext(insertStage).verifyComplete()
        verify(
            workoutStageDAL
        ).insertWorkoutStage(insertStage.programmedWorkoutId, insertStage.stageTypeId, insertStage.position, insertStage.name)
    }

    @Test
    fun `updateWorkoutStage should return updated workout stage`() {
        val updatedStage = mockWorkoutStage(stageTypeId = 2, position = 2, name = "Accessory")
        whenever(
            workoutStageDAL.updateWorkoutStage(
                updatedStage.id,
                updatedStage.programmedWorkoutId,
                updatedStage.stageTypeId,
                updatedStage.position,
                updatedStage.name
            )
        ).thenReturn(Mono.just(updatedStage))
        val result =
            workoutStageService.updateWorkoutStage(
                updatedStage.id,
                updatedStage.programmedWorkoutId,
                updatedStage.stageTypeId,
                updatedStage.position,
                updatedStage.name
            )
        StepVerifier.create(result).expectNext(updatedStage).verifyComplete()
        verify(
            workoutStageDAL
        ).updateWorkoutStage(
            updatedStage.id,
            updatedStage.programmedWorkoutId,
            updatedStage.stageTypeId,
            updatedStage.position,
            updatedStage.name
        )
    }

    @Test
    fun `selectWorkoutStageByWorkoutIdAndPosition should return workout stage`() {
        whenever(
            workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(workoutStage.programmedWorkoutId, workoutStage.position)
        ).thenReturn(Mono.just(workoutStage))
        val result = workoutStageService.selectWorkoutStageByWorkoutIdAndPosition(workoutStage.programmedWorkoutId, workoutStage.position)
        StepVerifier.create(result).expectNext(workoutStage).verifyComplete()
        verify(workoutStageDAL).selectWorkoutStageByWorkoutIdAndPosition(workoutStage.programmedWorkoutId, workoutStage.position)
    }

    @Test
    fun `deleteWorkoutStage should return deleted workout stage`() {
        whenever(workoutStageDAL.deleteWorkoutStage(workoutStage.id)).thenReturn(Mono.just(workoutStage))
        val result = workoutStageService.deleteWorkoutStage(workoutStage.id)
        StepVerifier.create(result).expectNext(workoutStage).verifyComplete()
        verify(workoutStageDAL).deleteWorkoutStage(workoutStage.id)
    }

    @Test
    fun `isOwner returns true when user is owner`() {
        val workoutStageId = 1L
        val programmedWorkoutId = 2L
        val programId = 3L
        val ownerUserId = "42"
        val userId = "42"
        val workoutStage = mockWorkoutStage(id = workoutStageId, programmedWorkoutId = programmedWorkoutId)
        val programmedWorkout = mockProgrammedWorkout(id = programmedWorkoutId, programId = programId)
        val program = mockProgram(id = programId, userId = ownerUserId)

        whenever(workoutStageDAL.selectWorkoutStageById(workoutStageId)).thenReturn(Mono.just(workoutStage))
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(programmedWorkoutId)).thenReturn(Mono.just(programmedWorkout))
        whenever(programDAL.selectProgramById(programId)).thenReturn(Mono.just(program))

        val result = workoutStageService.isOwner(workoutStageId, userId)
        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
        verify(workoutStageDAL).selectWorkoutStageById(workoutStageId)
        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(programmedWorkoutId)
        verify(programDAL).selectProgramById(programId)
    }

    @Test
    fun `isOwner returns false when user is not owner`() {
        val workoutStageId = 1L
        val programmedWorkoutId = 2L
        val programId = 3L
        val ownerUserId = "99"
        val userId = "42"
        val workoutStage = mockWorkoutStage(id = workoutStageId, programmedWorkoutId = programmedWorkoutId)
        val programmedWorkout = mockProgrammedWorkout(id = programmedWorkoutId, programId = programId)
        val program = mockProgram(id = programId, userId = ownerUserId)

        whenever(workoutStageDAL.selectWorkoutStageById(workoutStageId)).thenReturn(Mono.just(workoutStage))
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(programmedWorkoutId)).thenReturn(Mono.just(programmedWorkout))
        whenever(programDAL.selectProgramById(programId)).thenReturn(Mono.just(program))

        val result = workoutStageService.isOwner(workoutStageId, userId)
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
        verify(workoutStageDAL).selectWorkoutStageById(workoutStageId)
        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(programmedWorkoutId)
        verify(programDAL).selectProgramById(programId)
    }

    @Test
    fun `isOwner returns false when workout stage not found`() {
        val workoutStageId = 1L
        val userId = "42"
        whenever(workoutStageDAL.selectWorkoutStageById(workoutStageId)).thenReturn(Mono.error(RuntimeException("Not found")))

        val result = workoutStageService.isOwner(workoutStageId, userId)
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
        verify(workoutStageDAL).selectWorkoutStageById(workoutStageId)
    }

    @Test
    fun `isOwner returns false when programmed workout not found`() {
        val workoutStageId = 1L
        val programmedWorkoutId = 2L
        val userId = "42"
        val workoutStage = mockWorkoutStage(id = workoutStageId, programmedWorkoutId = programmedWorkoutId)
        whenever(workoutStageDAL.selectWorkoutStageById(workoutStageId)).thenReturn(Mono.just(workoutStage))
        whenever(
            programmedWorkoutDAL.selectProgrammedWorkoutById(programmedWorkoutId)
        ).thenReturn(Mono.error(RuntimeException("Not found")))

        val result = workoutStageService.isOwner(workoutStageId, userId)
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
        verify(workoutStageDAL).selectWorkoutStageById(workoutStageId)
        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(programmedWorkoutId)
    }

    @Test
    fun `isOwner returns false when program not found`() {
        val workoutStageId = 1L
        val programmedWorkoutId = 2L
        val programId = 3L
        val userId = "42"
        val workoutStage = mockWorkoutStage(id = workoutStageId, programmedWorkoutId = programmedWorkoutId)
        val programmedWorkout = mockProgrammedWorkout(id = programmedWorkoutId, programId = programId)
        whenever(workoutStageDAL.selectWorkoutStageById(workoutStageId)).thenReturn(Mono.just(workoutStage))
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(programmedWorkoutId)).thenReturn(Mono.just(programmedWorkout))
        whenever(programDAL.selectProgramById(programId)).thenReturn(Mono.error(RuntimeException("Not found")))

        val result = workoutStageService.isOwner(workoutStageId, userId)
        StepVerifier.create(result)
            .expectNext(false)
            .verifyComplete()
        verify(workoutStageDAL).selectWorkoutStageById(workoutStageId)
        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(programmedWorkoutId)
        verify(programDAL).selectProgramById(programId)
    }

    @Test
    fun `isOwner handles userId as string vs int`() {
        val workoutStageId = 1L
        val programmedWorkoutId = 2L
        val programId = 3L
        val ownerUserId = "42"
        val userId = "42"
        val workoutStage = mockWorkoutStage(id = workoutStageId, programmedWorkoutId = programmedWorkoutId)
        val programmedWorkout = mockProgrammedWorkout(id = programmedWorkoutId, programId = programId)
        val program = mockProgram(id = programId, userId = ownerUserId)

        whenever(workoutStageDAL.selectWorkoutStageById(workoutStageId)).thenReturn(Mono.just(workoutStage))
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(programmedWorkoutId)).thenReturn(Mono.just(programmedWorkout))
        whenever(programDAL.selectProgramById(programId)).thenReturn(Mono.just(program))

        val result = workoutStageService.isOwner(workoutStageId, userId)
        StepVerifier.create(result)
            .expectNext(true)
            .verifyComplete()
        verify(workoutStageDAL).selectWorkoutStageById(workoutStageId)
        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(programmedWorkoutId)
        verify(programDAL).selectProgramById(programId)
    }

    @Test
    fun `selectWorkoutStagesByUserId returns list of user owned workout stages`() {
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val userWorkoutStages =
            listOf(
                mockWorkoutStage(id = 1L, programmedWorkoutId = 1L, position = 1, name = "User Stage 1"),
                mockWorkoutStage(id = 2L, programmedWorkoutId = 1L, position = 2, name = "User Stage 2")
            )
        whenever(workoutStageDAL.selectWorkoutStagesByUserId(userId)).thenReturn(Mono.just(userWorkoutStages))

        val result = workoutStageService.selectWorkoutStagesByUserId(userId)

        StepVerifier.create(result).expectNext(userWorkoutStages).verifyComplete()
        verify(workoutStageDAL).selectWorkoutStagesByUserId(userId)
    }

    @Test
    fun `selectWorkoutStagesByUserId returns empty list when user has no workout stages`() {
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val emptyList = emptyList<WorkoutStage>()
        whenever(workoutStageDAL.selectWorkoutStagesByUserId(userId)).thenReturn(Mono.just(emptyList))

        val result = workoutStageService.selectWorkoutStagesByUserId(userId)

        StepVerifier.create(result).expectNext(emptyList).verifyComplete()
        verify(workoutStageDAL).selectWorkoutStagesByUserId(userId)
    }

    @Test
    fun `selectWorkoutStagesByUserId propagates database errors`() {
        val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
        val databaseError = RuntimeException("Database connection failed")
        whenever(workoutStageDAL.selectWorkoutStagesByUserId(userId)).thenReturn(Mono.error(databaseError))

        val result = workoutStageService.selectWorkoutStagesByUserId(userId)

        StepVerifier.create(result).expectError(databaseError::class.java).verify()
        verify(workoutStageDAL).selectWorkoutStagesByUserId(userId)
    }
}
