package com.congen.generator

import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.service.SetSchemeService
import com.congen.model.ProgrammedWorkout
import com.congen.model.WeightUnit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration

/**
 * Service for atomic workout generation database writes.
 *
 * This service handles all database writes for workout generation in a single
 * transaction, ensuring data consistency and preventing partial workout data
 * from being written to the database.
 *
 * @param programmedWorkoutDAL Data access layer for programmed workout operations
 * @param workoutStageDAL Data access layer for workout stage operations
 * @param programmedExerciseDAL Data access layer for programmed exercise operations
 * @param setSchemeDAL Data access layer for set scheme operations
 * @param workoutStageTypeDAL Data access layer for workout stage type operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class AtomicWorkoutWriter(
    private val programmedWorkoutDAL: ProgrammedWorkoutDAL,
    private val workoutStageDAL: WorkoutStageDAL,
    private val programmedExerciseDAL: ProgrammedExerciseDAL,
    private val setSchemeService: SetSchemeService,
    private val workoutStageTypeDAL: WorkoutStageTypeDAL,
    private val userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(AtomicWorkoutWriter::class.java)
    }

    /**
     * Writes a complete workout generation result to the database atomically.
     *
     * This method ensures that either all data is written successfully or
     * none of it is written, maintaining database consistency.
     *
     * @param workoutResult The complete workout generation result to write
     * @return Mono containing the created programmed workout
     */
    fun writeWorkoutAtomically(workoutResult: WorkoutGenerationResult): Mono<ProgrammedWorkout> {
        logger.info("Writing workout atomically for program {} day {}", 
            workoutResult.programId, workoutResult.dayNumber)

        return programmedWorkoutDAL.insertProgrammedWorkout(
            workoutResult.programId, 
            workoutResult.dayNumber, 
            workoutResult.dayType
        )
            .flatMap { createdWorkout ->
                writeWorkoutStages(createdWorkout.id, workoutResult.stages, workoutResult.userId)
                    .then(Mono.just(createdWorkout))
            }
            .retryWhen(Retry.backoff(3, Duration.ofMillis(100)))
            .doOnSuccess { workout ->
                logger.info("Successfully wrote workout atomically: {}", workout.id)
            }
            .doOnError { error ->
                logger.error("Failed to write workout atomically for program {} day {}: {}", 
                    workoutResult.programId, workoutResult.dayNumber, error.message)
            }
    }

    /**
     * Writes all workout stages for a workout to the database.
     *
     * @param workoutId The ID of the workout these stages belong to
     * @param stagesData List of workout stage data to write
     * @return Mono indicating completion
     */
    private fun writeWorkoutStages(workoutId: Long, stagesData: List<WorkoutStageData>, userId: String): Mono<Void> {
        return reactor.core.publisher.Flux.fromIterable(stagesData)
            .concatMap { stageData ->
                writeWorkoutStage(workoutId, stageData, userId)
            }
            .then()
    }

    /**
     * Writes a single workout stage to the database.
     *
     * @param workoutId The ID of the workout this stage belongs to
     * @param stageData The workout stage data to write
     * @return Mono indicating completion
     */
    private fun writeWorkoutStage(workoutId: Long, stageData: WorkoutStageData, userId: String): Mono<Void> {
        return workoutStageTypeDAL.selectWorkoutStageTypeByEnum(stageData.stageType)
            .flatMap { workoutStageType ->
                workoutStageDAL.insertWorkoutStage(
                    workoutId,
                    workoutStageType.id,
                    stageData.position,
                    stageData.name
                )
            }
            .flatMap { createdStage ->
                writeProgrammedExercises(createdStage.id, stageData.exercises, userId)
            }
    }

    /**
     * Writes all programmed exercises for a stage to the database.
     *
     * @param stageId The ID of the stage these exercises belong to
     * @param exercisesData List of programmed exercise data to write
     * @return Mono indicating completion
     */
    private fun writeProgrammedExercises(stageId: Long, exercisesData: List<ProgrammedExerciseData>, userId: String): Mono<Void> {
        return reactor.core.publisher.Flux.fromIterable(exercisesData)
            .concatMap { exerciseData ->
                writeProgrammedExercise(stageId, exerciseData, userId)
            }
            .then()
    }

    /**
     * Writes a single programmed exercise to the database.
     *
     * @param stageId The ID of the stage this exercise belongs to
     * @param exerciseData The programmed exercise data to write
     * @return Mono indicating completion
     */
    private fun writeProgrammedExercise(stageId: Long, exerciseData: ProgrammedExerciseData, userId: String): Mono<Void> {
        return programmedExerciseDAL.insertProgrammedExercise(
            stageId,
            exerciseData.exerciseName,
            exerciseData.position,
            exerciseData.notes
        )
            .flatMap { createdExercise ->
                writeSetSchemes(createdExercise.id, exerciseData.setSchemes, exerciseData.exerciseName, userId)
            }
    }

    /**
     * Writes all set schemes for an exercise to the database.
     *
     * @param exerciseId The ID of the exercise these set schemes belong to
     * @param setSchemesData List of set scheme data to write
     * @return Mono indicating completion
     */
    private fun writeSetSchemes(exerciseId: Long, setSchemesData: List<SetSchemeParams>, exerciseName: String, userId: String): Mono<Void> {
        return reactor.core.publisher.Flux.fromIterable(setSchemesData)
            .concatMap { setSchemeData ->
                writeSetScheme(exerciseId, setSchemeData, exerciseName, userId)
            }
            .then()
    }

    /**
     * Writes a single set scheme to the database.
     *
     * @param exerciseId The ID of the exercise this set scheme belongs to
     * @param setSchemeData The set scheme data to write
     * @return Mono indicating completion
     */
    private fun writeSetScheme(exerciseId: Long, setSchemeData: SetSchemeParams, exerciseName: String, userId: String): Mono<Void> {
        // Get the user's exercise unit preference
        return getWeightUnitForExercise(userId, exerciseName)
            .flatMap { weightUnit ->
                setSchemeService.insertSetScheme(
                    programmedExerciseId = exerciseId,
                    setNumber = setSchemeData.setNumber,
                    isAmrap = setSchemeData.isAmrap,
                    isEmom = setSchemeData.isEmom,
                    useTempo = setSchemeData.useTempo,
                    eccentricTempo = setSchemeData.eccentricTempo,
                    isometricTempo = setSchemeData.isometricTempo,
                    concentricTempo = setSchemeData.concentricTempo,
                    targetWeight = setSchemeData.targetWeight?.toString(),
                    performedWeight = setSchemeData.performedWeight?.toString(),
                    targetRepCount = setSchemeData.targetRepCount,
                    performedRepCount = setSchemeData.performedRepCount,
                    restSeconds = setSchemeData.restSeconds,
                    unit = weightUnit.name,
                    band = setSchemeData.band
                )
            }
            .then()
    }

    /**
     * Gets the weight unit preference for an exercise.
     *
     * @param userId The user ID for weight unit preferences
     * @param exerciseName The name of the exercise
     * @return Mono containing the weight unit preference, defaulting to KG if not found
     */
    private fun getWeightUnitForExercise(
        userId: String,
        exerciseName: String
    ): Mono<WeightUnit> {
        return userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, exerciseName)
            .map { it.preferredUnit }
            .switchIfEmpty(Mono.just(WeightUnit.KG))
            .onErrorResume { error ->
                logger.debug("No weight unit preference found for user {} and exercise {}, using KG", userId, exerciseName)
                Mono.just(WeightUnit.KG)
            }
    }
}
