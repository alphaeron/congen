package com.congen.controllers

import com.congen.dal.ExerciseEquipmentDAL
import com.congen.model.ExerciseEquipment
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/exercise_equipment")
class ExerciseEquipmentController(
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseEquipmentController::class.java)
    }

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
