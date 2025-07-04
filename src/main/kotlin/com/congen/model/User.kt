package com.congen.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.math.BigDecimal

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonIgnoreProperties(ignoreUnknown = true)
data class User(
    @param:JsonProperty("id") val id: Int? = null,
    @param:JsonProperty("name") val name: String,
    @param:JsonProperty("age") val age: Int,
    @param:JsonProperty("height") val height: BigDecimal,
    @param:JsonProperty("weight") val weight: BigDecimal,
)
