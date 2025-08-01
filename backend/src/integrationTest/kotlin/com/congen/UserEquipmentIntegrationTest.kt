package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserEquipmentIntegrationTest : BaseIntegrationTest() {
    private var userId: Int = 0

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Create a unique user for each test
        val unique = System.nanoTime()
        val token = getValidToken("user")
        userId = IntegrationTestHelpers.createTestUser(webTestClient, "Test User $unique", token = token)
    }

    @Test
    fun `should save user equipment`() {
        val token = getValidToken("user")
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, IntegrationTestHelpers.TEST_EQUIPMENT_NAME, token = token)

        // Verify the user equipment was created correctly
        webTestClient.get()
            .uri("/api/v1/user_equipment/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].user_id").isEqualTo(userId)
            .jsonPath("$[0].equipment_name").isEqualTo(IntegrationTestHelpers.TEST_EQUIPMENT_NAME)
    }

    @Test
    fun `should get user equipment by user id`() {
        val token = getValidToken("user")
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, IntegrationTestHelpers.TEST_EQUIPMENT_NAME, token = token)
        webTestClient.get()
            .uri("/api/v1/user_equipment/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].user_id").isEqualTo(userId)
            .jsonPath("$[0].equipment_name").isEqualTo(IntegrationTestHelpers.TEST_EQUIPMENT_NAME)
    }

    @Test
    fun `should delete user equipment`() {
        val token = getValidToken("user")
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, IntegrationTestHelpers.TEST_EQUIPMENT_NAME, token = token)

        // Delete the user equipment using query parameters
        webTestClient.delete()
            .uri("/api/v1/user_equipment/?user_id=$userId&equipment_name=${IntegrationTestHelpers.TEST_EQUIPMENT_NAME}")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.equipment_name").isEqualTo(IntegrationTestHelpers.TEST_EQUIPMENT_NAME)
    }

    @Test
    fun `should handle multiple equipment for same user`() {
        val token = getValidToken("user")
        // Add multiple equipment for the same user
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, IntegrationTestHelpers.TEST_EQUIPMENT_NAME, token = token)
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, IntegrationTestHelpers.TEST_EQUIPMENT_NAME_2, token = token)

        // Get all equipment for the user
        webTestClient.get()
            .uri("/api/v1/user_equipment/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }
}
