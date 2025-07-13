package com.congen.service.conjugate

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.mockExercise
import com.congen.mockExerciseRotationHistory
import com.congen.mockUserEquipment
import com.congen.mockUserExercisePreference
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals

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
    private lateinit var exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL

    private lateinit var exerciseSelectionService: ExerciseSelectionService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        exerciseSelectionService = ExerciseSelectionService()
    }

    @Test
    fun `selectRotatingExercise should return exercise for primary movement`() {
        val targetMuscles = listOf("chest", "triceps")
        val userEquipment = listOf(mockUserEquipment(equipmentName = "Barbell"))
        val preferences = emptyList<com.congen.model.UserExercisePreference>()
        val exercises =
            listOf(
                mockExercise(name = EXERCISE_NAME, isAccessory = false),
                mockExercise(name = EXERCISE_NAME_4, isAccessory = false)
            )
        val rotationHistory = emptyList<com.congen.model.ExerciseRotationHistory>()

        val result =
            exerciseSelectionService.selectRotatingExercise(
                targetMuscles,
                userEquipment,
                preferences,
                exercises,
                false,
                rotationHistory
            )

        assert(result != null)
        assert(result!!.name in listOf(EXERCISE_NAME, EXERCISE_NAME_4))
    }

    @Test
    fun `selectRotatingExercise should return exercise for accessory movement`() {
        val targetMuscles = listOf("biceps")
        val userEquipment = listOf(mockUserEquipment(equipmentName = "Dumbbells"))
        val preferences = emptyList<com.congen.model.UserExercisePreference>()
        val exercises =
            listOf(
                mockExercise(name = EXERCISE_NAME_6, isAccessory = true),
                mockExercise(name = EXERCISE_NAME_7, isAccessory = true)
            )
        val rotationHistory = emptyList<com.congen.model.ExerciseRotationHistory>()

        val result =
            exerciseSelectionService.selectRotatingExercise(
                targetMuscles,
                userEquipment,
                preferences,
                exercises,
                true,
                rotationHistory
            )

        assert(result != null)
        assert(result!!.isAccessory)
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
        val rotationHistory = emptyList<com.congen.model.ExerciseRotationHistory>()

        val result =
            exerciseSelectionService.selectRotatingExercise(
                targetMuscles,
                userEquipment,
                preferences,
                exercises,
                false,
                rotationHistory
            )

        assert(result != null)
        assert(result!!.name == EXERCISE_NAME_4)
    }

    @Test
    fun `selectRotatingExercise should prioritize unused exercises`() {
        val targetMuscles = listOf("chest")
        val userEquipment = listOf(mockUserEquipment(equipmentName = "Barbell"))
        val preferences = emptyList<com.congen.model.UserExercisePreference>()
        val exercises =
            listOf(
                mockExercise(name = EXERCISE_NAME, isAccessory = false),
                mockExercise(name = EXERCISE_NAME_4, isAccessory = false)
            )
        val rotationHistory =
            listOf(
                mockExerciseRotationHistory(exerciseName = EXERCISE_NAME, isAccessory = false)
            )

        val result =
            exerciseSelectionService.selectRotatingExercise(
                targetMuscles,
                userEquipment,
                preferences,
                exercises,
                false,
                rotationHistory
            )

        assert(result != null)
        assert(result!!.name == EXERCISE_NAME_4)
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
        val rotationHistory = emptyList<com.congen.model.ExerciseRotationHistory>()

        val result =
            exerciseSelectionService.selectRotatingExercise(
                targetMuscles,
                userEquipment,
                preferences,
                exercises,
                false,
                rotationHistory
            )

        assert(result == null)
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
}
