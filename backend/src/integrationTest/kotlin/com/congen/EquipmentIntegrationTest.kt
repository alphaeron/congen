package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EquipmentIntegrationTest : BaseIntegrationTest() {
    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Additional setup if needed
    }

    @Test
    fun `should create equipment`() {
        val token = getValidToken("user")
        val uniqueName = "testequipmentout_${System.nanoTime()}"
        webTestClient.post()
            .uri("/api/v1/equipment/?name=$uniqueName&description=$uniqueName")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo(uniqueName)
            .jsonPath("$.description").isEqualTo(uniqueName)
    }

    @Test
    fun `should get equipment by name`() {
        val token = getValidToken("user")
        // Equipment already exists in migrations
        webTestClient.get()
            .uri("/api/v1/equipment/${IntegrationTestHelpers.TEST_EQUIPMENT_NAME}")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo(IntegrationTestHelpers.TEST_EQUIPMENT_NAME)
            .jsonPath("$.description").isEqualTo(IntegrationTestHelpers.TEST_EQUIPMENT_DESCRIPTION)
    }

    @Test
    fun `should return 404 when equipment not found`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/equipment/NonExistentEquipment")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get all equipment`() {
        val token = getValidToken("user")
        // Equipment already exists in migrations
        webTestClient.get()
            .uri("/api/v1/equipment/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").value { value: Any ->
                // Equipment exists in migrations, so we should have at least some equipment
                assert(value is Number && value.toInt() > 0)
            }
    }

    @Test
    fun `should get exercises for equipment`() {
        val token = getValidToken("user")
        // Equipment and exercises already exist in migrations
        // The relationship already exists in migration data, no need to create it
        webTestClient.get()
            .uri("/api/v1/equipment/${IntegrationTestHelpers.TEST_EQUIPMENT_NAME}/exercise")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].equipment_name").isEqualTo(IntegrationTestHelpers.TEST_EQUIPMENT_NAME)
            .jsonPath("$[0].exercise_name").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_NAME)
    }

    @Test
    fun `should return 404 when no exercises found for equipment`() {
        val token = getValidToken("user")
        // Equipment exists in migrations but no relationship created
        webTestClient.get()
            .uri("/api/v1/equipment/thisdefinitelydoesntexist/exercise")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should return 404 when equipment not found for exercises`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/equipment/NonExistentEquipment/exercise")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }
}
