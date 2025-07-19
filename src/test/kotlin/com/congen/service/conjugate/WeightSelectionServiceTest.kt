package com.congen.service.conjugate

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Band
import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.MovementType
import com.congen.model.UserOneRepMax
import com.congen.model.UserWeightUnitPreference
import com.congen.model.WeightUnit
import com.congen.service.UnitConversionService
import com.congen.service.WeightSelectionService
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
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class WeightSelectionServiceTest {
    @Mock
    private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL

    @Mock
    private lateinit var unitConversionService: UnitConversionService

    @Mock
    private lateinit var baseWeightSelectionService: WeightSelectionService

    @Mock
    private lateinit var bandWeightService: BandWeightService

    @Mock
    private lateinit var exerciseMatchingService: ExerciseMatchingService

    @Mock
    private lateinit var exerciseDAL: ExerciseDAL

    @Mock
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL

    @Mock
    private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL

    @Mock
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL

    @InjectMocks
    private lateinit var conjugateWeightSelectionService: ConjugateWeightSelectionService

    private val userId = 1
    private val exerciseName = "Bench Press"
    private val intensity = 0.8
    private val oneRepMax = BigDecimal("225")
    private val calculatedWeight = oneRepMax * BigDecimal(intensity)
    private val roundedWeight = BigDecimal("180")

    @BeforeEach
    fun setUp() {
        // No default setup needed - each test will set up its own mocks as needed
    }

    @Test
    fun `getTargetWeight should return target weight when user has 1RM`() {
        // Given
        val now = Instant.now()
        val oneRepMaxes = listOf(UserOneRepMax(userId, exerciseName, oneRepMax, now))
        `when`(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(eq(userId), any()))
            .thenReturn(Mono.just(UserWeightUnitPreference(userId, exerciseName, WeightUnit.LBS, now, now)))
        `when`(baseWeightSelectionService.roundWeightForExercise(eq(exerciseName), any(), eq(WeightUnit.LBS)))
            .thenReturn(Mono.just(roundedWeight))

        // When
        val result =
            conjugateWeightSelectionService.getTargetWeight(
                exerciseName,
                intensity,
                oneRepMaxes,
                userId
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ConjugateWeightSelectionService.TargetWeightResult(roundedWeight, null))
            .verifyComplete()
    }

    @Test
    fun `getTargetWeight should return target weight with band for dynamic effort`() {
        // Given
        val now = Instant.now()
        val oneRepMaxes = listOf(UserOneRepMax(userId, exerciseName, oneRepMax, now))
        `when`(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(eq(userId), any()))
            .thenReturn(Mono.just(UserWeightUnitPreference(userId, exerciseName, WeightUnit.LBS, now, now)))
        val band = Band(BigDecimal("20"))
        val bandWeightResult = BandWeightService.Companion.BandWeightResult(band, roundedWeight)

        `when`(
            bandWeightService.computeBandAndBarWeights(
                eq(exerciseName),
                any(),
                eq(WeightUnit.LBS),
                eq(1)
            )
        ).thenReturn(bandWeightResult)

        `when`(baseWeightSelectionService.roundWeightForExercise(eq(exerciseName), any(), eq(WeightUnit.LBS)))
            .thenReturn(Mono.just(roundedWeight))

        // When
        val result =
            conjugateWeightSelectionService.getTargetWeight(
                exerciseName,
                intensity,
                oneRepMaxes,
                userId,
                isDynamicEffort = true,
                currentWeekNumber = 1
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ConjugateWeightSelectionService.TargetWeightResult(roundedWeight, band))
            .verifyComplete()
    }

    @Test
    fun `getTargetWeight should estimate weight from similar exercises when no 1RM`() {
        // Given
        val now = Instant.now()
        val oneRepMaxes = listOf(UserOneRepMax(userId, "Squat", BigDecimal("315"), now))
        `when`(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(eq(userId), any()))
            .thenReturn(Mono.just(UserWeightUnitPreference(userId, exerciseName, WeightUnit.LBS, now, now)))
        val targetExercise = Exercise(exerciseName, "Compound push exercise", MovementType.HORIZONTAL_PUSH, false, true, false)
        val referenceExercise = Exercise("Squat", "Compound push exercise", MovementType.SQUAT, false, false, false)
        val allExercises = listOf(targetExercise, referenceExercise)
        val allEquipment = listOf<ExerciseEquipment>()
        val allMuscles = listOf<ExerciseMuscle>()

        val match =
            ExerciseMatchingService.Companion.ExerciseMatch(
                referenceExercise = referenceExercise,
                similarityScore = 0.8,
                movementPattern = MovementType.HORIZONTAL_PUSH,
                factors = ExerciseMatchingService.Companion.SimilarityFactors(0.8, 1.0, 0.5, 0.5)
            )

        `when`(exerciseDAL.selectExercises()).thenReturn(Mono.just(allExercises))
        `when`(exerciseEquipmentDAL.selectAllExerciseEquipment()).thenReturn(Mono.just(allEquipment))
        `when`(exerciseMuscleDAL.selectAllExerciseMuscle()).thenReturn(Mono.just(allMuscles))
        `when`(
            exerciseMatchingService.findBestReferenceExercise(
                targetExercise,
                allExercises,
                emptyMap(),
                emptyMap(),
                oneRepMaxes
            )
        ).thenReturn(match)
        `when`(
            exerciseMatchingService.estimateWeightFromReference(
                targetExercise,
                referenceExercise,
                BigDecimal("315"),
                0.8
            )
        ).thenReturn(BigDecimal("250"))
        `when`(baseWeightSelectionService.roundWeightForExercise(eq(exerciseName), any(), eq(WeightUnit.LBS)))
            .thenReturn(Mono.just(roundedWeight))

        // When
        val result =
            conjugateWeightSelectionService.getTargetWeight(
                exerciseName,
                intensity,
                oneRepMaxes,
                userId
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ConjugateWeightSelectionService.TargetWeightResult(roundedWeight, null))
            .verifyComplete()
    }

    @Test
    fun `getTargetWeight should use bodyweight estimate for isolation exercises`() {
        // Given
        val now = Instant.now()
        val oneRepMaxes = listOf<UserOneRepMax>()
        `when`(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(eq(userId), any()))
            .thenReturn(Mono.just(UserWeightUnitPreference(userId, "Bicep Curl", WeightUnit.LBS, now, now)))
        val targetExercise = Exercise("Bicep Curl", "Isolation exercise", MovementType.ISOLATION, false, true, true)
        val referenceExercise = Exercise("Bodyweight Push-up", "Bodyweight exercise", MovementType.HORIZONTAL_PUSH, false, true, false)
        val allExercises = listOf(targetExercise, referenceExercise)
        val allEquipment = listOf<ExerciseEquipment>()
        val allMuscles = listOf<ExerciseMuscle>()

        val match =
            ExerciseMatchingService.Companion.ExerciseMatch(
                referenceExercise = referenceExercise,
                similarityScore = 0.6,
                movementPattern = MovementType.ISOLATION,
                factors = ExerciseMatchingService.Companion.SimilarityFactors(0.6, 0.5, 0.5, 0.5)
            )

        `when`(exerciseDAL.selectExercises()).thenReturn(Mono.just(allExercises))
        `when`(exerciseEquipmentDAL.selectAllExerciseEquipment()).thenReturn(Mono.just(allEquipment))
        `when`(exerciseMuscleDAL.selectAllExerciseMuscle()).thenReturn(Mono.just(allMuscles))
        `when`(
            exerciseMatchingService.findBestReferenceExercise(
                targetExercise,
                allExercises,
                emptyMap(),
                emptyMap(),
                oneRepMaxes
            )
        ).thenReturn(match)
        `when`(exerciseMatchingService.estimateIsolationWeight(targetExercise, BigDecimal("70")))
            .thenReturn(BigDecimal("15"))
        `when`(baseWeightSelectionService.roundWeightForExercise(eq("Bicep Curl"), any(), eq(WeightUnit.LBS)))
            .thenReturn(Mono.just(BigDecimal("12")))

        // When
        val result =
            conjugateWeightSelectionService.getTargetWeight(
                "Bicep Curl",
                intensity,
                oneRepMaxes,
                userId
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ConjugateWeightSelectionService.TargetWeightResult(BigDecimal("12"), null))
            .verifyComplete()
    }

    @Test
    fun `getTargetWeight should use conservative estimate for curl exercises`() {
        // Given
        val now = Instant.now()
        val oneRepMaxes = listOf<UserOneRepMax>()
        `when`(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(eq(userId), any()))
            .thenReturn(Mono.just(UserWeightUnitPreference(userId, "Bicep Curl", WeightUnit.LBS, now, now)))
        val allExercises = listOf<Exercise>()
        val allEquipment = listOf<ExerciseEquipment>()
        val allMuscles = listOf<ExerciseMuscle>()

        `when`(exerciseDAL.selectExercises()).thenReturn(Mono.just(allExercises))
        `when`(exerciseEquipmentDAL.selectAllExerciseEquipment()).thenReturn(Mono.just(allEquipment))
        `when`(exerciseMuscleDAL.selectAllExerciseMuscle()).thenReturn(Mono.just(allMuscles))
        `when`(baseWeightSelectionService.roundWeightForExercise(eq("Bicep Curl"), any(), eq(WeightUnit.LBS)))
            .thenReturn(Mono.just(BigDecimal("35")))

        // When
        val result =
            conjugateWeightSelectionService.getTargetWeight(
                "Bicep Curl",
                intensity,
                oneRepMaxes,
                userId
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ConjugateWeightSelectionService.TargetWeightResult(BigDecimal("35"), null))
            .verifyComplete()
    }

    @Test
    fun `getTargetWeight should use conservative estimate for raise exercises`() {
        // Given
        val now = Instant.now()
        val oneRepMaxes = listOf<UserOneRepMax>()
        `when`(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(eq(userId), any()))
            .thenReturn(Mono.just(UserWeightUnitPreference(userId, "Lateral Raise", WeightUnit.LBS, now, now)))
        val allExercises = listOf<Exercise>()
        val allEquipment = listOf<ExerciseEquipment>()
        val allMuscles = listOf<ExerciseMuscle>()

        `when`(exerciseDAL.selectExercises()).thenReturn(Mono.just(allExercises))
        `when`(exerciseEquipmentDAL.selectAllExerciseEquipment()).thenReturn(Mono.just(allEquipment))
        `when`(exerciseMuscleDAL.selectAllExerciseMuscle()).thenReturn(Mono.just(allMuscles))
        `when`(baseWeightSelectionService.roundWeightForExercise(eq("Lateral Raise"), any(), eq(WeightUnit.LBS)))
            .thenReturn(Mono.just(BigDecimal("15")))

        // When
        val result =
            conjugateWeightSelectionService.getTargetWeight(
                "Lateral Raise",
                intensity,
                oneRepMaxes,
                userId
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ConjugateWeightSelectionService.TargetWeightResult(BigDecimal("15"), null))
            .verifyComplete()
    }

    @Test
    fun `getTargetWeight should use conservative estimate when exercise not found`() {
        // Given
        val now = Instant.now()
        val oneRepMaxes = listOf<UserOneRepMax>()
        `when`(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(eq(userId), any()))
            .thenReturn(Mono.just(UserWeightUnitPreference(userId, "Unknown Exercise", WeightUnit.LBS, now, now)))
        val allExercises = listOf<Exercise>()
        val allEquipment = listOf<ExerciseEquipment>()
        val allMuscles = listOf<ExerciseMuscle>()

        `when`(exerciseDAL.selectExercises()).thenReturn(Mono.just(allExercises))
        `when`(exerciseEquipmentDAL.selectAllExerciseEquipment()).thenReturn(Mono.just(allEquipment))
        `when`(exerciseMuscleDAL.selectAllExerciseMuscle()).thenReturn(Mono.just(allMuscles))
        `when`(baseWeightSelectionService.roundWeightForExercise(eq("Unknown Exercise"), any(), eq(WeightUnit.LBS)))
            .thenReturn(Mono.just(BigDecimal("35")))

        // When
        val result =
            conjugateWeightSelectionService.getTargetWeight(
                "Unknown Exercise",
                intensity,
                oneRepMaxes,
                userId
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ConjugateWeightSelectionService.TargetWeightResult(BigDecimal("35"), null))
            .verifyComplete()
    }

    @Test
    fun `getTargetWeight should default to KG when no weight unit preference found`() {
        // Given
        val now = Instant.now()
        val oneRepMaxes = listOf(UserOneRepMax(userId, exerciseName, oneRepMax, now))
        `when`(userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(eq(userId), any()))
            .thenReturn(Mono.error(NoResultsFoundException("No preference found")))
        `when`(baseWeightSelectionService.roundWeightForExercise(eq(exerciseName), any(), eq(WeightUnit.KG)))
            .thenReturn(Mono.just(roundedWeight))

        // When
        val result =
            conjugateWeightSelectionService.getTargetWeight(
                exerciseName,
                intensity,
                oneRepMaxes,
                userId
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ConjugateWeightSelectionService.TargetWeightResult(roundedWeight, null))
            .verifyComplete()
    }
}
