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
        val uniqueName = "testequipmentout_${System.nanoTime()}"
        webTestClient.post()
            .uri("/equipment/?name=$uniqueName&description=$uniqueName")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo(uniqueName)
            .jsonPath("$.description").isEqualTo(uniqueName)
    }

    @Test
    fun `should get equipment by name`() {
        // Equipment already exists in migrations
        webTestClient.get()
            .uri("/equipment/${IntegrationTestHelpers.TEST_EQUIPMENT_NAME}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo(IntegrationTestHelpers.TEST_EQUIPMENT_NAME)
            .jsonPath("$.description").isEqualTo(IntegrationTestHelpers.TEST_EQUIPMENT_DESCRIPTION)
    }

    @Test
    fun `should return 404 when equipment not found`() {
        webTestClient.get()
            .uri("/equipment/NonExistentEquipment")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get all equipment`() {
        // Equipment already exists in migrations
        webTestClient.get()
            .uri("/equipment/")
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
        // Equipment and exercises already exist in migrations
        // The relationship already exists in migration data, no need to create it
        webTestClient.get()
            .uri("/equipment/${IntegrationTestHelpers.TEST_EQUIPMENT_NAME}/exercise")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$[0].equipment_name").isEqualTo(IntegrationTestHelpers.TEST_EQUIPMENT_NAME)
            .jsonPath("$[0].exercise_name").isEqualTo(IntegrationTestHelpers.TEST_EXERCISE_NAME)
    }

    @Test
    fun `should return 404 when no exercises found for equipment`() {
        // Equipment exists in migrations but no relationship created
        webTestClient.get()
            .uri("/equipment/thisdefinitelydoesntexist/exercise")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should return 404 when equipment not found for exercises`() {
        webTestClient.get()
            .uri("/equipment/NonExistentEquipment/exercise")
            .exchange()
            .expectStatus().isNotFound()
    }
}
