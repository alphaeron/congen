package com.congen.service.conjugate

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockExercise
import com.congen.mockPrilepinGuidelines
import com.congen.mockProgrammedExercise
import com.congen.mockSetScheme
import com.congen.mockSetSchemeParams
import com.congen.mockUserOneRepMax
import com.congen.mockUserWeightUnitPreference
import com.congen.mockWorkoutStage
import com.congen.mockWorkoutStageType
import com.congen.model.MovementType
import com.congen.model.WeightUnit
import com.congen.model.WorkoutStageTypeEnum
import com.congen.service.SetSchemeService
import com.congen.service.UnitConversionService
import com.congen.service.WeightSelectionService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal

class WorkoutStageGeneratorTest {
    private lateinit var workoutStageGenerator: WorkoutStageGenerator
    private lateinit var workoutStageDAL: WorkoutStageDAL
    private lateinit var workoutStageTypeDAL: WorkoutStageTypeDAL
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL
    private lateinit var setSchemeDAL: SetSchemeDAL
    private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL
    private lateinit var unitConversionService: UnitConversionService
    private lateinit var setSchemeService: SetSchemeService
    private lateinit var prilepinGuidelinesService: PrilepinGuidelinesService
    private lateinit var weightSelectionService: WeightSelectionService
    private lateinit var bandWeightService: BandWeightService

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
        userWeightUnitPreferenceDAL = mock()
        unitConversionService = mock()
        setSchemeService = mock()
        prilepinGuidelinesService = mock()
        weightSelectionService = mock()
        bandWeightService = mock()

        val exerciseDAL = mock<com.congen.dal.ExerciseDAL>()
        val exerciseEquipmentDAL = mock<com.congen.dal.ExerciseEquipmentDAL>()
        val exerciseMuscleDAL = mock<com.congen.dal.ExerciseMuscleDAL>()
        val userOneRepMaxDAL = mock<com.congen.dal.UserOneRepMaxDAL>()

        workoutStageGenerator =
            WorkoutStageGenerator(
                workoutStageDAL = workoutStageDAL,
                workoutStageTypeDAL = workoutStageTypeDAL,
                programmedExerciseDAL = programmedExerciseDAL,
                setSchemeDAL = setSchemeDAL,
                userWeightUnitPreferenceDAL = userWeightUnitPreferenceDAL,
                unitConversionService = unitConversionService,
                setSchemeService = setSchemeService,
                prilepinGuidelinesService = prilepinGuidelinesService,
                weightSelectionService = weightSelectionService,
                bandWeightService = bandWeightService,
                exerciseMatchingService = ExerciseMatchingService(ReferenceExerciseDetector()),
                exerciseDAL = exerciseDAL,
                exerciseEquipmentDAL = exerciseEquipmentDAL,
                exerciseMuscleDAL = exerciseMuscleDAL,
                userOneRepMaxDAL = userOneRepMaxDAL
            )

        // Mock DAL methods that return null by default
        whenever(workoutStageDAL.selectWorkoutStageByWorkoutIdAndPosition(anyLong(), anyInt()))
            .thenReturn(Mono.error(NoResultsFoundException("No workout stage found")))
        whenever(programmedExerciseDAL.selectProgrammedExerciseByStageIdAndExerciseName(anyLong(), any()))
            .thenReturn(Mono.error(NoResultsFoundException("No programmed exercise found")))
        whenever(setSchemeDAL.selectSetSchemesByProgrammedExerciseId(anyLong()))
            .thenReturn(Mono.just(emptyList()))

        // Mock exercise DAL methods used in getTargetWeight
        whenever(exerciseDAL.selectExercises())
            .thenReturn(Mono.just(emptyList()))
        whenever(exerciseEquipmentDAL.selectAllExerciseEquipment())
            .thenReturn(Mono.just(emptyList()))
        whenever(exerciseMuscleDAL.selectAllExerciseMuscle())
            .thenReturn(Mono.just(emptyList()))
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
        val exerciseName = "Bench Press"
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
                    targetWeight = BigDecimal("95.0"),
                    targetRepCount = 5,
                    restSeconds = 180
                )
            )

        // Mock setSchemeService.createSetScheme
        val mockSetScheme =
            mockSetScheme(
                id = 1L,
                programmedExerciseId = programmedExerciseId,
                setNumber = 1,
                targetWeight = BigDecimal("100.0"),
                targetRepCount = 5,
                restSeconds = 180,
                band = null
            )
        whenever(
            setSchemeService.createSetScheme(
                anyLong(),
                anyInt(),
                anyBoolean(),
                anyBoolean(),
                anyBoolean(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(Mono.just(mockSetScheme))

        // Mock user weight unit preference (default to KG)
        whenever(
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(eq(userId), eq(exerciseName))
        ).thenReturn(Mono.error(NoResultsFoundException("No preference found")))

        val result = workoutStageGenerator.createSetSchemes(userId, programmedExerciseId, exerciseName, setSchemeParams)

        StepVerifier.create(result)
            .verifyComplete()

        verify(setSchemeService, times(2)).createSetScheme(
            anyLong(),
            anyInt(),
            anyBoolean(),
            anyBoolean(),
            anyBoolean(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `generatePrilepinBasedScheme should generate scheme with guidelines`() {
        val exercise =
            mockExercise(
                name = "Bench Press",
                description = "A compound upper body exercise",
                movementType = MovementType.HORIZONTAL_PUSH
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
                currentWeekNumber = currentWeekNumber
            )
        ).thenReturn(Pair(guidelines, intensity))

        // Mock weight unit preference
        val weightUnitPreference = mockUserWeightUnitPreference(userId, "Bench Press", WeightUnit.KG)
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, "Bench Press"))
            .thenReturn(Mono.just(weightUnitPreference))

        // Mock weight selection service - this is called by getTargetWeight method
        whenever(weightSelectionService.roundWeightForExercise(eq("Bench Press"), any(), eq(WeightUnit.KG)))
            .thenReturn(Mono.just(BigDecimal("85.00")))

        val result =
            workoutStageGenerator.generatePrilepinBasedScheme(
                exercise,
                movementRole,
                dayType,
                oneRepMaxes,
                currentWeekNumber,
                userId
            )

        StepVerifier.create(result)
            .expectNextMatches { schemes ->
                schemes.isNotEmpty() && schemes[0].setNumber == 1 &&
                    !schemes[0].isAmrap && !schemes[0].isEmom &&
                    schemes[0].targetWeight == BigDecimal("85.00") &&
                    schemes[0].targetRepCount in 2..4 &&
                    schemes[0].restSeconds in 180..300
            }
            .verifyComplete()
    }

    @Test
    fun `generateSecondaryExerciseScheme should generate secondary scheme`() {
        val exercise =
            mockExercise(
                name = "Squat",
                description = "A compound lower body exercise",
                movementType = MovementType.SQUAT,
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

        // Mock weight unit preference
        val weightUnitPreference = mockUserWeightUnitPreference(userId, "Squat", WeightUnit.KG)
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, "Squat"))
            .thenReturn(Mono.just(weightUnitPreference))

        // Mock weight selection service - this is called by getTargetWeight method
        whenever(weightSelectionService.roundWeightForExercise(eq("Squat"), any(), eq(WeightUnit.KG)))
            .thenReturn(Mono.just(BigDecimal("170.00")))

        val result = workoutStageGenerator.generateSecondaryExerciseScheme(exercise, oneRepMaxes, userId)

        StepVerifier.create(result)
            .expectNextMatches { schemes ->
                schemes.isNotEmpty() && schemes[0].setNumber == 1 &&
                    !schemes[0].isAmrap && !schemes[0].isEmom &&
                    schemes[0].targetWeight!! in BigDecimal("160.0")..BigDecimal("180.0") &&
                    schemes[0].targetRepCount in 5..8 &&
                    schemes[0].restSeconds in 180..300
            }
            .verifyComplete()
    }

    @Test
    fun `generateAmrapOrEmomScheme should generate conditioning scheme`() {
        val exercise =
            mockExercise(
                name = "Burpees",
                description = "A conditioning exercise",
                movementType = MovementType.PLYOMETRIC,
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

        // Mock weight unit preference
        val weightUnitPreference = mockUserWeightUnitPreference(userId, "Burpees", WeightUnit.KG)
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, "Burpees"))
            .thenReturn(Mono.just(weightUnitPreference))

        // Mock weight selection service - this is called by getTargetWeight method
        whenever(weightSelectionService.roundWeightForExercise(eq("Burpees"), any(), eq(WeightUnit.KG)))
            .thenReturn(Mono.just(BigDecimal("25.00")))

        val result = workoutStageGenerator.generateAmrapOrEmomScheme(exercise, oneRepMaxes, userId)

        StepVerifier.create(result)
            .expectNextMatches { schemes ->
                schemes.size == 1 && schemes[0].setNumber == 3 &&
                    schemes[0].isAmrap.xor(schemes[0].isEmom) &&
                    schemes[0].targetWeight == BigDecimal("25.00") &&
                    schemes[0].targetRepCount == null &&
                    schemes[0].restSeconds == 60
            }
            .verifyComplete()
    }

    @Test
    fun `generatePrilepinBasedScheme should use default weight when no 1RM found`() {
        val exercise =
            mockExercise(
                name = "New Exercise",
                description = "A new exercise",
                movementType = MovementType.HORIZONTAL_PUSH
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
                currentWeekNumber = currentWeekNumber
            )
        ).thenReturn(Pair(guidelines, intensity))

        // Mock weight unit preference
        val weightUnitPreference = mockUserWeightUnitPreference(userId, "New Exercise", WeightUnit.KG)
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, "New Exercise"))
            .thenReturn(Mono.just(weightUnitPreference))

        // Mock weight selection service - this is called by getTargetWeight method
        whenever(weightSelectionService.roundWeightForExercise(eq("New Exercise"), any(), eq(WeightUnit.KG)))
            .thenReturn(Mono.just(BigDecimal("50.00")))

        val result =
            workoutStageGenerator.generatePrilepinBasedScheme(
                exercise,
                movementRole,
                dayType,
                oneRepMaxes,
                currentWeekNumber,
                userId
            )

        StepVerifier.create(result)
            .expectNextMatches { schemes ->
                schemes.isNotEmpty() && schemes[0].setNumber == 1 &&
                    !schemes[0].isAmrap && !schemes[0].isEmom &&
                    schemes[0].targetWeight == BigDecimal("50.00") &&
                    schemes[0].targetRepCount in 2..4 &&
                    schemes[0].restSeconds in 180..300
            }
            .verifyComplete()
    }

    @Test
    fun `generateSecondaryExerciseScheme should use default weight when no 1RM found`() {
        val exercise =
            mockExercise(
                name = "New Secondary Exercise",
                description = "A new secondary exercise",
                movementType = MovementType.SQUAT,
                isUpper = false
            )
        val oneRepMaxes = emptyList<com.congen.model.UserOneRepMax>()

        // Mock weight unit preference
        val weightUnitPreference = mockUserWeightUnitPreference(userId, "New Secondary Exercise", WeightUnit.KG)
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, "New Secondary Exercise"))
            .thenReturn(Mono.just(weightUnitPreference))

        // Mock weight selection service - this is called by getTargetWeight method
        whenever(weightSelectionService.roundWeightForExercise(eq("New Secondary Exercise"), any(), eq(WeightUnit.KG)))
            .thenReturn(Mono.just(BigDecimal("50.00")))

        val result = workoutStageGenerator.generateSecondaryExerciseScheme(exercise, oneRepMaxes, userId)

        StepVerifier.create(result)
            .expectNextMatches { schemes ->
                schemes.isNotEmpty() && schemes[0].setNumber == 1 &&
                    !schemes[0].isAmrap && !schemes[0].isEmom &&
                    schemes[0].targetWeight == BigDecimal("50.00") &&
                    schemes[0].targetRepCount in 5..8 &&
                    schemes[0].restSeconds in 180..300
            }
            .verifyComplete()
    }

    @Test
    fun `generateAmrapOrEmomScheme should use default weight when no 1RM found`() {
        val exercise =
            mockExercise(
                name = "New Conditioning Exercise",
                description = "A new conditioning exercise",
                movementType = MovementType.PLYOMETRIC,
                isUpper = false,
                isAccessory = true
            )
        val oneRepMaxes = emptyList<com.congen.model.UserOneRepMax>()

        // Mock weight unit preference
        val weightUnitPreference = mockUserWeightUnitPreference(userId, "New Conditioning Exercise", WeightUnit.KG)
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, "New Conditioning Exercise"))
            .thenReturn(Mono.just(weightUnitPreference))

        // Mock weight selection service - this is called by getTargetWeight method
        whenever(weightSelectionService.roundWeightForExercise(eq("New Conditioning Exercise"), any(), eq(WeightUnit.KG)))
            .thenReturn(Mono.just(BigDecimal("25.00")))

        val result = workoutStageGenerator.generateAmrapOrEmomScheme(exercise, oneRepMaxes, userId)

        StepVerifier.create(result)
            .expectNextMatches { schemes ->
                schemes.size == 1 && schemes[0].setNumber == 3 &&
                    schemes[0].isAmrap.xor(schemes[0].isEmom) &&
                    schemes[0].targetWeight == BigDecimal("25.00") &&
                    schemes[0].targetRepCount == null &&
                    schemes[0].restSeconds == 60
            }
            .verifyComplete()
    }

    @Test
    fun `generatePrilepinBasedScheme should use band weight calculations for DE exercises`() {
        val exercise =
            mockExercise(
                name = "Bench Press",
                description = "A compound upper body exercise",
                movementType = MovementType.HORIZONTAL_PUSH
            )
        val movementRole = "primary"
        val dayType = "DE_Upper"
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
        val intensity = 0.60

        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = dayType,
                currentWeekNumber = currentWeekNumber
            )
        ).thenReturn(Pair(guidelines, intensity))

        // Mock weight unit preference
        val weightUnitPreference = mockUserWeightUnitPreference(userId, "Bench Press", WeightUnit.KG)
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, "Bench Press"))
            .thenReturn(Mono.just(weightUnitPreference))

        // Mock band weight service
        val bandWeightResult =
            BandWeightService.Companion.BandWeightResult(
                band = com.congen.model.Band(BigDecimal("30")),
                barWeight = BigDecimal("70.0")
            )
        whenever(
            bandWeightService.computeBandAndBarWeights(
                exerciseName = any(),
                totalTargetWeight = any(),
                weightUnit = any(),
                weekInCycle = any()
            )
        ).thenReturn(bandWeightResult)

        // Mock weight selection service - this is called by getTargetWeight method
        whenever(weightSelectionService.roundWeightForExercise(eq("Bench Press"), any(), eq(WeightUnit.KG)))
            .thenReturn(Mono.just(BigDecimal("70.0")))

        val result =
            workoutStageGenerator.generatePrilepinBasedScheme(
                exercise,
                movementRole,
                dayType,
                oneRepMaxes,
                currentWeekNumber,
                userId
            )

        StepVerifier.create(result)
            .expectNextMatches { schemes ->
                schemes.isNotEmpty() && schemes[0].setNumber == 1 &&
                    !schemes[0].isAmrap && !schemes[0].isEmom &&
                    schemes[0].targetWeight == BigDecimal("70.0") &&
                    schemes[0].targetRepCount in 2..4 &&
                    schemes[0].restSeconds in 180..300 &&
                    schemes[0].band == com.congen.model.Band(BigDecimal("30"))
            }
            .verifyComplete()
    }

    @Test
    fun `generatePrilepinBasedScheme should not use band weight calculations for non-DE exercises`() {
        val exercise =
            mockExercise(
                name = "Bench Press",
                description = "A compound upper body exercise",
                movementType = MovementType.HORIZONTAL_PUSH
            )
        val movementRole = "primary"
        val dayType = "ME_Upper"
        val oneRepMaxes =
            listOf(
                mockUserOneRepMax(
                    userId = userId,
                    exerciseName = "Bench Press",
                    oneRepMax = BigDecimal("200.0")
                )
            )
        val currentWeekNumber = 1

        val guidelines = mockPrilepinGuidelines()
        val intensity = 0.85

        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = dayType,
                currentWeekNumber = currentWeekNumber
            )
        ).thenReturn(Pair(guidelines, intensity))

        // Mock weight unit preference
        val weightUnitPreference = mockUserWeightUnitPreference(userId, "Bench Press", WeightUnit.KG)
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, "Bench Press"))
            .thenReturn(Mono.just(weightUnitPreference))

        // Mock weight selection service
        whenever(weightSelectionService.roundWeightForExercise(any(), any(), any()))
            .thenReturn(Mono.just(BigDecimal("170.0")))

        val result =
            workoutStageGenerator.generatePrilepinBasedScheme(
                exercise,
                movementRole,
                dayType,
                oneRepMaxes,
                currentWeekNumber,
                userId
            )

        StepVerifier.create(result)
            .expectNextMatches { schemes ->
                schemes.isNotEmpty() &&
                    schemes[0].targetWeight == BigDecimal("170.0") &&
                    schemes[0].band == null
            }
            .verifyComplete()

        verify(bandWeightService, times(0)).computeBandAndBarWeights(
            any(),
            any(),
            any(),
            any()
        )
    }

    @Test
    fun `generatePrilepinBasedScheme should handle DE exercises with no bands in deload week`() {
        val exercise =
            mockExercise(
                name = "Bench Press",
                description = "A compound upper body exercise",
                movementType = MovementType.HORIZONTAL_PUSH
            )
        val movementRole = "primary"
        val dayType = "DE_Upper"
        val oneRepMaxes =
            listOf(
                mockUserOneRepMax(
                    userId = userId,
                    exerciseName = "Bench Press",
                    oneRepMax = BigDecimal("100.0")
                )
            )
        val currentWeekNumber = 4 // Deload week

        val guidelines = mockPrilepinGuidelines()
        val intensity = 0.60

        whenever(
            prilepinGuidelinesService.getUndulatingPeriodizationGuidelines(
                dayType = dayType,
                currentWeekNumber = currentWeekNumber
            )
        ).thenReturn(Pair(guidelines, intensity))

        // Mock weight unit preference
        val weightUnitPreference = mockUserWeightUnitPreference(userId, "Bench Press", WeightUnit.KG)
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, "Bench Press"))
            .thenReturn(Mono.just(weightUnitPreference))

        // Mock band weight service for deload week (no bands)
        val bandWeightResult =
            BandWeightService.Companion.BandWeightResult(
                band = null,
                barWeight = BigDecimal("100.0")
            )
        whenever(
            bandWeightService.computeBandAndBarWeights(
                exerciseName = any(),
                totalTargetWeight = any(),
                weightUnit = any(),
                weekInCycle = any()
            )
        ).thenReturn(bandWeightResult)

        // Mock weight selection service - this is called by getTargetWeight method
        whenever(weightSelectionService.roundWeightForExercise(eq("Bench Press"), any(), eq(WeightUnit.KG)))
            .thenReturn(Mono.just(BigDecimal("100.0")))

        val result =
            workoutStageGenerator.generatePrilepinBasedScheme(
                exercise,
                movementRole,
                dayType,
                oneRepMaxes,
                currentWeekNumber,
                userId
            )

        StepVerifier.create(result)
            .expectNextMatches { schemes ->
                schemes.isNotEmpty() && schemes[0].setNumber == 1 &&
                    !schemes[0].isAmrap && !schemes[0].isEmom &&
                    schemes[0].targetWeight == BigDecimal("100.0") &&
                    schemes[0].targetRepCount in 2..4 &&
                    schemes[0].restSeconds in 180..300 &&
                    schemes[0].band == null
            }
            .verifyComplete()
    }
}
