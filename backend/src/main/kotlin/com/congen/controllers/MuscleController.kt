package com.congen.controllers

import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.MuscleDAL
import com.congen.model.ExerciseMuscle
import com.congen.model.Muscle
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
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for managing muscle operations.
 *
 * This controller provides endpoints for creating, reading, and querying muscle data.
 * Muscles represent anatomical muscle groups that can be targeted by exercises.
 * The controller also provides functionality to find exercises associated with
 * specific muscles.
 *
 * ## Muscle Model
 *
 * Muscles contain:
 * - **Name**: Unique identifier for the muscle group
 * - **Description**: Optional description of the muscle's function and location
 *
 * ## Exercise Associations
 *
 * Muscles can be associated with multiple exercises through the ExerciseMuscle
 * relationship. This allows for:
 * - Finding all exercises that target a specific muscle
 * - Understanding which muscles are worked by each exercise
 * - Building workout programs that target specific muscle groups
 *
 * ## Error Handling
 *
 * - `404 Not Found`: Muscle not found or no exercises associated
 * - `500 Internal Server Error`: Database or system errors
 *
 * @param muscleDAL Data access layer for muscle operations
 * @param exerciseMuscleDAL Data access layer for exercise-muscle relationships
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/muscle")
@Tag(
    name = "Muscle Management",
    description = "Operations for managing muscle data and exercise associations",
)
class MuscleController(
    private val muscleDAL: MuscleDAL,
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(MuscleController::class.java)
    }

    /**
     * Creates a new muscle entry.
     *
     * This endpoint creates a new muscle with the provided information.
     * The muscle name must be unique within the system.
     *
     * @param muscle The muscle data to create
     * @return The created muscle with assigned ID
     *
     * @throws DatabaseException if database operation fails
     */
    @PostMapping("/")
    @PreAuthorize("hasRole('admin') or hasRole('service')")
    @Operation(
        summary = "Create muscle",
        description = "Creates a new muscle entry.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Muscle created successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun save(
        @Parameter(description = "Name of the muscle", required = true)
        @RequestParam name: String,
        @Parameter(description = "Description of the muscle", required = true)
        @RequestParam description: String,
    ): Mono<ResponseEntity<Muscle>> {
        logger.info("Saving muscle: {}", name)
        return muscleDAL.insertMuscle(name, description)
            .map { ResponseEntity.ok(it) }
            .doOnError { e -> logger.error("Error saving muscle: {}", name, e) }
    }

    /**
     * Retrieves a muscle by its name.
     *
     * This endpoint fetches a specific muscle's information by its name.
     * If the muscle is not found, a 404 error will be returned.
     *
     * @param name The name of the muscle to retrieve
     * @return The muscle if found, or 404 if not found
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/{name}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get muscle by name",
        description = "Retrieves muscle details by name.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Muscle found",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Muscle not found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun get(
        @Parameter(description = "Name of the muscle", required = true)
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<Muscle>> {
        return muscleDAL.selectMuscleByName(name)
            .map { muscle ->
                logger.debug("Found muscle: {}", name)
                ResponseEntity.ok(muscle)
            }
            .doOnError { e ->
                logger.error("Error getting muscle: {}", name, e)
            }
    }

    /**
     * Retrieves all muscles in the system.
     *
     * This endpoint returns a list of all muscles available in the system.
     * The response includes basic muscle information for each muscle group.
     *
     * @return List of all muscles
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get all muscles",
        description = "Retrieves a list of all muscles.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Muscle list retrieved successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getAll(): Mono<ResponseEntity<List<Muscle>>> {
        logger.debug("Getting all muscles")
        return muscleDAL.selectMuscles()
            .map { muscles ->
                logger.debug("Found {} muscles", muscles.size)
                ResponseEntity.ok(muscles)
            }
            .doOnError { e ->
                logger.error("Error getting all muscles", e)
            }
    }

    /**
     * Retrieves exercises associated with a specific muscle.
     *
     * This endpoint fetches all exercises that target the specified muscle.
     * The response includes exercise information and the relationship details.
     *
     * @param muscleName The name of the muscle to find exercises for
     * @return List of exercises associated with the muscle
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/{muscle_name}/exercise")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get exercises by muscle",
        description = "Retrieves all exercises that target a specific muscle.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercises found successfully",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Muscle not found or no exercises associated",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getExercisesByMuscle(
        @Parameter(description = "Name of the muscle", required = true)
        @PathVariable("muscle_name") muscleName: String,
    ): Mono<ResponseEntity<List<ExerciseMuscle>>> {
        return exerciseMuscleDAL.selectExerciseMuscleByMuscle(muscleName)
            .map { exercises ->
                if (exercises.isEmpty()) {
                    logger.warn("No exercises found for muscle: {}", muscleName)
                    ResponseEntity.notFound().build()
                } else {
                    logger.debug("Found {} exercises for muscle: {}", exercises.size, muscleName)
                    ResponseEntity.ok(exercises)
                }
            }
            .doOnError { e ->
                logger.error("Error getting exercises for muscle: {}", muscleName, e)
            }
    }
}
