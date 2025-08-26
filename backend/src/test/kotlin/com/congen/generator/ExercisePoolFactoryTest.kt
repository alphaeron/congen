package com.congen.generator

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.model.Exercise
import com.congen.model.MovementType
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for the ExercisePoolFactory.
 *
 * These tests verify that the factory correctly creates user exercise pools
 * by filtering exercises based on user equipment, preferences, and workout types.
 */
class ExercisePoolFactoryTest {
    private lateinit var exercisePoolFactory: ExercisePoolFactory
    private lateinit var exerciseDAL: ExerciseDAL
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL
    private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL
    private lateinit var exerciseWorkoutTypeDAL: ExerciseWorkoutTypeDAL
    private lateinit var exerciseMatchingService: ExerciseMatchingService
    private lateinit var userEquipmentDAL: UserEquipmentDAL
    private lateinit var userExercisePreferenceDAL: UserExercisePreferenceDAL

    companion object {
        private const val USER_ID = "test-user-123"
    }

    @BeforeEach
    fun setUp() {
        exerciseDAL = mock()
        exerciseEquipmentDAL = mock()
        exerciseMuscleDAL = mock()
        exerciseWorkoutTypeDAL = mock()
        exerciseMatchingService = mock()
        userEquipmentDAL = mock()
        userExercisePreferenceDAL = mock()

        exercisePoolFactory =
            ExercisePoolFactory(
                exerciseEquipmentDAL = exerciseEquipmentDAL,
                exerciseMuscleDAL = exerciseMuscleDAL,
                exerciseWorkoutTypeDAL = exerciseWorkoutTypeDAL,
                exerciseMatchingService = exerciseMatchingService,
                exerciseDAL = exerciseDAL,
                userEquipmentDAL = userEquipmentDAL,
                userExercisePreferenceDAL = userExercisePreferenceDAL
            )
    }

    @Test
    fun `createPoolForUser should return pool with filtered exercises`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val preferences = createSamplePreferences()

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.just(userEquipment))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(USER_ID)).thenReturn(Mono.just(preferences))

        val result = exercisePoolFactory.createPoolForUser(USER_ID)

        StepVerifier.create(result)
            .expectNextMatches { pool ->
                pool.getAvailableExerciseCount() > 0
            }
            .verifyComplete()

        verify(exerciseDAL).selectExercises()
        verify(userEquipmentDAL).selectUserEquipmentByUser(USER_ID)
        verify(userExercisePreferenceDAL).selectUserExercisePreferencesByUser(USER_ID)
    }

    @Test
    fun `createPoolForUser should handle empty exercises list`() {
        val emptyExercises = emptyList<Exercise>()
        val userEquipment = createSampleUserEquipment()
        val preferences = createSamplePreferences()

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(emptyExercises))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.just(userEquipment))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(USER_ID)).thenReturn(Mono.just(preferences))

        val result = exercisePoolFactory.createPoolForUser(USER_ID)

        StepVerifier.create(result)
            .expectNextMatches { pool ->
                pool.getAvailableExerciseCount() == 0
            }
            .verifyComplete()
    }

    @Test
    fun `createPoolForUser should handle empty user equipment`() {
        val exercises = createSampleExercises()
        val emptyUserEquipment = emptyList<UserEquipment>()
        val preferences = createSamplePreferences()

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.just(emptyUserEquipment))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(USER_ID)).thenReturn(Mono.just(preferences))

        val result = exercisePoolFactory.createPoolForUser(USER_ID)

        StepVerifier.create(result)
            .expectNextMatches { pool ->
                pool.getAvailableExerciseCount() > 0
            }
            .verifyComplete()
    }

    @Test
    fun `createPoolForUser should handle empty preferences`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()
        val emptyPreferences = emptyList<UserExercisePreference>()

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.just(userEquipment))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(USER_ID)).thenReturn(Mono.just(emptyPreferences))

        val result = exercisePoolFactory.createPoolForUser(USER_ID)

        StepVerifier.create(result)
            .expectNextMatches { pool ->
                pool.getAvailableExerciseCount() > 0
            }
            .verifyComplete()
    }

    @Test
    fun `createPoolForUser should filter out exercises user wants to avoid`() {
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

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.just(userEquipment))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(USER_ID)).thenReturn(Mono.just(preferences))

        val result = exercisePoolFactory.createPoolForUser(USER_ID)

        StepVerifier.create(result)
            .expectNextMatches { pool ->
                !pool.getAvailableExercises().any { it.name == "Bench Press" }
            }
            .verifyComplete()
    }

    @Test
    fun `createPoolForUser should handle DAL errors gracefully`() {
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = exercisePoolFactory.createPoolForUser(USER_ID)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `createPoolForUser should handle user equipment DAL errors`() {
        val exercises = createSampleExercises()

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = exercisePoolFactory.createPoolForUser(USER_ID)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `createPoolForUser should handle preferences DAL errors`() {
        val exercises = createSampleExercises()
        val userEquipment = createSampleUserEquipment()

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.just(userEquipment))
        whenever(
            userExercisePreferenceDAL.selectUserExercisePreferencesByUser(USER_ID)
        ).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = exercisePoolFactory.createPoolForUser(USER_ID)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `createPoolForUser should handle mixed preferences`() {
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

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.just(userEquipment))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(USER_ID)).thenReturn(Mono.just(preferences))

        val result = exercisePoolFactory.createPoolForUser(USER_ID)

        StepVerifier.create(result)
            .expectNextMatches { pool ->
                !pool.getAvailableExercises().any { it.name == "Bench Press" } &&
                    pool.getAvailableExercises().any { it.name == "Squat" }
            }
            .verifyComplete()
    }

    @Test
    fun `createPoolForUser should handle exercises with no equipment requirements`() {
        val exercises =
            listOf(
                createExercise("Bodyweight Squat", MovementType.SQUAT),
                createExercise("Push-up", MovementType.HORIZONTAL_PUSH)
            )
        val userEquipment = createSampleUserEquipment()
        val preferences = createSamplePreferences()

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.just(userEquipment))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(USER_ID)).thenReturn(Mono.just(preferences))

        val result = exercisePoolFactory.createPoolForUser(USER_ID)

        StepVerifier.create(result)
            .expectNextMatches { pool ->
                pool.getAvailableExerciseCount() > 0
            }
            .verifyComplete()
    }

    @Test
    fun `createPoolForUser should handle exercises with multiple equipment requirements`() {
        val exercises =
            listOf(
                createExercise("Barbell Bench Press", MovementType.HORIZONTAL_PUSH),
                createExercise("Dumbbell Bench Press", MovementType.HORIZONTAL_PUSH)
            )
        val userEquipment =
            listOf(
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
        val preferences = createSamplePreferences()

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.just(userEquipment))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(USER_ID)).thenReturn(Mono.just(preferences))

        val result = exercisePoolFactory.createPoolForUser(USER_ID)

        StepVerifier.create(result)
            .expectNextMatches { pool ->
                pool.getAvailableExerciseCount() > 0
            }
            .verifyComplete()
    }

    private fun createSampleExercises(): List<Exercise> {
        return listOf(
            createExercise("Bench Press", MovementType.HORIZONTAL_PUSH),
            createExercise("Squat", MovementType.SQUAT),
            createExercise("Deadlift", MovementType.HINGE)
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

    private fun createSamplePreferences(): List<UserExercisePreference> {
        return listOf(
            UserExercisePreference(
                userId = USER_ID,
                exerciseName = "Bench Press",
                shouldAvoid = false,
                createdAt = Instant.now()
            )
        )
    }

    private fun createExercise(
        name: String,
        movementType: MovementType
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
            isAccessory = false
        )
    }
}
