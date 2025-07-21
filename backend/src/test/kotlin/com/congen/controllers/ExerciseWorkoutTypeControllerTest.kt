package com.congen.controllers

import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.exceptions.DatabaseException
import com.congen.exceptions.DatabaseQueryException
import com.congen.mockExerciseWorkoutType
import com.congen.model.ExerciseWorkoutType
import com.congen.model.MovementType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.util.stream.Stream
import kotlin.test.assertEquals

class ExerciseWorkoutTypeControllerTest {
    private lateinit var dal: ExerciseWorkoutTypeDAL
    private lateinit var controller: ExerciseWorkoutTypeController

    companion object {
        private const val EXERCISE_NAME = "Bench Press"
        private val MOVEMENT_TYPE = MovementType.HORIZONTAL_PUSH
        private const val WORKOUT_TYPE = "dynamic_effort"
        private const val SQUAT_NAME = "Back Squat"
        private val SQUAT_MOVEMENT = MovementType.SQUAT
        private const val MAXIMAL_EFFORT = "maximal_effort"
        private const val NON_EXISTENT_EXERCISE = "NonExistent"
        private val NON_EXISTENT_MOVEMENT = MovementType.HORIZONTAL_PUSH // Using a valid enum for testing

        @JvmStatic
        fun errorScenarios(): Stream<Arguments> =
            Stream.of(
                Arguments.of(
                    "getByExercise should handle database errors",
                    { controller: ExerciseWorkoutTypeController, dal: ExerciseWorkoutTypeDAL ->
                        whenever(dal.selectExerciseWorkoutTypesByExercise(EXERCISE_NAME))
                            .thenReturn(Mono.error(DatabaseQueryException("Database connection failed")))
                        controller.getByExercise(EXERCISE_NAME)
                    },
                    { dal: ExerciseWorkoutTypeDAL ->
                        verify(dal).selectExerciseWorkoutTypesByExercise(EXERCISE_NAME)
                    }
                ),
                Arguments.of(
                    "getByMovementType should handle database errors",
                    { controller: ExerciseWorkoutTypeController, dal: ExerciseWorkoutTypeDAL ->
                        whenever(dal.selectExerciseWorkoutTypesByMovementType(MOVEMENT_TYPE))
                            .thenReturn(Mono.error(DatabaseQueryException("Database connection failed")))
                        controller.getByMovementType(MOVEMENT_TYPE)
                    },
                    { dal: ExerciseWorkoutTypeDAL ->
                        verify(dal).selectExerciseWorkoutTypesByMovementType(MOVEMENT_TYPE)
                    }
                )
            )
    }

    @BeforeEach
    fun setUp() {
        dal = mock()
        controller = ExerciseWorkoutTypeController(dal)
    }

    @Test
    fun `getAll should return all relationships`() {
        val exerciseWorkoutTypes =
            listOf(
                mockExerciseWorkoutType(exerciseName = EXERCISE_NAME, movementType = MOVEMENT_TYPE, workoutType = WORKOUT_TYPE),
                mockExerciseWorkoutType(exerciseName = SQUAT_NAME, movementType = SQUAT_MOVEMENT, workoutType = MAXIMAL_EFFORT)
            )
        whenever(dal.selectAllExerciseWorkoutTypes()).thenReturn(Mono.just(exerciseWorkoutTypes))
        val response = controller.getAll()

        @Suppress("UNCHECKED_CAST")
        val body = (response.body as Mono<List<ExerciseWorkoutType>>)
        StepVerifier.create(body)
            .expectNext(exerciseWorkoutTypes)
            .verifyComplete()
    }

    @Test
    fun `getAll should handle database errors`() {
        whenever(dal.selectAllExerciseWorkoutTypes()).thenReturn(Mono.error(DatabaseException("Database error")))
        val response = controller.getAll()
        StepVerifier.create(response.body as Mono<*>)
            .expectError(DatabaseException::class.java)
            .verify()
    }

    @Test
    fun `getByExercise should return relationships for an exercise using new mapping`() {
        val exerciseWorkoutTypes =
            listOf(
                mockExerciseWorkoutType(exerciseName = EXERCISE_NAME, movementType = MOVEMENT_TYPE, workoutType = WORKOUT_TYPE),
                mockExerciseWorkoutType(exerciseName = EXERCISE_NAME, movementType = MOVEMENT_TYPE, workoutType = MAXIMAL_EFFORT)
            )
        whenever(dal.selectExerciseWorkoutTypesByExercise(EXERCISE_NAME)).thenReturn(Mono.just(exerciseWorkoutTypes))
        val result = controller.getByExercise(EXERCISE_NAME)
        StepVerifier.create(result)
            .assertNext {
                assertEquals(ResponseEntity.ok(exerciseWorkoutTypes), it)
            }
            .verifyComplete()
    }

    @Test
    fun `getByExercise should return empty list when no relationships found`() {
        whenever(dal.selectExerciseWorkoutTypesByExercise(NON_EXISTENT_EXERCISE)).thenReturn(Mono.just(emptyList()))
        val result = controller.getByExercise(NON_EXISTENT_EXERCISE)
        StepVerifier.create(result)
            .assertNext {
                assertEquals(ResponseEntity.ok(emptyList<ExerciseWorkoutType>()), it)
            }
            .verifyComplete()
    }

    @Test
    fun `save should insert and return the relationship`() {
        val input =
            mockExerciseWorkoutType(
                exerciseName = EXERCISE_NAME,
                movementType = MOVEMENT_TYPE,
                workoutType = WORKOUT_TYPE
            )
        whenever(dal.insertExerciseWorkoutType(EXERCISE_NAME, MOVEMENT_TYPE, WORKOUT_TYPE))
            .thenReturn(Mono.just(input))
        val response = controller.save(EXERCISE_NAME, MOVEMENT_TYPE, WORKOUT_TYPE)

        @Suppress("UNCHECKED_CAST")
        val body = (response.body as Mono<ExerciseWorkoutType>)
        StepVerifier.create(body)
            .expectNext(input)
            .verifyComplete()
    }

    @Test
    fun `save should handle database errors`() {
        val databaseException = DatabaseQueryException("Database connection failed")
        whenever(dal.insertExerciseWorkoutType(EXERCISE_NAME, MOVEMENT_TYPE, WORKOUT_TYPE))
            .thenReturn(Mono.error(databaseException))
        val response = controller.save(EXERCISE_NAME, MOVEMENT_TYPE, WORKOUT_TYPE)

        @Suppress("UNCHECKED_CAST")
        val body = (response.body as Mono<*>)
        StepVerifier.create(body)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `getByMovementType should return relationships for a movementType`() {
        val exerciseWorkoutTypes =
            listOf(
                mockExerciseWorkoutType(exerciseName = EXERCISE_NAME, movementType = MOVEMENT_TYPE, workoutType = WORKOUT_TYPE),
                mockExerciseWorkoutType(exerciseName = EXERCISE_NAME, movementType = MOVEMENT_TYPE, workoutType = MAXIMAL_EFFORT)
            )
        whenever(dal.selectExerciseWorkoutTypesByMovementType(MOVEMENT_TYPE)).thenReturn(Mono.just(exerciseWorkoutTypes))
        val result = controller.getByMovementType(MOVEMENT_TYPE)
        StepVerifier.create(result)
            .assertNext {
                assertEquals(ResponseEntity.ok(exerciseWorkoutTypes), it)
            }
            .verifyComplete()
    }

    @Test
    fun `getByMovementType should return empty list when no relationships found`() {
        whenever(dal.selectExerciseWorkoutTypesByMovementType(NON_EXISTENT_MOVEMENT)).thenReturn(Mono.just(emptyList()))
        val result = controller.getByMovementType(NON_EXISTENT_MOVEMENT)
        StepVerifier.create(result)
            .assertNext {
                assertEquals(ResponseEntity.ok(emptyList<ExerciseWorkoutType>()), it)
            }
            .verifyComplete()
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("errorScenarios")
    @Suppress("UnusedParameter")
    fun `should handle database errors`(
        _testName: String,
        testAction: (ExerciseWorkoutTypeController, ExerciseWorkoutTypeDAL) -> Mono<ResponseEntity<List<ExerciseWorkoutType>>>,
        verification: (ExerciseWorkoutTypeDAL) -> Unit
    ) {
        val result = testAction(controller, dal)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
        verification(dal)
    }
}
