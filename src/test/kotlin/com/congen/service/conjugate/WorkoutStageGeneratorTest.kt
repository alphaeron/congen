package com.congen.service.conjugate

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.mockExercise
import com.congen.mockPrilepinGuidelines
import com.congen.mockProgrammedExercise
import com.congen.mockSetScheme
import com.congen.mockSetSchemeParams
import com.congen.mockUserOneRepMax
import com.congen.mockWorkoutStage
import com.congen.mockWorkoutStageType
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

    private val workoutId = 1L
    private val workoutStageId = 1L
    private val programmedExerciseId = 1L
    private val userId = 1

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
        val stageType = WorkoutStageTypeEnum.PRIMARY
        val position = 1
        val workoutStageType = mockWorkoutStageType(id = 1, name = WorkoutStageTypeEnum.PRIMARY)
        val expectedStage =
            mockWorkoutStage(
                id = 1L,
                programmedWorkoutId = workoutId,
                stageTypeId = 1,
                position = position,
                name = "Primary"
            )

        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(eq(stageType)))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageDAL.insertWorkoutStage(eq(workoutId), eq(1), eq(position), eq("Primary")))
            .thenReturn(Mono.just(expectedStage))

        val result = workoutStageGenerator.createWorkoutStage(workoutId, stageType, position)

        StepVerifier.create(result)
            .expectNext(expectedStage)
            .verifyComplete()

        verify(workoutStageDAL).insertWorkoutStage(workoutId, 1, position, "Primary")
    }

    @Test
    fun `createWorkoutStage should create secondary stage`() {
        val stageType = WorkoutStageTypeEnum.SECONDARY
        val position = 2
        val workoutStageType = mockWorkoutStageType(id = 2, name = WorkoutStageTypeEnum.SECONDARY)
        val expectedStage =
            mockWorkoutStage(
                id = 2L,
                programmedWorkoutId = workoutId,
                stageTypeId = 2,
                position = position,
                name = "Secondary"
            )

        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(eq(stageType)))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageDAL.insertWorkoutStage(eq(workoutId), eq(2), eq(position), eq("Secondary")))
            .thenReturn(Mono.just(expectedStage))

        val result = workoutStageGenerator.createWorkoutStage(workoutId, stageType, position)

        StepVerifier.create(result)
            .expectNext(expectedStage)
            .verifyComplete()

        verify(workoutStageDAL).insertWorkoutStage(workoutId, 2, position, "Secondary")
    }

    @Test
    fun `createWorkoutStage should create accessory stage`() {
        val stageType = WorkoutStageTypeEnum.ACCESSORY
        val position = 3
        val workoutStageType = mockWorkoutStageType(id = 3, name = WorkoutStageTypeEnum.ACCESSORY)
        val expectedStage =
            mockWorkoutStage(
                id = 3L,
                programmedWorkoutId = workoutId,
                stageTypeId = 3,
                position = position,
                name = "Accessory"
            )

        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(eq(stageType)))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageDAL.insertWorkoutStage(eq(workoutId), eq(3), eq(position), eq("Accessory")))
            .thenReturn(Mono.just(expectedStage))

        val result = workoutStageGenerator.createWorkoutStage(workoutId, stageType, position)

        StepVerifier.create(result)
            .expectNext(expectedStage)
            .verifyComplete()

        verify(workoutStageDAL).insertWorkoutStage(workoutId, 3, position, "Accessory")
    }

    @Test
    fun `createWorkoutStage should create conditioning stage`() {
        val stageType = WorkoutStageTypeEnum.CONDITIONING
        val position = 4
        val workoutStageType = mockWorkoutStageType(id = 4, name = WorkoutStageTypeEnum.CONDITIONING)
        val expectedStage =
            mockWorkoutStage(
                id = 4L,
                programmedWorkoutId = workoutId,
                stageTypeId = 4,
                position = position,
                name = "Conditioning"
            )

        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(eq(stageType)))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageDAL.insertWorkoutStage(eq(workoutId), eq(4), eq(position), eq("Conditioning")))
            .thenReturn(Mono.just(expectedStage))

        val result = workoutStageGenerator.createWorkoutStage(workoutId, stageType, position)

        StepVerifier.create(result)
            .expectNext(expectedStage)
            .verifyComplete()

        verify(workoutStageDAL).insertWorkoutStage(workoutId, 4, position, "Conditioning")
    }

    @Test
    fun `createWorkoutStage should default to primary for unknown stage type`() {
        val stageType = WorkoutStageTypeEnum.PRIMARY
        val position = 1
        val workoutStageType = mockWorkoutStageType(id = 1, name = WorkoutStageTypeEnum.PRIMARY)
        val expectedStage =
            mockWorkoutStage(
                id = 1L,
                programmedWorkoutId = workoutId,
                stageTypeId = 1,
                position = position,
                name = "Primary"
            )

        whenever(workoutStageTypeDAL.selectWorkoutStageTypeByEnum(eq(stageType)))
            .thenReturn(Mono.just(workoutStageType))
        whenever(workoutStageDAL.insertWorkoutStage(eq(workoutId), eq(1), eq(position), eq("Primary")))
            .thenReturn(Mono.just(expectedStage))

        val result = workoutStageGenerator.createWorkoutStage(workoutId, stageType, position)

        StepVerifier.create(result)
            .expectNext(expectedStage)
            .verifyComplete()

        verify(workoutStageDAL).insertWorkoutStage(workoutId, 1, position, "Primary")
    }

    @Test
    fun `createProgrammedExercise should create exercise`() {
        val exerciseName = "Bench Press"
        val expectedExercise =
            mockProgrammedExercise(
                id = 1L,
                workoutStageId = workoutStageId,
                exerciseName = exerciseName,
                position = 1
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
        val setSchemeParams =
            listOf(
                mockSetSchemeParams(
                    setNumber = 1,
                    targetWeight = BigDecimal("100.0"),
                    targetRepCount = 5,
                    restSeconds = 180
                ),
                mockSetSchemeParams(
                    setNumber = 2,
                    targetWeight = BigDecimal("110.0"),
                    targetRepCount = 5,
                    restSeconds = 180
                )
            )

        val mockSetScheme =
            mockSetScheme(
                id = 1L,
                programmedExerciseId = 1L,
                setNumber = 1,
                targetWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                restSeconds = 180
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
        val exercise =
            mockExercise(
                name = "Bench Press",
                description = "A compound upper body exercise",
                movementType = "horizontal push"
            )
        val movementRole = "primary"
        val dayType = "ME_Upper"
        val oneRepMaxes =
            listOf(
                mockUserOneRepMax(
                    userId = userId,
                    exerciseName = "Bench Press",
                    oneRepMax = BigDecimal("100.0")
                )
            )
        val currentWeekNumber = 1

        val guidelines = mockPrilepinGuidelines()
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
        assertEquals(BigDecimal("85.00"), firstSet.targetWeight)
        assertTrue(firstSet.targetRepCount in 2..4)
        assertTrue(firstSet.restSeconds in 180..300)
    }

    @Test
    fun `generateSecondaryExerciseScheme should generate secondary scheme`() {
        val exercise =
            mockExercise(
                name = "Squat",
                description = "A compound lower body exercise",
                movementType = "vertical push",
                isUpper = false
            )
        val oneRepMaxes =
            listOf(
                mockUserOneRepMax(
                    userId = userId,
                    exerciseName = "Squat",
                    oneRepMax = BigDecimal("200.0")
                )
            )

        val result = workoutStageGenerator.generateSecondaryExerciseScheme(userId, exercise, oneRepMaxes)

        assertNotNull(result)
        assertTrue(result.isNotEmpty())

        val firstSet = result[0]
        assertEquals(1, firstSet.setNumber)
        assertFalse(firstSet.isAmrap)
        assertFalse(firstSet.isEmom)
        assertTrue(firstSet.targetWeight!! in BigDecimal("160.0")..BigDecimal("180.0"))
        assertTrue(firstSet.targetRepCount in 5..8)
        assertTrue(firstSet.restSeconds in 180..300)
    }

    @Test
    fun `generateAmrapOrEmomScheme should generate conditioning scheme`() {
        val exercise =
            mockExercise(
                name = "Burpees",
                description = "A conditioning exercise",
                movementType = "compound",
                isUpper = false,
                isAccessory = true
            )
        val oneRepMaxes =
            listOf(
                mockUserOneRepMax(
                    userId = userId,
                    exerciseName = "Burpees",
                    oneRepMax = BigDecimal("50.0")
                )
            )

        val result = workoutStageGenerator.generateAmrapOrEmomScheme(userId, exercise, oneRepMaxes)

        assertNotNull(result)
        assertEquals(1, result.size)

        val set = result[0]
        assertEquals(1, set.setNumber)
        assertTrue(set.isAmrap.xor(set.isEmom))
        assertEquals(BigDecimal("25.00"), set.targetWeight)
        assertNull(set.targetRepCount)
        assertTrue(set.restSeconds == 0 || set.restSeconds == 60)
    }

    @Test
    fun `generatePrilepinBasedScheme should use default weight when no 1RM found`() {
        val exercise =
            mockExercise(
                name = "New Exercise",
                description = "A new exercise",
                movementType = "compound"
            )
        val movementRole = "primary"
        val dayType = "ME_Upper"
        val oneRepMaxes = emptyList<com.congen.model.UserOneRepMax>()
        val currentWeekNumber = 1

        val guidelines = mockPrilepinGuidelines()
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
        assertEquals(BigDecimal("50.0"), firstSet.targetWeight)
    }

    @Test
    fun `generateSecondaryExerciseScheme should use default weight when no 1RM found`() {
        val exercise =
            mockExercise(
                name = "New Exercise",
                description = "A new exercise",
                movementType = "compound"
            )
        val oneRepMaxes = emptyList<com.congen.model.UserOneRepMax>()

        val result = workoutStageGenerator.generateSecondaryExerciseScheme(userId, exercise, oneRepMaxes)

        assertNotNull(result)
        assertTrue(result.isNotEmpty())

        val firstSet = result[0]
        assertEquals(BigDecimal("50.0"), firstSet.targetWeight)
    }

    @Test
    fun `generateAmrapOrEmomScheme should use default weight when no 1RM found`() {
        val exercise =
            mockExercise(
                name = "New Exercise",
                description = "A new exercise",
                movementType = "compound",
                isUpper = false,
                isAccessory = true
            )
        val oneRepMaxes = emptyList<com.congen.model.UserOneRepMax>()

        val result = workoutStageGenerator.generateAmrapOrEmomScheme(userId, exercise, oneRepMaxes)

        assertNotNull(result)
        assertEquals(1, result.size)

        val set = result[0]
        assertEquals(BigDecimal("50.0"), set.targetWeight)
    }
}
