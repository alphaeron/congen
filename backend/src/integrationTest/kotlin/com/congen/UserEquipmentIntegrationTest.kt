package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UserEquipmentIntegrationTest : BaseIntegrationTest() {
    private var userId: String = ""
    private lateinit var userToken: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Create a unique user for each test
        userToken = getValidToken("user")
        userId = IntegrationTestHelpers.createTestUser(webTestClient, token = userToken)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, userToken)
    }

    @Test
    fun `should save user equipment`() {
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, IntegrationTestHelpers.TEST_EQUIPMENT_NAME, token = userToken)

        // Verify the user equipment was created correctly
        webTestClient.get()
            .uri("/api/v1/user_equipment/$userId")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].user_id").isEqualTo(userId)
            .jsonPath("$[0].equipment_name").isEqualTo(IntegrationTestHelpers.TEST_EQUIPMENT_NAME)
    }

    @Test
    fun `should get user equipment by user id`() {
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, IntegrationTestHelpers.TEST_EQUIPMENT_NAME, token = userToken)
        webTestClient.get()
            .uri("/api/v1/user_equipment/$userId")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].user_id").isEqualTo(userId)
            .jsonPath("$[0].equipment_name").isEqualTo(IntegrationTestHelpers.TEST_EQUIPMENT_NAME)
    }

    @Test
    fun `should delete user equipment`() {
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, IntegrationTestHelpers.TEST_EQUIPMENT_NAME, token = userToken)

        // Delete the user equipment using query parameters
        webTestClient.delete()
            .uri("/api/v1/user_equipment/?user_id=$userId&equipment_name=${IntegrationTestHelpers.TEST_EQUIPMENT_NAME}")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.equipment_name").isEqualTo(IntegrationTestHelpers.TEST_EQUIPMENT_NAME)
    }

    @Test
    fun `should handle multiple equipment for same user`() {
        // Add multiple equipment for the same user
        IntegrationTestHelpers.createTestUserEquipment(webTestClient, userId, IntegrationTestHelpers.TEST_EQUIPMENT_NAME, token = userToken)
        IntegrationTestHelpers.createTestUserEquipment(
            webTestClient,
            userId,
            IntegrationTestHelpers.TEST_EQUIPMENT_NAME_2,
            token = userToken
        )

        // Get all equipment for the user
        webTestClient.get()
            .uri("/api/v1/user_equipment/$userId")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }
}
