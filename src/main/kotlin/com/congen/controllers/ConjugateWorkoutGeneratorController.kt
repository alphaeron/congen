package com.congen.controllers

import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Program
import com.congen.service.ConjugateWorkoutGeneratorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for conjugate workout program generation.
 *
 * This controller provides endpoints for generating conjugate powerlifting workout programs
 * based on the Westside Barbell methodology, incorporating user preferences, available
 * equipment, and exercise rotation history.
 *
 * ## Conjugate Method
 *
 * The conjugate method is a powerlifting training system developed by Louie Simmons
 * at Westside Barbell. It combines:
 * - **Max Effort (ME)**: Heavy singles, doubles, or triples at 85-92% 1RM
 * - **Dynamic Effort (DE)**: Speed work at 60-70% 1RM with explosive intent
 * - **Accessory Work**: Targeted muscle development and weak point training
 * - **Exercise Rotation**: Prevent accommodation by rotating exercises every 1-3 weeks
 *
 * ## Program Options
 *
 * - **2-day programs**: Condensed conjugate approach (Phil Daru method)
 * - **3-day programs**: Traditional conjugate with ME/DE/accessory split
 * - **4-day programs**: Extended conjugate with additional volume
 *
 * ## Endpoints
 *
 * - `GET /conjugate-workout-generator/{userId}/generate` - Generate next week of workouts
 *
 * ## Error Handling
 *
 * - **400 Bad Request**: When parameters are invalid
 * - **404 Not Found**: When user doesn't exist
 * - **422 Unprocessable Entity**: When generation fails
 * - **500 Internal Server Error**: When database operations fail
 *
 * @property conjugateWorkoutGeneratorService Service for generating conjugate workout programs
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/conjugate-workout-generator")
@Tag(name = "Conjugate Workout Generator", description = "Endpoints for generating conjugate powerlifting workout programs")
class ConjugateWorkoutGeneratorController(
    private val conjugateWorkoutGeneratorService: ConjugateWorkoutGeneratorService,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ConjugateWorkoutGeneratorController::class.java)
    }

    /**
     * Generates the next week of workouts for a user's conjugate powerlifting program.
     *
     * This endpoint creates a complete week of workouts based on the conjugate method,
     * incorporating user preferences, available equipment, and exercise rotation history.
     * The generated program includes programmed workouts, workout stages, programmed exercises,
     * and set schemes with Prilepin-based guidelines.
     *
     * @param userId The ID of the user
     * @param currentWeekNumber The current week number in the program (default: 1)
     * @return Mono containing the generated program with workouts
     */
    @PostMapping("/{userId}/generate")
    @Operation(
        summary = "Generate conjugate workout program",
        description = " Generates the next week of workouts for a user's conjugate powerlifting program."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Workout program generated successfully",
                content = [Content(mediaType = "application/json")]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid parameters"
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found"
            ),
            ApiResponse(
                responseCode = "422",
                description = "Workout generation failed"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun generateNextWeek(
        @Parameter(description = "User ID", required = true)
        @PathVariable("userId") userId: Int,
        @Parameter(description = "Current week number in the program", required = false)
        @RequestParam("currentWeekNumber", defaultValue = "1") currentWeekNumber: Int
    ): Mono<ResponseEntity<Program>> {
        logger.info("Generating conjugate workout program for user: {}, week: {}", userId, currentWeekNumber)

        // Validate parameters
        if (currentWeekNumber < 1) {
            logger.warn("Invalid currentWeekNumber: {} for user: {}", currentWeekNumber, userId)
            return Mono.just(ResponseEntity.badRequest().build())
        }

        return conjugateWorkoutGeneratorService.generateNextWeek(userId, currentWeekNumber)
            .map { program ->
                logger.debug("Successfully generated program: {} for user: {}", program.id, userId)
                ResponseEntity.ok(program)
            }
            .onErrorResume { error ->
                logger.error("Error generating workout program for user: {}", userId, error)
                when (error) {
                    is IllegalArgumentException -> Mono.just(ResponseEntity.badRequest().build())
                    is NoResultsFoundException -> Mono.just(ResponseEntity.notFound().build())
                    else -> Mono.just(ResponseEntity.unprocessableEntity().build())
                }
            }
    }
}
