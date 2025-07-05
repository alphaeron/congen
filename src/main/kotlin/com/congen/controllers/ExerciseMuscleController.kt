package com.congen.controllers

import com.congen.dal.ExerciseMuscleDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.ExerciseMuscle
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for managing exercise-muscle relationships.
 *
 * This controller provides endpoints for creating and querying relationships between
 * exercises and muscles. These relationships define which muscles are targeted by
 * each exercise, enabling muscle-specific workout generation and exercise selection.
 *
 * ## Exercise-Muscle Relationships
 *
 * These relationships enable:
 * - **Muscle Targeting**: Understanding which muscles each exercise works
 * - **Workout Planning**: Building workouts that target specific muscle groups
 * - **Exercise Selection**: Finding exercises for specific muscle development goals
 * - **Balanced Training**: Ensuring all muscle groups are adequately trained
 *
 * ## Relationship Structure
 *
 * Each relationship contains:
 * - **Exercise Name**: The exercise being analyzed
 * - **Muscle Name**: The muscle group targeted by the exercise
 * - **Primary/Secondary Classification**: Whether the muscle is a primary or secondary target
 *
 * ## Usage Examples
 *
 * - Find all exercises that target the chest muscles
 * - Get all muscles worked by the bench press
 * - Build a workout focusing on back muscles
 * - Ensure balanced training across all muscle groups
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/exercise_muscle")
@Tag(
    name = "Exercise-Muscle Management",
    description = "Operations for managing exercise-muscle relationships",
)
class ExerciseMuscleController(
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseMuscleController::class.java)
    }

    /**
     * Retrieves all exercise-muscle relationships.
     *
     * This endpoint returns a complete list of all exercise-muscle relationships
     * in the system. This is useful for understanding the complete mapping of
     * exercises to their target muscles.
     *
     * @return List of all exercise-muscle relationships
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/")
    @Operation(
        summary = "Get all exercise muscle relationships",
        description = "Retrieves all exercise-muscle relationships.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "List of exercise-muscle relationships retrieved successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all exercise muscle relationships")
        return ResponseEntity.ok(
            exerciseMuscleDAL.selectAllExerciseMuscle(),
        )
    }

    /**
     * Retrieves a specific exercise-muscle relationship.
     *
     * This endpoint finds the relationship between a specific exercise and muscle.
     * This helps understand how a particular exercise targets a specific muscle group.
     *
     * @param exerciseName The name of the exercise
     * @param muscleName The name of the muscle
     * @return The exercise-muscle relationship if found, or 404 if not found
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/exercise/{exerciseName}/muscle/{muscleName}")
    @Operation(
        summary = "Get exercise muscle relationship",
        description = "Retrieves a specific exercise-muscle relationship by exercise and muscle name.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise-muscle relationship found",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Exercise-muscle relationship not found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getExerciseMuscle(
        @Parameter(description = "Name of the exercise", required = true)
        @PathVariable exerciseName: String,
        @Parameter(description = "Name of the muscle", required = true)
        @PathVariable muscleName: String,
    ): Mono<ResponseEntity<ExerciseMuscle>> {
        return exerciseMuscleDAL.selectExerciseMuscle(exerciseName, muscleName)
            .map {
                logger.debug("Found exercise muscle relationship: {} - {}", exerciseName, muscleName)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Exercise muscle relationship not found: {} - {}", exerciseName, muscleName)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting exercise muscle relationship: {} - {}", exerciseName, muscleName, e)
            }
    }

    /**
     * Creates a new exercise-muscle relationship.
     *
     * This endpoint creates a new relationship between an exercise and a muscle.
     * This relationship defines how the exercise targets the specific muscle group.
     *
     * @param exerciseMuscle The exercise-muscle relationship to create
     * @return The created relationship with assigned ID
     *
     * @throws DatabaseException if database operation fails
     */
    @PostMapping("/")
    @Operation(
        summary = "Create exercise muscle relationship",
        description = "Creates a new exercise-muscle relationship.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise-muscle relationship created successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun save(
        @Parameter(description = "Exercise-muscle relationship to create", required = true)
        @RequestBody exerciseMuscle: ExerciseMuscle,
    ): ResponseEntity<*> {
        logger.info("Saving exercise muscle relationship: {} - {}", exerciseMuscle.exerciseName, exerciseMuscle.muscleName)
        return ResponseEntity.ok(
            exerciseMuscleDAL.insertExerciseMuscle(exerciseMuscle),
        )
    }
}
