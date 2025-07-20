package com.congen.controllers

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.MovementType
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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for managing exercise operations.
 *
 * This controller provides endpoints for creating, reading, and querying exercise data.
 * Exercises represent physical movements that can be performed as part of a workout.
 * The controller also provides functionality to find related muscles and equipment
 * for each exercise.
 *
 * ## Exercise Model
 *
 * Exercises contain:
 * - **Name**: Unique identifier for the exercise
 * - **Description**: Optional description of how to perform the exercise
 * - **Category**: Classification of the exercise type
 *
 * ## Related Data
 *
 * Each exercise can have associated:
 * - **Muscles**: Which muscle groups the exercise targets
 * - **Equipment**: What equipment is required or used
 * - **Workout Types**: Which types of workouts the exercise is suitable for
 *
 * ## Usage Examples
 *
 * - Get all exercises available in the system
 * - Find exercises that target specific muscles
 * - Get equipment requirements for an exercise
 * - Create new exercises for the system
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/exercise")
@Tag(
    name = "Exercise Management",
    description = "Operations for managing exercise data and relationships",
)
class ExerciseController(
    private val exerciseDAL: ExerciseDAL,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseController::class.java)
    }

    /**
     * Creates a new exercise entry.
     *
     * This endpoint creates a new exercise with the provided information.
     * The exercise name must be unique within the system.
     *
     * @param name The name of the exercise
     * @param description The description of the exercise
     * @param movementType The type of movement
     * @param isUnilateral Whether the exercise is unilateral
     * @param isUpper Whether the exercise targets upper body
     * @param isAccessory Whether the exercise is an accessory movement
     * @return The created exercise with assigned ID
     *
     * @throws DatabaseException if database operation fails
     */
    @PostMapping("/")
    @Operation(
        summary = "Create exercise",
        description = "Creates a new exercise entry.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise created successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun save(
        @Parameter(description = "Name of the exercise", required = true)
        @RequestParam name: String,
        @Parameter(description = "Description of the exercise", required = true)
        @RequestParam description: String,
        @Parameter(description = "Type of movement", required = true)
        @RequestParam("movement_type") movementType: MovementType,
        @Parameter(description = "Whether the exercise is unilateral", required = true)
        @RequestParam("is_unilateral") isUnilateral: Boolean,
        @Parameter(description = "Whether the exercise targets upper body", required = true)
        @RequestParam("is_upper") isUpper: Boolean,
        @Parameter(description = "Whether the exercise is an accessory movement", required = true)
        @RequestParam("is_accessory") isAccessory: Boolean,
    ): ResponseEntity<*> {
        logger.info("Saving exercise: {}", name)
        return ResponseEntity.ok(
            exerciseDAL.insertExercise(name, description, movementType, isUnilateral, isUpper, isAccessory),
        )
    }

    /**
     * Retrieves an exercise by its name.
     *
     * This endpoint fetches a specific exercise's information by its name.
     * If the exercise is not found, a 404 error will be returned.
     *
     * @param name The name of the exercise to retrieve
     * @return The exercise if found, or 404 if not found
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/{name}")
    @Operation(
        summary = "Get exercise by name",
        description = "Retrieves exercise details by name.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise found",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Exercise not found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun get(
        @Parameter(description = "Name of the exercise", required = true)
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<Exercise>> {
        return exerciseDAL.selectExerciseByName(name)
            .map {
                logger.debug("Found exercise: {}", name)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Exercise not found: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting exercise: {}", name, e)
            }
    }

    /**
     * Retrieves all muscles associated with a specific exercise.
     *
     * This endpoint finds all muscles that are targeted by the specified exercise.
     * The response includes exercise-muscle relationship data showing how
     * the exercise targets each muscle.
     *
     * @param name The name of the exercise to find muscles for
     * @return List of exercise-muscle relationships, or 404 if exercise not found or no muscles
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/{name}/muscle")
    @Operation(
        summary = "Get muscles for exercise",
        description = "Retrieves all muscles associated with a given exercise.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Muscles found",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "No muscles found or exercise not found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getMuscle(
        @Parameter(description = "Name of the exercise", required = true)
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<List<ExerciseMuscle>>> {
        return exerciseDAL.selectExerciseByName(name)
            .flatMap {
                exerciseMuscleDAL.selectExerciseMuscleByExercise(name)
                    .flatMap { muscles ->
                        if (muscles.isEmpty()) {
                            logger.warn("No muscles found for exercise: {}", name)
                            Mono.just(ResponseEntity.notFound().build())
                        } else {
                            logger.debug("Found {} muscles for exercise: {}", muscles.size, name)
                            Mono.just(ResponseEntity.ok(muscles))
                        }
                    }
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Exercise not found: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting muscles for exercise: {}", name, e)
            }
    }

    /**
     * Retrieves all equipment associated with a specific exercise.
     *
     * This endpoint finds all equipment that is required or used by the specified exercise.
     * The response includes exercise-equipment relationship data showing what
     * equipment is needed for the exercise.
     *
     * @param name The name of the exercise to find equipment for
     * @return List of exercise-equipment relationships, or 404 if exercise not found or no equipment
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/{name}/equipment")
    @Operation(
        summary = "Get equipment for exercise",
        description = "Retrieves all equipment associated with a given exercise.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Equipment found",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "No equipment found or exercise not found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getEquipment(
        @Parameter(description = "Name of the exercise", required = true)
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<List<ExerciseEquipment>>> {
        return exerciseDAL.selectExerciseByName(name)
            .flatMap {
                exerciseEquipmentDAL.selectExerciseEquipmentByExercise(name)
                    .flatMap { equipment ->
                        if (equipment.isEmpty()) {
                            logger.warn("No equipment found for exercise: {}", name)
                            Mono.just(ResponseEntity.notFound().build())
                        } else {
                            logger.debug("Found {} equipment for exercise: {}", equipment.size, name)
                            Mono.just(ResponseEntity.ok(equipment))
                        }
                    }
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Exercise not found: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting equipment for exercise: {}", name, e)
            }
    }

    /**
     * Retrieves all exercises in the system.
     *
     * This endpoint returns a list of all exercises available in the system.
     * The response includes basic exercise information for each exercise.
     *
     * @return List of all exercises
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/")
    @Operation(
        summary = "Get all exercises",
        description = "Retrieves a list of all exercises.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise list retrieved successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all exercises")
        return ResponseEntity.ok(
            exerciseDAL.selectExercises(),
        )
    }
}
