package com.congen.generator

import com.congen.client.PostgresClient
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.generator.ProgrammedExerciseData
import com.congen.generator.SetSchemeParams
import com.congen.generator.WorkoutGenerationResult
import com.congen.generator.WorkoutStageData
import com.congen.model.ProgrammedWorkout
import com.congen.model.ProgrammedExercise
import com.congen.model.WorkoutStage
import com.congen.model.WorkoutStageType
import com.congen.model.SetScheme
import com.congen.model.UserWeightUnitPreference
import com.congen.model.WeightUnit
import com.congen.model.WorkoutStageTypeEnum
import com.congen.service.SetSchemeService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.jvm.functions.Function1
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal

/**
 * Unit tests for AtomicWorkoutWriter
 */
class AtomicWorkoutWriterTest {
    private lateinit var atomicWorkoutWriter: AtomicWorkoutWriter
    private lateinit var postgresClient: PostgresClient
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    private lateinit var workoutStageDAL: WorkoutStageDAL
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL
    private lateinit var setSchemeService: SetSchemeService
    private lateinit var workoutStageTypeDAL: WorkoutStageTypeDAL
    private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        programmedWorkoutDAL = mock()
        workoutStageDAL = mock()
        programmedExerciseDAL = mock()
        setSchemeService = mock()
        workoutStageTypeDAL = mock()
        userWeightUnitPreferenceDAL = mock()
        
        atomicWorkoutWriter = AtomicWorkoutWriter(
            postgresClient,
            programmedWorkoutDAL,
            workoutStageDAL,
            programmedExerciseDAL,
            setSchemeService,
            workoutStageTypeDAL,
            userWeightUnitPreferenceDAL
        )
    }

    @Test
    fun `should write workout atomically with complete data`() {
        // Given
        val programId = 1L
        val dayNumber = 1
        val dayType = "ME_Upper"
        val userId = "user-123"
        
        val workoutResult = createWorkoutGenerationResult(programId, dayNumber, dayType, userId)
        val expectedWorkout = createMockProgrammedWorkout(id = 1, programId = programId, dayNumber = dayNumber, name = dayType)
        val expectedStage = createMockWorkoutStage(id = 1, programmedWorkoutId = 1)
        val expectedExercise = createMockProgrammedExercise(id = 1, workoutStageId = 1)
        val expectedSetScheme = createMockSetScheme(id = 1, programmedExerciseId = 1)
        val expectedWorkoutStageType = createMockWorkoutStageType(id = 1, name = WorkoutStageTypeEnum.PRIMARY)
        val expectedWeightPreference = createMockUserWeightUnitPreference(userId = userId, exerciseName = "Bench Press", preferredUnit = WeightUnit.KG)

        whenever(programmedWorkoutDAL.insertProgrammedWorkout(programId, dayNumber, dayType))
            .thenReturn(Mono.just(expectedWorkout))
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(WorkoutStageTypeEnum.PRIMARY))
            .thenReturn(Mono.just(expectedWorkoutStageType))
        whenever(workoutStageDAL.insertWorkoutStage(1, 1, 1, "Primary Movement"))
            .thenReturn(Mono.just(expectedStage))
        whenever(programmedExerciseDAL.insertProgrammedExercise(1, "Bench Press", 1, null))
            .thenReturn(Mono.just(expectedExercise))
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, "Bench Press"))
            .thenReturn(Mono.just(expectedWeightPreference))
        whenever(setSchemeService.insertSetScheme(
            programmedExerciseId = 1,
            setNumber = 1,
            isAmrap = false,
            isEmom = false,
            useTempo = false,
            eccentricTempo = null,
            isometricTempo = null,
            concentricTempo = null,
            targetWeight = "100.0",
            performedWeight = null,
            targetRepCount = 5,
            performedRepCount = null,
            restSeconds = null,
            unit = "KG"
        )).thenReturn(Mono.just(expectedSetScheme))
        
        // Mock the transaction to execute the block and return the expected workout
        whenever(postgresClient.withTransaction<ProgrammedWorkout>(any()))
            .thenAnswer { invocation ->
                val block = invocation.getArgument<Function0<Mono<ProgrammedWorkout>>>(0)
                block.invoke()
            }

        // When
        val result = atomicWorkoutWriter.writeWorkoutAtomically(workoutResult)

        // Then
        StepVerifier.create(result)
            .expectNext(expectedWorkout)
            .verifyComplete()

        verify(programmedWorkoutDAL).insertProgrammedWorkout(programId, dayNumber, dayType)
        verify(workoutStageTypeDAL).selectWorkoutStageTypeByEnum(WorkoutStageTypeEnum.PRIMARY)
        verify(workoutStageDAL).insertWorkoutStage(1, 1, 1, "Primary Movement")
        verify(programmedExerciseDAL).insertProgrammedExercise(1, "Bench Press", 1, null)
        verify(setSchemeService).insertSetScheme(
            programmedExerciseId = 1,
            setNumber = 1,
            isAmrap = false,
            isEmom = false,
            useTempo = false,
            eccentricTempo = null,
            isometricTempo = null,
            concentricTempo = null,
            targetWeight = "100.0",
            performedWeight = null,
            targetRepCount = 5,
            performedRepCount = null,
            restSeconds = null,
            unit = "KG"
        )
    }

    @Test
    fun `should handle workout stage generation failure`() {
        // Given
        val programId = 1L
        val dayNumber = 1
        val dayType = "ME_Upper"
        val userId = "user-123"
        
        val workoutResult = createWorkoutGenerationResult(programId, dayNumber, dayType, userId)
        val expectedWorkout = createMockProgrammedWorkout(id = 1, programId = programId, dayNumber = dayNumber, name = dayType)
        val expectedWorkoutStageType = createMockWorkoutStageType(id = 1, name = WorkoutStageTypeEnum.PRIMARY)

        whenever(programmedWorkoutDAL.insertProgrammedWorkout(programId, dayNumber, dayType))
            .thenReturn(Mono.just(expectedWorkout))
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(WorkoutStageTypeEnum.PRIMARY))
            .thenReturn(Mono.just(expectedWorkoutStageType))
        whenever(workoutStageDAL.insertWorkoutStage(1, 1, 1, "Primary Movement"))
            .thenReturn(Mono.error(RuntimeException("Database error")))
        
        // Mock the transaction to execute the block
        whenever(postgresClient.withTransaction<ProgrammedWorkout>(any()))
            .thenAnswer { invocation ->
                val block = invocation.getArgument<Function0<Mono<ProgrammedWorkout>>>(0)
                block.invoke()
            }

        // When
        val result = atomicWorkoutWriter.writeWorkoutAtomically(workoutResult)

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(programmedWorkoutDAL).insertProgrammedWorkout(programId, dayNumber, dayType)
        verify(workoutStageTypeDAL, atLeastOnce()).selectWorkoutStageTypeByEnum(WorkoutStageTypeEnum.PRIMARY)
        verify(workoutStageDAL, atLeastOnce()).insertWorkoutStage(1, 1, 1, "Primary Movement")
    }

    @Test
    fun `should handle empty workout stages`() {
        // Given
        val programId = 1L
        val dayNumber = 1
        val dayType = "ME_Upper"
        val userId = "user-123"
        
        val workoutResult = WorkoutGenerationResult(
            programId = programId,
            dayNumber = dayNumber,
            dayType = dayType,
            userId = userId,
            stages = emptyList(),
            preparedData = createSamplePreparedData()
        )
        val expectedWorkout = createMockProgrammedWorkout(id = 1, programId = programId, dayNumber = dayNumber, name = dayType)

        whenever(programmedWorkoutDAL.insertProgrammedWorkout(programId, dayNumber, dayType))
            .thenReturn(Mono.just(expectedWorkout))
        
        // Mock the transaction to execute the block
        whenever(postgresClient.withTransaction<ProgrammedWorkout>(any()))
            .thenAnswer { invocation ->
                val block = invocation.getArgument<Function0<Mono<ProgrammedWorkout>>>(0)
                block.invoke()
            }

        // When
        val result = atomicWorkoutWriter.writeWorkoutAtomically(workoutResult)

        // Then
        StepVerifier.create(result)
            .expectNext(expectedWorkout)
            .verifyComplete()

        verify(programmedWorkoutDAL).insertProgrammedWorkout(programId, dayNumber, dayType)
    }

    // Helper methods
    private fun createSetSchemeParams(
        setNumber: Int,
        isAmrap: Boolean = false,
        isEmom: Boolean = false,
        targetReps: Int,
        targetWeight: BigDecimal
    ): SetSchemeParams {
        return SetSchemeParams(
            setNumber = setNumber,
            isAmrap = isAmrap,
            isEmom = isEmom,
            useTempo = false,
            eccentricTempo = null,
            isometricTempo = null,
            concentricTempo = null,
            targetWeight = targetWeight,
            performedWeight = null,
            targetRepCount = targetReps,
            performedRepCount = null,
            restSeconds = null,
            band = null
        )
    }

    private fun createProgrammedExerciseData(
        exerciseName: String,
        setSchemes: List<SetSchemeParams>
    ): ProgrammedExerciseData {
        return ProgrammedExerciseData(
            exerciseName = exerciseName,
            position = 1,
            notes = null,
            setSchemes = setSchemes
        )
    }

    private fun createWorkoutStageData(
        stageType: WorkoutStageTypeEnum,
        stageName: String,
        exercises: List<ProgrammedExerciseData>
    ): WorkoutStageData {
        return WorkoutStageData(
            stageType = stageType,
            position = 1,
            name = stageName,
            exercises = exercises
        )
    }

    private fun createWorkoutGenerationResult(
        programId: Long,
        dayNumber: Int,
        dayType: String,
        userId: String
    ): WorkoutGenerationResult {
        val setSchemeParams = createSetSchemeParams(
            setNumber = 1,
            targetReps = 5,
            targetWeight = BigDecimal("100.0")
        )
        val exerciseData = createProgrammedExerciseData(
            exerciseName = "Bench Press",
            setSchemes = listOf(setSchemeParams)
        )
        val stageData = createWorkoutStageData(
            stageType = WorkoutStageTypeEnum.PRIMARY,
            stageName = "Primary Movement",
            exercises = listOf(exerciseData)
        )
        
        return WorkoutGenerationResult(
            programId = programId,
            dayNumber = dayNumber,
            dayType = dayType,
            userId = userId,
            stages = listOf(stageData),
            preparedData = createSamplePreparedData()
        )
    }

    // Mock creation helpers
    private fun createMockProgrammedWorkout(
        id: Long,
        programId: Long,
        dayNumber: Int,
        name: String
    ): ProgrammedWorkout {
        return ProgrammedWorkout(
            id = id,
            programId = programId,
            dayNumber = dayNumber,
            name = name,
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now()
        )
    }

    private fun createMockWorkoutStage(
        id: Long,
        programmedWorkoutId: Long
    ): WorkoutStage {
        return WorkoutStage(
            id = id,
            programmedWorkoutId = programmedWorkoutId,
            stageTypeId = 1,
            position = 1,
            name = "Primary Movement",
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now()
        )
    }

    private fun createMockProgrammedExercise(
        id: Long,
        workoutStageId: Long
    ): ProgrammedExercise {
        return ProgrammedExercise(
            id = id,
            workoutStageId = workoutStageId,
            exerciseName = "Bench Press",
            position = 1,
            notes = null,
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now()
        )
    }

    private fun createMockSetScheme(
        id: Long,
        programmedExerciseId: Long
    ): SetScheme {
        return SetScheme(
            id = id,
            programmedExerciseId = programmedExerciseId,
            setNumber = 1,
            isAmrap = false,
            isEmom = false,
            useTempo = false,
            eccentricTempo = null,
            isometricTempo = null,
            concentricTempo = null,
            targetWeight = BigDecimal("100.0"),
            performedWeight = null,
            targetRepCount = 5,
            performedRepCount = null,
            restSeconds = null,
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now(),
            band = null
        )
    }

    private fun createMockWorkoutStageType(
        id: Int,
        name: WorkoutStageTypeEnum
    ): WorkoutStageType {
        return WorkoutStageType(
            id = id,
            name = name,
            createdAt = java.time.Instant.now()
        )
    }

    private fun createMockUserWeightUnitPreference(
        userId: String,
        exerciseName: String,
        preferredUnit: WeightUnit
    ): UserWeightUnitPreference {
        return UserWeightUnitPreference(
            userId = userId,
            exerciseName = exerciseName,
            preferredUnit = preferredUnit,
            createdAt = java.time.Instant.now(),
            updatedAt = java.time.Instant.now()
        )
    }

    private fun createSamplePreparedData(): WorkoutGenerationPreparedData {
        return WorkoutGenerationPreparedData(
            userExercisePool = mock(),
            oneRepMaxes = emptyList(),
            programPreferences = mock(),
            weakMuscles = emptyList(),
            currentWeekNumber = 1,
            userId = "user-123",
            weightUnitPreferences = emptyMap(),
            exerciseMuscleMappings = emptyMap(),
            exerciseWorkoutTypeMappings = emptyMap(),
            exerciseEquipmentMappings = emptyMap(),
            previouslyProgrammedExercises = emptyList(),
            allExercises = emptyList(),
            userEquipment = emptyList(),
            userExercisePreferences = emptyList()
        )
    }
}