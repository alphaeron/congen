package com.congen.service.impl

import com.congen.dto.ExerciseData
import com.congen.models.Exercise
import com.congen.repo.ExerciseRepo
import com.congen.service.ExerciseService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ExerciseServiceImpl(
    val exerciseRepo: ExerciseRepo
) : ExerciseService {
    override fun saveExercise(exerciseData: ExerciseData): Mono<ExerciseData> {
        val exercise = Exercise(name=exerciseData.name, description=exerciseData.description)
        return exerciseRepo.save(exercise)
            .map {
                ExerciseData(it.name, it.description)
            }
    }

    override fun getExercise(name: String): Mono<ExerciseData> {
        return exerciseRepo.findByName(name)
            .map {
                ExerciseData(
                    it.name, it.description
                )
            }
    }

    override fun getAllExercises(): Flux<ExerciseData> {
        return exerciseRepo.findAll()
            .map {
                ExerciseData(
                    it.name, it.description
                )
            }
    }
}