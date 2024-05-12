package com.congen.models

import org.springframework.data.relational.core.mapping.Table

@Table("exercise_equipment")
class ExerciseEquipment(
    val exerciseName: String,
    val equipmentName: String,
)