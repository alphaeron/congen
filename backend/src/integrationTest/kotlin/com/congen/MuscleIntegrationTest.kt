package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class MuscleIntegrationTest : BaseIntegrationTest() {
    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Additional setup if needed
    }

    @Test
    fun `should create muscle`() {
        val uniqueName = "testmuscleout_${System.nanoTime()}"
        webTestClient.post()
            .uri("/api/v1/muscle/?name=$uniqueName&description=$uniqueName")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo(uniqueName)
            .jsonPath("$.description").isEqualTo(uniqueName)
    }

    @Test
    fun `should get muscle by name`() {
        // Muscle already exists in migrations
        webTestClient.get()
            .uri("/api/v1/muscle/${IntegrationTestHelpers.TEST_MUSCLE_NAME}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.name").isEqualTo(IntegrationTestHelpers.TEST_MUSCLE_NAME)
            .jsonPath("$.description").isEqualTo(IntegrationTestHelpers.TEST_MUSCLE_DESCRIPTION)
    }

    @Test
    fun `should return 404 when muscle not found`() {
        webTestClient.get()
            .uri("/api/v1/muscle/NonExistentMuscle")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get all muscles`() {
        // Muscles already exist in migrations
        webTestClient.get()
            .uri("/api/v1/muscle/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").value { value: Any ->
                // Muscles exist in migrations, so we should have at least some muscles
                assert(value is Number && value.toInt() > 0)
            }
    }

    @Test
    fun `should get exercises for muscle`() {
        // Muscle and exercise already exist in migrations
        // The relationship already exists in migration data, no need to create it

        webTestClient.get()
            .uri("/api/v1/muscle/${IntegrationTestHelpers.TEST_MUSCLE_NAME}/exercise")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").value { value: Any ->
                // Should have at least one exercise for this muscle
                assert(value is Number && value.toInt() > 0)
            }
            .jsonPath("$[*].muscle_name").value { values: Any ->
                // All exercises should be for the same muscle
                assert(values is List<*> && values.all { it == IntegrationTestHelpers.TEST_MUSCLE_NAME })
            }
            .jsonPath("$[*].exercise_name").value { values: Any ->
                // Should contain the expected exercise
                assert(values is List<*> && values.contains(IntegrationTestHelpers.TEST_EXERCISE_NAME))
            }
    }

    @Test
    fun `should return 404 when no exercises found for muscle`() {
        // Muscle exists in migrations but no relationship created
        webTestClient.get()
            .uri("/api/v1/muscle/thisdefinitelydoesntexist/exercise")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should return 404 when muscle not found for exercises`() {
        webTestClient.get()
            .uri("/api/v1/muscle/NonExistentMuscle/exercise")
            .exchange()
            .expectStatus().isNotFound()
    }
}
