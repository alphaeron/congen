package com.congen.repo

import com.congen.models.Equipment
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface EquipmentRepo : ReactiveCrudRepository<Equipment, String> {
    fun findByName(name: String): Mono<Equipment>
}