package com.congen.models

import org.springframework.data.relational.core.mapping.Table

@Table("exercise_muscle")
class ExerciseMuscle(
    val exerciseName: String,
    val muscleName: String,
)