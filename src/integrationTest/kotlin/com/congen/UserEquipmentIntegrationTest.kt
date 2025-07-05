package com.congen

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test

class UserEquipmentIntegrationTest : BaseIntegrationTest() {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should save user equipment`() {
        webTestClient.post()
            .uri("/user-equipment/?userId=1&equipmentName=Barbell")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.userId").isEqualTo(1)
            .jsonPath("$.equipmentName").isEqualTo("Barbell")
    }

    @Test
    fun `should get user equipment by user id`() {
        // First create user equipment
        webTestClient.post()
            .uri("/user-equipment/?userId=2&equipmentName=Dumbbells")
            .exchange()
            .expectStatus().isOk()

        // Then get equipment for the user
        webTestClient.get()
            .uri("/user-equipment/2")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].userId").isEqualTo(2)
            .jsonPath("$[0].equipmentName").isEqualTo("Dumbbells")
    }

    @Test
    fun `should delete user equipment`() {
        // First create user equipment
        webTestClient.post()
            .uri("/user-equipment/?userId=3&equipmentName=Kettlebell")
            .exchange()
            .expectStatus().isOk()

        // Then delete the equipment
        webTestClient.delete()
            .uri("/user-equipment/3/Kettlebell")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.userId").isEqualTo(3)
            .jsonPath("$.equipmentName").isEqualTo("Kettlebell")
    }

    @Test
    fun `should handle multiple equipment for same user`() {
        // Add multiple equipment for the same user
        webTestClient.post()
            .uri("/user-equipment/?userId=4&equipmentName=Barbell")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/user-equipment/?userId=4&equipmentName=Dumbbells")
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/user-equipment/?userId=4&equipmentName=Bench")
            .exchange()
            .expectStatus().isOk()

        // Get all equipment for the user
        webTestClient.get()
            .uri("/user-equipment/4")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(3)
    }
}
