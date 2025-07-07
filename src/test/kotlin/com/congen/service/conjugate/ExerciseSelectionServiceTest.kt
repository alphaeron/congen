package com.congen.service.conjugate

import com.congen.model.Exercise
import com.congen.model.ExerciseRotationHistory
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ExerciseSelectionServiceTest {
    private lateinit var exerciseSelectionService: ExerciseSelectionService

    @BeforeEach
    fun setUp() {
        exerciseSelectionService = ExerciseSelectionService()
    }

    @Test
    fun `determineWeakMuscles should return default weak muscles`() {
        val oneRepMaxes =
            listOf(
                UserOneRepMax(
                    userId = 1,
                    exerciseName = "Bench Press",
                    oneRepMax = BigDecimal("100.0"),
                    updatedAt = LocalDateTime.now()
                )
            )
        val rotationHistory =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    userId = 1,
                    exerciseName = "Bench Press",
                    isAccessory = false,
                    createdAt = LocalDateTime.now()
                )
            )

        val result = exerciseSelectionService.determineWeakMuscles(oneRepMaxes, rotationHistory)

        assertEquals(ConjugateConstants.DEFAULT_WEAK_MUSCLES, result)
        assertEquals(4, result.size)
        assertTrue(result.contains("hamstrings"))
        assertTrue(result.contains("glutes"))
        assertTrue(result.contains("upper_back"))
        assertTrue(result.contains("core"))
    }

    @Test
    fun `determineWeakMuscles should return default weak muscles for empty lists`() {
        val result = exerciseSelectionService.determineWeakMuscles(emptyList(), emptyList())

        assertEquals(ConjugateConstants.DEFAULT_WEAK_MUSCLES, result)
    }

    @Test
    fun `selectRotatingExercise should return null when no exercises available`() {
        val result =
            exerciseSelectionService.selectRotatingExercise(
                userId = 1,
                targetMuscles = listOf("chest"),
                userEquipment = emptyList(),
                preferences = emptyList(),
                exercises = emptyList(),
                isAccessory = false,
                rotationHistory = emptyList()
            )

        assertNull(result)
    }

    @Test
    fun `selectRotatingExercise should filter out avoided exercises`() {
        val exercises =
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "A compound upper body exercise",
                    movementType = "horizontal push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = false
                ),
                Exercise(
                    name = "Squat",
                    description = "A compound lower body exercise",
                    movementType = "vertical push",
                    isUnilateral = false,
                    isUpper = false,
                    isAccessory = false
                )
            )
        val preferences =
            listOf(
                UserExercisePreference(
                    userId = 1,
                    exerciseName = "Bench Press",
                    shouldAvoid = true,
                    createdAt = LocalDateTime.now()
                )
            )

        val result =
            exerciseSelectionService.selectRotatingExercise(
                userId = 1,
                targetMuscles = listOf("chest", "legs"),
                userEquipment = emptyList(),
                preferences = preferences,
                exercises = exercises,
                isAccessory = false,
                rotationHistory = emptyList()
            )

        assertNotNull(result)
        assertEquals("Squat", result.name)
    }

    @Test
    fun `selectRotatingExercise should prefer unused exercises`() {
        val exercises =
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "A compound upper body exercise",
                    movementType = "horizontal push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = false
                ),
                Exercise(
                    name = "Squat",
                    description = "A compound lower body exercise",
                    movementType = "vertical push",
                    isUnilateral = false,
                    isUpper = false,
                    isAccessory = false
                )
            )
        val rotationHistory =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    userId = 1,
                    exerciseName = "Bench Press",
                    isAccessory = false,
                    createdAt = LocalDateTime.now()
                )
            )

        val result =
            exerciseSelectionService.selectRotatingExercise(
                userId = 1,
                targetMuscles = listOf("chest", "legs"),
                userEquipment = emptyList(),
                preferences = emptyList(),
                exercises = exercises,
                isAccessory = false,
                rotationHistory = rotationHistory
            )

        assertNotNull(result)
        assertEquals("Bench Press", result.name) // Should prefer unused exercise (alphabetical tiebreaker)
    }

    @Test
    fun `selectRotatingExercise should return least recently used when all exercises used`() {
        val exercises =
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "A compound upper body exercise",
                    movementType = "horizontal push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = false
                ),
                Exercise(
                    name = "Squat",
                    description = "A compound lower body exercise",
                    movementType = "vertical push",
                    isUnilateral = false,
                    isUpper = false,
                    isAccessory = false
                )
            )
        val rotationHistory =
            listOf(
                ExerciseRotationHistory(
                    id = 1L,
                    userId = 1,
                    exerciseName = "Bench Press",
                    isAccessory = false,
                    createdAt = LocalDateTime.now()
                ),
                ExerciseRotationHistory(
                    id = 2L,
                    userId = 1,
                    exerciseName = "Bench Press",
                    isAccessory = false,
                    createdAt = LocalDateTime.now()
                ),
                ExerciseRotationHistory(
                    id = 3L,
                    userId = 1,
                    exerciseName = "Squat",
                    isAccessory = false,
                    createdAt = LocalDateTime.now()
                )
            )

        val result =
            exerciseSelectionService.selectRotatingExercise(
                userId = 1,
                targetMuscles = listOf("chest", "legs"),
                userEquipment = emptyList(),
                preferences = emptyList(),
                exercises = exercises,
                isAccessory = false,
                rotationHistory = rotationHistory
            )

        assertNotNull(result)
        assertEquals("Bench Press", result.name) // Should prefer least used exercise (alphabetical tiebreaker)
    }

    @Test
    fun `selectRotatingExercise should handle accessory exercises`() {
        val exercises =
            listOf(
                Exercise(
                    name = "Push-ups",
                    description = "A bodyweight exercise",
                    movementType = "horizontal push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = true
                ),
                Exercise(
                    name = "Pull-ups",
                    description = "A bodyweight exercise",
                    movementType = "vertical pull",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = true
                )
            )

        val result =
            exerciseSelectionService.selectRotatingExercise(
                userId = 1,
                targetMuscles = listOf("chest", "back"),
                userEquipment = emptyList(),
                preferences = emptyList(),
                exercises = exercises,
                isAccessory = true,
                rotationHistory = emptyList()
            )

        assertNotNull(result)
        assertTrue(result.isAccessory)
    }

    @Test
    fun `selectRotatingExercise should handle primary exercises`() {
        val exercises =
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "A compound upper body exercise",
                    movementType = "horizontal push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = false
                ),
                Exercise(
                    name = "Squat",
                    description = "A compound lower body exercise",
                    movementType = "vertical push",
                    isUnilateral = false,
                    isUpper = false,
                    isAccessory = false
                )
            )

        val result =
            exerciseSelectionService.selectRotatingExercise(
                userId = 1,
                targetMuscles = listOf("chest", "legs"),
                userEquipment = emptyList(),
                preferences = emptyList(),
                exercises = exercises,
                isAccessory = false,
                rotationHistory = emptyList()
            )

        assertNotNull(result)
        assertFalse(result.isAccessory)
    }

    @Test
    fun `filterExercisesByAccessoryStatus should filter accessory exercises`() {
        val exercises =
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "A compound upper body exercise",
                    movementType = "horizontal push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = false
                ),
                Exercise(
                    name = "Push-ups",
                    description = "A bodyweight exercise",
                    movementType = "horizontal push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = true
                ),
                Exercise(
                    name = "Squat",
                    description = "A compound lower body exercise",
                    movementType = "vertical push",
                    isUnilateral = false,
                    isUpper = false,
                    isAccessory = false
                )
            )

        val accessoryExercises = exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, true)
        val primaryExercises = exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, false)

        assertEquals(1, accessoryExercises.size)
        assertEquals("Push-ups", accessoryExercises[0].name)
        assertEquals(2, primaryExercises.size)
        assertTrue(primaryExercises.any { it.name == "Bench Press" })
        assertTrue(primaryExercises.any { it.name == "Squat" })
    }

    @Test
    fun `filterExercisesByAccessoryStatus should return empty list for no matches`() {
        val exercises =
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "A compound upper body exercise",
                    movementType = "horizontal push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = false
                )
            )

        val result = exerciseSelectionService.filterExercisesByAccessoryStatus(exercises, true)

        assertTrue(result.isEmpty())
    }

    @Test
    fun `filterExercisesExcluding should exclude specified exercise`() {
        val exercises =
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "A compound upper body exercise",
                    movementType = "horizontal push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = false
                ),
                Exercise(
                    name = "Squat",
                    description = "A compound lower body exercise",
                    movementType = "vertical push",
                    isUnilateral = false,
                    isUpper = false,
                    isAccessory = false
                ),
                Exercise(
                    name = "Deadlift",
                    description = "A compound lower body exercise",
                    movementType = "hinge",
                    isUnilateral = false,
                    isUpper = false,
                    isAccessory = false
                )
            )

        val result = exerciseSelectionService.filterExercisesExcluding(exercises, "Bench Press")

        assertEquals(2, result.size)
        assertTrue(result.any { it.name == "Squat" })
        assertTrue(result.any { it.name == "Deadlift" })
        assertFalse(result.any { it.name == "Bench Press" })
    }

    @Test
    fun `filterExercisesExcluding should return all exercises when exercise not found`() {
        val exercises =
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "A compound upper body exercise",
                    movementType = "horizontal push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = false
                ),
                Exercise(
                    name = "Squat",
                    description = "A compound lower body exercise",
                    movementType = "vertical push",
                    isUnilateral = false,
                    isUpper = false,
                    isAccessory = false
                )
            )

        val result = exerciseSelectionService.filterExercisesExcluding(exercises, "NonExistentExercise")

        assertEquals(2, result.size)
        assertTrue(result.any { it.name == "Bench Press" })
        assertTrue(result.any { it.name == "Squat" })
    }

    @Test
    fun `filterExercisesExcluding should return empty list for empty input`() {
        val result = exerciseSelectionService.filterExercisesExcluding(emptyList(), "Bench Press")

        assertTrue(result.isEmpty())
    }

    @Test
    fun `selectRotatingExercise should handle case sensitivity`() {
        val exercises =
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "A compound upper body exercise",
                    movementType = "horizontal push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = false
                )
            )
        val preferences =
            listOf(
                UserExercisePreference(
                    userId = 1,
                    exerciseName = "bench press", // Different case
                    shouldAvoid = true,
                    createdAt = LocalDateTime.now()
                )
            )

        val result =
            exerciseSelectionService.selectRotatingExercise(
                userId = 1,
                targetMuscles = listOf("chest"),
                userEquipment = emptyList(),
                preferences = preferences,
                exercises = exercises,
                isAccessory = false,
                rotationHistory = emptyList()
            )

        // Should not filter out due to case sensitivity
        assertNotNull(result)
        assertEquals("Bench Press", result.name)
    }

    @Test
    fun `selectRotatingExercise should handle multiple target muscles`() {
        val exercises =
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "A compound upper body exercise",
                    movementType = "horizontal push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = false
                ),
                Exercise(
                    name = "Squat",
                    description = "A compound lower body exercise",
                    movementType = "vertical push",
                    isUnilateral = false,
                    isUpper = false,
                    isAccessory = false
                )
            )

        val result =
            exerciseSelectionService.selectRotatingExercise(
                userId = 1,
                targetMuscles = listOf("chest", "shoulders", "triceps"),
                userEquipment = emptyList(),
                preferences = emptyList(),
                exercises = exercises,
                isAccessory = false,
                rotationHistory = emptyList()
            )

        assertNotNull(result)
        assertTrue(result.name in listOf("Bench Press", "Squat"))
    }

    @Test
    fun `selectRotatingExercise should handle empty target muscles`() {
        val exercises =
            listOf(
                Exercise(
                    name = "Bench Press",
                    description = "A compound upper body exercise",
                    movementType = "horizontal push",
                    isUnilateral = false,
                    isUpper = true,
                    isAccessory = false
                )
            )

        val result =
            exerciseSelectionService.selectRotatingExercise(
                userId = 1,
                targetMuscles = emptyList(),
                userEquipment = emptyList(),
                preferences = emptyList(),
                exercises = exercises,
                isAccessory = false,
                rotationHistory = emptyList()
            )

        assertNotNull(result)
        assertEquals("Bench Press", result.name)
    }
}
