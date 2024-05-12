package com.congen.models

import org.springframework.data.relational.core.mapping.Table

@Table("exercise")
class Exercise (
    val name: String,
    val description: String,
    val movementType: String,
    val isUnilateral: Boolean,
    val isUpper: Boolean,
    val isAccessory: Boolean,
)