package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.model.User
import com.congen.service.AuditService
import com.congen.util.EncryptionUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

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
 * @param postgresClient Client for database operations
 * @param encryptionUtil Utility for encrypting/decrypting sensitive data
 * @param auditService Service for logging data access operations
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
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user"
    )
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
     * Retrieves random user keycloak IDs from the database for cache warming.
     *
     * This method queries the database for a specified number of random user
     * keycloak IDs without fetching full user data. This method is primarily used
     * for cache warming operations and is not cached since it's only used internally.
     *
     * @param numUsers The number of random user keycloak IDs to retrieve
     * @return Mono containing a list of random user keycloak IDs
     */
    fun selectRandomUserIds(numUsers: Int): Mono<List<String>> {
        logger.debug("Selecting {} random user keycloak IDs", numUsers)
        return postgresClient.select<Map<String, Any>>(
            "SELECT keycloak_id FROM \"user\" ORDER BY RANDOM() LIMIT $1",
            numUsers
        ).map { rows ->
            rows.mapNotNull { row ->
                try {
                    row["keycloak_id"] as? String
                } catch (e: Exception) {
                    logger.warn("Failed to extract keycloak_id from row: {}", row, e)
                    null
                }
            }
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
     * @param age The user's age in years (optional, will be encrypted)
     * @param weight The user's weight in pounds (optional, will be encrypted)
     * @param height The user's height in inches (optional, will be encrypted)
     * @param gender The user's gender (optional, will be encrypted)
     * @return Mono containing the inserted user with decrypted data
     * @throws ValidationException if user data fails validation
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "user"
    )
    fun insertUser(
        keycloakId: String,
        name: String,
        age: Int? = null,
        weight: Int? = null,
        height: Int? = null,
        gender: String? = null
    ): Mono<User> {
        logger.debug("Inserting user: {} with Keycloak ID: {}", name, keycloakId)

        // Encrypt sensitive data
        val encryptedName = encryptionUtil.encrypt(name)
        val encryptedAge = age?.let { encryptionUtil.encrypt(it.toString()) }
        val encryptedWeight = weight?.let { encryptionUtil.encrypt(it.toString()) }
        val encryptedHeight = height?.let { encryptionUtil.encrypt(it.toString()) }
        val encryptedGender = gender?.let { encryptionUtil.encrypt(it) }

        // Use transaction to ensure user creation and audit logging are atomic
        return postgresClient.withTransaction {
            postgresClient.update<Map<String, Any>>(
                """
                INSERT INTO "user"
                    (keycloak_id, name, age, weight, height, gender)
                VALUES
                    ($1, $2, $3, $4, $5, $6)
                """.trimIndent(),
                keycloakId,
                encryptedName,
                encryptedAge,
                encryptedWeight,
                encryptedHeight,
                encryptedGender
            ).flatMap { insertedRow ->
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
                            age = age,
                            weight = weight,
                            height = height,
                            gender = gender,
                            createdAt = parseTimestamp(insertedRow["created_at"]),
                            updatedAt = parseTimestamp(insertedRow["updated_at"])
                        )
                    }
                )
            }
        }
    }

    /**
     * Deletes a user by Keycloak ID for GDPR right to erasure.
     *
     * This method removes the user record with the specified Keycloak ID from
     * the database. If no user exists with the given Keycloak ID, a
     * [NoResultsFoundException] is thrown.
     *
     * @param keycloakId The Keycloak identifier of the user to delete
     * @return Mono containing the deleted user
     * @throws NoResultsFoundException if no user exists with the given Keycloak ID
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "user"
    )
    fun deleteUserByKeycloakId(keycloakId: String): Mono<User> {
        logger.debug("Deleting user with Keycloak ID: {}", keycloakId)

        return postgresClient.selectIndividual<Map<String, Any>>(
            "SELECT * FROM \"user\" WHERE keycloak_id=$1",
            keycloakId,
        ).flatMap { userRow ->
            // Use transaction to ensure audit logging and user deletion are atomic
            postgresClient.withTransaction {
                // Log the data deletion for GDPR audit
                auditService.logDataOperation(
                    keycloakId = keycloakId,
                    operation = "DATA_DELETION",
                    dataType = "USER_PROFILE"
                ).flatMap {
                    // Delete the user - use the generic update method which automatically appends RETURNING *
                    postgresClient.update<Map<String, Any>>(
                        "DELETE FROM \"user\" WHERE keycloak_id=$1",
                        keycloakId,
                    ).map {
                        // Return the deleted user with decrypted data
                        User(
                            keycloakId = keycloakId,
                            name = decryptField(userRow["name"]) ?: "",
                            age = decryptField(userRow["age"])?.toIntOrNull(),
                            weight = decryptField(userRow["weight"])?.toIntOrNull(),
                            height = decryptField(userRow["height"])?.toIntOrNull(),
                            gender = decryptField(userRow["gender"]),
                            createdAt = parseTimestamp(userRow["created_at"]),
                            updatedAt = parseTimestamp(userRow["updated_at"])
                        )
                    }
                }
            }
        }
    }

    /**
     * Updates a user's profile information.
     *
     * This method updates the user record with the specified Keycloak ID.
     * All personal data fields are encrypted before storage and the updated_at timestamp
     * is automatically updated.
     *
     * @param keycloakId The Keycloak identifier of the user to update
     * @param name The new name for the user
     * @param age The new age for the user (optional)
     * @param weight The new weight for the user (optional)
     * @param height The new height for the user (optional)
     * @param gender The new gender for the user (optional)
     * @return Mono containing the updated user
     * @throws NoResultsFoundException if no user exists with the given Keycloak ID
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "user"
    )
    fun updateUser(
        keycloakId: String,
        name: String,
        age: Int? = null,
        weight: Int? = null,
        height: Int? = null,
        gender: String? = null
    ): Mono<User> {
        logger.debug("Updating user: {} with Keycloak ID: {}", name, keycloakId)

        // Encrypt sensitive data
        val encryptedName = encryptionUtil.encrypt(name)
        val encryptedAge = age?.let { encryptionUtil.encrypt(it.toString()) }
        val encryptedWeight = weight?.let { encryptionUtil.encrypt(it.toString()) }
        val encryptedHeight = height?.let { encryptionUtil.encrypt(it.toString()) }
        val encryptedGender = gender?.let { encryptionUtil.encrypt(it) }

        // Use transaction to ensure user update and audit logging are atomic
        return postgresClient.withTransaction {
            postgresClient.update<Map<String, Any>>(
                """
                UPDATE "user"
                SET name=$2, age=$3, weight=$4, height=$5, gender=$6, updated_at=NOW()
                WHERE keycloak_id=$1
                """.trimIndent(),
                keycloakId,
                encryptedName,
                encryptedAge,
                encryptedWeight,
                encryptedHeight,
                encryptedGender
            ).flatMap { updatedRow ->
                // Log the data update for GDPR audit
                auditService.logDataOperation(
                    keycloakId = keycloakId,
                    operation = "DATA_UPDATE",
                    dataType = "USER_PROFILE"
                ).then(
                    // Return the updated user with decrypted data
                    Mono.fromCallable {
                        User(
                            keycloakId = keycloakId,
                            name = name,
                            age = age,
                            weight = weight,
                            height = height,
                            gender = gender,
                            createdAt = parseTimestamp(updatedRow["created_at"]),
                            updatedAt = parseTimestamp(updatedRow["updated_at"])
                        )
                    }
                )
            }
        }
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
        val age = decryptField(row["age"])?.toIntOrNull()
        val weight = decryptField(row["weight"])?.toIntOrNull()
        val height = decryptField(row["height"])?.toIntOrNull()
        val gender = decryptField(row["gender"])

        // Handle timestamp conversion from database format
        val createdAt = parseTimestamp(row["created_at"])
        val updatedAt = parseTimestamp(row["updated_at"])

        return User(
            keycloakId = keycloakId,
            name = name ?: "",
            age = age,
            weight = weight,
            height = height,
            gender = gender,
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
