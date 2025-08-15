package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Program
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

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
 * @property postgresClient Client for database operations
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
    fun selectPrograms(): Mono<List<Program>> {
        logger.debug("Selecting all programs")
        return postgresClient.select("SELECT * FROM program ORDER BY name")
    }

    /**
     * Retrieves programs for a specific user, optionally filtered by active status.
     *
     * This method queries the database for programs belonging to the specified user.
     * If isActive is provided, only programs with that active status are returned.
     * If no programs exist, an empty list is returned.
     *
     * @param userId The Keycloak user ID to filter programs by
     * @param isActive Optional filter for active status. If null, returns all programs for the user
     * @return Mono containing a list of programs for the user
     */
    fun selectProgramsByUserId(
        userId: String,
        isActive: Boolean? = null
    ): Mono<List<Program>> {
        logger.debug("Selecting programs for user: {} with isActive filter: {}", userId, isActive)

        val query =
            if (isActive != null) {
                "SELECT * FROM program WHERE user_id=$1 AND is_active=$2 ORDER BY name"
            } else {
                "SELECT * FROM program WHERE user_id=$1 ORDER BY name"
            }

        return if (isActive != null) {
            postgresClient.select(query, userId, isActive)
        } else {
            postgresClient.select(query, userId)
        }
    }

    /**
     * Safely deactivates all programs for a user, handling the case where no programs exist.
     *
     * @param userId The Keycloak user ID whose programs should be deactivated
     * @return Mono that completes when deactivation is done (or when no programs exist)
     */
    private fun deactivateProgramsForUser(userId: String): Mono<Unit> {
        return postgresClient.updateLiteral<Any>(
            "UPDATE program SET is_active=false, updated_at=NOW() WHERE user_id=$1",
            Any::class,
            userId
        ).then(Mono.just(Unit))
        .onErrorResume(NoResultsFoundException::class.java) {
            logger.warn("No programs found to deactivate for user {}", userId)
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
            // If creating an active program, first deactivate all existing programs for this user
            deactivateProgramsForUser(userId).then(
                postgresClient.update(insertQuery, userId, name, currentWeekNumber, isActive)
            )
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
     * provided parameters. If no program exists with the given ID, a
     * [NoResultsFoundException] is thrown.
     *
     * @param id The unique identifier of the program to update
     * @param name The updated name of the program
     * @param currentWeekNumber The updated current week number
     * @param isActive Whether the program should be active
     * @return Mono containing the updated program
     * @throws NoResultsFoundException if no program exists with the given ID
     */
    fun updateProgram(
        id: Long,
        name: String,
        currentWeekNumber: Int,
        isActive: Boolean
    ): Mono<Program> {
        logger.debug("Updating program: {} with isActive: {}", id, isActive)

        return postgresClient.update(
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
    fun deleteProgram(id: Long): Mono<Program> {
        logger.debug("Deleting program: {}", id)
        return postgresClient.update(
            "DELETE FROM program WHERE id=$1",
            id,
        )
    }
}
