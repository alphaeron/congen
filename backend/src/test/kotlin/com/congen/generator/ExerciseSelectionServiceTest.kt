package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.MovementType
import com.congen.model.ProgramPreferences
import com.congen.model.UserEquipment
import com.congen.model.UserOneRepMax
import com.congen.model.WeightUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

/**
 * Unit tests for ExerciseSelectionService.
 *
 * These tests focus on the public API and verify that the service correctly
 * selects exercises based on the provided criteria.
 */
class ExerciseSelectionServiceTest {
    private lateinit var exerciseSelectionService: ExerciseSelectionService
    private lateinit var movementBalanceService: MovementBalanceService
    private lateinit var exerciseMatchingService: ExerciseMatchingService
    private lateinit var userExercisePool: UserExercisePool

    companion object {
        private const val USER_ID = "test-user-123"
        private val now = Instant.now()
    }

    @BeforeEach
    fun setUp() {
        movementBalanceService = mock()
        exerciseMatchingService = mock()
        userExercisePool = mock()

        exerciseSelectionService =
            ExerciseSelectionService(
                movementBalanceService = movementBalanceService,
                exerciseMatchingService = exerciseMatchingService
            )
    }

    @Test
    fun `selectExercise should return exercise when available`() {
        val exercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val targetMuscles = listOf("chest", "triceps")
        val workoutType = "maximal_effort"
        val dayType = "ME_Upper"
        val preparedData = createSamplePreparedData()

        whenever(userExercisePool.getUserId()).thenReturn(USER_ID)
        whenever(userExercisePool.getAvailablePrimaryExercises()).thenReturn(listOf(exercise))
        whenever(userExercisePool.filterExercisesByEquipment(any(), any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(userExercisePool.filterExercisesByMuscles(any(), any())).thenReturn(Mono.just(listOf(exercise)))
        whenever(userExercisePool.getPreviouslyUsedExercises()).thenReturn(emptyList())

        val result =
            exerciseSelectionService.selectExercise(
                userExercisePool = userExercisePool,
                targetMuscles = targetMuscles,
                isAccessory = false,
                workoutType = workoutType,
                dayType = dayType,
                exerciseWorkoutTypeMappings = preparedData.exerciseWorkoutTypeMappings,
                exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                currentWeekNumber = 1
            )

        StepVerifier.create(result)
            .expectNext(exercise)
            .verifyComplete()
    }

    @Test
    fun `selectExercise should return empty when no exercises available`() {
        val targetMuscles = listOf("chest", "triceps")
        val workoutType = "maximal_effort"
        val dayType = "ME_Upper"
        val preparedData = createSamplePreparedData()

        whenever(userExercisePool.getUserId()).thenReturn(USER_ID)
        whenever(userExercisePool.getAvailablePrimaryExercises()).thenReturn(emptyList())
        whenever(userExercisePool.getPreviouslyUsedExercises()).thenReturn(emptyList())

        val result =
            exerciseSelectionService.selectExercise(
                userExercisePool = userExercisePool,
                targetMuscles = targetMuscles,
                isAccessory = false,
                workoutType = workoutType,
                dayType = dayType,
                exerciseWorkoutTypeMappings = preparedData.exerciseWorkoutTypeMappings,
                exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                currentWeekNumber = 1
            )

        StepVerifier.create(result)
            .verifyComplete()
    }

    @Test
    fun `selectSimilarSecondaryExercise should select similar exercise`() {
        val primaryExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val similarExercise = createSampleExercise("Incline Bench Press", MovementType.HORIZONTAL_PUSH).copy(isAccessory = false)
        val preparedData = createSamplePreparedData()

        // Use a real UserExercisePool instead of a mock
        val realUserExercisePool =
            UserExercisePool(
                allExercises = listOf(similarExercise),
                preferences = emptyList(),
                userEquipment = createSampleUserEquipment(),
                exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
                exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
                previouslyUsedExercises = emptyList(),
                userId = USER_ID
            )

        // Create workout type mappings that include the similar exercise
        val exerciseWorkoutTypeMappings =
            mapOf(
                "Bench Press" to listOf("maximal_effort"),
                "Incline Bench Press" to listOf("maximal_effort")
            )

        val result =
            exerciseSelectionService.selectSimilarSecondaryExercise(
                primaryExercise = primaryExercise,
                userExercisePool = realUserExercisePool,
                workoutType = "maximal_effort",
                dayType = "ME_Upper",
                exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings,
            )

        StepVerifier.create(result)
            .expectNext(similarExercise)
            .verifyComplete()
    }

    @Test
    fun `selectWarmupExercises should select appropriate warmup exercises without duplicates`() {
        val primaryExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val warmup1 = createSampleExercise("Push-ups", MovementType.HORIZONTAL_PUSH).copy(isAccessory = true)
        val warmup2 = createSampleExercise("Band Rows", MovementType.HORIZONTAL_PULL).copy(isAccessory = true)
        val warmup3 = createSampleExercise("4-way neck", MovementType.ISOLATION).copy(isAccessory = true)
        val preparedData = createSamplePreparedData()

        val realUserExercisePool =
            UserExercisePool(
                allExercises = listOf(warmup1, warmup2, warmup3),
                preferences = emptyList(),
                userEquipment = createSampleUserEquipment(),
                exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
                exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
                previouslyUsedExercises = emptyList(),
                userId = USER_ID
            )

        val result =
            exerciseSelectionService.selectWarmupExercises(
                userExercisePool = realUserExercisePool,
                primaryExercise = primaryExercise,
                isFourDayTemplate = false,
                dayType = "ME_Upper",
                workoutType = "maximal_effort",
                exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                exerciseEquipmentMappings = preparedData.exerciseEquipmentMappings,
                exerciseWorkoutTypeMappings = preparedData.exerciseWorkoutTypeMappings,
                currentWeekNumber = 1
            )

        StepVerifier.create(result)
            .expectNextMatches { list ->
                list.size == 3 && list.map { it.name }.toSet().size == 3
            }
            .verifyComplete()
    }

    @Test
    fun `filterBandedExercisesForSecondary should exclude banded exercises for secondary movements`() {
        val bandedExercise = createSampleExercise("Banded Bench Press", MovementType.HORIZONTAL_PUSH)
        val regularExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val exercises = listOf(bandedExercise, regularExercise)

        val result = exerciseSelectionService.filterBandedExercisesForSecondary(exercises, isSecondary = true)

        assert(result.size == 1)
        assert(result.contains(regularExercise))
        assert(!result.contains(bandedExercise))
    }

    @Test
    fun `filterBandedExercisesForSecondary should include all exercises for primary movements`() {
        val bandedExercise = createSampleExercise("Banded Bench Press", MovementType.HORIZONTAL_PUSH)
        val regularExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val exercises = listOf(bandedExercise, regularExercise)

        val result = exerciseSelectionService.filterBandedExercisesForSecondary(exercises, isSecondary = false)

        assert(result.size == 2)
        assert(result.contains(regularExercise))
        assert(result.contains(bandedExercise))
    }

    @Test
    fun `filterBandedExercisesForDayType should exclude banded exercises on non-DE days`() {
        val bandedExercise = createSampleExercise("Banded Bench Press", MovementType.HORIZONTAL_PUSH)
        val regularExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val exercises = listOf(bandedExercise, regularExercise)

        val result = exerciseSelectionService.filterBandedExercisesForDayType(exercises, dayType = "ME_Upper")

        assert(result.size == 1)
        assert(result.contains(regularExercise))
        assert(!result.contains(bandedExercise))
    }

    @Test
    fun `filterBandedExercisesForDayType should include banded exercises on DE days`() {
        val bandedExercise = createSampleExercise("Banded Bench Press", MovementType.HORIZONTAL_PUSH)
        val regularExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val exercises = listOf(bandedExercise, regularExercise)

        val result = exerciseSelectionService.filterBandedExercisesForDayType(exercises, dayType = "DE_Upper")

        assert(result.size == 2)
        assert(result.contains(regularExercise))
        assert(result.contains(bandedExercise))
    }

    @Test
    fun `filterBandedExercisesForDayType should include banded exercises on combined ME+DE days`() {
        val bandedExercise = createSampleExercise("Banded Bench Press", MovementType.HORIZONTAL_PUSH)
        val regularExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val exercises = listOf(bandedExercise, regularExercise)

        val result = exerciseSelectionService.filterBandedExercisesForDayType(exercises, dayType = "ME+DE_Upper")

        assert(result.size == 2)
        assert(result.contains(regularExercise))
        assert(result.contains(bandedExercise))
    }

    @Test
    fun `selectSimilarSecondaryExercise should exclude banded exercises for secondary movements`() {
        val primaryExercise = createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)
        val bandedExercise = createSampleExercise("Banded Bench Press", MovementType.HORIZONTAL_PUSH)
        val regularExercise = createSampleExercise("Incline Bench Press", MovementType.HORIZONTAL_PUSH)

        // Use a real UserExercisePool with both banded and regular exercises
        val realUserExercisePool =
            UserExercisePool(
                allExercises = listOf(bandedExercise, regularExercise),
                preferences = emptyList(),
                userEquipment = createSampleUserEquipment(),
                exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
                exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
                previouslyUsedExercises = emptyList(),
                userId = USER_ID
            )

        // Create workout type mappings that include both exercises
        val exerciseWorkoutTypeMappings =
            mapOf(
                "Bench Press" to listOf("maximal_effort"),
                "Banded Bench Press" to listOf("maximal_effort"),
                "Incline Bench Press" to listOf("maximal_effort")
            )

        val result =
            exerciseSelectionService.selectSimilarSecondaryExercise(
                primaryExercise = primaryExercise,
                userExercisePool = realUserExercisePool,
                workoutType = "maximal_effort",
                dayType = "ME_Upper",
                exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
                exerciseWorkoutTypeMappings = exerciseWorkoutTypeMappings
            )

        StepVerifier.create(result)
            .expectNext(regularExercise) // Should select regular exercise, not banded
            .verifyComplete()
    }

    private fun createSampleExercise(
        name: String,
        movementType: MovementType
    ): Exercise {
        return Exercise(
            name = name,
            description = "Sample exercise description",
            movementType = movementType,
            isUnilateral = false,
            isUpper = true,
            isAccessory = false
        )
    }

    private fun createSamplePreparedData(): WorkoutGenerationPreparedData {
        return WorkoutGenerationPreparedData(
            userExercisePool =
                UserExercisePool(
                    allExercises = listOf(createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)),
                    preferences = emptyList(),
                    userEquipment = createSampleUserEquipment(),
                    exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
                    exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
                    previouslyUsedExercises = emptyList(),
                    userId = USER_ID
                ),
            oneRepMaxes =
                listOf(
                    UserOneRepMax(USER_ID, "Bench Press", BigDecimal("225"), now)
                ),
            programPreferences =
                ProgramPreferences(
                    programId = 1L,
                    programDaysPerWeek = 4,
                    sessionTimeLengthInMinutes = 60,
                    createdAt = now,
                    updatedAt = now
                ),
            weakMuscles = listOf("chest"),
            currentWeekNumber = 1,
            userId = USER_ID,
            weightUnitPreferences = mapOf("Bench Press" to WeightUnit.LBS),
            exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
            exerciseWorkoutTypeMappings = mapOf("Bench Press" to listOf("maximal_effort")),
            exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
            previouslyProgrammedExercises = emptyList(),
            allExercises = listOf(createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH)),
            userEquipment = createSampleUserEquipment(),
            userExercisePreferences = emptyList()
        )
    }

    private fun createSampleUserEquipment(): List<UserEquipment> {
        return listOf(
            UserEquipment(USER_ID, "power bar", now),
            UserEquipment(USER_ID, "bench", now),
            UserEquipment(USER_ID, "squat rack", now)
        )
    }

    private fun createSampleExerciseEquipmentMappings(): Map<String, List<ExerciseEquipment>> {
        return mapOf(
            "Bench Press" to listOf(ExerciseEquipment("Bench Press", "power bar")),
            "Squat" to listOf(ExerciseEquipment("Squat", "power bar")),
            "Deadlift" to listOf(ExerciseEquipment("Deadlift", "power bar"))
        )
    }

    private fun createSampleExerciseMuscleMappings(): Map<String, List<ExerciseMuscle>> {
        return mapOf(
            "Bench Press" to
                listOf(
                    ExerciseMuscle("Bench Press", "chest"),
                    ExerciseMuscle("Bench Press", "triceps")
                ),
            "Squat" to
                listOf(
                    ExerciseMuscle("Squat", "quadriceps"),
                    ExerciseMuscle("Squat", "glutes")
                ),
            "Deadlift" to
                listOf(
                    ExerciseMuscle("Deadlift", "hamstrings"),
                    ExerciseMuscle("Deadlift", "glutes")
                )
        )
    }
}
