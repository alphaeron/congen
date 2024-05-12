package com.congen.service.impl

import com.congen.dto.MuscleData
import com.congen.models.Muscle
import com.congen.repo.MuscleRepo
import com.congen.service.MuscleService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class MuscleServiceImpl(
    val muscleRepo: MuscleRepo
) : MuscleService {
    override fun saveMuscle(muscleData: MuscleData): Mono<MuscleData> {
        val muscle = Muscle(
            name=muscleData.name,
            description=muscleData.description,
        )
        return muscleRepo.save(muscle)
            .map {
                MuscleData(
                    name=it.name,
                    description=it.description,
                )
            }
    }

    override fun getMuscle(name: String): Mono<MuscleData> {
        return muscleRepo.findByName(name)
            .map {
                MuscleData(
                    name=it.name,
                    description=it.description,
                )
            }
    }

    override fun getAllMuscles(): Flux<MuscleData> {
        return muscleRepo.findAll()
            .map {
                MuscleData(
                    name=it.name,
                    description=it.description,
                )
            }
    }
}