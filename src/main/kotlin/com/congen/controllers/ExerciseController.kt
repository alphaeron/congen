package com.congen.controllers

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
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
@RequestMapping("/exercise")
class ExerciseController(
    private val exerciseDAL: ExerciseDAL,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseController::class.java)
    }

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
        @Parameter(description = "Exercise to create", required = true)
        @RequestBody exercise: Exercise,
    ): ResponseEntity<*> {
        logger.info("Saving exercise: {}", exercise.name)
        return ResponseEntity.ok(
            exerciseDAL.insertExercise(exercise),
        )
    }

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
            .flatMap { _ ->
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
            .flatMap { _ ->
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
