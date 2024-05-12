package com.congen.controllers

import com.congen.dto.MuscleData
import com.congen.service.ExerciseMuscleService
import com.congen.service.MuscleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/muscle")
class MuscleController(
    private val muscleService: MuscleService,
    private val exerciseMuscleService: ExerciseMuscleService,
) {
    @PostMapping("/")
    fun save(@RequestBody muscleData: MuscleData) : ResponseEntity<*>{
        return ResponseEntity.ok(
            muscleService.saveMuscle(muscleData)
        )
    }

    @GetMapping("/{name}")
    fun get(@PathVariable("name") name: String): ResponseEntity<*>{
        return ResponseEntity.ok(
            muscleService.getMuscle(name)
        )
    }

    @GetMapping("/{name}/exercise")
    fun getExercise(@PathVariable("name") name: String): ResponseEntity<*>{
        return ResponseEntity.ok(
            exerciseMuscleService.getByMuscleName(name)
        )
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        return ResponseEntity.ok(
            muscleService.getAllMuscles()
        )
    }
}