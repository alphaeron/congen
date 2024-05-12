package com.congen.service.impl

import com.congen.dto.ExerciseEquipmentData
import com.congen.models.ExerciseEquipment
import com.congen.repo.ExerciseEquipmentRepo
import com.congen.service.ExerciseEquipmentService
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Service
class ExerciseEquipmentServiceImpl(
    val exerciseEquipmentRepo: ExerciseEquipmentRepo
) : ExerciseEquipmentService {
    override fun saveExerciseEquipment(exerciseEquipmentData: ExerciseEquipmentData): Mono<ExerciseEquipmentData> {
        val equipment = ExerciseEquipment(
            exerciseName=exerciseEquipmentData.exerciseName,
            equipmentName=exerciseEquipmentData.equipmentName,
        )
        return exerciseEquipmentRepo.save(equipment)
            .map {
                ExerciseEquipmentData(
                    exerciseName=it.exerciseName,
                    equipmentName=it.equipmentName,
                )
            }
    }

    override fun getByEquipmentName(equipmentName: String): Flux<ExerciseEquipmentData> {
        return exerciseEquipmentRepo.findByEquipmentName(equipmentName)
            .map {
                ExerciseEquipmentData(
                    exerciseName=it.exerciseName,
                    equipmentName=it.equipmentName,
                )
            }
    }

    override fun getByExerciseName(exerciseName: String): Flux<ExerciseEquipmentData> {
        return exerciseEquipmentRepo.findByExerciseName(exerciseName)
            .map {
                ExerciseEquipmentData(
                    exerciseName=it.exerciseName,
                    equipmentName=it.equipmentName,
                )
            }
    }
}