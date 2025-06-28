package com.congen.controllers

import com.congen.model.ExerciseMuscle
import com.congen.model.Muscle
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.MuscleDAL
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/muscle")
class MuscleController(
    private val muscleDAL: MuscleDAL,
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
) {
    @PostMapping("/")
    fun save(@RequestBody muscle: Muscle) : ResponseEntity<*> {
        return ResponseEntity.ok(
            muscleDAL.insertMuscle(muscle)
        )
    }

    @GetMapping("/{name}")
    fun get(@PathVariable("name") name: String): Mono<ResponseEntity<Muscle>> {
        return muscleDAL.selectMuscleByName(name)
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/{name}/exercise")
    fun getExercise(@PathVariable("name") name: String): Mono<ResponseEntity<List<ExerciseMuscle>>> {
        return exerciseMuscleDAL.selectExerciseMuscleByMuscle(name)
            .map { ResponseEntity.ok(it) }
            .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()))
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        return ResponseEntity.ok(
            muscleDAL.selectMuscles()
        )
    }
}
