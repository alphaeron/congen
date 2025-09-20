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
 * Unit tests for WorkoutStageGenerationOrchestrator.
 */
class WorkoutStageGenerationOrchestratorTest {
    private lateinit var orchestrator: WorkoutStageGenerationOrchestrator
    private lateinit var workoutStageGenerationServiceFactory: WorkoutStageGenerationServiceFactory

    companion object {
        private const val USER_ID = "test-user-123"
        private val now = Instant.now()
    }

    @BeforeEach
    fun setUp() {
        workoutStageGenerationServiceFactory = mock()
        orchestrator =
            WorkoutStageGenerationOrchestrator(
                workoutStageGenerationServiceFactory = workoutStageGenerationServiceFactory
            )
    }

    @Test
    fun `generateWorkoutStages should generate stages successfully`() {
        val preparedData = createSamplePreparedData()
        val mockService = mock<WorkoutStageGenerationService>()

        whenever(workoutStageGenerationServiceFactory.getWorkoutStageGenerationService(any())).thenReturn(mockService)
        whenever(mockService.generateWorkoutStages(any(), any(), any(), any())).thenReturn(Mono.just(createSampleWorkoutGenerationResult()))

        val result =
            orchestrator.generateWorkoutStages(
                programId = 1L,
                dayNumber = 1,
                dayType = "ME_Upper",
                preparedData = preparedData
            )

        StepVerifier.create(result)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `generateWorkoutStages should handle multiple day types`() {
        val preparedData = createSamplePreparedData()
        val mockService = mock<WorkoutStageGenerationService>()

        whenever(workoutStageGenerationServiceFactory.getWorkoutStageGenerationService(any())).thenReturn(mockService)
        whenever(mockService.generateWorkoutStages(any(), any(), any(), any())).thenReturn(Mono.just(createSampleWorkoutGenerationResult()))

        val result =
            orchestrator.generateWorkoutStages(
                programId = 1L,
                dayNumber = 2,
                dayType = "ME_Lower",
                preparedData = preparedData
            )

        StepVerifier.create(result)
            .expectNextCount(1)
            .verifyComplete()
    }

    @Test
    fun `generateWorkoutStages should handle dynamic effort day types`() {
        val preparedData = createSamplePreparedData()
        val mockService = mock<WorkoutStageGenerationService>()

        whenever(workoutStageGenerationServiceFactory.getWorkoutStageGenerationService(any())).thenReturn(mockService)
        whenever(mockService.generateWorkoutStages(any(), any(), any(), any())).thenReturn(Mono.just(createSampleWorkoutGenerationResult()))

        val result =
            orchestrator.generateWorkoutStages(
                programId = 1L,
                dayNumber = 3,
                dayType = "DE_Upper",
                preparedData = preparedData
            )

        StepVerifier.create(result)
            .expectNextCount(1)
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
                    allExercises = createSampleExercises(),
                    preferences = emptyList(),
                    userEquipment = createSampleUserEquipment(),
                    exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
                    exerciseMuscleMappings = createSampleExerciseMuscleMappings(),
                    previouslyUsedExercises = emptyList(),
                    userId = USER_ID
                ),
            oneRepMaxes =
                listOf(
                    UserOneRepMax(USER_ID, "Bench Press", BigDecimal("225"), now),
                    UserOneRepMax(USER_ID, "Squat", BigDecimal("315"), now),
                    UserOneRepMax(USER_ID, "Deadlift", BigDecimal("405"), now)
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
            exerciseWorkoutTypeMappings =
                mapOf(
                    "Bench Press" to listOf("maximal_effort", "dynamic_effort"),
                    "Squat" to listOf("maximal_effort", "dynamic_effort"),
                    "Deadlift" to listOf("maximal_effort")
                ),
            exerciseEquipmentMappings = createSampleExerciseEquipmentMappings(),
            previouslyProgrammedExercises = emptyList(),
            allExercises = createSampleExercises(),
            userEquipment = createSampleUserEquipment(),
            userExercisePreferences = emptyList()
        )
    }

    private fun createSampleExercises(): List<Exercise> {
        return listOf(
            createSampleExercise("Bench Press", MovementType.HORIZONTAL_PUSH),
            createSampleExercise("Squat", MovementType.SQUAT),
            createSampleExercise("Deadlift", MovementType.HINGE),
            createSampleExercise("Sled Push", MovementType.PLYOMETRIC)
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

    private fun createSampleWorkoutGenerationResult(): WorkoutGenerationResult {
        val preparedData = createSamplePreparedData()
        return WorkoutGenerationResult(
            programId = 1L,
            dayNumber = 1,
            dayType = "ME_Upper",
            userId = USER_ID,
            stages = emptyList(),
            preparedData = preparedData
        )
    }
}
