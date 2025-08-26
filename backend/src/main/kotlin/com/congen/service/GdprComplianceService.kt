package com.congen.service

import com.congen.dal.GdprComplianceDAL
import com.congen.dal.ProgramDAL
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserProgramPreferencesDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.model.Program
import com.congen.model.ProgramWithWorkouts
import com.congen.model.ProgrammedExercise
import com.congen.model.ProgrammedExerciseWithSetSchemes
import com.congen.model.ProgrammedWorkout
import com.congen.model.ProgrammedWorkoutWithStages
import com.congen.model.SetScheme
import com.congen.model.UserConsent
import com.congen.model.UserDataExport
import com.congen.model.WorkoutStage
import com.congen.model.WorkoutStageWithExercises
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
 * @param userProgramPreferencesDAL Data access layer for program preferences
 * @param userOneRepMaxDAL Data access layer for one-rep-max records
 * @param userWeightUnitPreferenceDAL Data access layer for weight unit preferences
 * @param programDAL Data access layer for training programs
 * @param programmedWorkoutDAL Data access layer for programmed workouts
 * @param workoutStageDAL Data access layer for workout stages
 * @param programmedExerciseDAL Data access layer for programmed exercises
 * @param setSchemeDAL Data access layer for set schemes
 * @param auditService Service for audit logging
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
    private val userProgramPreferencesDAL: UserProgramPreferencesDAL,
    private val userOneRepMaxDAL: UserOneRepMaxDAL,
    private val userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL,
    private val programDAL: ProgramDAL,
    private val programmedWorkoutDAL: ProgrammedWorkoutDAL,
    private val workoutStageDAL: WorkoutStageDAL,
    private val programmedExerciseDAL: ProgrammedExerciseDAL,
    private val setSchemeDAL: SetSchemeDAL,
    private val auditService: AuditService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(GdprComplianceService::class.java)
    }

    /**
     * Helper function to fetch set schemes for an exercise.
     */
    private fun fetchSetSchemesForExercise(exercise: ProgrammedExercise): Mono<List<SetScheme>> {
        return setSchemeDAL.selectSetSchemesByProgrammedExerciseId(exercise.id)
    }

    /**
     * Helper function to fetch exercises for a stage.
     */
    private fun fetchExercisesForStage(stage: WorkoutStage): Mono<List<ProgrammedExerciseWithSetSchemes>> {
        return programmedExerciseDAL.selectProgrammedExercisesByWorkoutStageId(stage.id)
            .flatMap { exercises ->
                Flux.fromIterable(exercises)
                    .flatMap { exercise ->
                        fetchSetSchemesForExercise(exercise)
                            .map { setSchemes ->
                                ProgrammedExerciseWithSetSchemes(
                                    exercise = exercise,
                                    setSchemes = setSchemes
                                )
                            }
                    }
                    .collectList()
            }
    }

    /**
     * Helper function to fetch stages for a workout.
     */
    private fun fetchStagesForWorkout(workout: ProgrammedWorkout): Mono<List<WorkoutStageWithExercises>> {
        return workoutStageDAL.selectWorkoutStagesByProgrammedWorkoutId(workout.id)
            .flatMap { stages ->
                Flux.fromIterable(stages)
                    .flatMap { stage ->
                        fetchExercisesForStage(stage)
                            .map { exercises ->
                                WorkoutStageWithExercises(
                                    stage = stage,
                                    exercises = exercises
                                )
                            }
                    }
                    .collectList()
            }
    }

    /**
     * Helper function to fetch workouts for a program.
     */
    private fun fetchWorkoutsForProgram(program: Program): Mono<List<ProgrammedWorkoutWithStages>> {
        return programmedWorkoutDAL.selectProgrammedWorkoutsByProgramId(program.id)
            .flatMap { workouts ->
                Flux.fromIterable(workouts)
                    .flatMap { workout ->
                        fetchStagesForWorkout(workout)
                            .map { stages ->
                                ProgrammedWorkoutWithStages(
                                    workout = workout,
                                    stages = stages
                                )
                            }
                    }
                    .collectList()
            }
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
     * - User profile information (name, timestamps)
     * - Consent status and history
     * - Equipment preferences
     * - Exercise preferences (avoid/include)
     * - Program preferences (days per week, session length)
     * - One-rep-max records
     * - Weight unit preferences
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
            val userProgramPreferencesMono = userProgramPreferencesDAL.selectUserProgramPreferences(keycloakId)
            val userOneRepMaxMono = userOneRepMaxDAL.selectUserOneRepMaxByUser(keycloakId)
            val userWeightUnitPreferencesMono = userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(keycloakId)
            val programsMono = programDAL.selectProgramsByUserId(keycloakId)
            val auditLogsMono = gdprComplianceDAL.getUserAuditLogs(keycloakId)
            val dataRetentionPoliciesMono = gdprComplianceDAL.getDataRetentionPolicies()

            // Combine data in smaller groups to avoid type inference issues
            Mono.zip(userConsentMono, userEquipmentMono, userExercisePreferencesMono)
                .flatMap { tuple ->
                    val consent =
                        tuple.t1 ?: UserConsent(
                            keycloakId = keycloakId,
                            dataProcessingConsent = false,
                            consentTimestamp = null,
                            createdAt = Instant.now(),
                            updatedAt = Instant.now()
                        )
                    val equipment = tuple.t2 ?: emptyList()
                    val exercisePreferences = tuple.t3 ?: emptyList()

                    Mono.zip(userProgramPreferencesMono, userOneRepMaxMono, userWeightUnitPreferencesMono)
                        .flatMap { tuple2 ->
                            val programPreferences = tuple2.t1
                            val oneRepMax = tuple2.t2 ?: emptyList()
                            val weightUnitPreferences = tuple2.t3 ?: emptyList()

                            Mono.zip(programsMono, auditLogsMono, dataRetentionPoliciesMono)
                                .flatMap { tuple3 ->
                                    val programs = tuple3.t1 ?: emptyList()
                                    val auditLogs = tuple3.t2 ?: emptyList()
                                    val retentionPolicies = tuple3.t3 ?: emptyList()

                                    // Fetch complete training programs with workouts, stages, exercises, and set schemes
                                    Flux.fromIterable(programs)
                                        .flatMap { program ->
                                            fetchWorkoutsForProgram(program)
                                                .map { workouts ->
                                                    ProgramWithWorkouts(
                                                        program = program,
                                                        workouts = workouts
                                                    )
                                                }
                                        }
                                        .collectList()
                                        .map { programsWithWorkouts ->
                                            UserDataExport(
                                                keycloakId = user.keycloakId,
                                                name = user.name,
                                                createdAt = user.createdAt,
                                                updatedAt = user.updatedAt,
                                                dataProcessingConsent = consent.dataProcessingConsent,
                                                consentTimestamp = consent.consentTimestamp,
                                                userEquipment = equipment,
                                                userExercisePreferences = exercisePreferences,
                                                userProgramPreferences = programPreferences,
                                                userOneRepMax = oneRepMax,
                                                userWeightUnitPreferences = weightUnitPreferences,
                                                trainingPrograms = programsWithWorkouts,
                                                auditLogs = auditLogs,
                                                dataRetentionPolicies = retentionPolicies,
                                                exportTimestamp = Instant.now()
                                            )
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
     * - Training programs and workouts
     * - Performance data and set schemes
     * - Consent records
     * - Audit logs
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
            // Delete the user which will cascade delete related data
            userDAL.deleteUserByKeycloakId(keycloakId)
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
