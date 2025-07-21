package com.congen.controllers

import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import com.congen.generator.ConjugateWorkoutGeneratorService
import com.congen.model.Program
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
 *
 * ## Features
 *
 * - **Program-based Generation**: Works with existing programs instead of creating new ones
 * - **Automatic Week Progression**: Automatically determines the next week number from the program
 * - **User Preference Integration**: Incorporates user exercise preferences and equipment
 * - **Exercise Rotation**: Implements exercise rotation to prevent accommodation
 * - **Validation**: Comprehensive validation of program parameters
 *
 * @property conjugateWorkoutGeneratorService Service for generating conjugate workout programs
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/conjugate_workout_generator")
@Tag(name = "Conjugate Workout Generator", description = "Endpoints for generating conjugate powerlifting workout programs")
class ConjugateWorkoutGeneratorController(
    private val conjugateWorkoutGeneratorService: ConjugateWorkoutGeneratorService,
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
        logger.info("Generating conjugate workout program for program: {}, next week", programId)

        return conjugateWorkoutGeneratorService.generateNextWeek(programId)
            .map { program -> ResponseEntity.ok(program) }
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
