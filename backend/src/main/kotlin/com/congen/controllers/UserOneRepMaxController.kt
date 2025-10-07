package com.congen.controllers

import com.congen.model.UserOneRepMax
import com.congen.service.GdprComplianceService
import com.congen.service.UserOneRepMaxService
import com.congen.util.KeycloakUtil
import com.congen.util.ValidationUtil
import io.swagger.v3.oas.annotations.Parameter
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * REST controller for UserOneRepMax entity operations.
 *
 * This controller provides CRUD operations for user one rep max values in the Congen API.
 * User one rep max values allow users to track their maximum weight for different exercises,
 * which is used for workout generation and progression calculations.
 *
 * ## UserOneRepMax Entity
 *
 * A user one rep max represents:
 * - Association between a user and an exercise
 * - User's one rep max weight for the exercise
 * - Timestamp of when the 1RM was last updated
 * - Used for workout generation and progression calculations
 *
 * ## Endpoints
 *
 * - `PUT /user_one_rep_max/` - Create or update a user one rep max (upsert)
 * - `GET /user_one_rep_max/{userId}` - Retrieve all one rep max values for a user
 * - `GET /user_one_rep_max/{userId}/{exerciseName}` - Retrieve a specific one rep max
 * - `DELETE /user_one_rep_max/{userId}/{exerciseName}` - Delete a user one rep max
 *
 * ## Error Handling
 *
 * - **404 Not Found**: When a one rep max with the specified parameters doesn't exist
 * - **422 Unprocessable Entity**: When validation fails
 * - **500 Internal Server Error**: When database operations fail
 *
 * @param userOneRepMaxService Service for user one rep max operations
 * @param validationUtil Utility for validation operations
 * @param keycloakUtil Utility for Keycloak operations
 * @param gdprComplianceService Service for GDPR compliance operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user_one_rep_max")
class UserOneRepMaxController(
    private val userOneRepMaxService: UserOneRepMaxService,
    private val validationUtil: ValidationUtil,
    private val keycloakUtil: KeycloakUtil,
    private val gdprComplianceService: GdprComplianceService
) {
    private val logger = LoggerFactory.getLogger(UserOneRepMaxController::class.java)

    /**
     * Get all one rep max records for a user.
     *
     * @param userId The Keycloak user ID
     * @param unit Optional unit to convert all weights to (kg or lbs). If null, uses user preferences.
     * @return Mono containing list of one rep max records
     */
    @GetMapping("/user/{user_id}")
    @PreAuthorize("isAuthenticated()")
    fun getOneRepMaxesByUserId(
        @PathVariable("user_id") userId: String,
        @RequestParam(value = "unit", required = false) unit: String?,
    ): Mono<ResponseEntity<List<UserOneRepMax>>> {
        return keycloakUtil.getCurrentUserId().zipWith(keycloakUtil.getCurrentUserRoles()) { currentUserId, roles ->
            Pair(currentUserId, roles)
        }.flatMap { (currentUserId, roles) ->
            val isAdminOrService = roles.contains("admin") || roles.contains("service")
            if (isAdminOrService || currentUserId == userId) {
                val consentUserIdMono =
                    if (isAdminOrService) {
                        Mono.just(userId)
                    } else {
                        Mono.just(currentUserId)
                    }
                consentUserIdMono.flatMap { ownerId ->
                    gdprComplianceService.withUserConsent(ownerId) {
                        userOneRepMaxService.selectUserOneRepMaxByUser(userId, unit)
                            .map { ResponseEntity.ok(it) }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only view their own one rep maxes"))
            }
        }
    }

    /**
     * Get a specific one rep max record by user and exercise.
     *
     * @param userId The Keycloak user ID
     * @param exerciseName The exercise name
     * @param unit Optional unit to convert the weight to (kg or lbs)
     * @return Mono containing the one rep max record or empty if not found
     */
    @GetMapping("/user/{user_id}/exercise/{exercise_name}")
    @PreAuthorize("isAuthenticated()")
    fun getOneRepMaxByUserAndExercise(
        @PathVariable("user_id") userId: String,
        @PathVariable("exercise_name") exerciseName: String,
        @RequestParam(value = "unit", required = false) unit: String?,
    ): Mono<ResponseEntity<UserOneRepMax>> {
        return keycloakUtil.getCurrentUserId().zipWith(keycloakUtil.getCurrentUserRoles()) { currentUserId, roles ->
            Pair(currentUserId, roles)
        }.flatMap { (currentUserId, roles) ->
            val isAdminOrService = roles.contains("admin") || roles.contains("service")
            if (isAdminOrService || currentUserId == userId) {
                val consentUserIdMono =
                    if (isAdminOrService) {
                        Mono.just(userId)
                    } else {
                        Mono.just(currentUserId)
                    }
                consentUserIdMono.flatMap { ownerId ->
                    gdprComplianceService.withUserConsent(ownerId) {
                        userOneRepMaxService.selectUserOneRepMax(userId, exerciseName, unit)
                            .map { ResponseEntity.ok(it) }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only view their own one rep maxes"))
            }
        }
    }

    /**
     * Create or update a one rep max record.
     *
     * @param userId The Keycloak user ID
     * @param exerciseName The exercise name
     * @param oneRepMax The one rep max weight value
     * @param unit The weight unit (kg or lbs)
     * @return Mono containing the created or updated one rep max record
     */
    @PutMapping("/")
    @PreAuthorize("isAuthenticated()")
    fun upsertOneRepMax(
        @Parameter(description = "User ID", required = true, example = "b226d772-c063-4974-ae08-ab64134abbcf")
        @RequestParam("user_id") userId: String,
        @Parameter(description = "Exercise name", required = true, example = "Bench Press")
        @RequestParam("exercise_name") exerciseName: String,
        @Parameter(description = "One rep max weight", required = true, example = "225.0")
        @RequestParam("one_rep_max") oneRepMax: BigDecimal,
        @Parameter(description = "Weight unit", required = true, example = "KG")
        @RequestParam("unit") unit: String,
    ): Mono<ResponseEntity<UserOneRepMax>> {
        return keycloakUtil.getCurrentUserId().zipWith(keycloakUtil.getCurrentUserRoles()) { currentUserId, roles ->
            Pair(currentUserId, roles)
        }.flatMap { (currentUserId, roles) ->
            val isAdminOrService = roles.contains("admin") || roles.contains("service")
            if (isAdminOrService || currentUserId == userId) {
                val consentUserIdMono =
                    if (isAdminOrService) {
                        Mono.just(userId)
                    } else {
                        Mono.just(currentUserId)
                    }
                consentUserIdMono.flatMap { ownerId ->
                    gdprComplianceService.withUserConsent(ownerId) {
                        userOneRepMaxService.upsertUserOneRepMax(userId, exerciseName, oneRepMax, unit)
                            .map { ResponseEntity.ok(it) }
                            .doOnError { e ->
                                logger.error(
                                    "Error upserting user one rep max: userId={}, exerciseName={}, oneRepMax={}, unit={}",
                                    userId,
                                    exerciseName,
                                    oneRepMax,
                                    unit,
                                    e
                                )
                            }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only update their own one rep maxes"))
            }
        }
    }

    /**
     * Delete a one rep max record.
     *
     * @param userId The Keycloak user ID
     * @param exerciseName The exercise name
     * @return Mono containing confirmation of deletion
     */
    @DeleteMapping("/user/{user_id}/exercise/{exercise_name}")
    @PreAuthorize("isAuthenticated()")
    fun deleteOneRepMax(
        @PathVariable("user_id") userId: String,
        @PathVariable("exercise_name") exerciseName: String
    ): Mono<ResponseEntity<UserOneRepMax>> {
        return keycloakUtil.getCurrentUserId().zipWith(keycloakUtil.getCurrentUserRoles()) { currentUserId, roles ->
            Pair(currentUserId, roles)
        }.flatMap { (currentUserId, roles): Pair<String, Set<String>> ->
            val isAdminOrService = roles.contains("admin") || roles.contains("service")
            if (isAdminOrService || currentUserId == userId) {
                val consentUserIdMono =
                    if (isAdminOrService) {
                        Mono.just(userId)
                    } else {
                        Mono.just(currentUserId)
                    }
                consentUserIdMono.flatMap { ownerId ->
                    gdprComplianceService.withUserConsent(ownerId) {
                        userOneRepMaxService.deleteUserOneRepMax(userId, exerciseName)
                            .map { ResponseEntity.ok(it) }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only delete their own one rep maxes"))
            }
        }
    }
}
