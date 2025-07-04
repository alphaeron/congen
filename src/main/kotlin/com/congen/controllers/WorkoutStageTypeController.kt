package com.congen.controllers

import com.congen.dal.WorkoutStageTypeDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.WorkoutStageType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/workout-stage-type")
class WorkoutStageTypeController(
    private val workoutStageTypeDAL: WorkoutStageTypeDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(WorkoutStageTypeController::class.java)
    }

    @GetMapping("/{id}")
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
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Workout stage type not found: {}", id)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting workout stage type: {}", id, e)
            }
    }

    @GetMapping("/name/{name}")
    @Operation(
        summary = "Get workout stage type by name",
        description = "Retrieves a workout stage type by its name.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Workout stage type found by name",
                content = [Content(mediaType = "application/json")],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Workout stage type not found by name",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getByName(
        @Parameter(description = "Name of the workout stage type", required = true)
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<WorkoutStageType>> {
        return workoutStageTypeDAL.selectWorkoutStageTypeByName(name)
            .map {
                logger.debug("Found workout stage type by name: {}", name)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Workout stage type not found by name: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting workout stage type by name: {}", name, e)
            }
    }

    @GetMapping("/")
    @Operation(
        summary = "Get all workout stage types",
        description = "Retrieves a list of all workout stage types.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Workout stage type list retrieved successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all workout stage types")
        return try {
            ResponseEntity.ok(
                workoutStageTypeDAL.selectWorkoutStageTypes(),
            )
        } catch (e: Exception) {
            logger.error("Error getting all workout stage types", e)
            throw e
        }
    }
}
