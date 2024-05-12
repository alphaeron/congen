package com.congen.repo

import com.congen.models.ExerciseEquipment
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux

@Repository
interface ExerciseEquipmentRepo : ReactiveCrudRepository<ExerciseEquipment, String> {
    fun findByExerciseName(exerciseName: String): Flux<ExerciseEquipment>

    fun findByEquipmentName(equipmentName: String): Flux<ExerciseEquipment>
}