package com.congen.service

import com.congen.dto.MuscleData
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface MuscleService {
    fun saveMuscle(muscleData: MuscleData): Mono<MuscleData>

    fun getMuscle(name: String): Mono<MuscleData>

    fun getAllMuscles(): Flux<MuscleData>
}