package com.congen.service

import com.congen.client.KeycloakClient
import com.congen.client.PostgresClient
import com.congen.dal.GdprComplianceDAL
import com.congen.dal.ProgramDAL
import com.congen.dal.ProgramPreferencesDAL
import com.congen.dal.UserDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserPerformanceMetricsDAL
import com.congen.dal.UserPerformanceScoresDAL
import com.congen.dal.UserTestResultDAL
import com.congen.dal.UserWeakMuscleDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.model.ProgramWithWorkouts
import com.congen.model.UserConsent
import com.congen.model.UserDataExport
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Service for managing GDPR compliance operations.
 *
 * This service provides comprehensive GDPR compliance functionality including
 * consent management, data export for portability, and data deletion for the
 * right to be forgotten. All operations are logged for audit purposes.
 *
 * ## GDPR Rights Implemented
 *
 * - **Right to Information (Articles 13-14)**: Privacy policy access
 * - **Right of Access (Article 15)**: Data export and consent status
 * - **Right to Rectification (Article 16)**: Consent updates
 * - **Right to Erasure (Article 17)**: Complete data deletion
 * - **Right to Data Portability (Article 20)**: Structured data export
 *
 * ## Data Export Includes
 *
 * - User profile information
 * - Consent status and history
 * - Equipment preferences
 * - Exercise preferences
 * - Program preferences
 * - One-rep-max records
 * - Weight unit preferences
 * - Complete training programs with workouts
 * - Performance data and set schemes
 * - Audit logs for data access
 * - Data retention policies
 *
 * @param gdprComplianceDAL Data access layer for GDPR operations
 * @param userDAL Data access layer for user operations
 * @param userEquipmentDAL Data access layer for user equipment
 * @param userExercisePreferenceDAL Data access layer for exercise preferences
 * @param programPreferencesDAL Data access layer for program preferences
 * @param userOneRepMaxService Service for user one rep max operations
 * @param userWeightUnitPreferenceDAL Data access layer for weight unit preferences
 * @param userPerformanceMetricsDAL Data access layer for performance metrics
 * @param userPerformanceScoresDAL Data access layer for performance scores
 * @param userTestResultDAL Data access layer for individual test results
 * @param userWeakMuscleDAL Data access layer for weak muscle preferences
 * @param programDAL Data access layer for training programs
 * @param auditService Service for audit logging
 * @param keycloakClient Client for Keycloak operations
 * @param postgresClient Client for PostgreSQL operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class GdprComplianceService(
    private val gdprComplianceDAL: GdprComplianceDAL,
    private val userDAL: UserDAL,
    private val userEquipmentDAL: UserEquipmentDAL,
    private val userExercisePreferenceDAL: UserExercisePreferenceDAL,
    private val programPreferencesDAL: ProgramPreferencesDAL,
    private val userOneRepMaxService: UserOneRepMaxService,
    private val userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
    private val userPerformanceMetricsDAL: UserPerformanceMetricsDAL,
    private val userPerformanceScoresDAL: UserPerformanceScoresDAL,
    private val userTestResultDAL: UserTestResultDAL,
    private val userWeakMuscleDAL: UserWeakMuscleDAL,
    private val programDAL: ProgramDAL,
    private val auditService: AuditService,
    private val keycloakClient: KeycloakClient,
    private val postgresClient: PostgresClient
) {
    companion object {
        private val logger = LoggerFactory.getLogger(GdprComplianceService::class.java)
    }

    /**
     * Records user consent for data processing (GDPR Article 7).
     *
     * This method records whether a user has given or withdrawn consent for data processing.
     * The consent status is stored with a timestamp and logged for audit purposes.
     * This operation is required for GDPR compliance and data protection regulations.
     *
     * @param keycloakId The user's Keycloak ID
     * @param consent Whether the user has given consent
     * @return Mono containing the updated consent record
     */
    fun updateUserConsent(
        keycloakId: String,
        consent: Boolean
    ): Mono<UserConsent> {
        logger.info("Recording consent for user: {} - consent: {}", keycloakId, consent)

        return auditService.logDataOperation(
            keycloakId = keycloakId,
            operation = if (consent) "CONSENT_GIVEN" else "CONSENT_WITHDRAWN",
            dataType = "USER_CONSENT"
        ).then(
            gdprComplianceDAL.updateUserConsent(keycloakId, consent)
        ).doOnSuccess { userConsentRecord ->
            logger.info(
                "Successfully recorded consent for user: {} - consent: {}",
                userConsentRecord.keycloakId,
                userConsentRecord.dataProcessingConsent
            )
        }.doOnError { error ->
            logger.error("Failed to record consent for user: {}", keycloakId, error)
        }
    }

    /**
     * Exports all personal data for a user (GDPR Article 20 - Right to Data Portability).
     *
     * This method retrieves all personal data associated with a user and
     * returns it in a structured format for export. The operation is logged
     * for audit purposes.
     *
     * ## Data Exported
     * - User profile information (name, age, weight, height, timestamps)
     * - Consent status and history
     * - Equipment preferences
     * - Exercise preferences (avoid/include)
     * - Program preferences (days per week, session length)
     * - One-rep-max records
     * - Weight unit preferences
     * - Daily performance metrics (VO₂ max, strain, recovery, HRV, sleep data)
     * - Performance scores (HP/MP/Fatigue, athleticism level, skills)
     * - Weekly test protocol results
     * - Individual test results
     * - Weak muscle group preferences
     * - Complete training programs with workouts
     * - Performance data and set schemes
     * - Audit logs for data access history
     * - Data retention policies
     *
     * @param keycloakId The user's Keycloak ID
     * @return Mono containing the user's exportable data
     */
    fun exportUserData(keycloakId: String): Mono<UserDataExport> {
        logger.info("Exporting data for user: {}", keycloakId)

        return auditService.logDataOperation(
            keycloakId = keycloakId,
            operation = "DATA_EXPORT",
            dataType = "ALL_USER_DATA"
        ).then(
            userDAL.selectUserByKeycloakId(keycloakId)
        ).flatMap { user ->
            // Get all user-related data in parallel using smaller Mono.zip calls
            val userConsentMono = gdprComplianceDAL.getUserConsent(keycloakId)
            val userEquipmentMono = userEquipmentDAL.selectUserEquipmentByUser(keycloakId)
            val userExercisePreferencesMono = userExercisePreferenceDAL.selectUserExercisePreferencesByUser(keycloakId)
            val userOneRepMaxMono = userOneRepMaxService.selectUserOneRepMaxByUser(keycloakId)
            val userWeightUnitPreferencesMono = userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(keycloakId)
            val userPerformanceMetricsMono =
                userPerformanceMetricsDAL.getUserPerformanceMetricsInRange(
                    keycloakId,
                    Instant.EPOCH,
                    Instant.now()
                )
            val userPerformanceScoresMono = userPerformanceScoresDAL.selectUserPerformanceScoresInRange(keycloakId, null, null)
            val userTestResultsMono = userTestResultDAL.getUserTestResultsInRange(keycloakId, null, null)
            val userWeakMusclesMono = userWeakMuscleDAL.selectUserWeakMusclesByUser(keycloakId)
            val auditLogsMono = gdprComplianceDAL.getUserAuditLogs(keycloakId)
            val dataRetentionPoliciesMono = gdprComplianceDAL.getDataRetentionPolicies()

            // Combine data in smaller groups to avoid type inference issues
            Mono.zip(userConsentMono, userEquipmentMono, userExercisePreferencesMono)
                .flatMap { tuple ->
                    val consent = tuple.t1
                    val equipment = tuple.t2 ?: emptyList()
                    val exercisePreferences = tuple.t3 ?: emptyList()

                    Mono.zip(userOneRepMaxMono, userWeightUnitPreferencesMono)
                        .flatMap { tuple2 ->
                            val oneRepMax = tuple2.t1 ?: emptyList()
                            val weightUnitPreferences = tuple2.t2 ?: emptyList()

                            Mono.zip(userPerformanceMetricsMono, userPerformanceScoresMono)
                                .flatMap { tuple3 ->
                                    val performanceMetrics = tuple3.t1 ?: emptyList()
                                    val performanceScores = tuple3.t2 ?: emptyList()

                                    userTestResultsMono
                                        .flatMap { testResults ->

                                            Mono.zip(userWeakMusclesMono, auditLogsMono)
                                                .flatMap { tuple5 ->
                                                    val weakMuscles = tuple5.t1 ?: emptyList()
                                                    val auditLogs = tuple5.t2 ?: emptyList()

                                                    dataRetentionPoliciesMono
                                                        .flatMap { retentionPolicies ->
                                                            // Use optimized single query to fetch complete training programs with workouts, stages, exercises, and set schemes
                                                            programDAL.selectProgramsWithWorkoutHierarchyByUserId(keycloakId)
                                                                .flatMap { programsWithWorkouts ->
                                                                    // If no programs are found, return empty list instead of throwing error
                                                                    // This ensures GDPR compliance by returning all available data
                                                                    if (programsWithWorkouts.isEmpty()) {
                                                                        Mono.just(emptyList<ProgramWithWorkouts>())
                                                                    } else {
                                                                        // For each program, fetch its preferences and create enriched program data
                                                                        Flux.fromIterable(programsWithWorkouts)
                                                                            .flatMap { programWithWorkouts ->
                                                                                programPreferencesDAL.selectProgramPreferences(
                                                                                    programWithWorkouts.program.id
                                                                                )
                                                                                    .map { preferences ->
                                                                                        ProgramWithWorkouts(
                                                                                            program = programWithWorkouts.program,
                                                                                            programPreferences = preferences,
                                                                                            workouts = programWithWorkouts.workouts
                                                                                        )
                                                                                    }
                                                                            }
                                                                            .collectList()
                                                                    }
                                                                }
                                                                .map { enrichedPrograms ->
                                                                    UserDataExport(
                                                                        keycloakId = user.keycloakId,
                                                                        name = user.name,
                                                                        age = user.age,
                                                                        weight = user.weight,
                                                                        height = user.height,
                                                                        gender = user.gender,
                                                                        createdAt = user.createdAt,
                                                                        updatedAt = user.updatedAt,
                                                                        dataProcessingConsent = consent.dataProcessingConsent,
                                                                        consentTimestamp = consent.consentTimestamp,
                                                                        userEquipment = equipment,
                                                                        userExercisePreferences = exercisePreferences,
                                                                        userOneRepMax = oneRepMax,
                                                                        userWeightUnitPreferences = weightUnitPreferences,
                                                                        userPerformanceMetrics = performanceMetrics,
                                                                        userPerformanceScores = performanceScores,
                                                                        userTestResults = testResults,
                                                                        userWeakMuscles = weakMuscles,
                                                                        trainingPrograms = enrichedPrograms,
                                                                        auditLogs = auditLogs,
                                                                        dataRetentionPolicies = retentionPolicies ?: emptyList(),
                                                                        exportTimestamp = Instant.now()
                                                                    )
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
    }

    /**
     * Deletes all personal data for a user (GDPR Article 17 - Right to Erasure).
     *
     * This method permanently deletes all personal data associated with a user.
     * The deletion is irreversible and should only be performed after proper
     * verification of the user's identity and intent.
     *
     * ## Data Deleted
     * - User profile information
     * - Exercise preferences
     * - Equipment preferences
     * - One-rep-max records
     * - Program preferences
     * - Weight unit preferences
     * - Daily performance metrics
     * - Performance scores (historical)
     * - Weekly test protocol results
     * - Individual test results
     * - Weak muscle group preferences
     * - Training programs and workouts
     * - Performance data and set schemes
     * - Consent records
     * - Audit logs
     * - Keycloak user account
     *
     * @param keycloakId The user's Keycloak ID
     * @return Mono that completes when deletion is finished
     */
    fun deleteAllPersonalData(keycloakId: String): Mono<Void> {
        logger.warn("Starting complete data deletion for user: {}", keycloakId)

        return auditService.logDataOperation(
            keycloakId = keycloakId,
            operation = "DATA_DELETION_STARTED",
            dataType = "ALL_USER_DATA"
        ).then(
            // Use transaction to ensure all database operations are atomic
            postgresClient.withTransaction {
                // Delete the user which will cascade delete related data
                userDAL.deleteUserByKeycloakId(keycloakId)
            }
        ).then(
            // Delete the user from Keycloak to complete the GDPR deletion
            keycloakClient.deleteUser(keycloakId)
                .doOnSuccess {
                    logger.info("Successfully deleted Keycloak user: {}", keycloakId)
                }
                .doOnError { error ->
                    logger.error("Failed to delete Keycloak user: {}", keycloakId, error)
                    // Log the error but don't fail the entire operation
                    auditService.logDataOperation(
                        keycloakId = keycloakId,
                        operation = "KEYCLOAK_DELETION_FAILED",
                        dataType = "KEYCLOAK_USER",
                        additionalInfo = "Error: ${error.message}"
                    ).subscribe()
                }
        ).doOnSuccess {
            logger.info("Successfully deleted all data for user: {}", keycloakId)
        }.doOnError { error ->
            logger.error("Failed to delete data for user: {}", keycloakId, error)
            auditService.logDataOperation(
                keycloakId = keycloakId,
                operation = "DATA_DELETION_FAILED",
                dataType = "ALL_USER_DATA",
                additionalInfo = "Error: ${error.message}"
            ).subscribe()
        }
    }

    /**
     * Checks if a user has given consent for data processing.
     *
     * @param keycloakId The user's Keycloak ID
     * @return Mono containing true if user has given consent, false otherwise
     */
    fun hasUserConsent(keycloakId: String): Mono<Boolean> {
        return gdprComplianceDAL.getUserConsent(keycloakId)
            .map { it.dataProcessingConsent }
            .onErrorReturn(false)
            .defaultIfEmpty(false)
    }

    /**
     * Gets the user's consent record.
     *
     * @param keycloakId The user's Keycloak ID
     * @return Mono containing the user's consent record
     */
    fun getUserConsent(keycloakId: String): Mono<UserConsent> {
        return gdprComplianceDAL.getUserConsent(keycloakId)
    }

    /**
     * Executes a function only if the user has given consent.
     *
     * This method provides a convenient way to ensure operations are only
     * performed when the user has given consent for data processing.
     *
     * @param T The type of the operation result
     * @param keycloakId The user's Keycloak ID
     * @param operation The operation to execute if consent is given
     * @return Mono containing the result of the operation, or empty if no consent
     */
    fun <T> withUserConsent(
        keycloakId: String,
        operation: () -> Mono<T>
    ): Mono<T> {
        return hasUserConsent(keycloakId)
            .flatMap { hasConsent ->
                if (hasConsent) {
                    operation()
                } else {
                    Mono.empty()
                }
            }
    }
}
