package com.congen.generator

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.mockExercise
import com.congen.mockExerciseEquipment
import com.congen.mockExerciseMuscle
import com.congen.mockExerciseRotationHistory
import com.congen.mockUserEquipment
import com.congen.mockUserExercisePreference
import com.congen.model.Exercise
import com.congen.model.ExerciseMuscle
import com.congen.model.ExerciseRotationHistory
import com.congen.model.MovementType
import com.congen.model.UserExercisePreference
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ExerciseSelectionServiceTest {
    private companion object {
        private const val USER_ID = 1
        private const val EXERCISE_NAME = "Bench Press"
        private const val EXERCISE_NAME_2 = "Squat"
        private const val EXERCISE_NAME_3 = "Deadlift"
        private const val EXERCISE_NAME_4 = "Overhead Press"
        private const val EXERCISE_NAME_5 = "Pull-ups"
        private const val EXERCISE_NAME_6 = "Bicep Curls"
        private const val EXERCISE_NAME_7 = "Tricep Extensions"
        private const val EXERCISE_NAME_8 = "Dips"
    }

    @Mock
    private lateinit var exerciseDAL: ExerciseDAL

    @Mock
    private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL

    @Mock
    private lateinit var exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL

    @Mock
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL

    @Mock
    private lateinit var exerciseWorkoutTypeDAL: ExerciseWorkoutTypeDAL

    private lateinit var exerciseSelectionService: ExerciseSelectionService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        exerciseSelectionService =
            ExerciseSelectionService(
                exerciseDAL,
                exerciseMuscleDAL,
                exerciseWorkoutTypeDAL,
                exerciseEquipmentDAL,
                MovementBalanceService()
            )
    }

    @Test
    fun `selectRotatingExercise should return exercise for primary movement`() {
        val targetMuscles = listOf("chest", "triceps")
        val userEquipment = listOf(mockUserEquipment(equipmentName = "Barbell"))
        val preferences = emptyList<UserExercisePreference>()
        val exercises =
            listOf(
                mockExercise(name = EXERCISE_NAME, isAccessory = false),
                mockExercise(name = EXERCISE_NAME_4, isAccessory = false)
            )
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))

        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise(any())
        ).thenReturn(Mono.just(listOf(mockExerciseMuscle(exerciseName = EXERCISE_NAME, muscleName = "chest"))))

        whenever(
            exerciseEquipmentDAL.selectExerciseEquipmentByExercise(any())
        ).thenReturn(Mono.just(listOf(mockExerciseEquipment(exerciseName = EXERCISE_NAME, equipmentName = "Barbell"))))

        val result =
            exerciseSelectionService.selectRotatingExercise(
                targetMuscles,
                userEquipment,
                preferences,
                exercises,
                false,
                rotationHistory
            )

        StepVerifier.create(result)
            .expectNextMatches { exercise ->
                exercise != null && exercise.name in listOf(EXERCISE_NAME, EXERCISE_NAME_4)
            }
            .verifyComplete()
    }

    @Test
    fun `selectRotatingExercise should return exercise for accessory movement`() {
        val targetMuscles = listOf("biceps")
        val userEquipment = listOf(mockUserEquipment(equipmentName = "Dumbbells"))
        val preferences = emptyList<UserExercisePreference>()
        val exercises =
            listOf(
                mockExercise(name = EXERCISE_NAME_6, isAccessory = true),
                mockExercise(name = EXERCISE_NAME_7, isAccessory = true)
            )
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))

        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise(any())
        ).thenReturn(Mono.just(listOf(mockExerciseMuscle(exerciseName = EXERCISE_NAME_6, muscleName = "biceps"))))

        whenever(
            exerciseEquipmentDAL.selectExerciseEquipmentByExercise(any())
        ).thenReturn(Mono.just(listOf(mockExerciseEquipment(exerciseName = EXERCISE_NAME_6, equipmentName = "Dumbbells"))))

        val result =
            exerciseSelectionService.selectRotatingExercise(
                targetMuscles,
                userEquipment,
                preferences,
                exercises,
                true,
                rotationHistory
            )

        StepVerifier.create(result)
            .expectNextMatches { exercise ->
                exercise != null && exercise.isAccessory
            }
            .verifyComplete()
    }

    @Test
    fun `selectRotatingExercise should avoid exercises with preferences`() {
        val targetMuscles = listOf("chest")
        val userEquipment = listOf(mockUserEquipment(equipmentName = "Barbell"))
        val preferences =
            listOf(
                mockUserExercisePreference(exerciseName = EXERCISE_NAME, shouldAvoid = true)
            )
        val exercises =
            listOf(
                mockExercise(name = EXERCISE_NAME, isAccessory = false),
                mockExercise(name = EXERCISE_NAME_4, isAccessory = false)
            )
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))

        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise(any())
        ).thenReturn(Mono.just(listOf(mockExerciseMuscle(exerciseName = EXERCISE_NAME_4, muscleName = "chest"))))

        whenever(
            exerciseEquipmentDAL.selectExerciseEquipmentByExercise(any())
        ).thenReturn(Mono.just(listOf(mockExerciseEquipment(exerciseName = EXERCISE_NAME_4, equipmentName = "Barbell"))))

        val result =
            exerciseSelectionService.selectRotatingExercise(
                targetMuscles,
                userEquipment,
                preferences,
                exercises,
                false,
                rotationHistory
            )

        StepVerifier.create(result)
            .expectNextMatches { exercise ->
                exercise != null && exercise.name == EXERCISE_NAME_4
            }
            .verifyComplete()
    }

    @Test
    fun `selectRotatingExercise should prioritize unused exercises`() {
        val targetMuscles = listOf("chest")
        val userEquipment = listOf(mockUserEquipment(equipmentName = "Barbell"))
        val preferences = emptyList<UserExercisePreference>()
        val exercises =
            listOf(
                mockExercise(name = EXERCISE_NAME, isAccessory = false),
                mockExercise(name = EXERCISE_NAME_4, isAccessory = false)
            )
        val rotationHistory =
            listOf(
                mockExerciseRotationHistory(exerciseName = EXERCISE_NAME, isAccessory = false)
            )

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))

        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise(any())
        ).thenReturn(Mono.just(listOf(mockExerciseMuscle(exerciseName = EXERCISE_NAME_4, muscleName = "chest"))))

        whenever(
            exerciseEquipmentDAL.selectExerciseEquipmentByExercise(any())
        ).thenReturn(Mono.just(listOf(mockExerciseEquipment(exerciseName = EXERCISE_NAME_4, equipmentName = "Barbell"))))

        val result =
            exerciseSelectionService.selectRotatingExercise(
                targetMuscles,
                userEquipment,
                preferences,
                exercises,
                false,
                rotationHistory
            )

        StepVerifier.create(result)
            .expectNextMatches { exercise ->
                exercise != null && exercise.name == EXERCISE_NAME_4
            }
            .verifyComplete()
    }

    @Test
    fun `selectRotatingExercise should return null when no exercises available`() {
        val targetMuscles = listOf("chest")
        val userEquipment = listOf(mockUserEquipment(equipmentName = "Barbell"))
        val preferences =
            listOf(
                mockUserExercisePreference(exerciseName = EXERCISE_NAME, shouldAvoid = true),
                mockUserExercisePreference(exerciseName = EXERCISE_NAME_4, shouldAvoid = true)
            )
        val exercises =
            listOf(
                mockExercise(name = EXERCISE_NAME, isAccessory = false),
                mockExercise(name = EXERCISE_NAME_4, isAccessory = false)
            )
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))

        val result =
            exerciseSelectionService.selectRotatingExercise(
                targetMuscles,
                userEquipment,
                preferences,
                exercises,
                false,
                rotationHistory
            )

        StepVerifier.create(result)
            .verifyComplete()
    }

    @Test
    fun `filterExercisesByAccessoryStatus should filter primary exercises`() {
        val exercises =
            listOf(
                mockExercise(name = EXERCISE_NAME, isAccessory = false),
                mockExercise(name = EXERCISE_NAME_6, isAccessory = true),
                mockExercise(name = EXERCISE_NAME_4, isAccessory = false)
            )

        val result = exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, false)

        assert(result.size == 2)
        assert(result.all { !it.isAccessory })
    }

    @Test
    fun `filterExercisesByAccessoryStatus should filter accessory exercises`() {
        val exercises =
            listOf(
                mockExercise(name = EXERCISE_NAME, isAccessory = false),
                mockExercise(name = EXERCISE_NAME_6, isAccessory = true),
                mockExercise(name = EXERCISE_NAME_7, isAccessory = true)
            )

        val result = exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, true)

        assert(result.size == 2)
        assert(result.all { it.isAccessory })
    }

    @Test
    fun `filterExercisesExcluding should exclude specified exercise`() {
        val exercises =
            listOf(
                mockExercise(name = EXERCISE_NAME),
                mockExercise(name = EXERCISE_NAME_4),
                mockExercise(name = EXERCISE_NAME_6)
            )

        val result = exerciseSelectionService.filterExercisesExcluding(exercises, EXERCISE_NAME)

        assert(result.size == 2)
        assert(result.none { it.name == EXERCISE_NAME })
    }

    @Test
    fun `determineWeakMuscles should return default weak muscles`() {
        val result = exerciseSelectionService.determineWeakMuscles()

        assert(result.isNotEmpty())
        assert(result.containsAll(ConjugateConstants.DEFAULT_WEAK_MUSCLES))
        assertEquals(ConjugateConstants.DEFAULT_WEAK_MUSCLES.size, result.size)
    }

    @Test
    fun `selectSimilarSecondaryExercise returns null if no exercises available`() {
        val primary = mockExercise(name = EXERCISE_NAME, movementType = MovementType.HORIZONTAL_PUSH)
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(EXERCISE_NAME)).thenReturn(Mono.just(emptyList()))
        val result =
            exerciseSelectionService.selectSimilarSecondaryExercise(
                primary,
                userEquipment = listOf(mockUserEquipment()),
                preferences = emptyList(),
                exercises = emptyList(),
                rotationHistory = emptyList()
            ).block()
        assertNull(result)
    }

    @Test
    fun `selectSimilarSecondaryExercise prefers same movement type`() {
        val primary = mockExercise(name = EXERCISE_NAME, movementType = MovementType.HORIZONTAL_PUSH)
        val candidate1 = mockExercise(name = "Incline Bench Press", movementType = MovementType.HORIZONTAL_PUSH)
        val candidate2 = mockExercise(name = "Overhead Press", movementType = MovementType.VERTICAL_PUSH)
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(EXERCISE_NAME)).thenReturn(Mono.just(emptyList()))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise("Incline Bench Press")).thenReturn(Mono.just(emptyList()))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise("Overhead Press")).thenReturn(Mono.just(emptyList()))
        val result =
            exerciseSelectionService.selectSimilarSecondaryExercise(
                primary,
                userEquipment = listOf(mockUserEquipment()),
                preferences = emptyList(),
                exercises = listOf(candidate1, candidate2),
                rotationHistory = emptyList()
            ).block()
        assertNotNull(result)
        assertEquals("Incline Bench Press", result!!.name)
    }

    @Test
    fun `selectSimilarSecondaryExercise prefers muscle overlap`() {
        val primary = mockExercise(name = EXERCISE_NAME, movementType = MovementType.HORIZONTAL_PUSH)
        val candidate1 = mockExercise(name = "Incline Bench Press", movementType = MovementType.HORIZONTAL_PUSH)
        val candidate2 = mockExercise(name = "Overhead Press", movementType = MovementType.HORIZONTAL_PUSH)
        // Primary targets chest, triceps
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(EXERCISE_NAME)).thenReturn(
            Mono.just(
                listOf(
                    ExerciseMuscle(EXERCISE_NAME, "chest"),
                    ExerciseMuscle(EXERCISE_NAME, "triceps")
                )
            )
        )
        // Candidate1 targets chest only
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise("Incline Bench Press")).thenReturn(
            Mono.just(listOf(ExerciseMuscle("Incline Bench Press", "chest")))
        )
        // Candidate2 targets chest and triceps (full overlap)
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise("Overhead Press")).thenReturn(
            Mono.just(
                listOf(
                    ExerciseMuscle("Overhead Press", "chest"),
                    ExerciseMuscle("Overhead Press", "triceps")
                )
            )
        )
        val result =
            exerciseSelectionService.selectSimilarSecondaryExercise(
                primary,
                userEquipment = listOf(mockUserEquipment()),
                preferences = emptyList(),
                exercises = listOf(candidate1, candidate2),
                rotationHistory = emptyList()
            ).block()
        assertNotNull(result)
        assertEquals("Overhead Press", result!!.name)
    }

    @Test
    fun `selectSimilarSecondaryExercise applies rotation bonus`() {
        val primary = mockExercise(name = EXERCISE_NAME, movementType = MovementType.HORIZONTAL_PUSH)
        val candidate1 = mockExercise(name = "Incline Bench Press", movementType = MovementType.HORIZONTAL_PUSH)
        val candidate2 = mockExercise(name = "Overhead Press", movementType = MovementType.HORIZONTAL_PUSH)
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(EXERCISE_NAME)).thenReturn(Mono.just(emptyList()))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise("Incline Bench Press")).thenReturn(Mono.just(emptyList()))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise("Overhead Press")).thenReturn(Mono.just(emptyList()))
        // Candidate1 has been used 2 times, candidate2 never used
        val rotationHistory =
            listOf(
                mockExerciseRotationHistory(exerciseName = "Incline Bench Press", isAccessory = false),
                mockExerciseRotationHistory(exerciseName = "Incline Bench Press", isAccessory = false)
            )
        val result =
            exerciseSelectionService.selectSimilarSecondaryExercise(
                primary,
                userEquipment = listOf(mockUserEquipment()),
                preferences = emptyList(),
                exercises = listOf(candidate1, candidate2),
                rotationHistory = rotationHistory
            ).block()
        assertNotNull(result)
        assertEquals("Overhead Press", result!!.name)
    }

    @Test
    fun `selectSimilarSecondaryExercise respects user preferences`() {
        val primary = mockExercise(name = EXERCISE_NAME, movementType = MovementType.HORIZONTAL_PUSH)
        val candidate1 = mockExercise(name = "Incline Bench Press", movementType = MovementType.HORIZONTAL_PUSH)
        val candidate2 = mockExercise(name = "Overhead Press", movementType = MovementType.HORIZONTAL_PUSH)
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(EXERCISE_NAME)).thenReturn(Mono.just(emptyList()))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise("Incline Bench Press")).thenReturn(Mono.just(emptyList()))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise("Overhead Press")).thenReturn(Mono.just(emptyList()))
        val preferences = listOf(mockUserExercisePreference(exerciseName = "Incline Bench Press", shouldAvoid = true))
        val result =
            exerciseSelectionService.selectSimilarSecondaryExercise(
                primary,
                userEquipment = listOf(mockUserEquipment()),
                preferences = preferences,
                exercises = listOf(candidate1, candidate2),
                rotationHistory = emptyList()
            ).block()
        assertNotNull(result)
        assertEquals("Overhead Press", result!!.name)
    }

    @Test
    fun `selectSimilarSecondaryExercise returns null if all filtered out`() {
        val primary = mockExercise(name = EXERCISE_NAME, movementType = MovementType.HORIZONTAL_PUSH)
        val candidate1 = mockExercise(name = "Incline Bench Press", movementType = MovementType.HORIZONTAL_PUSH)
        val preferences = listOf(mockUserExercisePreference(exerciseName = "Incline Bench Press", shouldAvoid = true))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(EXERCISE_NAME)).thenReturn(Mono.just(emptyList()))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise("Incline Bench Press")).thenReturn(Mono.just(emptyList()))
        val result =
            exerciseSelectionService.selectSimilarSecondaryExercise(
                primary,
                userEquipment = listOf(mockUserEquipment()),
                preferences = preferences,
                exercises = listOf(candidate1),
                rotationHistory = emptyList()
            ).block()
        assertNull(result)
    }

    @Test
    fun `selectWarmupExercises should return exercises for 4-day template`() {
        // Given
        val exercises =
            listOf(
                mockExercise(name = "Bicep Curl", isAccessory = true),
                mockExercise(name = "Tricep Extension", isAccessory = true),
                mockExercise(name = "Shoulder Press", isAccessory = true)
            )
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = listOf(mockUserEquipment(equipmentName = "Dumbbells"))
        val dayType = "ME_Upper"
        val primaryExercise = mockExercise(name = "Bench Press", isAccessory = false, movementType = MovementType.HORIZONTAL_PUSH)

        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise("Bench Press")
        ).thenReturn(Mono.just(listOf(mockExerciseMuscle(exerciseName = "Bench Press", muscleName = "chest"))))

        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise("Bicep Curl")
        ).thenReturn(Mono.just(listOf(mockExerciseMuscle(exerciseName = "Bicep Curl", muscleName = "biceps"))))

        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise("Tricep Extension")
        ).thenReturn(Mono.just(listOf(mockExerciseMuscle(exerciseName = "Tricep Extension", muscleName = "triceps"))))

        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise("Shoulder Press")
        ).thenReturn(Mono.just(listOf(mockExerciseMuscle(exerciseName = "Shoulder Press", muscleName = "shoulders"))))

        whenever(
            exerciseEquipmentDAL.selectExerciseEquipmentByExercise(any())
        ).thenReturn(Mono.just(listOf(mockExerciseEquipment(exerciseName = "Bicep Curl", equipmentName = "Dumbbells"))))

        // When
        val result =
            exerciseSelectionService.selectWarmupExercises(
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                dayType = dayType,
                primaryExercise = primaryExercise,
                isFourDayTemplate = true
            )

        // Then
        StepVerifier.create(result)
            .expectNextMatches { warmupExercises ->
                warmupExercises.isNotEmpty() && warmupExercises.all { it.isAccessory }
            }
            .verifyComplete()
    }

    @Test
    fun `selectWarmupExercises should return exercises for 2-3 day template`() {
        // Given
        val exercises =
            listOf(
                mockExercise(name = "Bicep Curl", isAccessory = true),
                mockExercise(name = "Tricep Extension", isAccessory = true),
                mockExercise(name = "Shoulder Press", isAccessory = true)
            )
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = listOf(mockUserEquipment(equipmentName = "Dumbbells"))
        val dayType = "ME_Upper_DE_Lower"

        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise("Bicep Curl")
        ).thenReturn(Mono.just(listOf(mockExerciseMuscle(exerciseName = "Bicep Curl", muscleName = "biceps"))))

        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise("Tricep Extension")
        ).thenReturn(Mono.just(listOf(mockExerciseMuscle(exerciseName = "Tricep Extension", muscleName = "triceps"))))

        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise("Shoulder Press")
        ).thenReturn(Mono.just(listOf(mockExerciseMuscle(exerciseName = "Shoulder Press", muscleName = "shoulders"))))

        whenever(
            exerciseEquipmentDAL.selectExerciseEquipmentByExercise(any())
        ).thenReturn(Mono.just(listOf(mockExerciseEquipment(exerciseName = "Bicep Curl", equipmentName = "Dumbbells"))))

        // When
        val result =
            exerciseSelectionService.selectWarmupExercises(
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                dayType = dayType,
                primaryExercise = null,
                isFourDayTemplate = false
            )

        // Then
        StepVerifier.create(result)
            .expectNextMatches { warmupExercises ->
                warmupExercises.isNotEmpty() && warmupExercises.all { it.isAccessory }
            }
            .verifyComplete()
    }

    @Test
    fun `selectWarmupExercises should return empty list when no exercises available`() {
        // Given
        val exercises = emptyList<Exercise>()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = listOf(mockUserEquipment(equipmentName = "Dumbbells"))
        val dayType = "ME_Upper"
        val primaryExercise = mockExercise(name = "Bench Press", isAccessory = false)

        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise("Bench Press")
        ).thenReturn(Mono.just(listOf(mockExerciseMuscle(exerciseName = "Bench Press", muscleName = "chest"))))

        // When
        val result =
            exerciseSelectionService.selectWarmupExercises(
                exercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                dayType = dayType,
                primaryExercise = primaryExercise,
                isFourDayTemplate = true
            )

        // Then
        StepVerifier.create(result)
            .expectNext(emptyList())
            .verifyComplete()
    }

    @Test
    fun `selectRotatingExercise should include preferred exercises not in original list`() {
        val targetMuscles = listOf("chest", "triceps")
        val userEquipment = listOf(mockUserEquipment(equipmentName = "Barbell"))
        val preferences =
            listOf(
                mockUserExercisePreference(exerciseName = "Preferred Exercise", shouldAvoid = false)
            )
        val exercises =
            listOf(
                mockExercise(name = EXERCISE_NAME, isAccessory = false)
            )
        val allExercises =
            listOf(
                mockExercise(name = EXERCISE_NAME, isAccessory = false),
                mockExercise(name = "Preferred Exercise", isAccessory = false)
            )
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(allExercises))

        // Mock that both exercises have muscle and equipment data
        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise(EXERCISE_NAME)
        ).thenReturn(Mono.just(listOf(mockExerciseMuscle(exerciseName = EXERCISE_NAME, muscleName = "chest"))))

        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise("Preferred Exercise")
        ).thenReturn(Mono.just(listOf(mockExerciseMuscle(exerciseName = "Preferred Exercise", muscleName = "chest"))))

        whenever(
            exerciseEquipmentDAL.selectExerciseEquipmentByExercise(EXERCISE_NAME)
        ).thenReturn(Mono.just(listOf(mockExerciseEquipment(exerciseName = EXERCISE_NAME, equipmentName = "Barbell"))))

        whenever(
            exerciseEquipmentDAL.selectExerciseEquipmentByExercise("Preferred Exercise")
        ).thenReturn(Mono.just(listOf(mockExerciseEquipment(exerciseName = "Preferred Exercise", equipmentName = "Barbell"))))

        val result =
            exerciseSelectionService.selectRotatingExercise(
                targetMuscles,
                userEquipment,
                preferences,
                exercises,
                false,
                rotationHistory
            )

        StepVerifier.create(result)
            .expectNextMatches { exercise ->
                exercise != null && (exercise.name == EXERCISE_NAME || exercise.name == "Preferred Exercise")
            }
            .verifyComplete()
    }
}
