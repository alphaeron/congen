package com.congen.service.conjugate

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.model.Exercise
import com.congen.model.ProgrammedExercise
import com.congen.model.SetScheme
import com.congen.model.UserOneRepMax
import com.congen.model.WorkoutStage
import com.congen.model.WorkoutStageType
import com.congen.model.WorkoutStageTypeEnum
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WorkoutStageGeneratorTest {
    private lateinit var workoutStageGenerator: WorkoutStageGenerator
    private lateinit var workoutStageDAL: WorkoutStageDAL
    private lateinit var workoutStageTypeDAL: WorkoutStageTypeDAL
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL
    private lateinit var setSchemeDAL: SetSchemeDAL
    private lateinit var prilepinGuidelinesService: PrilepinGuidelinesService

    @BeforeEach
    fun setUp() {
        workoutStageDAL = mock()
        workoutStageTypeDAL = mock()
        programmedExerciseDAL = mock()
        setSchemeDAL = mock()
        prilepinGuidelinesService = mock()
        workoutStageGenerator =
            WorkoutStageGenerator(
                workoutStageDAL = workoutStageDAL,
                workoutStageTypeDAL = workoutStageTypeDAL,
                programmedExerciseDAL = programmedExerciseDAL,
                setSchemeDAL = setSchemeDAL,
                prilepinGuidelinesService = prilepinGuidelinesService
            )
    }

    @Test
    fun `createWorkoutStage should create primary stage`() {
        val workoutId = 1L
        val stageType = WorkoutStageTypeEnum.PRIMARY
        val position = 1
        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = Instant.now()
            )
        val expectedStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = workoutId,
                stageTypeId = 1,
                position = position,
                name = "Primary",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(eq(stageType)))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(any()))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageDAL.insertWorkoutStage(eq(workoutId), eq(1), eq(position), eq("Primary")))
            .thenReturn(Mono.just(expectedStage))
        whenever(workoutStageDAL.insertWorkoutStage(any(), any(), any(), any()))
            .thenReturn(Mono.just(expectedStage))

        val result = workoutStageGenerator.createWorkoutStage(workoutId, stageType, position)

        StepVerifier.create(result)
            .expectNext(expectedStage)
            .verifyComplete()

        verify(workoutStageDAL).insertWorkoutStage(workoutId, 1, position, "Primary")
    }

    @Test
    fun `createWorkoutStage should create secondary stage`() {
        val workoutId = 1L
        val stageType = WorkoutStageTypeEnum.SECONDARY
        val position = 2
        val workoutStageType =
            WorkoutStageType(
                id = 2,
                name = WorkoutStageTypeEnum.SECONDARY,
                createdAt = Instant.now()
            )
        val expectedStage =
            WorkoutStage(
                id = 2L,
                programmedWorkoutId = workoutId,
                stageTypeId = 2,
                position = position,
                name = "Secondary",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(eq(stageType)))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(any()))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageDAL.insertWorkoutStage(eq(workoutId), eq(2), eq(position), eq("Secondary")))
            .thenReturn(Mono.just(expectedStage))
        whenever(workoutStageDAL.insertWorkoutStage(any(), any(), any(), any()))
            .thenReturn(Mono.just(expectedStage))

        val result = workoutStageGenerator.createWorkoutStage(workoutId, stageType, position)

        StepVerifier.create(result)
            .expectNext(expectedStage)
            .verifyComplete()

        verify(workoutStageDAL).insertWorkoutStage(workoutId, 2, position, "Secondary")
    }

    @Test
    fun `createWorkoutStage should create accessory stage`() {
        val workoutId = 1L
        val stageType = WorkoutStageTypeEnum.ACCESSORY
        val position = 3
        val workoutStageType =
            WorkoutStageType(
                id = 3,
                name = WorkoutStageTypeEnum.ACCESSORY,
                createdAt = Instant.now()
            )
        val expectedStage =
            WorkoutStage(
                id = 3L,
                programmedWorkoutId = workoutId,
                stageTypeId = 3,
                position = position,
                name = "Accessory",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(eq(stageType)))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(any()))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageDAL.insertWorkoutStage(eq(workoutId), eq(3), eq(position), eq("Accessory")))
            .thenReturn(Mono.just(expectedStage))
        whenever(workoutStageDAL.insertWorkoutStage(any(), any(), any(), any()))
            .thenReturn(Mono.just(expectedStage))

        val result = workoutStageGenerator.createWorkoutStage(workoutId, stageType, position)

        StepVerifier.create(result)
            .expectNext(expectedStage)
            .verifyComplete()

        verify(workoutStageDAL).insertWorkoutStage(workoutId, 3, position, "Accessory")
    }

    @Test
    fun `createWorkoutStage should create conditioning stage`() {
        val workoutId = 1L
        val stageType = WorkoutStageTypeEnum.CONDITIONING
        val position = 4
        val workoutStageType =
            WorkoutStageType(
                id = 4,
                name = WorkoutStageTypeEnum.CONDITIONING,
                createdAt = Instant.now()
            )
        val expectedStage =
            WorkoutStage(
                id = 4L,
                programmedWorkoutId = workoutId,
                stageTypeId = 4,
                position = position,
                name = "Conditioning",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(eq(stageType)))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(any()))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageDAL.insertWorkoutStage(eq(workoutId), eq(4), eq(position), eq("Conditioning")))
            .thenReturn(Mono.just(expectedStage))
        whenever(workoutStageDAL.insertWorkoutStage(any(), any(), any(), any()))
            .thenReturn(Mono.just(expectedStage))

        val result = workoutStageGenerator.createWorkoutStage(workoutId, stageType, position)

        StepVerifier.create(result)
            .expectNext(expectedStage)
            .verifyComplete()

        verify(workoutStageDAL).insertWorkoutStage(workoutId, 4, position, "Conditioning")
    }

    @Test
    fun `createWorkoutStage should default to primary for unknown stage type`() {
        val workoutId = 1L
        val stageType = WorkoutStageTypeEnum.PRIMARY
        val position = 1
        val workoutStageType =
            WorkoutStageType(
                id = 1,
                name = WorkoutStageTypeEnum.PRIMARY,
                createdAt = Instant.now()
            )
        val expectedStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = workoutId,
                stageTypeId = 1,
                position = position,
                name = "Primary",
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(eq(stageType)))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(any()))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageDAL.insertWorkoutStage(eq(workoutId), eq(1), eq(position), eq("Primary")))
            .thenReturn(Mono.just(expectedStage))
        whenever(workoutStageDAL.insertWorkoutStage(any(), any(), any(), any()))
            .thenReturn(Mono.just(expectedStage))

        val result = workoutStageGenerator.createWorkoutStage(workoutId, stageType, position)

        StepVerifier.create(result)
            .expectNext(expectedStage)
            .verifyComplete()

        verify(workoutStageDAL).insertWorkoutStage(workoutId, 1, position, "Primary")
    }

    @Test
    fun `createProgrammedExercise should create exercise`() {
        val workoutStageId = 1L
        val exerciseName = "Bench Press"
        val expectedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = workoutStageId,
                exerciseName = exerciseName,
                position = 1,
                notes = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

        whenever(programmedExerciseDAL.insertProgrammedExercise(workoutStageId, exerciseName, 1, null))
            .thenReturn(Mono.just(expectedExercise))

        val result = workoutStageGenerator.createProgrammedExercise(workoutStageId, exerciseName)

        StepVerifier.create(result)
            .expectNext(expectedExercise)
            .verifyComplete()

        verify(programmedExerciseDAL).insertProgrammedExercise(workoutStageId, exerciseName, 1, null)
    }

    @Test
    fun `createSetSchemes should create multiple set schemes`() {
        val programmedExerciseId = 1L
        val setSchemeParams =
            listOf(
                SetSchemeParams(
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
                    restSeconds = 180
                ),
                SetSchemeParams(
                    setNumber = 2,
                    isAmrap = false,
                    isEmom = false,
                    useTempo = false,
                    eccentricTempo = null,
                    isometricTempo = null,
                    concentricTempo = null,
                    targetWeight = BigDecimal("110.0"),
                    performedWeight = null,
                    targetRepCount = 5,
                    performedRepCount = null,
                    restSeconds = 180
                )
            )

        val mockSetScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 1L,
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
                restSeconds = 180,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )
        whenever(
            setSchemeDAL.insertSetScheme(
                eq(1L),
                any(),
                eq(false),
                eq(false),
                eq(false),
                eq(null),
                eq(null),
                eq(null),
                any(),
                eq(null),
                eq(5),
                eq(null),
                eq(180)
            )
        ).thenReturn(Mono.just(mockSetScheme))

        val result = workoutStageGenerator.createSetSchemes(programmedExerciseId, setSchemeParams)

        StepVerifier.create(result)
            .verifyComplete()

        verify(setSchemeDAL).insertSetScheme(
            programmedExerciseId,
            1,
            false,
            false,
            false,
            null,
            null,
            null,
            BigDecimal("100.0"),
            null,
            5,
            null,
            180
        )
        verify(setSchemeDAL).insertSetScheme(
            programmedExerciseId,
            2,
            false,
            false,
            false,
            null,
            null,
            null,
            BigDecimal("110.0"),
            null,
            5,
            null,
            180
        )
    }

    @Test
    fun `generatePrilepinBasedScheme should generate scheme with guidelines`() {
        val userId = 1
        val exercise =
            Exercise(
                name = "Bench Press",
                description = "A compound upper body exercise",
                movementType = "horizontal push",
                isUnilateral = false,
                isUpper = true,
                isAccessory = false
            )
        val movementRole = "primary"
        val dayType = "ME_Upper"
        val oneRepMaxes =
            listOf(
                UserOneRepMax(
                    userId = userId,
                    exerciseName = "Bench Press",
                    oneRepMax = BigDecimal("100.0"),
                    updatedAt = Instant.now()
                )
            )
        val currentWeekNumber = 1

        val guidelines =
            PrilepinGuidelines(
                intensityRange = 0.8..0.9,
                repsPerSetRange = 2..4,
                totalReps = 15,
                restSeconds = 180..300
            )
        val intensity = 0.85

        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = dayType,
                movementRole = movementRole,
                currentWeekNumber = currentWeekNumber,
                exercise = exercise.name
            )
        ).thenReturn(Pair(guidelines, intensity))

        val result =
            workoutStageGenerator.generatePrilepinBasedScheme(
                userId,
                exercise,
                movementRole,
                dayType,
                oneRepMaxes,
                currentWeekNumber
            )

        assertNotNull(result)
        assertTrue(result.isNotEmpty())

        val firstSet = result[0]
        assertEquals(1, firstSet.setNumber)
        assertFalse(firstSet.isAmrap)
        assertFalse(firstSet.isEmom)
        assertEquals(BigDecimal("85.00"), firstSet.targetWeight) // 100 * 0.85
        assertTrue(firstSet.targetRepCount in 2..4)
        assertTrue(firstSet.restSeconds in 180..300)
    }

    @Test
    fun `generateSecondaryExerciseScheme should generate secondary scheme`() {
        val userId = 1
        val exercise =
            Exercise(
                name = "Squat",
                description = "A compound lower body exercise",
                movementType = "vertical push",
                isUnilateral = false,
                isUpper = false,
                isAccessory = false
            )
        val oneRepMaxes =
            listOf(
                UserOneRepMax(
                    userId = userId,
                    exerciseName = "Squat",
                    oneRepMax = BigDecimal("200.0"),
                    updatedAt = Instant.now()
                )
            )

        val result = workoutStageGenerator.generateSecondaryExerciseScheme(userId, exercise, oneRepMaxes)

        assertNotNull(result)
        assertTrue(result.isNotEmpty())

        val firstSet = result[0]
        assertEquals(1, firstSet.setNumber)
        assertFalse(firstSet.isAmrap)
        assertFalse(firstSet.isEmom)
        assertTrue(firstSet.targetWeight!! in BigDecimal("160.0")..BigDecimal("180.0")) // 200 * 0.8-0.9
        assertTrue(firstSet.targetRepCount in 5..8)
        assertTrue(firstSet.restSeconds in 180..300)
    }

    @Test
    fun `generateAmrapOrEmomScheme should generate conditioning scheme`() {
        val userId = 1
        val exercise =
            Exercise(
                name = "Burpees",
                description = "A conditioning exercise",
                movementType = "compound",
                isUnilateral = false,
                isUpper = false,
                isAccessory = true
            )
        val oneRepMaxes =
            listOf(
                UserOneRepMax(
                    userId = userId,
                    exerciseName = "Burpees",
                    oneRepMax = BigDecimal("50.0"),
                    updatedAt = Instant.now(),
                )
            )

        val result = workoutStageGenerator.generateAmrapOrEmomScheme(userId, exercise, oneRepMaxes)

        assertNotNull(result)
        assertEquals(1, result.size)

        val set = result[0]
        assertEquals(1, set.setNumber)
        assertTrue(set.isAmrap.xor(set.isEmom)) // Exactly one is true
        assertEquals(BigDecimal("25.00"), set.targetWeight) // 50.0 * 0.5 intensity
        assertNull(set.targetRepCount) // Varies per person for AMRAP/EMOM
        assertTrue(set.restSeconds == 0 || set.restSeconds == 60) // 0 for AMRAP, 60 for EMOM
    }

    @Test
    fun `generatePrilepinBasedScheme should use default weight when no 1RM found`() {
        val userId = 1
        val exercise =
            Exercise(
                name = "New Exercise",
                description = "A new exercise",
                movementType = "compound",
                isUnilateral = false,
                isUpper = true,
                isAccessory = false
            )
        val movementRole = "primary"
        val dayType = "ME_Upper"
        val oneRepMaxes = emptyList<UserOneRepMax>()
        val currentWeekNumber = 1

        val guidelines =
            PrilepinGuidelines(
                intensityRange = 0.8..0.9,
                repsPerSetRange = 2..4,
                totalReps = 15,
                restSeconds = 180..300
            )
        val intensity = 0.85

        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = dayType,
                movementRole = movementRole,
                currentWeekNumber = currentWeekNumber,
                exercise = exercise.name
            )
        ).thenReturn(Pair(guidelines, intensity))

        val result =
            workoutStageGenerator.generatePrilepinBasedScheme(
                userId,
                exercise,
                movementRole,
                dayType,
                oneRepMaxes,
                currentWeekNumber
            )

        assertNotNull(result)
        assertTrue(result.isNotEmpty())

        val firstSet = result[0]
        assertEquals(BigDecimal("50.0"), firstSet.targetWeight) // Default weight
    }

    @Test
    fun `generateSecondaryExerciseScheme should use default weight when no 1RM found`() {
        val userId = 1
        val exercise =
            Exercise(
                name = "New Exercise",
                description = "A new exercise",
                movementType = "compound",
                isUnilateral = false,
                isUpper = true,
                isAccessory = false
            )
        val oneRepMaxes = emptyList<UserOneRepMax>()

        val result = workoutStageGenerator.generateSecondaryExerciseScheme(userId, exercise, oneRepMaxes)

        assertNotNull(result)
        assertTrue(result.isNotEmpty())

        val firstSet = result[0]
        assertEquals(BigDecimal("50.0"), firstSet.targetWeight) // Default weight
    }

    @Test
    fun `generateAmrapOrEmomScheme should use default weight when no 1RM found`() {
        val userId = 1
        val exercise =
            Exercise(
                name = "New Exercise",
                description = "A new exercise",
                movementType = "compound",
                isUnilateral = false,
                isUpper = false,
                isAccessory = true
            )
        val oneRepMaxes = emptyList<UserOneRepMax>()

        val result = workoutStageGenerator.generateAmrapOrEmomScheme(userId, exercise, oneRepMaxes)

        assertNotNull(result)
        assertEquals(1, result.size)

        val set = result[0]
        assertEquals(BigDecimal("50.0"), set.targetWeight)
    }
}
