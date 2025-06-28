package com.congen.controllers

import com.congen.model.ExerciseEquipment
import com.congen.dal.ExerciseEquipmentDAL
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
    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        return ResponseEntity.ok(
            exerciseEquipmentDAL.selectAllExerciseEquipment()
        )
    }

    @PostMapping("/")
    fun save(@RequestBody exerciseEquipment: ExerciseEquipment) : ResponseEntity<*> {
        return ResponseEntity.ok(
            exerciseEquipmentDAL.insertExerciseEquipment(exerciseEquipment)
        )
    }
}
