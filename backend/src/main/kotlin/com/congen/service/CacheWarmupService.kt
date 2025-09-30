package com.congen.service

import com.congen.config.CacheWarmupConfig
import com.congen.dal.EquipmentDAL
import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.dal.GdprComplianceDAL
import com.congen.dal.MuscleDAL
import com.congen.dal.ProgramDAL
import com.congen.dal.ProgramPreferencesDAL
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserWeakMuscleDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * Service responsible for warming up the application cache on startup.
 *
 * This service pre-loads frequently accessed data into the cache before the
 * application starts serving requests, reducing latency for initial user interactions.
 * It uses Spring's ApplicationRunner to execute after the application context is
 * initialized but before it starts serving requests.
 *
 * The warmup process is configurable and can be enabled/disabled via properties.
 * Different sections of the cache can be warmed up independently based on configuration.
 */
@Service
class CacheWarmupService(
    private val exerciseDAL: ExerciseDAL,
    private val equipmentDAL: EquipmentDAL,
    private val muscleDAL: MuscleDAL,
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
    private val workoutStageTypeDAL: WorkoutStageTypeDAL,
    private val exerciseWorkoutTypeDAL: ExerciseWorkoutTypeDAL,
    private val programDAL: ProgramDAL,
    private val userDAL: UserDAL,
    private val userEquipmentDAL: UserEquipmentDAL,
    private val userExercisePreferenceDAL: UserExercisePreferenceDAL,
    private val userOneRepMaxDAL: UserOneRepMaxDAL,
    private val userWeakMuscleDAL: UserWeakMuscleDAL,
    private val programPreferencesDAL: ProgramPreferencesDAL,
    private val userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
    private val gdprComplianceDAL: GdprComplianceDAL,
    private val programmedWorkoutDAL: ProgrammedWorkoutDAL,
    private val workoutStageDAL: WorkoutStageDAL,
    private val programmedExerciseDAL: ProgrammedExerciseDAL,
    private val setSchemeDAL: SetSchemeDAL,
    private val cacheWarmupConfig: CacheWarmupConfig
) {
    private val logger = LoggerFactory.getLogger(CacheWarmupService::class.java)

    /**
     * Executes the cache warmup process on application startup.
     *
     * This method is called by Spring Boot after the application is ready to serve requests.
     * It builds a chain of warmup operations based on configuration and executes them
     * reactively without blocking the main thread.
     *
     * @param event Application ready event
     */
    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady(event: ApplicationReadyEvent) {
        if (!cacheWarmupConfig.enabled) {
            logger.info("Cache warmup is disabled, skipping warmup process")
            return
        }

        logger.info("Starting cache warmup process")
        val startTime = System.currentTimeMillis()

        // Build warmup chain based on configuration
        var warmupChain = Mono.just(Unit)

        if (cacheWarmupConfig.warmupReferenceData) {
            warmupChain = warmupChain.then(warmupReferenceData())
        }

        if (cacheWarmupConfig.warmupLists) {
            warmupChain = warmupChain.then(warmupFrequentlyAccessedLists())
        }

        if (cacheWarmupConfig.warmupRelationships) {
            warmupChain = warmupChain.then(warmupCoreRelationships())
        }

        if (cacheWarmupConfig.warmupUserData) {
            warmupChain = warmupChain.then(warmupUserData())
        }

        warmupChain
            .doOnSuccess {
                val duration = System.currentTimeMillis() - startTime
                logger.info("Cache warmup completed successfully in {} ms", duration)
            }
            .doOnError { error ->
                val duration = System.currentTimeMillis() - startTime
                logger.error("Cache warmup failed after {} ms", duration, error)
            }
            .subscribe()
    }

    /**
     * Warms up reference data (exercises, equipment, muscles) for popular items.
     *
     * This method pre-loads individual records for the most commonly accessed
     * exercises, equipment, and muscles. These are cached with LONG_TERM TTL
     * and provide the foundation for workout generation.
     */
    private fun warmupReferenceData(): Mono<Unit> {
        logger.info("Warming up reference data (popular exercises, equipment, muscles)")

        return Flux.merge(
            Flux.fromIterable(cacheWarmupConfig.popularExercises)
                .flatMap { exerciseName ->
                    exerciseDAL.selectExerciseByName(exerciseName)
                        .doOnSuccess { logger.debug("Warmed up exercise: {}", exerciseName) }
                        .doOnError { logger.warn("Failed to warm up exercise: {}", exerciseName, it) }
                        .onErrorComplete()
                },
            Flux.fromIterable(cacheWarmupConfig.popularEquipment)
                .flatMap { equipmentName ->
                    equipmentDAL.selectEquipmentByName(equipmentName)
                        .doOnSuccess { logger.debug("Warmed up equipment: {}", equipmentName) }
                        .doOnError { logger.warn("Failed to warm up equipment: {}", equipmentName, it) }
                        .onErrorComplete()
                },
            Flux.fromIterable(cacheWarmupConfig.popularMuscles)
                .flatMap { muscleName ->
                    muscleDAL.selectMuscleByName(muscleName)
                        .doOnSuccess { logger.debug("Warmed up muscle: {}", muscleName) }
                        .doOnError { logger.warn("Failed to warm up muscle: {}", muscleName, it) }
                        .onErrorComplete()
                }
        ).then(Mono.just(Unit))
    }

    /**
     * Warms up frequently accessed list queries.
     *
     * This method pre-loads the "select all" queries for exercises, equipment,
     * and muscles. These are commonly used for dropdowns and selection interfaces.
     */
    private fun warmupFrequentlyAccessedLists(): Mono<Unit> {
        logger.info("Warming up frequently accessed lists")

        return Flux.merge(
            exerciseDAL.selectExercises()
                .doOnSuccess { logger.debug("Warmed up exercises list") }
                .doOnError { logger.warn("Failed to warm up exercises list", it) }
                .onErrorComplete(),
            equipmentDAL.selectEquipment()
                .doOnSuccess { logger.debug("Warmed up equipment list") }
                .doOnError { logger.warn("Failed to warm up equipment list", it) }
                .onErrorComplete(),
            muscleDAL.selectMuscles()
                .doOnSuccess { logger.debug("Warmed up muscles list") }
                .doOnError { logger.warn("Failed to warm up muscles list", it) }
                .onErrorComplete(),
            workoutStageTypeDAL.selectWorkoutStageTypes()
                .doOnSuccess { logger.debug("Warmed up workout stage types list") }
                .doOnError { logger.warn("Failed to warm up workout stage types list", it) }
                .onErrorComplete()
        ).then(Mono.just(Unit))
    }

    /**
     * Warms up core relationship data.
     *
     * This method pre-loads exercise-muscle and exercise-equipment relationships
     * for popular exercises. These relationships are crucial for workout generation
     * and muscle targeting.
     */
    private fun warmupCoreRelationships(): Mono<Unit> {
        logger.info("Warming up core relationship data")

        return Flux.fromIterable(cacheWarmupConfig.popularExercises)
            .flatMap { exerciseName ->
                Flux.merge(
                    exerciseMuscleDAL.selectExerciseMuscleByExercise(exerciseName)
                        .doOnSuccess { logger.debug("Warmed up exercise-muscle relationships for: {}", exerciseName) }
                        .doOnError { logger.warn("Failed to warm up exercise-muscle relationships for: {}", exerciseName, it) }
                        .onErrorComplete(),
                    exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName)
                        .doOnSuccess { logger.debug("Warmed up exercise-equipment relationships for: {}", exerciseName) }
                        .doOnError { logger.warn("Failed to warm up exercise-equipment relationships for: {}", exerciseName, it) }
                        .onErrorComplete(),
                    exerciseWorkoutTypeDAL.selectExerciseWorkoutTypesByExercise(exerciseName)
                        .doOnSuccess { logger.debug("Warmed up exercise-workout type relationships for: {}", exerciseName) }
                        .doOnError {
                                error ->
                            logger.warn("Failed to warm up exercise-workout type relationships for: {}", exerciseName, error)
                        }
                        .onErrorComplete()
                )
            }.then(Mono.just(Unit))
    }

    /**
     * Warms up user-specific data for a random set of users.
     *
     * This method pre-loads comprehensive user data including profiles, programs,
     * preferences, and personalization data. It selects a random subset of users
     * to warm up their complete data profile, providing realistic cache warming
     * that mirrors actual user access patterns.
     */
    private fun warmupUserData(): Mono<Unit> {
        logger.info("Warming up user-specific data")

        // Get random user keycloak IDs to warm up
        return userDAL.selectRandomUserIds(cacheWarmupConfig.maxUsersToWarmup)
            .flatMap { userIds ->
                logger.info("Warming up data for {} users", userIds.size)

                Flux.fromIterable(userIds)
                    .flatMap { userId -> warmupUserCompleteData(userId) }
                    .collectList()
                    .flatMap { Mono.just(Unit) }
            }
            .doOnSuccess { logger.debug("Warmed up user data") }
            .doOnError { logger.warn("Failed to warm up user data", it) }
            .onErrorComplete()
    }

    /**
     * Warms up complete data for a specific user.
     *
     * This method pre-loads all user-specific data including:
     * - User profile
     * - User programs and workouts
     * - User equipment preferences
     * - User exercise preferences
     * - User one rep maxes
     * - User weak muscles
     * - User program preferences
     * - User weight unit preferences
     * - User GDPR consent status
     *
     * @param userId The Keycloak ID of the user to warm up
     * @return Mono that completes when all user data is warmed up
     */
    private fun warmupUserCompleteData(userId: String): Mono<Unit> {
        return Flux.concat(
            // User profile
            userDAL.selectUserByKeycloakId(userId)
                .doOnSuccess { logger.debug("Warmed up user profile for: {}", userId) }
                .doOnError { logger.warn("Failed to warm up user profile for: {}", userId, it) }
                .onErrorComplete(),
            // User programs
            programDAL.selectProgramsByUserId(userId)
                .flatMap { programs ->
                    if (programs.isEmpty()) {
                        Mono.just(Unit)
                    } else {
                        Flux.fromIterable(programs)
                            .flatMap { program ->
                                warmupProgramData(program.id)
                            }
                            .collectList()
                            .flatMap { Mono.just(Unit) }
                    }
                }
                .doOnSuccess { logger.debug("Warmed up user programs and related data for: {}", userId) }
                .doOnError { logger.warn("Failed to warm up user programs and related data for: {}", userId, it) }
                .onErrorComplete(),
            // User equipment
            userEquipmentDAL.selectUserEquipmentByUser(userId)
                .doOnSuccess { logger.debug("Warmed up user equipment for: {}", userId) }
                .doOnError { logger.warn("Failed to warm up user equipment for: {}", userId, it) }
                .onErrorComplete(),
            // User exercise preferences
            userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)
                .doOnSuccess { logger.debug("Warmed up user exercise preferences for: {}", userId) }
                .doOnError { logger.warn("Failed to warm up user exercise preferences for: {}", userId, it) }
                .onErrorComplete(),
            // User one rep maxes
            userOneRepMaxDAL.selectUserOneRepMaxByUser(userId)
                .doOnSuccess { logger.debug("Warmed up user one rep maxes for: {}", userId) }
                .doOnError { logger.warn("Failed to warm up user one rep maxes for: {}", userId, it) }
                .onErrorComplete(),
            // User weak muscles
            userWeakMuscleDAL.selectUserWeakMusclesByUser(userId)
                .doOnSuccess { logger.debug("Warmed up user weak muscles for: {}", userId) }
                .doOnError { logger.warn("Failed to warm up user weak muscles for: {}", userId, it) }
                .onErrorComplete(),
            // User weight unit preferences
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(userId)
                .doOnSuccess { logger.debug("Warmed up user weight unit preferences for: {}", userId) }
                .doOnError { logger.warn("Failed to warm up user weight unit preferences for: {}", userId, it) }
                .onErrorComplete(),
            // User GDPR consent
            gdprComplianceDAL.hasUserConsent(userId)
                .doOnSuccess { logger.debug("Warmed up user GDPR consent for: {}", userId) }
                .doOnError { logger.warn("Failed to warm up user GDPR consent for: {}", userId, it) }
                .onErrorResume { Mono.just(false) }
        ).then(Mono.just(Unit))
    }

    /**
     * Warms up program-related data for a specific program.
     *
     * This method pre-loads all program-related data including:
     * - Program preferences for the program
     * - Programmed workouts for the program
     * - Workout stages for each programmed workout
     * - Programmed exercises for each workout stage
     * - Set schemes for each programmed exercise
     *
     * @param programId The ID of the program to warm up
     * @return Mono that completes when all program data is warmed up
     */
    private fun warmupProgramData(programId: Long): Mono<Unit> {
        return Flux.concat(
            // Program preferences
            programPreferencesDAL.selectProgramPreferences(programId)
                .doOnSuccess { logger.debug("Warmed up program preferences for program: {}", programId) }
                .doOnError { logger.warn("Failed to warm up program preferences for program: {}", programId, it) }
                .onErrorComplete(),
            // Programmed workouts
            programmedWorkoutDAL.selectProgrammedWorkoutsByProgramId(programId)
                .flatMap { programmedWorkouts ->
                    if (programmedWorkouts.isEmpty()) {
                        Mono.just(Unit)
                    } else {
                        Flux.fromIterable(programmedWorkouts)
                            .flatMap { programmedWorkout ->
                                warmupWorkoutData(programmedWorkout.id)
                            }
                            .collectList()
                            .flatMap { Mono.just(Unit) }
                    }
                }
                .doOnSuccess { logger.debug("Warmed up programmed workouts for program: {}", programId) }
                .doOnError { logger.warn("Failed to warm up programmed workouts for program: {}", programId, it) }
                .onErrorComplete()
        ).then(Mono.just(Unit))
    }

    /**
     * Warms up workout-related data for a specific programmed workout.
     *
     * This method pre-loads all workout-related data including:
     * - Workout stages for the programmed workout
     * - Programmed exercises for each workout stage
     * - Set schemes for each programmed exercise
     *
     * @param programmedWorkoutId The ID of the programmed workout to warm up
     * @return Mono that completes when all workout data is warmed up
     */
    private fun warmupWorkoutData(programmedWorkoutId: Long): Mono<Unit> {
        return workoutStageDAL.selectWorkoutStagesByProgrammedWorkoutId(programmedWorkoutId)
            .flatMap { workoutStages ->
                if (workoutStages.isEmpty()) {
                    Mono.just(Unit)
                } else {
                    Flux.fromIterable(workoutStages)
                        .flatMap { workoutStage ->
                            warmupWorkoutStageData(workoutStage.id)
                        }
                        .collectList()
                        .flatMap { Mono.just(Unit) }
                }
            }
            .doOnSuccess { logger.debug("Warmed up workout data for programmed workout: {}", programmedWorkoutId) }
            .doOnError { logger.warn("Failed to warm up workout data for programmed workout: {}", programmedWorkoutId, it) }
            .onErrorComplete()
    }

    /**
     * Warms up workout stage-related data for a specific workout stage.
     *
     * This method pre-loads all workout stage-related data including:
     * - Programmed exercises for the workout stage
     * - Set schemes for each programmed exercise
     *
     * @param workoutStageId The ID of the workout stage to warm up
     * @return Mono that completes when all workout stage data is warmed up
     */
    private fun warmupWorkoutStageData(workoutStageId: Long): Mono<Unit> {
        return programmedExerciseDAL.selectProgrammedExercisesByWorkoutStageId(workoutStageId)
            .flatMap { programmedExercises ->
                if (programmedExercises.isEmpty()) {
                    Mono.just(Unit)
                } else {
                    Flux.fromIterable(programmedExercises)
                        .flatMap { programmedExercise ->
                            warmupProgrammedExerciseData(programmedExercise.id)
                        }
                        .collectList()
                        .flatMap { Mono.just(Unit) }
                }
            }
            .doOnSuccess { logger.debug("Warmed up workout stage data for workout stage: {}", workoutStageId) }
            .doOnError { logger.warn("Failed to warm up workout stage data for workout stage: {}", workoutStageId, it) }
            .onErrorComplete()
    }

    /**
     * Warms up programmed exercise-related data for a specific programmed exercise.
     *
     * This method pre-loads all programmed exercise-related data including:
     * - Set schemes for the programmed exercise
     *
     * @param programmedExerciseId The ID of the programmed exercise to warm up
     * @return Mono that completes when all programmed exercise data is warmed up
     */
    private fun warmupProgrammedExerciseData(programmedExerciseId: Long): Mono<Unit> {
        return setSchemeDAL.selectSetSchemesByProgrammedExerciseId(programmedExerciseId)
            .doOnSuccess { logger.debug("Warmed up set schemes for programmed exercise: {}", programmedExerciseId) }
            .doOnError { logger.warn("Failed to warm up set schemes for programmed exercise: {}", programmedExerciseId, it) }
            .onErrorComplete()
            .then(Mono.just(Unit))
    }
}
