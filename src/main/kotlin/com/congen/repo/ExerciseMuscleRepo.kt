package com.congen.repo

import com.congen.models.ExerciseMuscle
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ExerciseMuscleRepo : ReactiveCrudRepository<ExerciseMuscle, String> {
    fun findByExerciseName(exerciseName: String): Flux<ExerciseMuscle>

    fun findByMuscleName(muscleName: String): Flux<ExerciseMuscle>
}