package com.congen.dto

data class ExerciseData (
    val name: String,
    val description: String,
    val movementType: String,
    val isUnilateral: Boolean,
    val isUpper: Boolean,
    val isAccessory: Boolean,
)