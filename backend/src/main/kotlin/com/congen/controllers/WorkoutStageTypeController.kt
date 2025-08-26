package com.congen.controllers

import com.congen.dal.WorkoutStageTypeDAL
import com.congen.model.WorkoutStageType
import com.congen.model.WorkoutStageTypeEnum
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for WorkoutStageType entity operations.
 *
 * This controller provides read operations for workout stage types in the Congen API.
 * Workout stage types define the categories of stages within a workout, such as
 * warm-up, main exercises, cool-down, and accessory work.
 *
 * ## WorkoutStageType Entity
 *
 * A workout stage type represents a category of workout stages:
 * - Unique identifier and name
 * - Description of the stage type
 * - Associated workout stages
 *
 * ## Endpoints
 *
 * - `GET /workout_stage_type/{id}` - Retrieve a workout stage type by ID
 * - `GET /workout_stage_type/name/{name}` - Retrieve a workout stage type by name
 * - `GET /workout_stage_type/` - Retrieve all workout stage types
 *
 * ## Error Handling
 *
 * - **404 Not Found**: When a workout stage type with the specified ID or name doesn't exist
 * - **500 Internal Server Error**: When database operations fail
 *
 * @param workoutStageTypeDAL Data access layer for workout stage type operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/workout_stage_type")
class WorkoutStageTypeController(
    private val workoutStageTypeDAL: WorkoutStageTypeDAL,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(WorkoutStageTypeController::class.java)
    }

    /**
     * Retrieves a workout stage type by its unique identifier.
     *
     * This endpoint fetches a workout stage type from the database using the provided ID.
     * If no workout stage type exists with the given ID, a 404 Not Found response is returned.
     *
     * @param id The unique identifier of the workout stage type to retrieve
     * @return Mono containing the workout stage type if found, or 404 if not found
     */
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get workout stage type by ID",
        description = "Retrieves a workout stage type by its unique identifier.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Workout stage type found",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Workout stage type not found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun get(
        @Parameter(description = "ID of the workout stage type", required = true)
        @PathVariable("id") id: Int,
    ): Mono<ResponseEntity<WorkoutStageType>> {
        return workoutStageTypeDAL.selectWorkoutStageTypeById(id)
            .map {
                logger.debug("Found workout stage type: {}", id)
                ResponseEntity.ok(it)
            }
            .doOnError { e ->
                logger.error("Error getting workout stage type: {}", id, e)
            }
    }

    /**
     * Retrieves a workout stage type by its name.
     *
     * This endpoint fetches a workout stage type from the database using the provided name.
     * If no workout stage type exists with the given name, a 404 Not Found response is returned.
     *
     * @param name The name of the workout stage type to retrieve
     * @return Mono containing the workout stage type if found, or 404 if not found
     */
    @GetMapping("/name/{name}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get workout stage type by name",
        description = "Retrieves a workout stage type by its name.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Workout stage type found",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Workout stage type not found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getByName(
        @Parameter(description = "Name of the workout stage type", required = true)
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<WorkoutStageType>> {
        val workoutStageTypeEnum =
            WorkoutStageTypeEnum.fromDisplayName(name)
                ?: return Mono.just(ResponseEntity.notFound().build())

        return workoutStageTypeDAL.selectWorkoutStageTypeByEnum(workoutStageTypeEnum)
            .map { workoutStageType ->
                logger.debug("Found workout stage type: {}", name)
                ResponseEntity.ok(workoutStageType)
            }
            .doOnError { exception ->
                logger.error("Error getting workout stage type: {}", name, exception)
            }
    }

    /**
     * Retrieves all workout stage types from the database.
     *
     * This endpoint fetches all workout stage type records and returns them as a list.
     * If no workout stage types exist, an empty list is returned.
     *
     * @return ResponseEntity containing a list of all workout stage types
     */
    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get all workout stage types",
        description = "Retrieves a list of all workout stage types.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Workout stage types retrieved successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getAll(): Mono<ResponseEntity<List<WorkoutStageType>>> {
        logger.debug("Getting all workout stage types")
        return workoutStageTypeDAL.selectWorkoutStageTypes()
            .map { workoutStageTypes ->
                ResponseEntity.ok(workoutStageTypes)
            }
            .doOnError { e ->
                logger.error("Error getting all workout stage types", e)
            }
    }
}
