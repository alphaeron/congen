package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.MovementType
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for the UserExercisePool.
 *
 * These tests verify that the exercise pool correctly manages available exercises,
 * handles user preferences, and provides thread-safe exercise filtering.
 */
class UserExercisePoolTest {
    private lateinit var userExercisePool: UserExercisePool

    companion object {
        private const val USER_ID = "test-user-123"
        private val now = Instant.now()
    }

    @BeforeEach
    fun setUp() {
        // Setup will be done in individual test methods
    }

    @Test
    fun `should initialize with all exercises when no preferences exist`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()
        val exerciseEquipmentMappings = createSampleExerciseEquipmentMappings()
        val exerciseMuscleMappings = createSampleExerciseMuscleMappings()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentMappings = exerciseEquipmentMappings,
                exerciseMuscleMappings = exerciseMuscleMappings,
                previouslyUsedExercises = emptyList(),
                userId = USER_ID
            )

        assertEquals(3, userExercisePool.getAvailableExerciseCount())
        assertEquals(3, userExercisePool.getAvailableExercises().size)
        assertEquals(3, userExercisePool.getAvailablePrimaryExercises().size)
        assertEquals(0, userExercisePool.getAvailableAccessoryExercises().size)
    }

    @Test
    fun `should filter out exercises that user wants to avoid`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences =
            listOf(
                UserExercisePreference(
                    userId = USER_ID,
                    exerciseName = "Bench Press",
                    shouldAvoid = true,
                    createdAt = now
                )
            )
        val exerciseEquipmentMappings = createSampleExerciseEquipmentMappings()
        val exerciseMuscleMappings = createSampleExerciseMuscleMappings()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentMappings = exerciseEquipmentMappings,
                exerciseMuscleMappings = exerciseMuscleMappings,
                previouslyUsedExercises = emptyList(),
                userId = USER_ID
            )

        assertEquals(2, userExercisePool.getAvailableExerciseCount())
        assertFalse(userExercisePool.getAvailableExercises().any { it.name == "Bench Press" })
        assertTrue(userExercisePool.getAvailableExercises().any { it.name == "Squat" })
        assertTrue(userExercisePool.getAvailableExercises().any { it.name == "Deadlift" })
    }

    @Test
    fun `should include exercises that user prefers`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences =
            listOf(
                UserExercisePreference(
                    userId = USER_ID,
                    exerciseName = "Bench Press",
                    shouldAvoid = false,
                    createdAt = now
                )
            )
        val exerciseEquipmentMappings = createSampleExerciseEquipmentMappings()
        val exerciseMuscleMappings = createSampleExerciseMuscleMappings()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentMappings = exerciseEquipmentMappings,
                exerciseMuscleMappings = exerciseMuscleMappings,
                previouslyUsedExercises = emptyList(),
                userId = USER_ID
            )

        assertEquals(3, userExercisePool.getAvailableExerciseCount())
        assertTrue(userExercisePool.getAvailableExercises().any { it.name == "Bench Press" })
        assertTrue(userExercisePool.getAvailableExercises().any { it.name == "Squat" })
        assertTrue(userExercisePool.getAvailableExercises().any { it.name == "Deadlift" })
    }

    @Test
    fun `should filter exercises by equipment availability`() {
        val exercises = createSampleExercises()
        val userEquipment =
            listOf(
                UserEquipment(USER_ID, "power bar", now),
                UserEquipment(USER_ID, "bench", now)
            )
        val preferences = emptyList<UserExercisePreference>()
        val exerciseEquipmentMappings = createSampleExerciseEquipmentMappings()
        val exerciseMuscleMappings = createSampleExerciseMuscleMappings()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentMappings = exerciseEquipmentMappings,
                exerciseMuscleMappings = exerciseMuscleMappings,
                previouslyUsedExercises = emptyList(),
                userId = USER_ID
            )

        val result =
            userExercisePool.filterExercisesByEquipment(
                exercises = exercises,
                isPrimaryExercise = false,
                isUpperBody = true
            )

        StepVerifier.create(result)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `should filter exercises by muscle groups`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()
        val exerciseEquipmentMappings = createSampleExerciseEquipmentMappings()
        val exerciseMuscleMappings = createSampleExerciseMuscleMappings()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentMappings = exerciseEquipmentMappings,
                exerciseMuscleMappings = exerciseMuscleMappings,
                previouslyUsedExercises = emptyList(),
                userId = USER_ID
            )

        val result =
            userExercisePool.filterExercisesByMuscles(
                exercises = exercises,
                targetMuscles = listOf("chest", "triceps")
            )

        StepVerifier.create(result)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `should track previously used exercises`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()
        val exerciseEquipmentMappings = createSampleExerciseEquipmentMappings()
        val exerciseMuscleMappings = createSampleExerciseMuscleMappings()
        val previouslyUsedExercises = listOf("Bench Press", "Squat")

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentMappings = exerciseEquipmentMappings,
                exerciseMuscleMappings = exerciseMuscleMappings,
                previouslyUsedExercises = previouslyUsedExercises,
                userId = USER_ID
            )

        assertEquals(3, userExercisePool.getAvailableExerciseCount())
        assertTrue(userExercisePool.getAvailableExercises().any { it.name == "Bench Press" })
        assertTrue(userExercisePool.getAvailableExercises().any { it.name == "Squat" })
        assertTrue(userExercisePool.getAvailableExercises().any { it.name == "Deadlift" })
    }

    @Test
    fun `should return correct user ID`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()
        val exerciseEquipmentMappings = createSampleExerciseEquipmentMappings()
        val exerciseMuscleMappings = createSampleExerciseMuscleMappings()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentMappings = exerciseEquipmentMappings,
                exerciseMuscleMappings = exerciseMuscleMappings,
                previouslyUsedExercises = emptyList(),
                userId = USER_ID
            )

        assertEquals(USER_ID, userExercisePool.getUserId())
    }

    @Test
    fun `should handle empty exercise list`() {
        val exercises = emptyList<Exercise>()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()
        val exerciseEquipmentMappings = createSampleExerciseEquipmentMappings()
        val exerciseMuscleMappings = createSampleExerciseMuscleMappings()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentMappings = exerciseEquipmentMappings,
                exerciseMuscleMappings = exerciseMuscleMappings,
                previouslyUsedExercises = emptyList(),
                userId = USER_ID
            )

        assertEquals(0, userExercisePool.getAvailableExerciseCount())
        assertTrue(userExercisePool.getAvailableExercises().isEmpty())
        assertTrue(userExercisePool.getAvailablePrimaryExercises().isEmpty())
        assertTrue(userExercisePool.getAvailableAccessoryExercises().isEmpty())
    }

    @Test
    fun `should handle exercises with different movement types`() {
        val exercises =
            listOf(
                Exercise("Bench Press", "Sample exercise description", MovementType.HORIZONTAL_PUSH, false, true, false),
                Exercise("Squat", "Sample exercise description", MovementType.SQUAT, false, false, false),
                Exercise("Deadlift", "Sample exercise description", MovementType.HINGE, false, false, false),
                Exercise("Push-ups", "Sample exercise description", MovementType.HORIZONTAL_PUSH, false, true, true),
                Exercise("Burpees", "Sample exercise description", MovementType.PLYOMETRIC, false, false, false)
            )
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()
        val exerciseEquipmentMappings = createSampleExerciseEquipmentMappings()
        val exerciseMuscleMappings = createSampleExerciseMuscleMappings()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentMappings = exerciseEquipmentMappings,
                exerciseMuscleMappings = exerciseMuscleMappings,
                previouslyUsedExercises = emptyList(),
                userId = USER_ID
            )

        assertEquals(5, userExercisePool.getAvailableExerciseCount())
        assertEquals(4, userExercisePool.getAvailablePrimaryExercises().size)
        assertEquals(1, userExercisePool.getAvailableAccessoryExercises().size)
    }

    private fun createSampleExercises(): List<Exercise> {
        return listOf(
            Exercise("Bench Press", "Sample exercise description", MovementType.HORIZONTAL_PUSH, false, true, false),
            Exercise("Squat", "Sample exercise description", MovementType.SQUAT, false, false, false),
            Exercise("Deadlift", "Sample exercise description", MovementType.HINGE, false, false, false)
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
