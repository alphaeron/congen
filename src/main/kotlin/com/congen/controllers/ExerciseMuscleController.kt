package com.congen.controllers

import com.congen.model.ExerciseMuscle
import com.congen.dal.ExerciseMuscleDAL
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/exercise_muscle")
class ExerciseMuscleController(
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
) {
    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        return ResponseEntity.ok(
            exerciseMuscleDAL.selectAllExerciseMuscle()
        )
    }

    @PostMapping("/")
    fun save(@RequestBody exerciseMuscle: ExerciseMuscle) : ResponseEntity<*> {
        return ResponseEntity.ok(
            exerciseMuscleDAL.insertExerciseMuscle(exerciseMuscle)
        )
    }
}
