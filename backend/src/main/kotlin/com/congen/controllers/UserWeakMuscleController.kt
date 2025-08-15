package com.congen.controllers

import com.congen.dal.UserWeakMuscleDAL
import com.congen.model.UserWeakMuscle
import com.congen.service.GdprComplianceService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for UserWeakMuscle entity operations.
 *
 * Provides endpoints to manage user weak muscle groups for targeted accessory selection.
 *
 * @property userWeakMuscleDAL Data access layer for user weak muscle operations
 */
@RestController
@RequestMapping("/user_weak_muscle")
@Tag(
    name = "User Weak Muscle Management",
    description = "Operations for managing user weak muscle groups",
)
class UserWeakMuscleController(
    private val userWeakMuscleDAL: UserWeakMuscleDAL,
    private val gdprComplianceService: GdprComplianceService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserWeakMuscleController::class.java)
    }

    /**
     * Adds a weak muscle for a user.
     *
     * @param userId The Keycloak identifier of the user
     * @param muscleName The name of the weak muscle group
     * @return ResponseEntity containing the created UserWeakMuscle
     */
    @PostMapping("/")
    @PreAuthorize("hasRole('admin') or hasRole('service') or #userId == principal.subject")
    @Operation(summary = "Add user weak muscle", description = "Adds a weak muscle group for a user.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User weak muscle added successfully",
                content = [Content(mediaType = "application/json")]
            ),
        ],
    )
    fun add(
        @Parameter(description = "User ID", required = true, example = "b226d772-c063-4974-ae08-ab64134abbcf")
        @RequestParam("user_id") userId: String,
        @Parameter(description = "Muscle name", required = true)
        @RequestParam("muscle_name") muscleName: String,
    ): Mono<ResponseEntity<UserWeakMuscle>> {
        logger.info("Adding user weak muscle: {} - {}", userId, muscleName)
        return gdprComplianceService.withUserConsent(userId) {
            userWeakMuscleDAL.insertUserWeakMuscle(userId, muscleName)
                .map { ResponseEntity.ok(it) }
                .doOnError { e ->
                    logger.error("Error adding user weak muscle: {} - {}", userId, muscleName, e)
                }
        }
    }

    /**
     * Retrieves all weak muscles for a user.
     *
     * @param userId The Keycloak identifier of the user
     * @return Mono containing a list of UserWeakMuscle
     */
    @GetMapping("/{user_id}")
    @PreAuthorize("hasRole('admin') or hasRole('service') or #userId == principal.subject")
    @Operation(summary = "Get user weak muscles", description = "Retrieves all weak muscle groups for a user.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User weak muscles found", content = [Content(mediaType = "application/json")]),
        ],
    )
    fun getByUser(
        @Parameter(description = "User ID", required = true, example = "b226d772-c063-4974-ae08-ab64134abbcf")
        @PathVariable("user_id") userId: String,
    ): Mono<ResponseEntity<List<UserWeakMuscle>>> {
        return gdprComplianceService.withUserConsent(userId) {
            userWeakMuscleDAL.selectUserWeakMusclesByUser(userId)
                .map { ResponseEntity.ok(it) }
        }
    }

    /**
     * Deletes a weak muscle for a user.
     *
     * @param userId The Keycloak identifier of the user
     * @param muscleName The name of the weak muscle group
     * @return ResponseEntity containing the deleted UserWeakMuscle
     */
    @DeleteMapping("/")
    @PreAuthorize("hasRole('admin') or hasRole('service') or #userId == principal.subject")
    @Operation(summary = "Delete user weak muscle", description = "Deletes a weak muscle group for a user.")
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User weak muscle deleted successfully",
                content = [Content(mediaType = "application/json")]
            ),
        ],
    )
    fun delete(
        @Parameter(description = "User ID", required = true, example = "b226d772-c063-4974-ae08-ab64134abbcf")
        @RequestParam("user_id") userId: String,
        @Parameter(description = "Muscle name", required = true)
        @RequestParam("muscle_name") muscleName: String,
    ): Mono<ResponseEntity<UserWeakMuscle>> {
        logger.info("Deleting user weak muscle: {} - {}", userId, muscleName)
        return gdprComplianceService.withUserConsent(userId) {
            userWeakMuscleDAL.deleteUserWeakMuscle(userId, muscleName)
                .map { ResponseEntity.ok(it) }
                .doOnError { e ->
                    logger.error("Error deleting user weak muscle: {} - {}", userId, muscleName, e)
                }
        }
    }
}
