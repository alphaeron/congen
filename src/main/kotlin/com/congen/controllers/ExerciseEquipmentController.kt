package com.congen.controllers

import com.congen.dto.ExerciseEquipmentData
import com.congen.service.ExerciseEquipmentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/exercise_equipment")
class ExerciseEquipmentController(
    private val exerciseEquipmentService: ExerciseEquipmentService,
) {
    @PostMapping("/")
    fun save(@RequestBody exerciseEquipmentData: ExerciseEquipmentData) : ResponseEntity<*>{
        return ResponseEntity.ok(
            exerciseEquipmentService.saveExerciseEquipment(exerciseEquipmentData)
        )
    }
}