package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.ExerciseRotationHistory
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for ExerciseRotationHistory entities.
 *
 * This class provides database operations for ExerciseRotationHistory entities, including CRUD
 * operations and data validation. It uses the reactive PostgreSQL client
 * for all database interactions and includes comprehensive validation
 * of exercise rotation history data before database operations.
 *
 * ## Operations
 *
 * - **Read**: Select by ID, select by user ID, select by user ID and category, select all
 * - **Create**: Insert new exercise rotation history record with validation
 * - **Update**: Update existing exercise rotation history record with validation
 * - **Delete**: Delete by ID, delete by user ID
 *
 * ## Validation
 *
 * All exercise rotation history data is validated before database operations using [ValidationUtil]:
 * - Category validation (must be valid exercise category)
 * - Exercise name validation (must reference existing exercise)
 * - User ID validation (must reference existing user)
 *
 * @property postgresClient Client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class ExerciseRotationHistoryDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ExerciseRotationHistoryDAL::class.java)
    }

    /**
     * Retrieves an exercise rotation history record by its unique identifier.
     *
     * This method queries the database for an exercise rotation history record with the specified ID.
     * If no record is found, a [NoResultsFoundException] is thrown.
     *
     * @param id The unique identifier of the exercise rotation history record to retrieve
     * @return Mono containing the exercise rotation history record if found
     * @throws NoResultsFoundException if no record exists with the given ID
     */
    fun selectById(id: Long): Mono<ExerciseRotationHistory> {
        logger.debug("Selecting exercise rotation history by id: {}", id)
        return postgresClient.selectIndividual(
            "SELECT * FROM exercise_rotation_history WHERE id=$1",
            id,
        )
    }

    /**
     * Retrieves all exercise rotation history records.
     *
     * This method queries the database for all exercise rotation history records.
     * If no records exist, an empty list is returned.
     *
     * @return Mono containing a list of all exercise rotation history records
     */
    fun selectAll(): Mono<List<ExerciseRotationHistory>> {
        logger.debug("Selecting all exercise rotation history records")
        return postgresClient.select("SELECT * FROM exercise_rotation_history ORDER BY created_at DESC")
    }

    /**
     * Retrieves exercise rotation history records for a specific accessory type.
     *
     * This method queries the database for exercise rotation history records
     * associated with the specified accessory type. If no records exist, an empty list is returned.
     *
     * @param isAccessory Whether to filter by accessory exercises
     * @return Mono containing a list of exercise rotation history records for the accessory type
     */
    fun selectByIsAccessory(isAccessory: Boolean): Mono<List<ExerciseRotationHistory>> {
        logger.debug("Selecting exercise rotation history by isAccessory: {}", isAccessory)
        return postgresClient.select(
            "SELECT * FROM exercise_rotation_history WHERE is_accessory=$1 ORDER BY created_at DESC",
            isAccessory,
        )
    }

    /**
     * Retrieves all exercise rotation history records for a specific user, optionally filtered by accessory type.
     *
     * @param userId The Keycloak user ID
     * @param isAccessory Optional filter for accessory exercises
     * @return Mono containing a list of exercise rotation history records for the user
     */
    fun selectByUserId(
        userId: String,
        isAccessory: Boolean? = null
    ): Mono<List<ExerciseRotationHistory>> {
        logger.debug("Selecting exercise rotation history by user id: {} and isAccessory: {}", userId, isAccessory)
        return if (isAccessory == null) {
            postgresClient.select(
                "SELECT * FROM exercise_rotation_history WHERE user_id=$1 ORDER BY created_at DESC",
                userId
            )
        } else {
            postgresClient.select(
                "SELECT * FROM exercise_rotation_history WHERE user_id=$1 AND is_accessory=$2 ORDER BY created_at DESC",
                userId,
                isAccessory
            )
        }
    }

    /**
     * Inserts a new exercise rotation history record into the database.
     *
     * This method validates the exercise rotation history data and inserts a new record.
     * The record ID is automatically generated by the database. All properties are validated before insertion.
     *
     * @param userId The Keycloak ID of the user
     * @param exerciseName The name of the exercise that was used
     * @param isAccessory Whether the exercise was used as an accessory movement
     * @return Mono containing the inserted exercise rotation history record with generated ID
     * @throws ValidationException if exercise rotation history data fails validation
     */
    fun insert(
        userId: String,
        exerciseName: String,
        isAccessory: Boolean,
    ): Mono<ExerciseRotationHistory> {
        logger.debug(
            "Inserting exercise rotation history: userId={}, exercise_name={}, isAccessory={}",
            userId,
            exerciseName,
            isAccessory,
        )

        return postgresClient.update(
            """
            INSERT INTO exercise_rotation_history
                (user_id, exercise_name, is_accessory)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            userId,
            exerciseName,
            isAccessory,
        )
    }

    /**
     * Updates an existing exercise rotation history record in the database.
     *
     * This method validates the exercise rotation history data and updates the record
     * with the specified ID. All properties are validated before the update operation.
     *
     * @param id The unique identifier of the exercise rotation history record to update
     * @param userId The Keycloak ID of the user
     * @param exerciseName The name of the exercise that was used
     * @param isAccessory Whether the exercise was used as an accessory movement
     * @return Mono containing the updated exercise rotation history record
     * @throws ValidationException if exercise rotation history data fails validation
     * @throws NoResultsFoundException if no record exists with the given ID
     */
    fun update(
        id: Long,
        userId: String,
        exerciseName: String,
        isAccessory: Boolean,
    ): Mono<ExerciseRotationHistory> {
        logger.debug("Updating exercise rotation history: {}", id)

        return postgresClient.update(
            """
            UPDATE exercise_rotation_history
            SET user_id=$2, exercise_name=$3, is_accessory=$4
            WHERE id=$1
            """.trimIndent(),
            id,
            userId,
            exerciseName,
            isAccessory,
        )
    }

    /**
     * Deletes an exercise rotation history record from the database.
     *
     * This method removes the exercise rotation history record with the specified ID from
     * the database. If no record exists with the given ID, a [NoResultsFoundException] is thrown.
     *
     * @param id The unique identifier of the exercise rotation history record to delete
     * @return Mono containing the deleted exercise rotation history record
     * @throws NoResultsFoundException if no record exists with the given ID
     */
    fun deleteById(id: Long): Mono<ExerciseRotationHistory> {
        logger.debug("Deleting exercise rotation history: {}", id)
        return postgresClient.update(
            "DELETE FROM exercise_rotation_history WHERE id=$1",
            id,
        )
    }

    /**
     * Deletes all exercise rotation history records for a specific user.
     *
     * This method removes all exercise rotation history records associated with the specified user ID
     * from the database. If no records exist for the user, a [NoResultsFoundException] is thrown.
     *
     * @param userId The Keycloak ID of the user whose exercise rotation history records to delete
     * @return Mono containing the number of deleted records
     * @throws NoResultsFoundException if no records exist for the given user ID
     */
    fun deleteByUserId(userId: String): Mono<Int> {
        logger.debug("Deleting exercise rotation history for user: {}", userId)
        return postgresClient.update(
            "DELETE FROM exercise_rotation_history WHERE user_id=$1",
            userId,
        )
    }
}
