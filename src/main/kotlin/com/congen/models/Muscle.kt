package com.congen.models

import org.springframework.data.relational.core.mapping.Table

@Table("muscle")
class Muscle(
    val name: String,
    val description: String,
)