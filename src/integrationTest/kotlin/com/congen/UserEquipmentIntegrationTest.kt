package com.congen

import com.congen.model.UserEquipment
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class UserEquipmentIntegrationTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should save user equipment`() {
        val userEquipment =
            UserEquipment(
                userId = 1,
                equipmentName = "Barbell",
            )

        webTestClient.post()
            .uri("/user-equipment/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(userEquipment))
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.userId").isEqualTo(1)
            .jsonPath("$.equipmentName").isEqualTo("Barbell")
    }

    @Test
    fun `should get user equipment by user id`() {
        // First create user equipment
        val userEquipment =
            UserEquipment(
                userId = 2,
                equipmentName = "Dumbbells",
            )

        webTestClient.post()
            .uri("/user-equipment/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(userEquipment))
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
        val userEquipment =
            UserEquipment(
                userId = 3,
                equipmentName = "Kettlebell",
            )

        webTestClient.post()
            .uri("/user-equipment/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(userEquipment))
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
        val equipment1 = UserEquipment(userId = 4, equipmentName = "Barbell")
        val equipment2 = UserEquipment(userId = 4, equipmentName = "Dumbbells")
        val equipment3 = UserEquipment(userId = 4, equipmentName = "Bench")

        // Add multiple equipment for the same user
        webTestClient.post()
            .uri("/user-equipment/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(equipment1))
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/user-equipment/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(equipment2))
            .exchange()
            .expectStatus().isOk()

        webTestClient.post()
            .uri("/user-equipment/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(objectMapper.writeValueAsString(equipment3))
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
