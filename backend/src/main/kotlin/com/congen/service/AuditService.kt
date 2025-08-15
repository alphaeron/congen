package com.congen.service

import com.congen.client.PostgresClient
import com.congen.model.AuditLog
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Service for audit logging to ensure GDPR compliance.
 *
 * This service provides comprehensive audit logging for all data operations
 * to meet GDPR accountability requirements (Article 5.2). All access to
 * personal data, consent changes, and data modifications are logged both
 * to application logs and to the database for persistent audit trails.
 *
 * ## Audit Requirements Under GDPR
 *
 * - **Accountability**: Organizations must demonstrate compliance (Article 5.2)
 * - **Data Access Logging**: Record who accessed what data when
 * - **Consent Tracking**: Log all consent changes with timestamps
 * - **Data Modification**: Track all changes to personal data
 * - **Data Deletion**: Log all data deletion operations
 * - **Retention**: Audit logs must be retained for compliance periods
 *
 * ## Storage
 *
 * - **Database**: Persistent audit trail in `gdpr_audit_log` table
 * - **Application Logs**: Immediate visibility for monitoring
 * - **TTL**: Automatic cleanup based on retention policies
 *
 * ## Log Levels
 *
 * - **INFO**: Normal data access operations
 * - **WARN**: Sensitive operations (data export, consent withdrawal)
 * - **ERROR**: Failed operations, security violations
 *
 * @property postgresClient Client for database audit log storage
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
@ConditionalOnProperty(
    name = ["congen.gdpr.audit-enabled"],
    havingValue = "true",
    matchIfMissing = false
)
class AuditService(
    private val postgresClient: PostgresClient
) {
    companion object {
        private val logger = LoggerFactory.getLogger(AuditService::class.java)
        private const val AUDIT_LOG_PREFIX = "[GDPR_AUDIT]"
    }

    /**
     * Logs a data operation for GDPR audit purposes.
     *
     * This method creates a structured audit log entry with minimal necessary
     * information for GDPR compliance while respecting data minimization principles.
     *
     * @param keycloakId The user's Keycloak ID.  NULL for system operations.
     * @param operation The type of operation performed
     * @param dataType The type of data accessed/modified
     * @param timestamp When the operation occurred
     * @param userId Optional user ID performing the operation (for admin access)
     * @param additionalInfo Optional additional context
     * @return Mono that completes when log is written
     */
    fun logDataOperation(
        keycloakId: String?,
        operation: String,
        dataType: String,
        userId: String? = null,
        additionalInfo: String? = null
    ): Mono<AuditLog> {
        val logMessage =
            buildAuditLogMessage(
                keycloakId = keycloakId,
                operation = operation,
                dataType = dataType,
                userId = userId,
                additionalInfo = additionalInfo
            )

        // Log to application logs for immediate visibility
        when (operation) {
            "DATA_ACCESS" -> logger.info(logMessage)
            "DATA_EXPORT", "CONSENT_WITHDRAWN", "DATA_DELETION_STARTED" -> logger.warn(logMessage)
            "DATA_DELETION_FAILED", "UNAUTHORIZED_ACCESS", "DECRYPTION_FAILED" -> logger.error(logMessage)
            else -> logger.info(logMessage)
        }

        // Store in database for persistent audit trail
        return postgresClient.update(
            """
            INSERT INTO gdpr_audit_log
                (keycloak_id, operation, data_type, performed_by, additional_info)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent(),
            keycloakId,
            operation,
            dataType,
            userId,
            additionalInfo
        )
    }

    /**
     * Logs user data access for audit purposes.
     *
     * Simplified logging that focuses on essential GDPR requirements.
     * Only logs significant data access events, not routine operations.
     *
     * @param keycloakId The user's Keycloak ID
     * @param dataType The type of data accessed
     * @param accessedBy Who accessed the data (user, admin, system)
     * @return Mono that completes when log is written
     */
    fun logDataAccess(
        keycloakId: String,
        dataType: String,
        accessedBy: String
    ): Mono<AuditLog> {
        // Only log if this is admin access or significant data access
        if (accessedBy != keycloakId) {
            return logDataOperation(
                keycloakId = keycloakId,
                operation = "DATA_ACCESS",
                dataType = dataType,
                userId = accessedBy
            )
        }
        // Skip logging for users accessing their own data (routine operation)
        return Mono.empty()
    }

    /**
     * Logs consent changes for GDPR compliance.
     *
     * All consent changes must be logged with timestamps to demonstrate
     * compliance with GDPR consent requirements (Article 7).
     * This is one of the essential GDPR audit requirements.
     *
     * @param keycloakId The user's Keycloak ID
     * @param consentType The type of consent (data_processing, marketing, etc.)
     * @param consentGiven Whether consent was given or withdrawn
     * @return Mono that completes when log is written
     */
    fun logConsentChange(
        keycloakId: String,
        consentType: String,
        consentGiven: Boolean
    ): Mono<AuditLog> {
        val operation = if (consentGiven) "CONSENT_GIVEN" else "CONSENT_WITHDRAWN"
        return logDataOperation(
            keycloakId = keycloakId,
            operation = operation,
            dataType = consentType,
            additionalInfo = "Consent: $consentGiven"
        )
    }

    /**
     * Logs significant data modification operations.
     *
     * Only logs modifications that are significant for GDPR compliance,
     * such as admin changes or bulk modifications. Regular user profile
     * updates by the user themselves are not logged to minimize data.
     *
     * @param keycloakId The user's Keycloak ID
     * @param dataType The type of data modified
     * @param modifiedBy Who modified the data
     * @param changes Description of what was changed
     * @return Mono that completes when log is written
     */
    fun logDataModification(
        keycloakId: String,
        dataType: String,
        modifiedBy: String,
        changes: String
    ): Mono<AuditLog> {
        // Only log if this is admin modification or significant change
        if (modifiedBy != keycloakId) {
            return logDataOperation(
                keycloakId = keycloakId,
                operation = "DATA_MODIFICATION",
                dataType = dataType,
                userId = modifiedBy,
                additionalInfo = changes
            )
        }
        // Skip logging for users modifying their own data (routine operation)
        return Mono.empty()
    }

    /**
     * Logs security violations or unauthorized access attempts.
     *
     * These logs are essential for GDPR breach notification requirements
     * and for detecting unauthorized access to personal data.
     *
     * @param keycloakId The user's Keycloak ID (if known)
     * @param violation Description of the security violation
     * @param severity Severity level of the violation
     * @return Mono that completes when log is written
     */
    fun logSecurityViolation(
        keycloakId: String?,
        violation: String,
        severity: String = "HIGH"
    ): Mono<AuditLog> {
        return logDataOperation(
            keycloakId = keycloakId ?: "UNKNOWN",
            operation = "SECURITY_VIOLATION",
            dataType = "SECURITY",
            additionalInfo = "Severity: $severity - $violation"
        )
    }

    /**
     * Builds a structured audit log message.
     *
     * Creates a standardized log format with minimal necessary information
     * for GDPR compliance while respecting data minimization principles.
     *
     * @param keycloakId The user's Keycloak ID.  NULL for system operations.
     * @param operation The operation performed
     * @param dataType The type of data involved
     * @param timestamp When the operation occurred
     * @param userId User performing the operation
     * @param additionalInfo Additional context
     * @return Formatted audit log message
     */
    private fun buildAuditLogMessage(
        keycloakId: String?,
        operation: String,
        dataType: String,
        userId: String?,
        additionalInfo: String?
    ): String {
        val parts =
            mutableListOf<String>().apply {
                add("$AUDIT_LOG_PREFIX $operation")
                add("user_id=${keycloakId ?: "SYSTEM"}")
                add("data_type=$dataType")
                add("timestamp=${Instant.now()}")
                userId?.let { add("performed_by=$it") }
                additionalInfo?.let { add("details=$it") }
            }

        return parts.joinToString(" | ")
    }
}
