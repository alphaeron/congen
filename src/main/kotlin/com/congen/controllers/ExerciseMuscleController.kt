package com.congen.controllers

import com.congen.dal.ExerciseMuscleDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.ExerciseMuscle
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/exercise_muscle")
class ExerciseMuscleController(
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseMuscleController::class.java)
    }

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
