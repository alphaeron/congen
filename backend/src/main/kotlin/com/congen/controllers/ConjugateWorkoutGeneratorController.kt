package com.congen.controllers

import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import com.congen.generator.ConjugateWorkoutGeneratorService
import com.congen.generator.ExercisePoolFactory
import com.congen.model.Program
import com.congen.model.UserExercisePoolResponse
import com.congen.service.GdprComplianceService
import com.congen.service.ProgramService
import com.congen.util.KeycloakUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * Controller for generating conjugate workout programs.
 *
 * This controller provides endpoints for generating conjugate powerlifting workout programs.
 * The conjugate method is a training system that rotates exercises to prevent accommodation
 * and promote continuous strength gains.
 *
 * ## Endpoints
 *
 * - **POST /{programId}/generate**: Generate the next week of workouts for an existing program
 * - **GET /exercise_pool**: Get the user's exercise pool based on preferences and equipment
 *
 * ## Features
 *
 * - **Program-based Generation**: Works with existing programs instead of creating new ones
 * - **Automatic Week Progression**: Automatically determines the next week number from the program
 * - **User Preference Integration**: Incorporates user exercise preferences and equipment
 * - **Exercise Rotation**: Implements exercise rotation to prevent accommodation
 * - **Validation**: Comprehensive validation of program parameters
 *
 * @param conjugateWorkoutGeneratorService Service for generating conjugate workout programs
 * @param exercisePoolFactory Factory for creating user exercise pools
 * @param programService Service for program operations
 * @param keycloakUtil Utility for Keycloak operations
 * @param gdprComplianceService Service for GDPR compliance operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/conjugate_workout_generator")
@Tag(name = "Conjugate Workout Generator", description = "Endpoints for generating conjugate powerlifting workout programs")
class ConjugateWorkoutGeneratorController(
    private val conjugateWorkoutGeneratorService: ConjugateWorkoutGeneratorService,
    private val exercisePoolFactory: ExercisePoolFactory,
    private val programService: ProgramService,
    private val keycloakUtil: KeycloakUtil,
    private val gdprComplianceService: GdprComplianceService
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ConjugateWorkoutGeneratorController::class.java)
    }

    /**
     * Generates the next week of workouts for an existing conjugate powerlifting program.
     *
     * This endpoint generates a complete week of workouts for an existing program based on the conjugate method.
     * The week number is automatically determined from the program's current week number and incremented by 1.
     *
     * @param programId The ID of the existing program to generate workouts for
     * @return ResponseEntity containing the updated program with new workouts
     * @throws NoResultsFoundException if the program is not found
     * @throws ValidationException if the program parameters are invalid
     */
    @PostMapping("/{program_id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Generate next week of conjugate workout program",
        description =
            "Generates the next week of workouts for an existing conjugate powerlifting program. " +
                "The week number is automatically determined from the program's current week number."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Workout program generated successfully",
                content = [Content(mediaType = "application/json")]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Program not found"
            ),
            ApiResponse(
                responseCode = "422",
                description = "Validation error"
            )
        ]
    )
    fun generateNextWeek(
        @Parameter(description = "ID of the program to generate workouts for", required = true)
        @PathVariable("program_id") programId: Long
    ): Mono<ResponseEntity<Program>> {
        return keycloakUtil.getCurrentUserId().zipWith(keycloakUtil.getCurrentUserRoles()) { userId, roles ->
            Pair(userId, roles)
        }.flatMap { (userId, roles) ->
            val isAdminOrService = roles.contains("admin") || roles.contains("service")
            // First check if the program exists
            programService.selectProgramById(programId)
                .flatMap { program ->
                    // Program exists, now check ownership
                    val hasAccess = isAdminOrService || program.userId == userId
                    if (hasAccess) {
                        val consentUserIdMono =
                            if (isAdminOrService) {
                                Mono.just(program.userId)
                            } else {
                                Mono.just(userId)
                            }
                        consentUserIdMono.flatMap { ownerId ->
                            gdprComplianceService.withUserConsent(ownerId) {
                                conjugateWorkoutGeneratorService.generateNextWeek(programId)
                                    .map { updatedProgram -> ResponseEntity.ok(updatedProgram) }
                                    .doOnError(NoResultsFoundException::class.java) { error ->
                                        logger.error("Error generating workout program for program: {}", programId, error)
                                    }
                                    .doOnError(ValidationException::class.java) { error ->
                                        logger.error("Validation error generating workout program for program: {}", programId, error)
                                    }
                                    .doOnError { error ->
                                        logger.error("Unexpected error generating workout program for program: {}", programId, error)
                                    }
                            }
                        }
                    } else {
                        Mono.error(AccessDeniedException("Access denied: User is not the owner of this program"))
                    }
                }
                .doOnError { e ->
                    logger.error("Error generating workout program for program: {}", programId, e)
                }
        }
    }

    /**
     * Gets the user's exercise pool based on their preferences, equipment, and previous usage.
     *
     * This endpoint returns a structured representation of the user's available exercises,
     * including primary exercises, accessory exercises, and exercises that target weak muscles.
     * The pool is filtered based on user preferences, available equipment, and sliding window
     * logic to prevent exercise reuse.
     *
     * @return ResponseEntity containing the user's exercise pool
     */
    @GetMapping("/exercise_pool")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get user's exercise pool",
        description = "Returns the user's available exercise pool based on preferences, equipment, and previous usage"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise pool retrieved successfully",
                content = [Content(mediaType = "application/json")]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized"
            )
        ]
    )
    fun getExercisePool(): Mono<ResponseEntity<UserExercisePoolResponse>> {
        return keycloakUtil.getCurrentUserId().flatMap { userId ->
            gdprComplianceService.withUserConsent(userId) {
                exercisePoolFactory.createPoolForUser(userId)
                    .map { userExercisePool ->
                        // Convert UserExercisePool to UserExercisePoolResponse
                        val availableExercises = userExercisePool.getAvailableExercises()
                        val primaryExercises = availableExercises.filter { !it.isAccessory }
                        val accessoryExercises = availableExercises.filter { it.isAccessory }

                        val response =
                            UserExercisePoolResponse(
                                userId = userId,
                                totalExercises = userExercisePool.getAllExercises().size,
                                availableExercises = availableExercises.size,
                                primaryExercises = primaryExercises,
                                accessoryExercises = accessoryExercises,
                                userEquipment = userExercisePool.getUserEquipment(),
                                userPreferences = userExercisePool.getUserPreferences(),
                                previouslyUsedExercises = userExercisePool.getPreviouslyUsedExercises()
                            )

                        ResponseEntity.ok(response)
                    }
                    .doOnError { error ->
                        logger.error("Error retrieving exercise pool for user: {}", userId, error)
                    }
            }
        }
    }
}
