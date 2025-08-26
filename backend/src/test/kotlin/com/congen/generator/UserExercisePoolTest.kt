package com.congen.generator

import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
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
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
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
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL
    private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL

    companion object {
        private const val USER_ID = "test-user-123"
    }

    @BeforeEach
    fun setUp() {
        exerciseEquipmentDAL = mock()
        exerciseMuscleDAL = mock()
    }

    @Test
    fun `should initialize with all exercises when no preferences exist`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
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
                    createdAt = Instant.now()
                )
            )

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
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
                    createdAt = Instant.now()
                )
            )

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        assertEquals(3, userExercisePool.getAvailableExerciseCount())
        assertTrue(userExercisePool.getAvailableExercises().any { it.name == "Bench Press" })
    }

    @Test
    fun `should handle mixed preferences correctly`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences =
            listOf(
                UserExercisePreference(
                    userId = USER_ID,
                    exerciseName = "Bench Press",
                    shouldAvoid = true,
                    createdAt = Instant.now()
                ),
                UserExercisePreference(
                    userId = USER_ID,
                    exerciseName = "Squat",
                    shouldAvoid = false,
                    createdAt = Instant.now()
                )
            )

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        assertEquals(2, userExercisePool.getAvailableExerciseCount())
        assertFalse(userExercisePool.getAvailableExercises().any { it.name == "Bench Press" })
        assertTrue(userExercisePool.getAvailableExercises().any { it.name == "Squat" })
        assertTrue(userExercisePool.getAvailableExercises().any { it.name == "Deadlift" })
    }

    @Test
    fun `should correctly separate primary and accessory exercises`() {
        val exercises = createSampleExercisesWithAccessories()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        assertEquals(5, userExercisePool.getAvailableExerciseCount())
        assertEquals(3, userExercisePool.getAvailablePrimaryExercises().size)
        assertEquals(2, userExercisePool.getAvailableAccessoryExercises().size)
    }

    @Test
    fun `markExerciseAsUsed should remove exercise from available pool`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        val result = userExercisePool.markExerciseAsUsed("Bench Press")

        assertTrue(result)
        assertEquals(2, userExercisePool.getAvailableExerciseCount())
        assertFalse(userExercisePool.getAvailableExercises().any { it.name == "Bench Press" })
    }

    @Test
    fun `markExerciseAsUsed should return false for already used exercise`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        val firstResult = userExercisePool.markExerciseAsUsed("Bench Press")
        val secondResult = userExercisePool.markExerciseAsUsed("Bench Press")

        assertTrue(firstResult)
        assertFalse(secondResult)
        assertEquals(2, userExercisePool.getAvailableExerciseCount())
    }

    @Test
    fun `markExerciseAsUsed should return false for non-existent exercise`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        val result = userExercisePool.markExerciseAsUsed("Non-existent Exercise")

        assertFalse(result)
        assertEquals(3, userExercisePool.getAvailableExerciseCount())
    }

    @Test
    fun `filterExercisesByEquipment should return empty list for empty exercises`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        val result = userExercisePool.filterExercisesByEquipment(emptyList())

        StepVerifier.create(result)
            .expectNext(emptyList<Exercise>())
            .verifyComplete()
    }

    @Test
    fun `filterExercisesByEquipment should filter exercises based on user equipment`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        val exerciseEquipment =
            listOf(
                ExerciseEquipment(exerciseName = "Bench Press", equipmentName = "Barbell"),
                ExerciseEquipment(exerciseName = "Bench Press", equipmentName = "Bench")
            )

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise("Bench Press"))
            .thenReturn(Mono.just(exerciseEquipment))

        val result = userExercisePool.filterExercisesByEquipment(listOf(exercises[0]))

        StepVerifier.create(result)
            .expectNext(listOf(exercises[0]))
            .verifyComplete()
    }

    @Test
    fun `filterExercisesByEquipment should return all exercises when no equipment matches`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        val exerciseEquipment =
            listOf(
                ExerciseEquipment(exerciseName = "Bench Press", equipmentName = "Dumbbell")
            )

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise("Bench Press"))
            .thenReturn(Mono.just(exerciseEquipment))

        val result = userExercisePool.filterExercisesByEquipment(listOf(exercises[0]))

        StepVerifier.create(result)
            .expectNext(listOf(exercises[0]))
            .verifyComplete()
    }

    @Test
    fun `filterExercisesByEquipment should handle DAL errors gracefully`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise("Bench Press"))
            .thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userExercisePool.filterExercisesByEquipment(listOf(exercises[0]))

        StepVerifier.create(result)
            .expectNext(listOf(exercises[0]))
            .verifyComplete()
    }

    @Test
    fun `filterExercisesByMuscles should return all exercises when no target muscles specified`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        val result = userExercisePool.filterExercisesByMuscles(exercises, emptyList(), exerciseMuscleDAL)

        StepVerifier.create(result)
            .expectNext(exercises)
            .verifyComplete()
    }

    @Test
    fun `filterExercisesByMuscles should return all exercises when exercises list is empty`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        val result = userExercisePool.filterExercisesByMuscles(emptyList(), listOf("Chest"), exerciseMuscleDAL)

        StepVerifier.create(result)
            .expectNext(emptyList<Exercise>())
            .verifyComplete()
    }

    @Test
    fun `filterExercisesByMuscles should filter exercises based on target muscles`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        val exerciseMuscles =
            listOf(
                ExerciseMuscle(exerciseName = "Bench Press", muscleName = "Chest")
            )

        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise("Bench Press"))
            .thenReturn(Mono.just(exerciseMuscles))

        val result = userExercisePool.filterExercisesByMuscles(listOf(exercises[0]), listOf("Chest"), exerciseMuscleDAL)

        StepVerifier.create(result)
            .expectNext(listOf(exercises[0]))
            .verifyComplete()
    }

    @Test
    fun `filterExercisesByMuscles should handle DAL errors gracefully`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise("Bench Press"))
            .thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userExercisePool.filterExercisesByMuscles(listOf(exercises[0]), listOf("Chest"), exerciseMuscleDAL)

        StepVerifier.create(result)
            .expectNext(listOf(exercises[0]))
            .verifyComplete()
    }

    @Test
    fun `should handle case insensitive equipment matching`() {
        val exercises = createSampleExercises()
        val userEquipment =
            listOf(
                UserEquipment(userId = USER_ID, equipmentName = "BARBELL", createdAt = Instant.now()),
                UserEquipment(userId = USER_ID, equipmentName = "bench", createdAt = Instant.now())
            )
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        val exerciseEquipment =
            listOf(
                ExerciseEquipment(exerciseName = "Bench Press", equipmentName = "Barbell"),
                ExerciseEquipment(exerciseName = "Bench Press", equipmentName = "Bench")
            )

        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise("Bench Press"))
            .thenReturn(Mono.just(exerciseEquipment))

        val result = userExercisePool.filterExercisesByEquipment(listOf(exercises[0]))

        StepVerifier.create(result)
            .expectNext(listOf(exercises[0]))
            .verifyComplete()
    }

    @Test
    fun `should handle case insensitive muscle matching`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = emptyList<UserExercisePreference>()

        userExercisePool =
            UserExercisePool(
                allExercises = exercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL
            )

        val exerciseMuscles =
            listOf(
                ExerciseMuscle(exerciseName = "Bench Press", muscleName = "Chest")
            )

        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise("Bench Press"))
            .thenReturn(Mono.just(exerciseMuscles))

        val result = userExercisePool.filterExercisesByMuscles(listOf(exercises[0]), listOf("CHEST"), exerciseMuscleDAL)

        StepVerifier.create(result)
            .expectNext(listOf(exercises[0]))
            .verifyComplete()
    }

    private fun createSampleExercises(): List<Exercise> {
        return listOf(
            createExercise("Bench Press", MovementType.HORIZONTAL_PUSH),
            createExercise("Squat", MovementType.SQUAT),
            createExercise("Deadlift", MovementType.HINGE)
        )
    }

    private fun createSampleExercisesWithAccessories(): List<Exercise> {
        return listOf(
            createExercise("Bench Press", MovementType.HORIZONTAL_PUSH),
            createExercise("Squat", MovementType.SQUAT),
            createExercise("Deadlift", MovementType.HINGE),
            createExercise("Bicep Curl", MovementType.ISOLATION, isAccessory = true),
            createExercise("Tricep Extension", MovementType.ISOLATION, isAccessory = true)
        )
    }

    private fun createSampleUserEquipment(): List<UserEquipment> {
        return listOf(
            UserEquipment(
                userId = USER_ID,
                equipmentName = "Barbell",
                createdAt = Instant.now()
            ),
            UserEquipment(
                userId = USER_ID,
                equipmentName = "Bench",
                createdAt = Instant.now()
            )
        )
    }

    private fun createExercise(
        name: String,
        movementType: MovementType,
        isAccessory: Boolean = false
    ): Exercise {
        return Exercise(
            name = name,
            description = "Test exercise description",
            movementType = movementType,
            isUnilateral = false,
            isUpper =
                movementType in
                    listOf(
                        MovementType.HORIZONTAL_PUSH,
                        MovementType.VERTICAL_PUSH,
                        MovementType.HORIZONTAL_PULL,
                        MovementType.VERTICAL_PULL
                    ),
            isAccessory = isAccessory
        )
    }
}
