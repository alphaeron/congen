package com.congen.repo

import com.congen.models.Muscle
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface MuscleRepo : ReactiveCrudRepository<Muscle, String> {
    fun findByName(name: String): Mono<Muscle>
}