package com.congen.service

import com.congen.dto.ExerciseData
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ExerciseService {
    fun saveExercise(exerciseData: ExerciseData): Mono<ExerciseData>

    fun getExercise(name: String): Mono<ExerciseData>

    fun getAllExercises(): Flux<ExerciseData>
}