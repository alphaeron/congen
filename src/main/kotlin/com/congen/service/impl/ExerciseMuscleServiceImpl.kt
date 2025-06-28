package com.congen.service.impl

import com.congen.dto.ExerciseMuscleData
import com.congen.models.ExerciseMuscle
import com.congen.repo.ExerciseMuscleRepo
import com.congen.service.ExerciseMuscleService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ExerciseMuscleServiceImpl(
    val exerciseMuscleRepo: ExerciseMuscleRepo
) : ExerciseMuscleService {
    override fun saveExerciseMuscle(exerciseMuscleData: ExerciseMuscleData): Mono<ExerciseMuscleData> {
        val muscle = ExerciseMuscle(
            exerciseName=exerciseMuscleData.exerciseName,
            muscleName=exerciseMuscleData.muscleName,
        )
        return exerciseMuscleRepo.save(muscle)
            .map {
                ExerciseMuscleData(
                    exerciseName=it.exerciseName,
                    muscleName=it.muscleName,
                )
            }
    }

    override fun getAllExerciseMuscle(): Flux<ExerciseMuscleData> {
        return exerciseMuscleRepo.findAll()
            .map {
                ExerciseMuscleData(
                    exerciseName=it.exerciseName,
                    muscleName=it.muscleName,
                )
            }
    }

    override fun getByMuscleName(muscleName: String): Flux<ExerciseMuscleData> {
        return exerciseMuscleRepo.findByMuscleName(muscleName)
            .map {
                ExerciseMuscleData(
                    exerciseName=it.exerciseName,
                    muscleName=it.muscleName,
                )
            }
    }

    override fun getByExerciseName(exerciseName: String): Flux<ExerciseMuscleData> {
        return exerciseMuscleRepo.findByExerciseName(exerciseName)
            .map {
                ExerciseMuscleData(
                    exerciseName=it.exerciseName,
                    muscleName=it.muscleName,
                )
            }
    }
}
