package com.congen.controllers

import com.congen.dto.ExerciseData
import com.congen.dto.ExerciseMuscleData
import com.congen.dto.ExerciseEquipmentData
import com.congen.service.ExerciseService
import com.congen.service.ExerciseEquipmentService
import com.congen.service.ExerciseMuscleService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/exercise")
class ExerciseController(
    private val exerciseService: ExerciseService,
    private val exerciseEquipmentService: ExerciseEquipmentService,
    private val exerciseMuscleService: ExerciseMuscleService,
) {
    @PostMapping("/")
    fun save(@RequestBody exerciseData: ExerciseData) : ResponseEntity<*> {
        return ResponseEntity.ok(
            exerciseService.saveExercise(exerciseData)
        )
    }

    @GetMapping("/{name}")
    fun get(@PathVariable("name") name: String): Mono<ResponseEntity<ExerciseData>> {
        return exerciseService
            .getExercise(name)
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/{name}/muscle")
    fun getMuscle(@PathVariable("name") name: String): Mono<ResponseEntity<List<ExerciseMuscleData>>> {
        return exerciseMuscleService
            .getByExerciseName(name)
            .collectList()
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/{name}/equipment")
    fun getEquipment(@PathVariable("name") name: String): Mono<ResponseEntity<List<ExerciseEquipmentData>>> {
        return exerciseEquipmentService
            .getByExerciseName(name)
            .collectList()
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        return ResponseEntity.ok(
            exerciseService.getAllExercises()
        )
    }
}
