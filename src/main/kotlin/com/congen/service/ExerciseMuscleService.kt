package com.congen.service

import com.congen.dto.ExerciseMuscleData
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ExerciseMuscleService {
    fun saveExerciseMuscle(exerciseMuscleData: ExerciseMuscleData): Mono<ExerciseMuscleData>

    fun getAllExerciseMuscle(): Flux<ExerciseMuscleData>

    fun getByMuscleName(muscleName: String): Flux<ExerciseMuscleData>

    fun getByExerciseName(exerciseName: String): Flux<ExerciseMuscleData>
}
