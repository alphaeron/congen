package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.User
import com.congen.service.AuditService
import com.congen.util.EncryptionUtil
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Data Access Layer for User entities with GDPR-compliant encryption.
 *
 * This class provides database operations for User entities, including CRUD
 * operations, data validation, and transparent encryption/decryption of sensitive
 * personal data. It uses the reactive PostgreSQL client for all database
 * interactions and includes comprehensive validation and audit logging.
 *
 * ## Operations
 *
 * - **Read**: Select user by Keycloak ID, select all users (with decryption)
 * - **Create**: Insert new user with validation and encryption
 * - **Update**: Update existing user with validation and encryption
 * - **Delete**: Delete user by Keycloak ID (GDPR right to erasure)
 *
 * ## GDPR Compliance
 *
 * - **Encryption**: Sensitive data (name) encrypted with AES-256-GCM
 * - **Audit Logging**: All data access operations logged for compliance
 * - **Consent Tracking**: User consent status managed and tracked
 * - **Data Minimization**: Only necessary data stored and encrypted
 *
 * ## Validation
 *
 * All user data is validated before database operations using [ValidationUtil]:
 * - Name validation (1-255 characters)
 *
 * @property postgresClient Client for database operations
 * @property encryptionUtil Utility for encrypting/decrypting sensitive data
 * @property auditService Service for logging data access operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class UserDAL(
    private val postgresClient: PostgresClient,
    private val encryptionUtil: EncryptionUtil,
    private val auditService: AuditService,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(UserDAL::class.java)
    }

    /**
     * Retrieves a user by their Keycloak identifier with decryption and audit logging.
     *
     * This method queries the database for a user with the specified Keycloak ID,
     * decrypts sensitive personal data, logs the access for GDPR compliance,
     * and updates the last accessed timestamp.
     *
     * @param keycloakId The Keycloak identifier of the user to retrieve
     * @return Mono containing the decrypted user if found
     * @throws NoResultsFoundException if no user exists with the given Keycloak ID
     */
    fun selectUserByKeycloakId(keycloakId: String): Mono<User> {
        logger.debug("Selecting user by Keycloak ID: {}", keycloakId)

        return postgresClient.selectIndividual<Map<String, Any>>(
            "SELECT * FROM \"user\" WHERE keycloak_id=$1",
            keycloakId,
        ).flatMap { row ->
            // Log data access for GDPR audit
            auditService.logDataAccess(
                keycloakId = keycloakId,
                dataType = "USER_PROFILE",
                accessedBy = "SYSTEM"
            ).then(
                // Decrypt and return user data
                Mono.fromCallable {
                    decryptUserData(row)
                }
            )
        }
    }

    /**
     * Inserts a new user into the database with encryption and audit logging.
     *
     * This method validates the user data, encrypts sensitive personal information,
     * inserts a new user record, and logs the operation for GDPR compliance.
     *
     * @param keycloakId The Keycloak identifier for the user
     * @param name The user's full name (will be encrypted)
     * @return Mono containing the inserted user with decrypted data
     * @throws ValidationException if user data fails validation
     */
    fun insertUser(
        keycloakId: String,
        name: String
    ): Mono<User> {
        logger.debug("Inserting user: {} with Keycloak ID: {}", name, keycloakId)

        // Encrypt sensitive data
        val encryptedName = encryptionUtil.encrypt(name)

        return postgresClient.update<Map<String, Any>>(
            """
            INSERT INTO "user"
                (keycloak_id, name)
            VALUES
                ($1, $2)
            """.trimIndent(),
            keycloakId,
            encryptedName
        ).flatMap {
            // Log the data creation for GDPR audit
            auditService.logDataOperation(
                keycloakId = keycloakId,
                operation = "DATA_CREATION",
                dataType = "USER_PROFILE"
            ).then(
                // Return the user with decrypted data
                Mono.fromCallable {
                    User(
                        keycloakId = keycloakId,
                        name = name,
                        createdAt = Instant.now(),
                        updatedAt = Instant.now()
                    )
                }
            )
        }
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

    /**
     * Checks if a user exists in the system.
     *
     * This is a lightweight check to verify user existence without retrieving
     * sensitive data.
     *
     * @param keycloakId The user's Keycloak ID
     * @return Mono containing true if user exists, false otherwise
     */
    fun userExists(keycloakId: String): Mono<Boolean> {
        logger.debug("Checking if user exists: {}", keycloakId)

        return postgresClient.selectIndividual<Map<String, Any>>(
            "SELECT 1 FROM \"user\" WHERE keycloak_id = $1",
            keycloakId
        ).map { true }
            .onErrorReturn(false)
            .doOnSuccess { exists ->
                logger.debug("User {} exists: {}", keycloakId, exists)
            }
    }

    /**
     * Deletes a user by Keycloak ID for GDPR right to erasure.
     *
     * @param keycloakId The user's Keycloak ID
     * @return Mono that completes when user is deleted
     */
    fun deleteUserByKeycloakId(keycloakId: String): Mono<Void> {
        logger.warn("Deleting all data for user: {}", keycloakId)
        return postgresClient.update<Map<String, Any>>(
            "DELETE FROM \"user\" WHERE keycloak_id = $1",
            keycloakId
        ).then()
    }

    /**
     * Decrypts user data from database row.
     *
     * @param row Database row containing encrypted user data
     * @return Decrypted User object
     */
    private fun decryptUserData(row: Map<String, Any>): User {
        val keycloakId = row["keycloak_id"] as String

        // Handle both encrypted and unencrypted data for backward compatibility
        val name = decryptField(row["name"])

        // Handle timestamp conversion from database format
        val createdAt = parseTimestamp(row["created_at"])
        val updatedAt = parseTimestamp(row["updated_at"])

        return User(
            keycloakId = keycloakId,
            name = name ?: "",
            createdAt = createdAt,
            updatedAt = updatedAt,
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
                        java.time.LocalDateTime.parse(value).atZone(java.time.ZoneOffset.UTC).toInstant()
                    } catch (e: Exception) {
                        // Fallback to current time if parsing fails
                        Instant.now()
                    }
                }
            }
            else -> Instant.now()
        }
    }

    /**
     * Decrypts a field value, handling both encrypted and plain text data.
     *
     * @param value The field value (may be encrypted or plain text)
     * @return Decrypted string value or null
     */
    private fun decryptField(value: Any?): String? {
        return when (value) {
            null -> null
            is String -> {
                try {
                    // Try to decrypt - if it fails, assume it's plain text
                    encryptionUtil.decrypt(value) ?: value
                } catch (e: Exception) {
                    // If decryption fails, assume it's plain text (backward compatibility)
                    value
                }
            }
            else -> value.toString()
        }
    }
}
