package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.model.AuditLog
import com.congen.model.DataRetentionPolicy
import com.congen.model.UserConsent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Data Access Layer for GDPR compliance operations.
 *
 * This class provides database operations specifically for GDPR compliance,
 * including consent management, audit logging, and data retention queries.
 * It separates GDPR-specific database logic from general user operations.
 *
 * ## Operations
 *
 * - **Consent Management**: Check and update user consent status
 * - **Audit Operations**: Store and retrieve audit log entries
 * - **Data Retention**: Support for TTL and cleanup operations
 *
 * ## Security & Compliance
 *
 * - All operations are logged for audit purposes
 * - Consent queries are read-only to prevent unauthorized modifications
 * - Error handling ensures operations fail safely
 *
 * @param postgresClient Client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class GdprComplianceDAL(
    private val postgresClient: PostgresClient
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(GdprComplianceDAL::class.java)
    }

    /**
     * Checks if a user has given consent for data processing.
     *
     * This method queries the user table to retrieve the actual consent status
     * for the specified user. Returns false if the user doesn't exist or
     * if the consent field is null/false.
     *
     * @param keycloakId The user's Keycloak ID
     * @return Mono containing the consent status (true if consent given, false otherwise)
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_consent"
    )
    fun hasUserConsent(keycloakId: String): Mono<Boolean> {
        logger.debug("Querying consent status for user: {}", keycloakId)

        return postgresClient.selectIndividual<Map<String, Any>>(
            "SELECT data_processing_consent FROM user_consent WHERE keycloak_id = $1",
            keycloakId
        ).map { row ->
            val consent = row["data_processing_consent"] as? Boolean ?: false
            logger.debug("User {} consent status: {}", keycloakId, consent)
            consent
        }.onErrorResume { error ->
            logger.warn("Failed to query consent status for user {}: {}", keycloakId, error.message)
            // Default to no consent on error
            Mono.just(false)
        }
    }

    /**
     * Retrieves the full user consent record for GDPR compliance.
     *
     * This method returns the complete consent information including timestamps
     * and consent status. If the user doesn't exist, it returns an empty Mono.
     *
     * @param keycloakId The user's Keycloak ID
     * @return Mono containing the user consent record, or empty if user not found
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_consent"
    )
    fun getUserConsent(keycloakId: String): Mono<UserConsent> {
        logger.debug("Retrieving full consent record for user: {}", keycloakId)

        return postgresClient.selectIndividual<UserConsent>(
            """
            SELECT keycloak_id, data_processing_consent, consent_timestamp, created_at, updated_at
            FROM user_consent
            WHERE keycloak_id = $1
            """.trimIndent(),
            keycloakId
        ).onErrorResume { error ->
            logger.debug("No consent record found for user {}, returning default: {}", keycloakId, error.message)
            // Return a default consent record with false consent
            Mono.just(
                UserConsent(
                    keycloakId = keycloakId,
                    dataProcessingConsent = false,
                    consentTimestamp = null,
                    createdAt = Instant.now(),
                    updatedAt = Instant.now()
                )
            )
        }
    }

    /**
     * Updates user consent status for GDPR compliance.
     *
     * This method upserts the consent status and timestamp for a user in the user_consent table.
     * If no consent record exists, it creates one. If one exists, it updates it.
     *
     * @param keycloakId The user's Keycloak ID
     * @param consent Whether consent is given (true) or withdrawn (false)
     * @return Mono containing the updated user consent
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.USER_DATA,
        entityName = "user_consent"
    )
    fun updateUserConsent(
        keycloakId: String,
        consent: Boolean
    ): Mono<UserConsent> {
        logger.debug("Updating consent for user: {} to {}", keycloakId, consent)

        return postgresClient.update(
            """
            INSERT INTO user_consent (keycloak_id, data_processing_consent, consent_timestamp, created_at, updated_at)
            VALUES ($1, $2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
            ON CONFLICT (keycloak_id)
            DO UPDATE SET
                data_processing_consent = EXCLUDED.data_processing_consent,
                consent_timestamp = CURRENT_TIMESTAMP,
                updated_at = CURRENT_TIMESTAMP
            """.trimIndent(),
            keycloakId,
            consent
        )
    }

    /**
     * Retrieves audit logs for a user.
     *
     * @param keycloakId The user's Keycloak ID
     * @return Mono containing a list of audit logs
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "audit_log"
    )
    fun getUserAuditLogs(keycloakId: String): Mono<List<AuditLog>> {
        logger.debug("Retrieving audit logs for user: {}", keycloakId)

        // For now, return empty list as audit logs table may not exist yet
        return Mono.just(emptyList<AuditLog>())
    }

    /**
     * Retrieves data retention policies.
     *
     * @return Mono containing a list of data retention policies
     */
    @Cacheable(
        ttl = CacheTTL.MEDIUM_TERM,
        keyStrategy = CacheKeyStrategy.LIST_QUERY,
        entityName = "data_retention_policy"
    )
    fun getDataRetentionPolicies(): Mono<List<DataRetentionPolicy>> {
        logger.debug("Retrieving data retention policies")

        // For now, return empty list as retention policies table may not exist yet
        return Mono.just(emptyList<DataRetentionPolicy>())
    }
}
