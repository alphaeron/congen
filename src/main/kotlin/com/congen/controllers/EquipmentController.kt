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
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for managing equipment operations.
 *
 * This controller provides endpoints for creating, reading, and querying equipment data.
 * Equipment represents physical tools, machines, or apparatus used in exercises.
 * The controller also provides functionality to find exercises that use specific equipment.
 *
 * ## Equipment Model
 *
 * Equipment contains:
 * - **Name**: Unique identifier for the equipment
 * - **Description**: Optional description of the equipment and its use
 * - **Category**: Classification of the equipment type
 *
 * ## Exercise Associations
 *
 * Equipment can be associated with multiple exercises through the ExerciseEquipment
 * relationship. This allows for:
 * - Finding all exercises that can be performed with specific equipment
 * - Understanding what equipment is needed for each exercise
 * - Building workouts based on available equipment
 * - Filtering exercises by equipment availability
 *
 * ## Equipment Categories
 *
 * Common equipment categories include:
 * - **Free Weights**: Dumbbells, barbells, kettlebells
 * - **Machines**: Cable machines, Smith machines, leg press
 * - **Cardio**: Treadmills, stationary bikes, rowing machines
 * - **Bodyweight**: Pull-up bars, dip bars, resistance bands
 * - **Accessories**: Foam rollers, yoga mats, stability balls
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/equipment")
@Tag(
    name = "Equipment Management",
    description = "Operations for managing equipment data and exercise associations",
)
class EquipmentController(
    private val equipmentDAL: EquipmentDAL,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(EquipmentController::class.java)
    }

    /**
     * Creates a new equipment entry.
     *
     * This endpoint creates a new equipment with the provided information.
     * The equipment name must be unique within the system.
     *
     * @param equipment The equipment data to create
     * @return The created equipment with assigned ID
     *
     * @throws DatabaseException if database operation fails
     */
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
        @Parameter(description = "Name of the equipment", required = true)
        @RequestParam name: String,
        @Parameter(description = "Description of the equipment", required = true)
        @RequestParam description: String,
    ): ResponseEntity<*> {
        logger.info("Saving equipment: {}", name)
        return ResponseEntity.ok(
            equipmentDAL.insertEquipment(name, description),
        )
    }

    /**
     * Retrieves equipment by its name.
     *
     * This endpoint fetches a specific equipment's information by its name.
     * If the equipment is not found, a 404 error will be returned.
     *
     * @param name The name of the equipment to retrieve
     * @return The equipment if found, or 404 if not found
     *
     * @throws DatabaseException if database operation fails
     */
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

    /**
     * Retrieves all exercises associated with specific equipment.
     *
     * This endpoint finds all exercises that can be performed using the specified equipment.
     * The response includes exercise-equipment relationship data showing how
     * each exercise uses the equipment.
     *
     * @param name The name of the equipment to find exercises for
     * @return List of exercise-equipment relationships, or 404 if equipment not found or no exercises
     *
     * @throws DatabaseException if database operation fails
     */
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

    /**
     * Retrieves all equipment in the system.
     *
     * This endpoint returns a list of all equipment available in the system.
     * The response includes basic equipment information for each piece of equipment.
     *
     * @return List of all equipment
     *
     * @throws DatabaseException if database operation fails
     */
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
