package com.congen.generator

import com.congen.model.Band
import com.congen.model.ExerciseEquipment
import com.congen.model.ProgramPreferences
import com.congen.model.UserOneRepMax
import com.congen.model.WeightUnit
import com.congen.util.UnitConverter
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class WeightSelectionServiceTest {
    @Mock
    private lateinit var unitConverter: UnitConverter

    @Mock
    private lateinit var supportedEquipmentWeightRoundingService: SupportedEquipmentWeightRoundingService

    @Mock
    private lateinit var bandWeightService: BandWeightService

    @Mock
    private lateinit var exerciseMatchingService: ExerciseMatchingService

    @InjectMocks
    private lateinit var weightSelectionService: WeightSelectionService

    private val userId = "b226d772-c063-4974-ae08-ab64134abbcf"
    private val exerciseName = "Bench Press"
    private val intensity = 0.8
    private val oneRepMax = BigDecimal("225")
    private val calculatedWeight = (oneRepMax * BigDecimal(intensity)).setScale(2, RoundingMode.HALF_UP)
    private val roundedWeight = BigDecimal("180")
    private val now = Instant.now()

    private lateinit var preparedData: WorkoutGenerationPreparedData

    @BeforeEach
    fun setUp() {
        preparedData =
            WorkoutGenerationPreparedData(
                userExercisePool =
                    UserExercisePool(
                        allExercises = emptyList(),
                        preferences = emptyList(),
                        userEquipment = emptyList(),
                        exerciseEquipmentMappings = emptyMap(),
                        exerciseMuscleMappings = emptyMap(),
                        previouslyUsedExercises = emptyList(),
                        userId = userId
                    ),
                oneRepMaxes = emptyList(),
                programPreferences =
                    ProgramPreferences(
                        programId = 1L,
                        programDaysPerWeek = 4,
                        sessionTimeLengthInMinutes = 60,
                        createdAt = now,
                        updatedAt = now
                    ),
                weakMuscles = emptyList(),
                currentWeekNumber = 1,
                userId = userId,
                weightUnitPreferences = mapOf(exerciseName to WeightUnit.LBS),
                exerciseMuscleMappings = emptyMap(),
                exerciseEquipmentMappings = mapOf(exerciseName to listOf(ExerciseEquipment(exerciseName, "power bar"))),
                exerciseWorkoutTypeMappings = emptyMap(),
                previouslyProgrammedExercises = emptyList(),
                allExercises = emptyList(),
                userEquipment = emptyList(),
                userExercisePreferences = emptyList()
            )
    }

    @Test
    fun `getTargetWeight should return target weight when user has 1RM`() {
        val oneRepMaxes = listOf(UserOneRepMax(userId, exerciseName, oneRepMax, now))
        val updatedPreparedData = preparedData.copy(oneRepMaxes = oneRepMaxes)

        `when`(
            supportedEquipmentWeightRoundingService.roundWeightForExercise(
                eq(exerciseName),
                any(),
                eq(WeightUnit.LBS),
                any()
            )
        ).thenReturn(Mono.just(roundedWeight))

        val result =
            weightSelectionService.getTargetWeight(
                exerciseName,
                intensity,
                oneRepMaxes,
                isDynamicEffort = false,
                currentWeekNumber = 1,
                preparedData = updatedPreparedData
            )

        StepVerifier.create(result)
            .expectNext(WeightSelectionService.TargetWeightResult(roundedWeight, null))
            .verifyComplete()
    }

    @Test
    fun `getTargetWeight should return target weight with band for dynamic effort`() {
        val oneRepMaxes = listOf(UserOneRepMax(userId, exerciseName, oneRepMax, now))
        val updatedPreparedData = preparedData.copy(oneRepMaxes = oneRepMaxes)
        val band = Band(BigDecimal("20.0"))
        val bandWeightResult = BandWeightService.Companion.BandWeightResult(band, BigDecimal("160"))

        `when`(
            bandWeightService.computeBandAndBarWeights(
                eq(calculatedWeight),
                eq(WeightUnit.LBS),
                eq(1),
                eq(intensity)
            )
        ).thenReturn(bandWeightResult)

        `when`(
            supportedEquipmentWeightRoundingService.roundWeightForExercise(
                eq(exerciseName),
                any(),
                eq(WeightUnit.LBS),
                any()
            )
        ).thenReturn(Mono.just(BigDecimal("160")))

        val result =
            weightSelectionService.getTargetWeight(
                exerciseName,
                intensity,
                oneRepMaxes,
                isDynamicEffort = true,
                currentWeekNumber = 0,
                preparedData = updatedPreparedData
            )

        StepVerifier.create(result)
            .expectNext(WeightSelectionService.TargetWeightResult(BigDecimal("160"), band))
            .verifyComplete()
    }

    @Test
    fun `getTargetWeight should return conservative bodyweight estimate when no 1RM found`() {
        val oneRepMaxes = emptyList<UserOneRepMax>()
        val updatedPreparedData = preparedData.copy(oneRepMaxes = oneRepMaxes)
        val conservativeWeight = BigDecimal("150")

        `when`(
            supportedEquipmentWeightRoundingService.roundWeightForExercise(
                eq(exerciseName),
                any(),
                eq(WeightUnit.LBS),
                any()
            )
        ).thenReturn(Mono.just(conservativeWeight))

        val result =
            weightSelectionService.getTargetWeight(
                exerciseName,
                intensity,
                oneRepMaxes,
                isDynamicEffort = false,
                currentWeekNumber = 1,
                preparedData = updatedPreparedData
            )

        StepVerifier.create(result)
            .expectNext(WeightSelectionService.TargetWeightResult(conservativeWeight, null))
            .verifyComplete()
    }

    @Test
    fun `getTargetWeight should handle kg weight units`() {
        val oneRepMaxes = listOf(UserOneRepMax(userId, exerciseName, oneRepMax, now))
        val kgPreparedData =
            preparedData.copy(
                oneRepMaxes = oneRepMaxes,
                weightUnitPreferences = mapOf(exerciseName to WeightUnit.KG)
            )
        val roundedWeightKg = BigDecimal("82")

        `when`(
            supportedEquipmentWeightRoundingService.roundWeightForExercise(
                eq(exerciseName),
                any(),
                eq(WeightUnit.KG),
                any()
            )
        ).thenReturn(Mono.just(roundedWeightKg))

        val result =
            weightSelectionService.getTargetWeight(
                exerciseName,
                intensity,
                oneRepMaxes,
                isDynamicEffort = false,
                currentWeekNumber = 1,
                preparedData = kgPreparedData
            )

        StepVerifier.create(result)
            .expectNext(WeightSelectionService.TargetWeightResult(roundedWeightKg, null))
            .verifyComplete()
    }

    @Test
    fun `getTargetWeight should handle dynamic effort with kg units`() {
        val oneRepMaxes = listOf(UserOneRepMax(userId, exerciseName, oneRepMax, now))
        val kgPreparedData =
            preparedData.copy(
                oneRepMaxes = oneRepMaxes,
                weightUnitPreferences = mapOf(exerciseName to WeightUnit.KG)
            )
        val band = Band(BigDecimal("9.0"))
        val bandWeightResult = BandWeightService.Companion.BandWeightResult(band, BigDecimal("73"))

        `when`(
            bandWeightService.computeBandAndBarWeights(
                eq(calculatedWeight),
                eq(WeightUnit.KG),
                eq(1),
                eq(intensity)
            )
        ).thenReturn(bandWeightResult)

        `when`(
            supportedEquipmentWeightRoundingService.roundWeightForExercise(
                eq(exerciseName),
                any(),
                eq(WeightUnit.KG),
                any()
            )
        ).thenReturn(Mono.just(BigDecimal("73")))

        val result =
            weightSelectionService.getTargetWeight(
                exerciseName,
                intensity,
                oneRepMaxes,
                isDynamicEffort = true,
                currentWeekNumber = 0,
                preparedData = kgPreparedData
            )

        StepVerifier.create(result)
            .expectNext(WeightSelectionService.TargetWeightResult(BigDecimal("73"), band))
            .verifyComplete()
    }

    @Test
    fun `getTargetWeight should handle missing weight unit preference`() {
        val oneRepMaxes = listOf(UserOneRepMax(userId, exerciseName, oneRepMax, now))
        val noWeightUnitPreparedData =
            preparedData.copy(
                oneRepMaxes = oneRepMaxes,
                weightUnitPreferences = emptyMap()
            )
        val roundedWeightKg = BigDecimal("82")

        `when`(
            supportedEquipmentWeightRoundingService.roundWeightForExercise(
                eq(exerciseName),
                any(),
                eq(WeightUnit.KG),
                any()
            )
        ).thenReturn(Mono.just(roundedWeightKg))

        val result =
            weightSelectionService.getTargetWeight(
                exerciseName,
                intensity,
                oneRepMaxes,
                isDynamicEffort = false,
                currentWeekNumber = 1,
                preparedData = noWeightUnitPreparedData
            )

        StepVerifier.create(result)
            .expectNext(WeightSelectionService.TargetWeightResult(roundedWeightKg, null))
            .verifyComplete()
    }

    @Test
    fun `getTargetWeight should handle different week numbers for dynamic effort`() {
        val oneRepMaxes = listOf(UserOneRepMax(userId, exerciseName, oneRepMax, now))
        val updatedPreparedData = preparedData.copy(oneRepMaxes = oneRepMaxes)
        val band = Band(BigDecimal("30.0"))
        val bandWeightResult = BandWeightService.Companion.BandWeightResult(band, BigDecimal("140"))

        `when`(
            bandWeightService.computeBandAndBarWeights(
                eq(calculatedWeight),
                eq(WeightUnit.LBS),
                eq(3),
                eq(intensity)
            )
        ).thenReturn(bandWeightResult)

        `when`(
            supportedEquipmentWeightRoundingService.roundWeightForExercise(
                eq(exerciseName),
                any(),
                eq(WeightUnit.LBS),
                any()
            )
        ).thenReturn(Mono.just(BigDecimal("140")))

        val result =
            weightSelectionService.getTargetWeight(
                exerciseName,
                intensity,
                oneRepMaxes,
                isDynamicEffort = true,
                currentWeekNumber = 2,
                preparedData = updatedPreparedData
            )

        StepVerifier.create(result)
            .expectNext(WeightSelectionService.TargetWeightResult(BigDecimal("140"), band))
            .verifyComplete()
    }
}
