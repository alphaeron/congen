package com.congen.controllers

import com.congen.dto.ExerciseMuscleData
import com.congen.service.ExerciseMuscleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/exercise_muscle")
class ExerciseMuscleController(
    private val exerciseMuscleService: ExerciseMuscleService,
) {
    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        return ResponseEntity.ok(
            exerciseMuscleService.getAllExerciseMuscle()
        )
    }

    @PostMapping("/")
    fun save(@RequestBody exerciseMuscleData: ExerciseMuscleData) : ResponseEntity<*> {
        return ResponseEntity.ok(
            exerciseMuscleService.saveExerciseMuscle(exerciseMuscleData)
        )
    }
}
