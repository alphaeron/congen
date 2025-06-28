package com.congen.controllers

import com.congen.model.Exercise
import com.congen.model.ExerciseMuscle
import com.congen.model.ExerciseEquipment
import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
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
    private val exerciseDAL: ExerciseDAL,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
) {
    @PostMapping("/")
    fun save(@RequestBody exercise: Exercise) : ResponseEntity<*> {
        return ResponseEntity.ok(
            exerciseDAL.insertExercise(exercise)
        )
    }

    @GetMapping("/{name}")
    fun get(@PathVariable("name") name: String): Mono<ResponseEntity<Exercise>> {
        return exerciseDAL.selectExerciseByName(name)
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/{name}/muscle")
    fun getMuscle(@PathVariable("name") name: String): Mono<ResponseEntity<List<ExerciseMuscle>>> {
        return exerciseMuscleDAL.selectExerciseMuscleByExercise(name)
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/{name}/equipment")
    fun getEquipment(@PathVariable("name") name: String): Mono<ResponseEntity<List<ExerciseEquipment>>> {
        return exerciseEquipmentDAL.selectExerciseEquipmentByExercise(name)
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        return ResponseEntity.ok(
            exerciseDAL.selectExercises()
        )
    }
}
