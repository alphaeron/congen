package com.congen.controllers

import com.congen.dal.ExerciseMuscleDAL
import com.congen.model.ExerciseMuscle
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
    @PreAuthorize("isAuthenticated()")
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
    fun getAll(): Mono<ResponseEntity<List<ExerciseMuscle>>> {
        logger.debug("Getting all exercise muscle relationships")
        return exerciseMuscleDAL.selectAllExerciseMuscle()
            .map { ResponseEntity.ok(it) }
            .doOnError { e -> logger.error("Error getting all exercise muscle relationships", e) }
    }

    /**
     * Creates a new exercise-muscle relationship.
     *
     * This endpoint creates a relationship between an exercise and a muscle,
     * defining which muscle group is targeted by the exercise.
     *
     * @param exerciseName The name of the exercise
     * @param muscleName The name of the muscle
     * @return The created exercise-muscle relationship
     *
     * @throws DatabaseException if database operation fails
     */
    @PostMapping("/")
    @PreAuthorize("hasRole('admin') or hasRole('service')")
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
        @Parameter(description = "Name of the exercise", required = true)
        @RequestParam("exercise_name") exerciseName: String,
        @Parameter(description = "Name of the muscle", required = true)
        @RequestParam("muscle_name") muscleName: String,
    ): Mono<ResponseEntity<ExerciseMuscle>> {
        logger.info("Saving exercise muscle relationship: {} - {}", exerciseName, muscleName)
        return exerciseMuscleDAL.insertExerciseMuscle(exerciseName, muscleName)
            .map { ResponseEntity.ok(it) }
            .doOnError { e -> logger.error("Error saving exercise muscle relationship: {} - {}", exerciseName, muscleName, e) }
    }
}
