package com.congen.controllers

import com.congen.dal.UserEquipmentDAL
import com.congen.model.UserEquipment
import com.congen.service.GdprComplianceService
import com.congen.util.KeycloakUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

/**
 * REST controller for UserEquipment entity operations.
 *
 * This controller provides CRUD operations for user-equipment relationships in the Congen API.
 * User equipment represents the equipment that a user has available for their workouts,
 * which influences exercise selection and workout generation.
 *
 * ## UserEquipment Entity
 *
 * A user equipment relationship represents:
 * - Association between a user and available equipment
 * - Equipment name and user ID
 * - Used for workout generation and exercise filtering
 *
 * ## Endpoints
 *
 * - `POST /user_equipment/` - Create a new user-equipment relationship
 * - `GET /user_equipment/{userId}` - Retrieve all equipment for a specific user
 * - `DELETE /user_equipment/` - Delete a user-equipment relationship
 *
 * ## Error Handling
 *
 * - **422 Unprocessable Entity**: When validation fails
 * - **500 Internal Server Error**: When database operations fail
 *
 * @param userEquipmentDAL Data access layer for user equipment operations
 * @param keycloakUtil Utility for Keycloak operations
 * @param gdprComplianceService Service for GDPR compliance operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user_equipment")
class UserEquipmentController(
    private val userEquipmentDAL: UserEquipmentDAL,
    private val keycloakUtil: KeycloakUtil,
    private val gdprComplianceService: GdprComplianceService
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(UserEquipmentController::class.java)
    }

    /**
     * Creates a new user-equipment relationship.
     *
     * This endpoint creates an association between a user and a piece of equipment,
     * indicating that the user has access to that equipment for their workouts.
     *
     * @param userId The Keycloak identifier of the user
     * @param equipmentName The name of the equipment
     * @return Mono containing the created user-equipment relationship
     */
    @PostMapping("/")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create user equipment relationship",
        description = "Creates a new user-equipment relationship.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User-equipment relationship created successfully",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "409",
                description = "Relationship already exists",
                content = [Content(mediaType = "text/plain")],
            ),
            ApiResponse(
                responseCode = "422",
                description = "User or equipment does not exist",
                content = [Content(mediaType = "text/plain")],
            ),
        ],
    )
    fun save(
        @Parameter(description = "Keycloak user ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @RequestParam("user_id") userId: String,
        @Parameter(description = "Equipment name", required = true)
        @RequestParam("equipment_name") equipmentName: String,
    ): Mono<ResponseEntity<UserEquipment>> {
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
                        logger.info("Saving user equipment: {} - {}", userId, equipmentName)
                        userEquipmentDAL.insertUserEquipment(userId, equipmentName)
                            .map { ResponseEntity.ok(it) }
                            .doOnError { e ->
                                logger.error("Error saving user equipment: userId={}, equipmentName={}", userId, equipmentName, e)
                            }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only create equipment for themselves"))
            }
        }
    }

    /**
     * Retrieves all equipment for a specific user.
     *
     * This endpoint fetches all equipment that is associated with the specified user,
     * returning a list of user-equipment relationships.
     *
     * @param userId The Keycloak identifier of the user
     * @return Mono containing a list of user equipment relationships
     */
    @GetMapping("/{user_id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get user equipment by user ID",
        description = "Retrieves all equipment associated with a given user.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User equipment found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getByUser(
        @Parameter(description = "Keycloak user ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @PathVariable("user_id") userId: String,
    ): Mono<ResponseEntity<List<UserEquipment>>> {
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
                        userEquipmentDAL.selectUserEquipmentByUser(userId)
                            .map { equipment ->
                                logger.debug("Found {} equipment items for user: {}", equipment.size, userId)
                                ResponseEntity.ok(equipment)
                            }
                            .doOnError { e ->
                                logger.error("Error getting user equipment for user: {}", userId, e)
                            }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only view their own equipment"))
            }
        }
    }

    /**
     * Deletes a user-equipment relationship.
     *
     * This endpoint removes the association between a user and a piece of equipment,
     * indicating that the user no longer has access to that equipment.
     *
     * @param userId The Keycloak identifier of the user
     * @param equipmentName The name of the equipment
     * @return Mono containing the deleted user-equipment relationship
     */
    @DeleteMapping("/")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Delete user equipment relationship",
        description = "Deletes a user-equipment relationship.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User-equipment relationship deleted successfully",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "409",
                description = "Relationship already exists",
                content = [Content(mediaType = "text/plain")],
            ),
            ApiResponse(
                responseCode = "422",
                description = "User or equipment does not exist",
                content = [Content(mediaType = "text/plain")],
            ),
        ],
    )
    fun delete(
        @Parameter(description = "Keycloak user ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @RequestParam("user_id") userId: String,
        @Parameter(description = "Equipment name", required = true)
        @RequestParam("equipment_name") equipmentName: String,
    ): Mono<ResponseEntity<UserEquipment>> {
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
                        logger.info("Deleting user equipment: {} - {}", userId, equipmentName)
                        userEquipmentDAL.deleteUserEquipment(userId, equipmentName)
                            .map { ResponseEntity.ok(it) }
                            .doOnError { e ->
                                logger.error("Error deleting user equipment: {} - {}", userId, equipmentName, e)
                            }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only delete their own equipment"))
            }
        }
    }

    /**
     * Creates multiple user-equipment relationships from a list.
     *
     * This endpoint creates multiple equipment associations for a user in a single request,
     * which is useful for bulk operations when setting up a user's available equipment.
     *
     * @param userId The Keycloak identifier of the user
     * @param equipmentNames List of equipment names to associate with the user
     * @return Mono containing a list of created user-equipment relationships
     */
    @PostMapping("/bulk")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create multiple user equipment relationships",
        description = "Creates multiple user-equipment relationships in a single request.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User-equipment relationships created successfully",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "409",
                description = "Relationship already exists",
                content = [Content(mediaType = "text/plain")],
            ),
            ApiResponse(
                responseCode = "422",
                description = "User or equipment does not exist",
                content = [Content(mediaType = "text/plain")],
            ),
        ],
    )
    fun saveBulk(
        @Parameter(description = "Keycloak user ID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
        @RequestParam("user_id") userId: String,
        @Parameter(description = "Equipment names", required = true)
        @RequestParam("equipment_names") equipmentNames: List<String>,
    ): Mono<ResponseEntity<List<UserEquipment>>> {
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
                        logger.info("Saving bulk user equipment: {} - {}", userId, equipmentNames)
                        Flux.fromIterable(equipmentNames)
                            .flatMap { equipmentName ->
                                userEquipmentDAL.insertUserEquipment(userId, equipmentName)
                            }
                            .collectList()
                            .map { ResponseEntity.ok(it) }
                            .doOnError { e ->
                                logger.error("Error saving bulk user equipment: {} - {}", userId, equipmentNames, e)
                            }
                    }
                }
            } else {
                Mono.error(AccessDeniedException("Access denied: User can only create equipment for themselves"))
            }
        }
    }
}
