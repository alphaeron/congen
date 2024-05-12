package com.congen.models

import org.springframework.data.relational.core.mapping.Table

@Table("equipment")
class Equipment(
    val name: String,
    val description: String,
)