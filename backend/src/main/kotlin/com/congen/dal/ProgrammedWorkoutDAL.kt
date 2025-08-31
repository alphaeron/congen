package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.model.ProgrammedWorkout
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for ProgrammedWorkout entity operations.
 *
 * This class provides database operations for the ProgrammedWorkout entity in the Congen application.
 * ProgrammedWorkout represents a scheduled workout within a program, including the day number,
 * name, and association with a specific program.
 *
 * ## ProgrammedWorkout Entity
 *
 * ProgrammedWorkout represents:
 * - A scheduled workout within a program
 * - Day number, name, and program association
 * - Used for structuring multi-day workout programs
 *
 * ## Database Operations
 *
 * - **Select by ID**: Retrieve programmed workout by its unique identifier
 * - **Select by program**: Retrieve all programmed workouts for a specific program
 * - **Select all**: Retrieve all programmed workout records
 * - **Insert**: Create new programmed workout records
 * - **Update**: Modify existing programmed workout properties
 * - **Delete**: Remove programmed workout records
 *
 * ## Error Handling
 *
 * - **NoResultsFoundException**: When programmed workout with specified ID doesn't exist
 * - **DatabaseException**: When database operations fail
 *
 * @param postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class ProgrammedWorkoutDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ProgrammedWorkoutDAL::class.java)
    }

    /**
     * Retrieves a programmed workout by its unique identifier.
     *
     * This method queries the database to find a programmed workout with the specified ID.
     * If no programmed workout exists with the given ID, a NoResultsFoundException is thrown.
     *
     * @param id The unique identifier of the programmed workout to retrieve
     * @return Mono containing the programmed workout if found
     * @throws NoResultsFoundException when programmed workout with the specified ID doesn't exist
     */
    @Cacheable(
        ttl = CacheTTL.SHORT_TERM,
        keyStrategy = CacheKeyStrategy.STANDARD,
        entityName = "programmed_workout"
    )
    fun selectProgrammedWorkoutById(id: Long): Mono<ProgrammedWorkout> {
        logger.debug("Selecting programmed workout by id: {}", id)
        return postgresClient.selectIndividual(
            "SELECT * FROM programmed_workout WHERE id=$1",
            id,
        )
    }

    /**
     * Retrieves all programmed workouts for a specific program.
     *
     * This method fetches all programmed workouts that are assigned to the specified program.
     * If no programmed workouts exist for the program, an empty list is returned.
     * Optionally filters by week number if provided.
     *
     * @param programId The unique identifier of the program
     * @param weekNumber Optional week number to filter workouts (1-based)
     * @return Mono containing a list of programmed workouts
     */
    @Cacheable(
        ttl = CacheTTL.SHORT_TERM,
        keyStrategy = CacheKeyStrategy.STANDARD,
        entityName = "programmed_workout"
    )
    fun selectProgrammedWorkoutsByProgramId(programId: Long, weekNumber: Int? = null): Mono<List<ProgrammedWorkout>> {
        logger.debug("Selecting programmed workouts by program id: {} and week: {}", programId, weekNumber)
        
        val query = if (weekNumber != null) {
            """
            SELECT pw.*
            FROM programmed_workout pw
            JOIN program p ON pw.program_id = p.id
            JOIN program_preferences pp ON p.id = pp.program_id
            WHERE pw.program_id = $1 
            AND CEIL(pw.day_number::float / pp.program_days_per_week) = $2
            ORDER BY pw.day_number
            """.trimIndent()
        } else {
            "SELECT * FROM programmed_workout WHERE program_id = $1 ORDER BY day_number"
        }
        
        return if (weekNumber != null) {
            postgresClient.select(query, programId, weekNumber)
        } else {
            postgresClient.select(query, programId)
        }
    }

    /**
     * Retrieves all programmed workout records from the database.
     *
     * This method fetches all programmed workout records and returns them as a list.
     * If no programmed workouts exist, an empty list is returned.
     *
     * @return Mono containing a list of all programmed workouts
     */
    @Cacheable(
        ttl = CacheTTL.SHORT_TERM,
        keyStrategy = CacheKeyStrategy.LIST_QUERY,
        entityName = "programmed_workout"
    )
    fun selectProgrammedWorkouts(): Mono<List<ProgrammedWorkout>> {
        logger.debug("Selecting all programmed workouts")
        return postgresClient.select("SELECT * FROM programmed_workout ORDER BY program_id, day_number")
    }

    /**
     * Retrieves all programmed workouts owned by a specific user.
     *
     * This method efficiently fetches all programmed workouts that belong to programs
     * owned by the specified user by joining with the program table.
     * If no programmed workouts exist for the user, an empty list is returned.
     *
     * @param userId The Keycloak identifier of the user
     * @return Mono containing a list of programmed workouts owned by the user
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "programmed_workout"
    )
    fun selectProgrammedWorkoutsByUserId(userId: String): Mono<List<ProgrammedWorkout>> {
        logger.debug("Selecting programmed workouts by user id: {}", userId)
        return postgresClient.select(
            """
            SELECT pw.*
            FROM programmed_workout pw
            JOIN program p ON pw.program_id = p.id
            WHERE p.user_id = $1
            ORDER BY pw.program_id, pw.day_number
            """.trimIndent(),
            userId
        )
    }

    /**
     * Creates a new programmed workout record in the database.
     *
     * This method inserts a new programmed workout record with the provided properties.
     * The programmed workout ID is automatically generated by the database.
     * The day number is validated before insertion.
     *
     * @param programId The ID of the program this workout belongs to
     * @param dayNumber The day number within the program
     * @param name The name of the workout
     * @return Mono containing the created programmed workout with generated ID
     * @throws DatabaseException when database operation fails
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.STANDARD,
        entityName = "programmed_workout"
    )
    fun insertProgrammedWorkout(
        programId: Long,
        dayNumber: Int,
        name: String
    ): Mono<ProgrammedWorkout> {
        logger.debug("Inserting programmed workout: {}", name)

        // Validate all CHECK constraints
        ValidationUtil.validateDayNumber(dayNumber)

        return postgresClient.update(
            """
            INSERT INTO programmed_workout
                (program_id, day_number, name)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            programId,
            dayNumber,
            name,
        )
    }

    /**
     * Updates an existing programmed workout record in the database.
     *
     * This method modifies the properties of the programmed workout with the specified ID.
     * If no programmed workout exists with the given ID, a NoResultsFoundException is thrown.
     * The day number is validated before update.
     *
     * @param id The unique identifier of the programmed workout to update
     * @param programId The updated program ID
     * @param dayNumber The updated day number within the program
     * @param name The updated name of the workout
     * @return Mono containing the updated programmed workout
     * @throws NoResultsFoundException when programmed workout with the specified ID doesn't exist
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.STANDARD,
        entityName = "programmed_workout"
    )
    fun updateProgrammedWorkout(
        id: Long,
        programId: Long,
        dayNumber: Int,
        name: String
    ): Mono<ProgrammedWorkout> {
        logger.debug("Updating programmed workout: {}", id)

        // Validate all CHECK constraints
        ValidationUtil.validateDayNumber(dayNumber)

        return postgresClient.update(
            """
            UPDATE programmed_workout
            SET program_id=$2, day_number=$3, name=$4, updated_at=NOW()
            WHERE id=$1
            """.trimIndent(),
            id,
            programId,
            dayNumber,
            name,
        )
    }

    /**
     * Checks if a user has any existing programmed workouts.
     *
     * This method queries the database to determine if the specified user has any
     * programmed workouts by joining with the program table.
     *
     * @param userId The Keycloak identifier of the user
     * @return Mono containing true if the user has workouts, false otherwise
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "programmed_workout"
    )
    fun hasUserExistingWorkouts(userId: String): Mono<Boolean> {
        logger.debug("Checking if user has existing workouts: {}", userId)
        // Explicit type
        return postgresClient.selectIndividual<Map<String, Any>>(
            """
            SELECT EXISTS(
                SELECT 1
                FROM programmed_workout pw
                JOIN program p ON pw.program_id = p.id
                WHERE p.user_id = $1
            ) AS value
            """.trimIndent(),
            userId,
        ).map { row: Map<String, Any> ->
            row["value"] as Boolean
        }
    }

    /**
     * Deletes a programmed workout record from the database.
     *
     * This method removes the programmed workout record with the specified ID.
     * If no programmed workout exists with the given ID, a NoResultsFoundException is thrown.
     *
     * @param id The unique identifier of the programmed workout to delete
     * @return Mono containing the deleted programmed workout
     * @throws NoResultsFoundException when programmed workout with the specified ID doesn't exist
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.STANDARD,
        entityName = "programmed_workout"
    )
    fun deleteProgrammedWorkout(id: Long): Mono<ProgrammedWorkout> {
        logger.debug("Deleting programmed workout: {}", id)
        return postgresClient.update(
            "DELETE FROM programmed_workout WHERE id=$1",
            id,
        )
    }
}
