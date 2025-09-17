package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Program
import com.congen.model.ProgramPreferences
import com.congen.model.ProgramWithWorkouts
import com.congen.model.ProgrammedExercise
import com.congen.model.ProgrammedExerciseWithSetSchemes
import com.congen.model.ProgrammedWorkout
import com.congen.model.ProgrammedWorkoutWithStages
import com.congen.model.SetScheme
import com.congen.model.WorkoutStage
import com.congen.model.WorkoutStageWithExercises
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Data Access Layer for Program entities.
 *
 * This class provides database operations for Program entities, including CRUD
 * operations. It uses the reactive PostgreSQL client for all database interactions
 * and provides methods for managing workout programs in the system.
 *
 * ## Operations
 *
 * - **Read**: Select program by ID, select all programs
 * - **Create**: Insert new program
 * - **Update**: Update existing program
 * - **Delete**: Delete program by ID
 *
 * ## Program Entity
 *
 * Programs represent structured workout plans that contain:
 * - Unique identifier and name
 * - Description of the program
 * - Current week number
 * - Associated programmed workouts (via foreign key relationships)
 *
 * ## Database Schema
 *
 * The program table contains:
 * - `id`: Primary key (auto-generated)
 * - `user_id`: User ID (required)
 * - `name`: Program name (required)
 * - `description`: Program description (optional)
 * - `current_week_number`: Current week number (required)
 * - `created_at`: Creation timestamp (auto-generated)
 * - `updated_at`: Last update timestamp (auto-generated)
 *
 * @param postgresClient Client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class ProgramDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ProgramDAL::class.java)
    }

    /**
     * Retrieves a program by its unique identifier.
     *
     * This method queries the database for a program with the specified ID.
     * If no program is found, a [NoResultsFoundException] is thrown.
     *
     * @param id The unique identifier of the program to retrieve
     * @return Mono containing the program if found
     * @throws NoResultsFoundException if no program exists with the given ID
     */
    @Cacheable(
        ttl = CacheTTL.SHORT_TERM,
        keyStrategy = CacheKeyStrategy.STANDARD,
        entityName = "program"
    )
    fun selectProgramById(id: Long): Mono<Program> {
        logger.debug("Selecting program by id: {}", id)
        return postgresClient.selectIndividual(
            "SELECT * FROM program WHERE id=$1",
            id,
        )
    }

    /**
     * Retrieves all programs from the database.
     *
     * This method queries the database for all program records and returns
     * them as a list, ordered by name. If no programs exist, an empty list
     * is returned.
     *
     * @return Mono containing a list of all programs
     */
    @Cacheable(
        ttl = CacheTTL.SHORT_TERM,
        keyStrategy = CacheKeyStrategy.LIST_QUERY,
        entityName = "program"
    )
    fun selectPrograms(): Mono<List<Program>> {
        logger.debug("Selecting all programs")
        return postgresClient.select("SELECT * FROM program ORDER BY name")
    }

    /**
     * Retrieves all programs owned by a specific user.
     *
     * This method queries the database for all program records owned by the
     * specified user and returns them as a list, ordered by name. If no
     * programs exist for the user, an empty list is returned.
     *
     * @param userId The Keycloak identifier of the user
     * @param isActive Optional filter to return only active or inactive programs. If null, returns all programs
     * @return Mono containing a list of programs owned by the user
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "program"
    )
    fun selectProgramsByUserId(
        userId: String,
        isActive: Boolean? = null
    ): Mono<List<Program>> {
        logger.debug("Selecting programs by user id: {}", userId)
        var query = "SELECT * FROM program WHERE user_id=$1"
        val params = mutableListOf<Any?>(userId)

        if (isActive != null) {
            query += " AND is_active=$2"
            params.add(isActive)
        }

        query += " ORDER BY name"

        return postgresClient.select(query, *params.toTypedArray())
    }

    /**
     * Retrieves all programs with complete workout hierarchy for a specific user.
     *
     * This method efficiently fetches all programs and their complete workout hierarchy
     * (workouts, stages, exercises, and set schemes) in a single optimized query using JOINs.
     * This avoids the N+1 query problem that occurs when fetching each level separately.
     *
     * The query joins all related tables and returns the data in a flat structure that
     * is then reconstructed into the proper hierarchical model.
     *
     * @param userId The Keycloak identifier of the user
     * @return Mono containing a list of programs with complete workout hierarchy
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "program"
    )
    fun selectProgramsWithWorkoutHierarchyByUserId(userId: String): Mono<List<ProgramWithWorkouts>> {
        logger.debug("Selecting programs with complete workout hierarchy for user: {}", userId)

        return postgresClient.select<Map<String, Any>>(
            """
            SELECT
                p.id as program_id,
                p.user_id as program_user_id,
                p.name as program_name,
                p.current_week_number as program_current_week_number,
                p.is_active as program_is_active,
                p.created_at as program_created_at,
                p.updated_at as program_updated_at,
                pp.program_days_per_week as program_preferences_days_per_week,
                pp.session_time_length_in_minutes as program_preferences_session_time,
                pp.created_at as program_preferences_created_at,
                pp.updated_at as program_preferences_updated_at,
                pw.id as workout_id,
                pw.program_id as workout_program_id,
                pw.day_number as workout_day_number,
                pw.name as workout_name,
                pw.created_at as workout_created_at,
                pw.updated_at as workout_updated_at,
                ws.id as stage_id,
                ws.programmed_workout_id as stage_programmed_workout_id,
                ws.stage_type_id as stage_stage_type_id,
                ws.position as stage_position,
                ws.name as stage_name,
                ws.created_at as stage_created_at,
                ws.updated_at as stage_updated_at,
                pe.id as exercise_id,
                pe.workout_stage_id as exercise_workout_stage_id,
                pe.exercise_name as exercise_exercise_name,
                pe.position as exercise_position,
                pe.notes as exercise_notes,
                pe.created_at as exercise_created_at,
                pe.updated_at as exercise_updated_at,
                ss.id as set_scheme_id,
                ss.programmed_exercise_id as set_scheme_programmed_exercise_id,
                ss.set_number as set_scheme_set_number,
                ss.is_amrap as set_scheme_is_amrap,
                ss.is_emom as set_scheme_is_emom,
                ss.use_tempo as set_scheme_use_tempo,
                ss.eccentric_tempo as set_scheme_eccentric_tempo,
                ss.isometric_tempo as set_scheme_isometric_tempo,
                ss.concentric_tempo as set_scheme_concentric_tempo,
                ss.target_weight as set_scheme_target_weight,
                ss.performed_weight as set_scheme_performed_weight,
                ss.target_rep_count as set_scheme_target_rep_count,
                ss.performed_rep_count as set_scheme_performed_rep_count,
                ss.rest_seconds as set_scheme_rest_seconds,
                ss.created_at as set_scheme_created_at,
                ss.updated_at as set_scheme_updated_at
            FROM program p
            LEFT JOIN program_preferences pp ON p.id = pp.program_id
            LEFT JOIN programmed_workout pw ON p.id = pw.program_id
            LEFT JOIN workout_stage ws ON pw.id = ws.programmed_workout_id
            LEFT JOIN programmed_exercise pe ON ws.id = pe.workout_stage_id
            LEFT JOIN set_scheme ss ON pe.id = ss.programmed_exercise_id
            WHERE p.user_id = $1
            ORDER BY p.id, pw.day_number, ws.position, pe.position, ss.set_number
            """.trimIndent(),
            userId
        ).map { rows: List<Map<String, Any>> ->
            // Group the flat results into hierarchical structure
            val programsMap = mutableMapOf<Long, MutableMap<String, Any>>()

            rows.forEach { row: Map<String, Any> ->
                val programId = row["program_id"] as Long
                val workoutId = row["workout_id"] as Long?
                val stageId = row["stage_id"] as Long?
                val exerciseId = row["exercise_id"] as Long?
                val setSchemeId = row["set_scheme_id"] as Long?

                // Initialize program if not exists
                if (!programsMap.containsKey(programId)) {
                    programsMap[programId] =
                        mutableMapOf(
                            "program" to
                                Program(
                                    id = programId,
                                    userId = row["program_user_id"] as String,
                                    name = row["program_name"] as String,
                                    currentWeekNumber =
                                        (row["program_current_week_number"] as? Number)?.toInt()
                                            ?: throw IllegalStateException("program_current_week_number is null"),
                                    isActive =
                                        (row["program_is_active"] as? Boolean) ?: throw IllegalStateException(
                                            "program_is_active is null"
                                        ),
                                    createdAt = parseTimestamp(row["program_created_at"]),
                                    updatedAt = parseTimestamp(row["program_updated_at"])
                                ),
                            "programPreferences" to
                                ProgramPreferences(
                                    programId = programId,
                                    programDaysPerWeek = (row["program_preferences_days_per_week"] as? Number)?.toInt() ?: 4,
                                    sessionTimeLengthInMinutes = (row["program_preferences_session_time"] as? Number)?.toInt() ?: 60,
                                    createdAt =
                                        parseTimestamp(
                                            row["program_preferences_created_at"]
                                        ) ?: parseTimestamp(row["program_created_at"]),
                                    updatedAt =
                                        parseTimestamp(
                                            row["program_preferences_updated_at"]
                                        ) ?: parseTimestamp(row["program_updated_at"])
                                ),
                            "workouts" to mutableMapOf<Long, MutableMap<String, Any>>()
                        )
                }

                val program = programsMap[programId]!!
                val workouts = program["workouts"] as MutableMap<Long, MutableMap<String, Any>>

                // Add workout if exists and not already added
                if (workoutId != null && !workouts.containsKey(workoutId)) {
                    workouts[workoutId] =
                        mutableMapOf(
                            "workout" to
                                ProgrammedWorkout(
                                    id = workoutId,
                                    programId = row["workout_program_id"] as Long,
                                    dayNumber =
                                        (row["workout_day_number"] as? Number)?.toInt() ?: throw IllegalStateException(
                                            "workout_day_number is null"
                                        ),
                                    name = row["workout_name"] as String,
                                    createdAt = parseTimestamp(row["workout_created_at"]),
                                    updatedAt = parseTimestamp(row["workout_updated_at"])
                                ),
                            "stages" to mutableMapOf<Long, MutableMap<String, Any>>()
                        )
                }

                // Add stage if exists and not already added
                if (stageId != null && workoutId != null) {
                    val workout = workouts[workoutId]!!
                    val stages = workout["stages"] as MutableMap<Long, MutableMap<String, Any>>

                    if (!stages.containsKey(stageId)) {
                        stages[stageId] =
                            mutableMapOf(
                                "stage" to
                                    WorkoutStage(
                                        id = stageId,
                                        programmedWorkoutId = row["stage_programmed_workout_id"] as Long,
                                        stageTypeId =
                                            (row["stage_stage_type_id"] as? Number)?.toInt() ?: throw IllegalStateException(
                                                "stage_stage_type_id is null"
                                            ),
                                        position =
                                            (row["stage_position"] as? Number)?.toInt() ?: throw IllegalStateException(
                                                "stage_position is null"
                                            ),
                                        name = row["stage_name"] as String,
                                        createdAt = parseTimestamp(row["stage_created_at"]),
                                        updatedAt = parseTimestamp(row["stage_updated_at"])
                                    ),
                                "exercises" to mutableMapOf<Long, MutableMap<String, Any>>()
                            )
                    }

                    // Add exercise if exists and not already added
                    if (exerciseId != null) {
                        val stage = stages[stageId]!!
                        val exercises = stage["exercises"] as MutableMap<Long, MutableMap<String, Any>>

                        if (!exercises.containsKey(exerciseId)) {
                            exercises[exerciseId] =
                                mutableMapOf(
                                    "exercise" to
                                        ProgrammedExercise(
                                            id = exerciseId,
                                            workoutStageId = row["exercise_workout_stage_id"] as Long,
                                            exerciseName = row["exercise_exercise_name"] as String,
                                            position =
                                                (row["exercise_position"] as? Number)?.toInt() ?: throw IllegalStateException(
                                                    "exercise_position is null"
                                                ),
                                            notes = row["exercise_notes"] as String?,
                                            createdAt = parseTimestamp(row["exercise_created_at"]),
                                            updatedAt = parseTimestamp(row["exercise_updated_at"])
                                        ),
                                    "setSchemes" to mutableListOf<SetScheme>()
                                )
                        }

                        // Add set scheme if exists
                        if (setSchemeId != null) {
                            val exercise = exercises[exerciseId]!!
                            val setSchemes = exercise["setSchemes"] as MutableList<SetScheme>

                            setSchemes.add(
                                SetScheme(
                                    id = setSchemeId,
                                    programmedExerciseId = row["set_scheme_programmed_exercise_id"] as Long,
                                    setNumber =
                                        (row["set_scheme_set_number"] as? Number)?.toInt() ?: throw IllegalStateException(
                                            "set_scheme_set_number is null"
                                        ),
                                    isAmrap =
                                        (row["set_scheme_is_amrap"] as? Boolean) ?: throw IllegalStateException(
                                            "set_scheme_is_amrap is null"
                                        ),
                                    isEmom =
                                        (row["set_scheme_is_emom"] as? Boolean) ?: throw IllegalStateException(
                                            "set_scheme_is_emom is null"
                                        ),
                                    useTempo =
                                        (row["set_scheme_use_tempo"] as? Boolean) ?: throw IllegalStateException(
                                            "set_scheme_use_tempo is null"
                                        ),
                                    eccentricTempo = row["set_scheme_eccentric_tempo"] as String?,
                                    isometricTempo = row["set_scheme_isometric_tempo"] as String?,
                                    concentricTempo = row["set_scheme_concentric_tempo"] as String?,
                                    targetWeight = (row["set_scheme_target_weight"] as? Number)?.let { BigDecimal(it.toString()) },
                                    performedWeight = (row["set_scheme_performed_weight"] as? Number)?.let { BigDecimal(it.toString()) },
                                    targetRepCount = (row["set_scheme_target_rep_count"] as? Number)?.toInt(),
                                    performedRepCount = (row["set_scheme_performed_rep_count"] as? Number)?.toInt(),
                                    restSeconds = (row["set_scheme_rest_seconds"] as? Number)?.toInt(),
                                    // Set schemes don't have band data in this context
                                    band = null,
                                    createdAt = parseTimestamp(row["set_scheme_created_at"]),
                                    updatedAt = parseTimestamp(row["set_scheme_updated_at"])
                                )
                            )
                        }
                    }
                }
            }

            // Convert the hierarchical map to the final model structure
            programsMap.values.map { programData ->
                val program = programData["program"] as Program
                val programPreferences = programData["programPreferences"] as ProgramPreferences
                val workoutsData = programData["workouts"] as Map<Long, MutableMap<String, Any>>

                val workouts =
                    workoutsData.values.map { workoutData ->
                        val workout = workoutData["workout"] as ProgrammedWorkout
                        val stagesData = workoutData["stages"] as Map<Long, MutableMap<String, Any>>

                        val stages =
                            stagesData.values.map { stageData ->
                                val stage = stageData["stage"] as WorkoutStage
                                val exercisesData = stageData["exercises"] as Map<Long, MutableMap<String, Any>>

                                val exercises =
                                    exercisesData.values.map { exerciseData ->
                                        val exercise = exerciseData["exercise"] as ProgrammedExercise
                                        val setSchemes = exerciseData["setSchemes"] as List<SetScheme>

                                        ProgrammedExerciseWithSetSchemes(
                                            exercise = exercise,
                                            setSchemes = setSchemes
                                        )
                                    }

                                WorkoutStageWithExercises(
                                    stage = stage,
                                    exercises = exercises
                                )
                            }

                        ProgrammedWorkoutWithStages(
                            workout = workout,
                            stages = stages
                        )
                    }

                ProgramWithWorkouts(
                    program = program,
                    programPreferences = programPreferences,
                    workouts = workouts
                )
            }
        }
    }

    /**
     * Safely deactivates all programs for a user, handling the case where no programs exist.
     *
     * @param userId The Keycloak user ID whose programs should be deactivated
     * @return Mono that completes when deactivation is done (or when no programs exist)
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "program"
    )
    private fun deactivateProgramsForUser(userId: String): Mono<Unit> {
        return postgresClient.updateLiteral<Unit>(
            "UPDATE program SET is_active=false, updated_at=NOW() WHERE user_id=$1",
            Unit::class,
            userId
        ).then(Mono.just(Unit))
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("No programs found to deactivate for user {}", userId)
                Mono.just(Unit)
            }
            .doOnSuccess {
                logger.debug("Successfully deactivated all programs for user {}", userId)
            }
            .doOnError { error ->
                logger.error("Failed to deactivate programs for user {}: {}", userId, error.message)
            }
    }

    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "program"
    )
    private fun deactivateOtherProgramsForUser(
        userId: String,
        excludeProgramId: Long
    ): Mono<Unit> {
        return postgresClient.updateLiteral<Unit>(
            "UPDATE program SET is_active=false, updated_at=NOW() WHERE user_id=$1 AND id != $2",
            Unit::class,
            userId,
            excludeProgramId
        ).then(Mono.just(Unit))
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("No other programs found to deactivate for user {}", userId)
                Mono.just(Unit)
            }
    }

    /**
     * Inserts a new program for a user, deactivating any existing active programs if needed.
     *
     * If the new program is set as active, this method first deactivates all existing programs for the user
     * before inserting the new one. If no existing programs are found, it inserts the new program directly.
     * If the new program is not active, it is inserted without deactivating others.
     *
     * This method ensures that only one active program exists per user at any time.
     *
     * @param userId The Keycloak user ID to associate with the new program
     * @param name The name of the new program
     * @param currentWeekNumber The current week number for the new program
     * @param isActive Whether the new program should be active (default: true)
     * @return Mono containing the inserted program
     * @throws NoResultsFoundException if the deactivation or insert operation fails due to missing records
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "program"
    )
    fun insertProgram(
        userId: String,
        name: String,
        currentWeekNumber: Int,
        isActive: Boolean = true
    ): Mono<Program> {
        logger.debug("Inserting program: {} for user {} with week number {} and isActive: {}", name, userId, currentWeekNumber, isActive)

        val insertQuery =
            """
            INSERT INTO program
                (user_id, name, current_week_number, is_active)
            VALUES
                ($1, $2, $3, $4)
            """.trimIndent()
        return if (isActive) {
            // If creating an active program, use transaction to ensure atomicity
            postgresClient.withTransaction {
                deactivateProgramsForUser(userId).flatMap {
                    postgresClient.update(insertQuery, userId, name, currentWeekNumber, isActive)
                }
            }
        } else {
            // If not active, just insert the program without deactivating others
            postgresClient.update(
                insertQuery,
                userId,
                name,
                currentWeekNumber,
                isActive,
            )
        }
    }

    /**
     * Updates an existing program in the database.
     *
     * This method updates the program record with the specified ID using the
     * provided parameters. If the program is being set to active, all other
     * programs for the same user are automatically deactivated to ensure only
     * one active program per user. If no program exists with the given ID, a
     * [NoResultsFoundException] is thrown.
     *
     * @param id The unique identifier of the program to update
     * @param name The updated name of the program
     * @param currentWeekNumber The updated current week number
     * @param isActive Whether the program should be active
     * @return Mono containing the updated program
     * @throws NoResultsFoundException if no program exists with the given ID
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "program"
    )
    fun updateProgram(
        id: Long,
        name: String,
        currentWeekNumber: Int,
        isActive: Boolean
    ): Mono<Program> {
        logger.debug("Updating program: {} with isActive: {}", id, isActive)

        return if (isActive) {
            // If setting to active, use transaction to ensure atomicity
            postgresClient.withTransaction {
                selectProgramById(id).flatMap { program ->
                    deactivateOtherProgramsForUser(program.userId, id).then(
                        postgresClient.update(
                            """
                            UPDATE program
                            SET name=$2, current_week_number=$3, is_active=$4, updated_at=NOW()
                            WHERE id=$1
                            """.trimIndent(),
                            id,
                            name,
                            currentWeekNumber,
                            isActive,
                        )
                    )
                }
            }
        } else {
            // If setting to inactive, just update the program
            postgresClient.update(
                """
                UPDATE program
                SET name=$2, current_week_number=$3, is_active=$4, updated_at=NOW()
                WHERE id=$1
                """.trimIndent(),
                id,
                name,
                currentWeekNumber,
                isActive,
            )
        }
    }

    /**
     * Deletes a program from the database.
     *
     * This method removes the program record with the specified ID from
     * the database. If no program exists with the given ID, a
     * [NoResultsFoundException] is thrown. The method returns the deleted
     * program data for confirmation.
     *
     * @param id The unique identifier of the program to delete
     * @return Mono containing the deleted program
     * @throws NoResultsFoundException if no program exists with the given ID
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "program"
    )
    fun deleteProgram(id: Long): Mono<Program> {
        logger.debug("Deleting program: {}", id)
        return postgresClient.update(
            "DELETE FROM program WHERE id=$1",
            id,
        )
    }

    /**
     * Parses timestamp from database format to Instant.
     *
     * @param value The timestamp value from database
     * @return Instant representation of the timestamp
     */
    private fun parseTimestamp(value: Any?): Instant {
        return when (value) {
            is Instant -> value
            is String -> {
                try {
                    // Try to parse as ISO-8601 format first
                    Instant.parse(value)
                } catch (e: Exception) {
                    try {
                        // Try to parse as LocalDateTime and convert to Instant
                        LocalDateTime.parse(value).atZone(ZoneOffset.UTC).toInstant()
                    } catch (e: Exception) {
                        // Fallback to current time if parsing fails
                        Instant.now()
                    }
                }
            }
            else -> Instant.now()
        }
    }
}
