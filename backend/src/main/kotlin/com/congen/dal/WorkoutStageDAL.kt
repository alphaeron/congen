package com.congen.dal

import com.congen.cache.annotation.Cacheable
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.CacheTTL
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheInvalidationStrategy
import com.congen.client.PostgresClient
import com.congen.model.WorkoutStage
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for WorkoutStage entities.
 *
 * This class provides database operations for WorkoutStage entities, including CRUD
 * operations and data validation. It uses the reactive PostgreSQL client for all
 * database interactions and includes comprehensive validation of workout stage data
 * before database operations.
 *
 * ## Operations
 *
 * - **Read**: Select workout stage by ID, select by programmed workout ID, select all
 * - **Create**: Insert new workout stage with validation
 * - **Update**: Update existing workout stage with validation
 * - **Delete**: Delete workout stage by ID
 *
 * ## Validation
 *
 * All workout stage data is validated before database operations using [ValidationUtil]:
 * - Position validation (> 0)
 *
 * ## WorkoutStage Entity
 *
 * Workout stages represent components of a programmed workout:
 * - Unique identifier and position within the workout
 * - Reference to the parent programmed workout
 * - Stage type (warm-up, main, cool-down, etc.)
 * - Associated programmed exercises
 *
 * ## Database Schema
 *
 * The workout_stage table contains:
 * - `id`: Primary key (auto-generated)
 * - `programmed_workout_id`: Foreign key to programmed_workout table
 * - `stage_type_id`: Foreign key to workout_stage_type table
 * - `position`: Position within the workout (must be > 0)
 *
 * @property postgresClient Client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class WorkoutStageDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(WorkoutStageDAL::class.java)
    }

    /**
     * Retrieves a workout stage by its unique identifier.
     *
     * This method queries the database for a workout stage with the specified ID.
     * If no workout stage is found, a [NoResultsFoundException] is thrown.
     *
     * @param id The unique identifier of the workout stage to retrieve
     * @return Mono containing the workout stage if found
     * @throws NoResultsFoundException if no workout stage exists with the given ID
     */
    @Cacheable(
        ttl = CacheTTL.SHORT_TERM,
        keyStrategy = CacheKeyStrategy.STANDARD,
        entityName = "workout_stage"
    )
    fun selectWorkoutStageById(id: Long): Mono<WorkoutStage> {
        logger.debug("Selecting workout stage by id: {}", id)
        return postgresClient.selectIndividual(
            "SELECT * FROM workout_stage WHERE id=$1",
            id,
        )
    }

    /**
     * Retrieves all workout stages for a specific programmed workout.
     *
     * This method queries the database for all workout stages that belong to a
     * specific programmed workout, ordered by their position within the workout.
     *
     * @param programmedWorkoutId The unique identifier of the programmed workout
     * @return Mono containing a list of workout stages for the workout
     */
    @Cacheable(
        ttl = CacheTTL.SHORT_TERM,
        keyStrategy = CacheKeyStrategy.STANDARD,
        entityName = "workout_stage"
    )
    fun selectWorkoutStagesByProgrammedWorkoutId(programmedWorkoutId: Long): Mono<List<WorkoutStage>> {
        logger.debug("Selecting workout stages by programmed workout id: {}", programmedWorkoutId)
        return postgresClient.select(
            "SELECT * FROM workout_stage WHERE programmed_workout_id=$1 ORDER BY position",
            programmedWorkoutId,
        )
    }

    /**
     * Retrieves all workout stages from the database.
     *
     * This method queries the database for all workout stage records and returns
     * them as a list, ordered by programmed workout ID and position. If no workout
     * stages exist, an empty list is returned.
     *
     * @return Mono containing a list of all workout stages
     */
    @Cacheable(
        ttl = CacheTTL.SHORT_TERM,
        keyStrategy = CacheKeyStrategy.LIST_QUERY,
        entityName = "workout_stage"
    )
    fun selectWorkoutStages(): Mono<List<WorkoutStage>> {
        logger.debug("Selecting all workout stages")
        return postgresClient.select("SELECT * FROM workout_stage ORDER BY programmed_workout_id, position")
    }

    /**
     * Retrieves all workout stages owned by a specific user.
     *
     * This method efficiently fetches all workout stages that belong to programmed workouts
     * owned by the specified user by joining through the relationship chain:
     * WorkoutStage → ProgrammedWorkout → Program → User
     * If no workout stages exist for the user, an empty list is returned.
     *
     * @param userId The Keycloak identifier of the user
     * @return Mono containing a list of workout stages owned by the user
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "workout_stage"
    )
    fun selectWorkoutStagesByUserId(userId: String): Mono<List<WorkoutStage>> {
        logger.debug("Selecting workout stages by user id: {}", userId)
        return postgresClient.select(
            """
            SELECT ws.*
            FROM workout_stage ws
            JOIN programmed_workout pw ON ws.programmed_workout_id = pw.id
            JOIN program p ON pw.program_id = p.id
            WHERE p.user_id = $1
            ORDER BY ws.programmed_workout_id, ws.position
            """.trimIndent(),
            userId
        )
    }

    /**
     * Inserts a new workout stage into the database.
     *
     * This method validates the workout stage data and inserts a new workout stage
     * record. The workout stage ID is automatically generated by the database.
     * All workout stage properties are validated before insertion.
     *
     * @param programmedWorkoutId The ID of the programmed workout this stage belongs to
     * @param stageTypeId The ID of the stage type (warm-up, main, cool-down, etc.)
     * @param position The position of this stage within the workout
     * @param name The name of the workout stage
     * @return Mono containing the inserted workout stage with generated ID
     * @throws ValidationException if workout stage data fails validation
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.STANDARD,
        entityName = "workout_stage"
    )
    fun insertWorkoutStage(
        programmedWorkoutId: Long,
        stageTypeId: Int,
        position: Int,
        name: String,
    ): Mono<WorkoutStage> {
        logger.debug("Inserting workout stage for workout: {}, position: {}, name: {}", programmedWorkoutId, position, name)

        // Validate all CHECK constraints
        ValidationUtil.validatePosition(position)

        return postgresClient.update(
            """
            INSERT INTO workout_stage
                (programmed_workout_id, stage_type_id, position, name)
            VALUES
                ($1, $2, $3, $4)
            """.trimIndent(),
            programmedWorkoutId,
            stageTypeId,
            position,
            name,
        )
    }

    /**
     * Updates an existing workout stage in the database.
     *
     * This method validates the workout stage data and updates the workout stage
     * record with the specified ID. All workout stage properties are validated
     * before the update operation.
     *
     * @param id The unique identifier of the workout stage to update
     * @param programmedWorkoutId The ID of the programmed workout this stage belongs to
     * @param stageTypeId The ID of the stage type (warm-up, main, cool-down, etc.)
     * @param position The position of this stage within the workout
     * @param name The name of the workout stage
     * @return Mono containing the updated workout stage
     * @throws ValidationException if workout stage data fails validation
     * @throws NoResultsFoundException if no workout stage exists with the given ID
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.STANDARD,
        entityName = "workout_stage"
    )
    fun updateWorkoutStage(
        id: Long,
        programmedWorkoutId: Long,
        stageTypeId: Int,
        position: Int,
        name: String,
    ): Mono<WorkoutStage> {
        logger.debug("Updating workout stage: {}", id)

        // Validate all CHECK constraints
        ValidationUtil.validatePosition(position)

        return postgresClient.update(
            """
            UPDATE workout_stage
            SET programmed_workout_id=$2, stage_type_id=$3, position=$4, name=$5, updated_at=NOW()
            WHERE id=$1
            """.trimIndent(),
            id,
            programmedWorkoutId,
            stageTypeId,
            position,
            name,
        )
    }

    /**
     * Checks if a workout stage exists for a specific workout and position.
     *
     * This method queries the database to check if a workout stage already exists
     * for the given programmed workout ID and position. This is useful for preventing
     * duplicate stage creation.
     *
     * @param programmedWorkoutId The ID of the programmed workout
     * @param position The position of the stage within the workout
     * @return Mono containing the existing workout stage if found, or empty if not found
     */
    @Cacheable(
        ttl = CacheTTL.SHORT_TERM,
        keyStrategy = CacheKeyStrategy.RELATIONSHIP,
        entityName = "workout_stage"
    )
    fun selectWorkoutStageByWorkoutIdAndPosition(
        programmedWorkoutId: Long,
        position: Int
    ): Mono<WorkoutStage> {
        logger.debug("Checking for existing workout stage for workout: {}, position: {}", programmedWorkoutId, position)
        return postgresClient.selectIndividual(
            "SELECT * FROM workout_stage WHERE programmed_workout_id=$1 AND position=$2",
            programmedWorkoutId,
            position,
        )
    }

    /**
     * Deletes a workout stage from the database.
     *
     * This method removes the workout stage record with the specified ID from
     * the database. If no workout stage exists with the given ID, a
     * [NoResultsFoundException] is thrown. The method returns the deleted
     * workout stage data for confirmation.
     *
     * @param id The unique identifier of the workout stage to delete
     * @return Mono containing the deleted workout stage
     * @throws NoResultsFoundException if no workout stage exists with the given ID
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.STANDARD,
        entityName = "workout_stage"
    )
    fun deleteWorkoutStage(id: Long): Mono<WorkoutStage> {
        logger.debug("Deleting workout stage: {}", id)
        return postgresClient.update(
            "DELETE FROM workout_stage WHERE id=$1",
            id,
        )
    }
}
