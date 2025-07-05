package com.congen.controllers

import com.congen.dal.ExerciseEquipmentDAL
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
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for managing exercise-equipment relationships.
 *
 * This controller provides endpoints for creating and querying relationships between
 * exercises and equipment. These relationships define which equipment is required
 * or can be used for each exercise, enabling equipment-based workout generation
 * and exercise filtering.
 *
 * ## Exercise-Equipment Relationships
 *
 * These relationships enable:
 * - **Equipment Filtering**: Finding exercises that can be performed with available equipment
 * - **Workout Planning**: Building workouts based on available equipment
 * - **Exercise Selection**: Choosing exercises that match user's equipment access
 * - **Equipment Requirements**: Understanding what equipment is needed for specific exercises
 *
 * ## Relationship Structure
 *
 * Each relationship contains:
 * - **Exercise Name**: The exercise being analyzed
 * - **Equipment Name**: The equipment required or used by the exercise
 * - **Required/Optional Classification**: Whether the equipment is required or optional
 *
 * ## Usage Examples
 *
 * - Find all exercises that can be done with dumbbells
 * - Get all equipment needed for a bench press
 * - Build a bodyweight-only workout
 * - Filter exercises based on home gym equipment
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/exercise_equipment")
@Tag(
    name = "Exercise-Equipment Management",
    description = "Operations for managing exercise-equipment relationships",
)
class ExerciseEquipmentController(
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseEquipmentController::class.java)
    }

    /**
     * Retrieves all exercise-equipment relationships.
     *
     * This endpoint returns a complete list of all exercise-equipment relationships
     * in the system. This is useful for understanding the complete mapping of
     * exercises to their required or optional equipment.
     *
     * @return List of all exercise-equipment relationships
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/")
    @Operation(
        summary = "Get all exercise equipment relationships",
        description = "Retrieves all exercise-equipment relationships.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "List of exercise-equipment relationships retrieved successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all exercise equipment relationships")
        return ResponseEntity.ok(
            exerciseEquipmentDAL.selectAllExerciseEquipment(),
        )
    }

    /**
     * Creates a new exercise-equipment relationship.
     *
     * This endpoint creates a new relationship between an exercise and equipment.
     * This relationship defines what equipment is required or can be used for
     * the specific exercise.
     *
     * @param exerciseEquipment The exercise-equipment relationship to create
     * @return The created relationship with assigned ID
     *
     * @throws DatabaseException if database operation fails
     */
    @PostMapping("/")
    @Operation(
        summary = "Create exercise equipment relationship",
        description = "Creates a new exercise-equipment relationship.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Exercise-equipment relationship created successfully",
                content = [Content(mediaType = "application/json")],
            ),
        ],
    )
    fun save(
        @Parameter(description = "Exercise-equipment relationship to create", required = true)
        @RequestBody exerciseEquipment: ExerciseEquipment,
    ): ResponseEntity<*> {
        logger.info("Saving exercise equipment relationship: {} - {}", exerciseEquipment.exerciseName, exerciseEquipment.equipmentName)
        return ResponseEntity.ok(
            exerciseEquipmentDAL.insertExerciseEquipment(exerciseEquipment),
        )
    }
}
