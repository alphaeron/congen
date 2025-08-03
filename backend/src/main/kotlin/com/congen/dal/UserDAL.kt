package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.User
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Data Access Layer for User entities.
 *
 * This class provides database operations for User entities, including CRUD
 * operations and data validation. It uses the reactive PostgreSQL client
 * for all database interactions and includes comprehensive validation
 * of user data before database operations.
 *
 * ## Operations
 *
 * - **Read**: Select user by Keycloak ID, select all users
 * - **Create**: Insert new user with validation
 * - **Update**: Update existing user with validation
 * - **Delete**: Delete user by Keycloak ID
 *
 * ## Validation
 *
 * All user data is validated before database operations using [ValidationUtil]:
 * - Age validation (1-150 years)
 * - Height validation (0.01-300 cm)
 * - Weight validation (0.01-1000 kg)
 *
 * @property postgresClient Client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class UserDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(UserDAL::class.java)
    }

    /**
     * Retrieves a user by their Keycloak identifier.
     *
     * This method queries the database for a user with the specified Keycloak ID.
     * If no user is found, a [NoResultsFoundException] is thrown.
     *
     * @param keycloakId The Keycloak identifier of the user to retrieve
     * @return Mono containing the user if found
     * @throws NoResultsFoundException if no user exists with the given Keycloak ID
     */
    fun selectUserByKeycloakId(keycloakId: String): Mono<User> {
        logger.debug("Selecting user by Keycloak ID: {}", keycloakId)
        return postgresClient.selectIndividual(
            "SELECT * FROM \"user\" WHERE keycloak_id=$1",
            keycloakId,
        )
    }

    /**
     * Retrieves all users from the database.
     *
     * This method queries the database for all user records and returns
     * them as a list. If no users exist, an empty list is returned.
     *
     * @return Mono containing a list of all users
     */
    fun selectUsers(): Mono<List<User>> {
        logger.debug("Selecting all users")
        return postgresClient.select("SELECT * FROM \"user\"")
    }

    /**
     * Inserts a new user into the database.
     *
     * This method validates the user data and inserts a new user record.
     * The Keycloak ID is used as the primary key. All user properties
     * are validated before insertion.
     *
     * @param keycloakId The Keycloak identifier for the user
     * @param name The user's full name
     * @param age The user's age in years
     * @param height The user's height in centimeters
     * @param weight The user's weight in kilograms
     * @return Mono containing the inserted user
     * @throws ValidationException if user data fails validation
     */
    fun insertUser(
        keycloakId: String,
        name: String,
        age: Int,
        height: BigDecimal,
        weight: BigDecimal
    ): Mono<User> {
        logger.debug("Inserting user: {} with Keycloak ID: {}", name, keycloakId)

        // Validate all CHECK constraints
        ValidationUtil.validateUserAge(age)
        ValidationUtil.validateUserHeight(height)
        ValidationUtil.validateUserWeight(weight)

        return postgresClient.update(
            """
            INSERT INTO "user"
                (keycloak_id, name, age, height, weight)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent(),
            keycloakId,
            name,
            age,
            height,
            weight,
        )
    }

    /**
     * Updates an existing user in the database.
     *
     * This method validates the user data and updates the user record
     * with the specified Keycloak ID. All user properties are validated before
     * the update operation.
     *
     * @param keycloakId The Keycloak identifier of the user to update
     * @param name The updated user's full name
     * @param age The updated user's age in years
     * @param height The updated user's height in centimeters
     * @param weight The updated user's weight in kilograms
     * @return Mono containing the updated user
     * @throws ValidationException if user data fails validation
     * @throws NoResultsFoundException if no user exists with the given Keycloak ID
     */
    fun updateUser(
        keycloakId: String,
        name: String,
        age: Int,
        height: BigDecimal,
        weight: BigDecimal
    ): Mono<User> {
        logger.debug("Updating user with Keycloak ID: {}", keycloakId)

        // Validate all CHECK constraints
        ValidationUtil.validateUserAge(age)
        ValidationUtil.validateUserHeight(height)
        ValidationUtil.validateUserWeight(weight)

        return postgresClient.update(
            """
            UPDATE "user"
            SET name=$2, age=$3, height=$4, weight=$5, updated_at=NOW()
            WHERE keycloak_id=$1
            """.trimIndent(),
            keycloakId,
            name,
            age,
            height,
            weight,
        )
    }

    /**
     * Deletes a user from the database.
     *
     * This method removes the user record with the specified Keycloak ID from
     * the database. If no user exists with the given Keycloak ID, a
     * [NoResultsFoundException] is thrown.
     *
     * @param keycloakId The Keycloak identifier of the user to delete
     * @return Mono containing the deleted user
     * @throws NoResultsFoundException if no user exists with the given Keycloak ID
     */
    fun deleteUser(keycloakId: String): Mono<User> {
        logger.debug("Deleting user with Keycloak ID: {}", keycloakId)
        return postgresClient.update(
            "DELETE FROM \"user\" WHERE keycloak_id=$1",
            keycloakId,
        )
    }
}
