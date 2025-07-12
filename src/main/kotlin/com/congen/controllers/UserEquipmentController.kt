package com.congen.controllers

import com.congen.dal.UserEquipmentDAL
import com.congen.exceptions.DatabaseQueryException
import com.congen.model.UserEquipment
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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
 * @property userEquipmentDAL Data access layer for user equipment operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user_equipment")
class UserEquipmentController(
    private val userEquipmentDAL: UserEquipmentDAL,
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
     * @param userId The unique identifier of the user
     * @param equipmentName The name of the equipment
     * @return ResponseEntity containing the created user-equipment relationship
     */
    @PostMapping("/")
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
        ],
    )
    fun save(
        @Parameter(description = "User ID", required = true)
        @RequestParam userId: Int,
        @Parameter(description = "Equipment name", required = true)
        @RequestParam equipmentName: String,
    ): ResponseEntity<*> {
        logger.info("Saving user equipment: {} - {}", userId, equipmentName)
        return try {
            ResponseEntity.ok(
                userEquipmentDAL.insertUserEquipment(userId, equipmentName),
            )
        } catch (e: DatabaseQueryException) {
            val msg = e.cause?.message ?: e.message ?: "Database error"
            return when {
                msg.contains(
                    "duplicate key",
                    ignoreCase = true
                ) -> ResponseEntity.status(HttpStatus.CONFLICT).body("Relationship already exists")
                msg.contains(
                    "violates foreign key",
                    ignoreCase = true
                ) -> ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body("User or equipment does not exist")
                else -> throw e
            }
        }
    }

    /**
     * Retrieves all equipment for a specific user.
     *
     * This endpoint fetches all equipment that is associated with the specified user,
     * returning a list of user-equipment relationships.
     *
     * @param userId The unique identifier of the user
     * @return Mono containing a list of user equipment relationships
     */
    @GetMapping("/{userId}")
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
        @Parameter(description = "User ID", required = true)
        @PathVariable("userId") userId: Int,
    ): Mono<ResponseEntity<List<UserEquipment>>> {
        return userEquipmentDAL.selectUserEquipmentByUser(userId)
            .map {
                logger.debug("Found equipment for user: {}", userId)
                ResponseEntity.ok(it)
            }
            .doOnError { e ->
                logger.error("Error getting user equipment for user: {}", userId, e)
            }
    }

    /**
     * Deletes a user-equipment relationship.
     *
     * This endpoint removes the association between a user and a piece of equipment,
     * indicating that the user no longer has access to that equipment.
     *
     * @param userEquipment The user-equipment relationship to delete
     * @return ResponseEntity containing the deleted user-equipment relationship
     */
    @DeleteMapping("/")
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
        ],
    )
    fun delete(
        @Parameter(description = "User-equipment relationship to delete", required = true)
        @RequestBody userEquipment: UserEquipment,
    ): ResponseEntity<*> {
        logger.info("Deleting user equipment: {} - {}", userEquipment.userId, userEquipment.equipmentName)
        return ResponseEntity.ok(
            userEquipmentDAL.deleteUserEquipment(userEquipment.userId, userEquipment.equipmentName),
        )
    }
}
