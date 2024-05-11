package com.congen.repo

import com.congen.models.Exercise
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface ExerciseRepo : ReactiveCrudRepository<Exercise, String> {
    fun findByName(name: String): Mono<Exercise>
}