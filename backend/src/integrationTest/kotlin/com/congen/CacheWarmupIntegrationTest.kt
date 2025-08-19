package com.congen

import com.congen.dal.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@SpringBootTest
@ActiveProfiles("integration-test")
class CacheWarmupIntegrationTest : BaseIntegrationTest() {

    @Autowired private lateinit var exerciseDAL: ExerciseDAL
    @Autowired private lateinit var equipmentDAL: EquipmentDAL
    @Autowired private lateinit var muscleDAL: MuscleDAL
    @Autowired private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL
    @Autowired private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL
    @Autowired private lateinit var workoutStageTypeDAL: WorkoutStageTypeDAL
    @Autowired private lateinit var exerciseWorkoutTypeDAL: ExerciseWorkoutTypeDAL
    @Autowired private lateinit var programDAL: ProgramDAL

    @Test
    fun `should warm up cache successfully with real data`() {
        val popularExercises = listOf("Bench Press", "Squat", "Deadlift")
        popularExercises.forEach { exerciseName ->
            StepVerifier.create(exerciseDAL.selectExerciseByName(exerciseName))
                .expectNextMatches { exercise -> exercise.name == exerciseName }
                .verifyComplete()
        }
        StepVerifier.create(equipmentDAL.selectEquipment()).expectNextMatches { it.isNotEmpty() }.verifyComplete()
        StepVerifier.create(muscleDAL.selectMuscles()).expectNextMatches { it.isNotEmpty() }.verifyComplete()
        StepVerifier.create(workoutStageTypeDAL.selectWorkoutStageTypes()).expectNextMatches { it.isNotEmpty() }.verifyComplete()
        StepVerifier.create(programDAL.selectPrograms()).expectNextMatches { it.isNotEmpty() }.verifyComplete()
    }

    @Test
    fun `should warm up exercise relationships successfully`() {
        val popularExercises = listOf("Bench Press", "Squat")
        popularExercises.forEach { exerciseName ->
            StepVerifier.create(exerciseMuscleDAL.selectExerciseMuscleByExercise(exerciseName))
                .expectNextMatches { it.isNotEmpty() && it.all { rel -> rel.exerciseName == exerciseName } }
                .verifyComplete()
            StepVerifier.create(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName))
                .expectNextMatches { it.isNotEmpty() && it.all { rel -> rel.exerciseName == exerciseName } }
                .verifyComplete()
            StepVerifier.create(exerciseWorkoutTypeDAL.selectExerciseWorkoutTypesByExercise(exerciseName))
                .expectNextMatches { it.isNotEmpty() && it.all { rel -> rel.exerciseName == exerciseName } }
                .verifyComplete()
        }
    }

    @Test
    fun `should handle missing data gracefully during warmup`() {
        val nonExistentExercise = "NonExistentExercise"
        StepVerifier.create(exerciseDAL.selectExerciseByName(nonExistentExercise)).expectError().verify()
    }

    @Test
    fun `should complete warmup process without blocking application startup`() {
        StepVerifier.create(
            Mono.zip(
                exerciseDAL.selectExercises(),
                equipmentDAL.selectEquipment(),
                muscleDAL.selectMuscles()
            )
        ).expectNextMatches { tuple ->
            val exercises = tuple.t1
            val equipment = tuple.t2
            val muscles = tuple.t3
            exercises.isNotEmpty() && equipment.isNotEmpty() && muscles.isNotEmpty()
        }.verifyComplete()
    }
}
