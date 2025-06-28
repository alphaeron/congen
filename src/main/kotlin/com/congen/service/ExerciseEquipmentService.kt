package com.congen.service

import com.congen.dto.ExerciseEquipmentData
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface ExerciseEquipmentService {
    fun saveExerciseEquipment(exerciseEquipmentData: ExerciseEquipmentData): Mono<ExerciseEquipmentData>

    fun getAllExerciseEquipment(): Flux<ExerciseEquipmentData>

    fun getByEquipmentName(equipmentName: String): Flux<ExerciseEquipmentData>

    fun getByExerciseName(exerciseName: String): Flux<ExerciseEquipmentData>
}
