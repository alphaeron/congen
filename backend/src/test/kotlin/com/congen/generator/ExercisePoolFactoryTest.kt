package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.MovementType
import com.congen.model.ProgramPreferences
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.WeightUnit
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import java.math.BigDecimal
import java.time.Instant

/**
 * Unit tests for the ExercisePoolFactory.
 *
 * These tests verify that the factory correctly creates user exercise pools
 * using prepared data instead of making database calls.
 */
class ExercisePoolFactoryTest {
    private lateinit var exercisePoolFactory: ExercisePoolFactory
    private lateinit var exerciseMatchingService: ExerciseMatchingService

    companion object {
        private const val USER_ID = "test-user-123"
        private val now = Instant.now()
    }

    @BeforeEach
    fun setUp() {
        exerciseMatchingService = mock()

        exercisePoolFactory =
            ExercisePoolFactory(
                exerciseMatchingService = exerciseMatchingService
            )
    }

    @Test
    fun `createPoolForUser should return pool with filtered exercises using prepared data`() {
        val preparedData = createSamplePreparedData()

        val result =
            exercisePoolFactory.createPoolFromPreparedData(
                allExercises = createSampleExercises(),
                userEquipment = preparedData.userEquipment,
                userExercisePreferences = preparedData.userExercisePreferences,
                previouslyUsedExercises = emptyList(),
                exerciseEquipmentMappings = preparedData.exerciseEquipmentMappings,
                exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                userId = preparedData.userId
            )

        assertThat(result.getAvailableExerciseCount()).isGreaterThan(0)
    }

    @Test
    fun `createPoolForUser should handle empty exercises list`() {
        val preparedData =
            createSamplePreparedData().copy(
                userExercisePool =
                    UserExercisePool(
                        allExercises = emptyList(),
                        preferences = emptyList(),
                        userEquipment = emptyList(),
                        exerciseEquipmentMappings = emptyMap(),
                        exerciseMuscleMappings = emptyMap(),
                        previouslyUsedExercises = emptyList(),
                        userId = USER_ID
                    )
            )

        val result =
            exercisePoolFactory.createPoolFromPreparedData(
                allExercises = emptyList(),
                userEquipment = preparedData.userEquipment,
                userExercisePreferences = preparedData.userExercisePreferences,
                previouslyUsedExercises = emptyList(),
                exerciseEquipmentMappings = preparedData.exerciseEquipmentMappings,
                exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                userId = preparedData.userId
            )

        assertThat(result.getAvailableExerciseCount()).isEqualTo(0)
    }

    @Test
    fun `createPoolForUser should filter exercises based on user preferences`() {
        val exercises = createSampleExercises()
        val preferences =
            listOf(
                UserExercisePreference(
                    userId = USER_ID,
                    exerciseName = "Bench Press",
                    shouldAvoid = true,
                    createdAt = now
                )
            )
        val preparedData =
            createSamplePreparedData().copy(
                userExercisePool =
                    UserExercisePool(
                        allExercises = exercises,
                        preferences = preferences,
                        userEquipment = createSampleUserEquipment(),
                        exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
                        exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
                        previouslyUsedExercises = emptyList(),
                        userId = USER_ID
                    )
            )

        val result =
            exercisePoolFactory.createPoolFromPreparedData(
                allExercises = createSampleExercises(),
                userEquipment = preparedData.userEquipment,
                userExercisePreferences = preferences,
                previouslyUsedExercises = emptyList(),
                exerciseEquipmentMappings = preparedData.exerciseEquipmentMappings,
                exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                userId = preparedData.userId
            )

        assertThat(result.getAvailableExerciseCount()).isEqualTo(2)
        assertThat(result.getAvailableExercises().any { it.name == "Bench Press" }).isFalse()
    }

    @Test
    fun `createPoolForUser should handle exercises with different movement types`() {
        val exercises =
            listOf(
                Exercise("Bench Press", "Sample exercise description", MovementType.HORIZONTAL_PUSH, false, true, false),
                Exercise("Squat", "Sample exercise description", MovementType.SQUAT, false, false, false),
                Exercise("Deadlift", "Sample exercise description", MovementType.HINGE, false, false, false),
                Exercise("Push-ups", "Sample exercise description", MovementType.HORIZONTAL_PUSH, false, true, true),
                Exercise("Burpees", "Sample exercise description", MovementType.PLYOMETRIC, false, false, false)
            )
        val preparedData =
            createSamplePreparedData().copy(
                userExercisePool =
                    UserExercisePool(
                        allExercises = exercises,
                        preferences = emptyList(),
                        userEquipment = createSampleUserEquipment(),
                        exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
                        exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
                        previouslyUsedExercises = emptyList(),
                        userId = USER_ID
                    )
            )

        val result =
            exercisePoolFactory.createPoolFromPreparedData(
                allExercises = exercises,
                userEquipment = preparedData.userEquipment,
                userExercisePreferences = preparedData.userExercisePreferences,
                previouslyUsedExercises = emptyList(),
                exerciseEquipmentMappings = preparedData.exerciseEquipmentMappings,
                exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                userId = preparedData.userId
            )

        assertThat(result.getAvailableExerciseCount()).isEqualTo(5)
        assertThat(result.getAvailablePrimaryExercises().size).isEqualTo(4)
        assertThat(
            result.getAvailableAccessoryUpperExercises().size + result.getAvailableAccessoryLowerExercises().size
        ).isEqualTo(1)
    }

    @Test
    fun `createPoolForUser should handle previously used exercises`() {
        val exercises = createSampleExercises()
        val previouslyUsedExercises = listOf("Bench Press", "Squat")
        val preparedData =
            createSamplePreparedData().copy(
                userExercisePool =
                    UserExercisePool(
                        allExercises = exercises,
                        preferences = emptyList(),
                        userEquipment = createSampleUserEquipment(),
                        exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
                        exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
                        previouslyUsedExercises = previouslyUsedExercises,
                        userId = USER_ID
                    )
            )

        val result =
            exercisePoolFactory.createPoolFromPreparedData(
                allExercises = createSampleExercises(),
                userEquipment = preparedData.userEquipment,
                userExercisePreferences = preparedData.userExercisePreferences,
                previouslyUsedExercises = emptyList(),
                exerciseEquipmentMappings = preparedData.exerciseEquipmentMappings,
                exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                userId = preparedData.userId
            )

        assertThat(result.getAvailableExerciseCount()).isEqualTo(3)
    }

    @Test
    fun `createPoolForUser should handle user equipment filtering`() {
        val exercises = createSampleExercises()
        val userEquipment =
            listOf(
                UserEquipment(USER_ID, "power bar", now),
                UserEquipment(USER_ID, "bench", now)
            )
        val preparedData =
            createSamplePreparedData().copy(
                userExercisePool =
                    UserExercisePool(
                        allExercises = exercises,
                        preferences = emptyList(),
                        userEquipment = userEquipment,
                        exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
                        exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
                        previouslyUsedExercises = emptyList(),
                        userId = USER_ID
                    )
            )

        val result =
            exercisePoolFactory.createPoolFromPreparedData(
                allExercises = createSampleExercises(),
                userEquipment = preparedData.userEquipment,
                userExercisePreferences = preparedData.userExercisePreferences,
                previouslyUsedExercises = emptyList(),
                exerciseEquipmentMappings = preparedData.exerciseEquipmentMappings,
                exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                userId = preparedData.userId
            )

        assertThat(result.getAvailableExerciseCount()).isEqualTo(3)
    }

    @Test
    fun `createPoolForUser should handle mixed preferences`() {
        val exercises = createSampleExercises()
        val preferences =
            listOf(
                UserExercisePreference(
                    userId = USER_ID,
                    exerciseName = "Bench Press",
                    shouldAvoid = true,
                    createdAt = now
                ),
                UserExercisePreference(
                    userId = USER_ID,
                    exerciseName = "Squat",
                    shouldAvoid = false,
                    createdAt = now
                )
            )
        val preparedData =
            createSamplePreparedData().copy(
                userExercisePool =
                    UserExercisePool(
                        allExercises = exercises,
                        preferences = preferences,
                        userEquipment = createSampleUserEquipment(),
                        exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
                        exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
                        previouslyUsedExercises = emptyList(),
                        userId = USER_ID
                    )
            )

        val result =
            exercisePoolFactory.createPoolFromPreparedData(
                allExercises = createSampleExercises(),
                userEquipment = preparedData.userEquipment,
                userExercisePreferences = preferences,
                previouslyUsedExercises = emptyList(),
                exerciseEquipmentMappings = preparedData.exerciseEquipmentMappings,
                exerciseMuscleMappings = preparedData.exerciseMuscleMappings,
                userId = preparedData.userId
            )

        assertThat(result.getAvailableExerciseCount()).isEqualTo(2)
        assertThat(result.getAvailableExercises().any { it.name == "Bench Press" }).isFalse()
        assertThat(result.getAvailableExercises().any { it.name == "Squat" }).isTrue()
        assertThat(result.getAvailableExercises().any { it.name == "Deadlift" }).isTrue()
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

    private fun createSamplePreferences(): List<UserExercisePreference> {
        return emptyList()
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

    private fun createSamplePreparedData(): WorkoutGenerationPreparedData {
        return WorkoutGenerationPreparedData(
            userExercisePool =
                UserExercisePool(
                    allExercises = createSampleExercises(),
                    preferences = createSamplePreferences(),
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
            weakMuscles = emptyList(),
            currentWeekNumber = 1,
            userId = USER_ID,
            weightUnitPreferences = mapOf("Bench Press" to WeightUnit.LBS),
            exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
            exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
            exerciseWorkoutTypeMappings =
                mapOf(
                    "Bench Press" to listOf("maximal_effort")
                ),
            previouslyProgrammedExercises = emptyList(),
            allExercises = createSampleExercises(),
            userEquipment = createSampleUserEquipment(),
            userExercisePreferences = createSamplePreferences()
        )
    }
}
