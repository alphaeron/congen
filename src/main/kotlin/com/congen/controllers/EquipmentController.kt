package com.congen.controllers

import com.congen.dal.EquipmentDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Equipment
import com.congen.model.ExerciseEquipment
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
@RequestMapping("/equipment")
class EquipmentController(
    private val equipmentDAL: EquipmentDAL,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(EquipmentController::class.java)
    }

    @PostMapping("/")
    @Operation(
        summary = "Create equipment",
        description = "Creates a new equipment entry.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Equipment created successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun save(
        @Parameter(description = "Equipment to create", required = true)
        @RequestBody equipment: Equipment,
    ): ResponseEntity<*> {
        logger.info("Saving equipment: {}", equipment.name)
        return ResponseEntity.ok(
            equipmentDAL.insertEquipment(equipment),
        )
    }

    @GetMapping("/{name}")
    @Operation(
        summary = "Get equipment by name",
        description = "Retrieves equipment details by name.",
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
                description = "Equipment not found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun get(
        @Parameter(description = "Name of the equipment", required = true)
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<Equipment>> {
        return equipmentDAL.selectEquipmentByName(name)
            .map {
                logger.debug("Found equipment: {}", name)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Equipment not found: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting equipment: {}", name, e)
            }
    }

    @GetMapping("/{name}/exercise")
    @Operation(
        summary = "Get exercises for equipment",
        description = "Retrieves all exercises associated with a given equipment.",
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
                description = "No exercises found or equipment not found",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getExercise(
        @Parameter(description = "Name of the equipment", required = true)
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<List<ExerciseEquipment>>> {
        // First check if the equipment exists
        return equipmentDAL.selectEquipmentByName(name)
            .flatMap { _ ->
                // Equipment exists, now get its exercises
                exerciseEquipmentDAL.selectExerciseEquipmentByEquipment(name)
                    .flatMap { exercises ->
                        if (exercises.isEmpty()) {
                            logger.warn("No exercises found for equipment: {}", name)
                            Mono.just(ResponseEntity.notFound().build())
                        } else {
                            logger.debug("Found {} exercises for equipment: {}", exercises.size, name)
                            Mono.just(ResponseEntity.ok(exercises))
                        }
                    }
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Equipment not found: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting exercises for equipment: {}", name, e)
            }
    }

    @GetMapping("/")
    @Operation(
        summary = "Get all equipment",
        description = "Retrieves a list of all equipment.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Equipment list retrieved successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all equipment")
        return ResponseEntity.ok(
            equipmentDAL.selectEquipment(),
        )
    }
}
