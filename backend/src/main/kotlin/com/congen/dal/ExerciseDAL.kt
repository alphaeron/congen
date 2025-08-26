package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.model.Exercise
import com.congen.model.MovementType
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for Exercise entity operations.
 *
 * This class provides database operations for the Exercise entity in the Congen application.
 * Exercises represent physical movements or activities that can be performed during workouts,
 * with various characteristics such as movement type, unilateral/bilateral nature, and body focus.
 *
 * ## Exercise Entity
 *
 * Exercise represents:
 * - Physical movements or activities for workouts
 * - Movement type (push, pull, squat, hinge, etc.)
 * - Unilateral/bilateral classification
 * - Upper/lower body focus
 * - Accessory movement classification
 *
 * ## Database Operations
 *
 * - **Select by name**: Retrieve exercise by its unique name
 * - **Select all**: Retrieve all exercise records
 * - **Insert**: Create new exercise records
 * - **Update**: Modify existing exercise properties
 * - **Delete**: Remove exercise records
 *
 * ## Error Handling
 *
 * - **NoResultsFoundException**: When exercise with specified name doesn't exist
 * - **DatabaseException**: When database operations fail
 *
 * @param postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class ExerciseDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ExerciseDAL::class.java)
    }

    /**
     * Retrieves exercise by its unique name.
     *
     * This method queries the database to find exercise with the specified name.
     * If no exercise exists with the given name, a NoResultsFoundException is thrown.
     *
     * @param exerciseName The unique name of the exercise to retrieve
     * @return Mono containing the exercise if found
     * @throws NoResultsFoundException when exercise with the specified name doesn't exist
     */
    @Cacheable(
        ttl = CacheTTL.LONG_TERM,
        keyStrategy = CacheKeyStrategy.ENTITY_BY_NAME,
        entityName = "exercise"
    )
    fun selectExerciseByName(exerciseName: String): Mono<Exercise> {
        logger.debug("Selecting exercise by name: {}", exerciseName)
        return postgresClient.selectIndividual(
            "SELECT * FROM exercise WHERE name=$1",
            exerciseName,
        )
    }

    /**
     * Retrieves all exercise records from the database.
     *
     * This method fetches all exercise records and returns them as a list.
     * If no exercises exist, an empty list is returned.
     *
     * @return Mono containing a list of all exercises
     */
    @Cacheable(
        ttl = CacheTTL.LONG_TERM,
        keyStrategy = CacheKeyStrategy.LIST_QUERY,
        entityName = "exercise"
    )
    fun selectExercises(): Mono<List<Exercise>> {
        logger.debug("Selecting all exercises")
        return postgresClient.select("SELECT * FROM exercise")
    }

    /**
     * Creates a new exercise record in the database.
     *
     * This method inserts a new exercise record with the provided properties.
     * The exercise name must be unique in the database.
     *
     * @param name The name of the exercise
     * @param description The description of the exercise
     * @param movementType The movement type of the exercise
     * @param isUnilateral Whether the exercise is unilateral
     * @param isUpper Whether the exercise is upper body
     * @param isAccessory Whether the exercise is accessory
     * @return Mono containing the created exercise
     * @throws DatabaseException when the exercise name already exists or database operation fails
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.ENTITY_BY_NAME,
        entityName = "exercise"
    )
    fun insertExercise(
        name: String,
        description: String,
        movementType: MovementType,
        isUnilateral: Boolean,
        isUpper: Boolean,
        isAccessory: Boolean,
    ): Mono<Exercise> {
        logger.debug("Inserting exercise: {}", name)

        return postgresClient.update(
            """
            INSERT INTO exercise
                (name, description, movement_type, is_unilateral, is_upper, is_accessory)
            VALUES
                ($1, $2, $3, $4, $5, $6)
            """.trimIndent(),
            name,
            description,
            movementType,
            isUnilateral,
            isUpper,
            isAccessory,
        )
    }

    /**
     * Updates an existing exercise record in the database.
     *
     * This method modifies the properties of exercise with the specified name.
     * If no exercise exists with the given name, a NoResultsFoundException is thrown.
     *
     * @param name The name of the exercise
     * @param description The updated description of the exercise
     * @param movementType The updated movement type of the exercise
     * @param isUnilateral Whether the exercise is unilateral
     * @param isUpper Whether the exercise is upper body
     * @param isAccessory Whether the exercise is accessory
     * @return Mono containing the updated exercise
     * @throws NoResultsFoundException when exercise with the specified name doesn't exist
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.ENTITY_BY_NAME,
        entityName = "exercise"
    )
    fun updateExercise(
        name: String,
        description: String,
        movementType: MovementType,
        isUnilateral: Boolean,
        isUpper: Boolean,
        isAccessory: Boolean,
    ): Mono<Exercise> {
        logger.debug("Updating exercise: {}", name)

        return postgresClient.update(
            """
            UPDATE exercise
            SET description=$2, movement_type=$3, is_unilateral=$4, is_upper=$5, is_accessory=$6
            WHERE name=$1
            """.trimIndent(),
            name,
            description,
            movementType,
            isUnilateral,
            isUpper,
            isAccessory,
        )
    }

    /**
     * Deletes an exercise record from the database.
     *
     * This method removes the exercise record with the specified name.
     * If no exercise exists with the given name, a NoResultsFoundException is thrown.
     *
     * @param exerciseName The unique name of the exercise to delete
     * @return Mono containing the deleted exercise
     * @throws NoResultsFoundException when exercise with the specified name doesn't exist
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.ENTITY_BY_NAME,
        entityName = "exercise"
    )
    fun deleteExercise(exerciseName: String): Mono<Exercise> {
        logger.debug("Deleting exercise: {}", exerciseName)
        return postgresClient.update(
            "DELETE FROM exercise WHERE name=$1",
            exerciseName,
        )
    }
}
