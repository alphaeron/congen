package com.congen.controllers

import com.congen.dal.UserEquipmentDAL
import com.congen.model.UserEquipment
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/user_equipment")
class UserEquipmentController(
    private val userEquipmentDAL: UserEquipmentDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserEquipmentController::class.java)
    }

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
        @Parameter(description = "User-equipment relationship to create", required = true)
        @RequestBody userEquipment: UserEquipment,
    ): ResponseEntity<*> {
        logger.info("Saving user equipment: {} - {}", userEquipment.userId, userEquipment.equipmentName)
        return ResponseEntity.ok(
            userEquipmentDAL.insertUserEquipment(userEquipment),
        )
    }

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
