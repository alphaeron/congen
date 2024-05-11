package com.congen.controllers

import com.congen.dto.ExerciseData
import com.congen.service.ExerciseService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/exercise")
class ExerciseController(
    private val exerciseService: ExerciseService
) {
    @PostMapping("/")
    fun save(@RequestBody exerciseData: ExerciseData) : ResponseEntity<*>{
        return ResponseEntity.ok(
            exerciseService.saveExercise(exerciseData)
        )
    }

    @GetMapping("/{name}")
    fun get(@PathVariable("name") name: String): ResponseEntity<*>{
        return ResponseEntity.ok(
            exerciseService.getExercise(name)
        )
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        return ResponseEntity.ok(
            exerciseService.getAllExercises()
        )
    }
}