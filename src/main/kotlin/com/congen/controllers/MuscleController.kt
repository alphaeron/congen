package com.congen.controllers

import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.MuscleDAL
import com.congen.exceptions.NoResultsFoundException
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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
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
        @Parameter(description = "Muscle to create", required = true)
        @RequestBody muscle: Muscle,
    ): ResponseEntity<*> {
        logger.info("Saving muscle: {}", muscle.name)
        return ResponseEntity.ok(
            muscleDAL.insertMuscle(muscle),
        )
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
            .map {
                logger.debug("Found muscle: {}", name)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Muscle not found: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting muscle: {}", name, e)
            }
    }

    /**
     * Retrieves all exercises associated with a specific muscle.
     *
     * This endpoint finds all exercises that target the specified muscle.
     * The response includes exercise-muscle relationship data showing how
     * each exercise targets the muscle.
     *
     * @param name The name of the muscle to find exercises for
     * @return List of exercise-muscle relationships, or 404 if muscle not found or no exercises
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/{name}/exercise")
    @Operation(
        summary = "Get exercises for muscle",
        description = "Retrieves all exercises associated with a given muscle.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercises found",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "No exercises found or muscle not found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getExercise(
        @Parameter(description = "Name of the muscle", required = true)
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<List<ExerciseMuscle>>> {
        // First check if the muscle exists
        return muscleDAL.selectMuscleByName(name)
            .flatMap { _ ->
                // Muscle exists, now get its exercises
                exerciseMuscleDAL.selectExerciseMuscleByMuscle(name)
                    .flatMap { exercises ->
                        if (exercises.isEmpty()) {
                            logger.warn("No exercises found for muscle: {}", name)
                            Mono.just(ResponseEntity.notFound().build())
                        } else {
                            logger.debug("Found {} exercises for muscle: {}", exercises.size, name)
                            Mono.just(ResponseEntity.ok(exercises))
                        }
                    }
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Muscle not found: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting exercises for muscle: {}", name, e)
            }
    }

    /**
     * Retrieves all muscles in the system.
     *
     * This endpoint returns a list of all muscle groups available in the system.
     * The response includes basic muscle information for each muscle group.
     *
     * @return List of all muscles
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/")
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
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all muscles")
        return ResponseEntity.ok(
            muscleDAL.selectMuscles(),
        )
    }
}
