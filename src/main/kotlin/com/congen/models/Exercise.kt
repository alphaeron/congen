package com.congen.models

import org.springframework.data.relational.core.mapping.Table

@Table("exercise")
class Exercise (
    var name: String,
    val description: String,
)