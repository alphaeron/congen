package com.congen.controllers

import com.congen.dal.ExerciseEquipmentDAL
import com.congen.model.ExerciseEquipment
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
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all exercise equipment relationships")
        return ResponseEntity.ok(
            exerciseEquipmentDAL.selectAllExerciseEquipment(),
        )
    }

    @PostMapping("/")
    fun save(
        @RequestBody exerciseEquipment: ExerciseEquipment,
    ): ResponseEntity<*> {
        logger.info("Saving exercise equipment relationship: {} - {}", exerciseEquipment.exerciseName, exerciseEquipment.equipmentName)
        return ResponseEntity.ok(
            exerciseEquipmentDAL.insertExerciseEquipment(exerciseEquipment),
        )
    }
}
