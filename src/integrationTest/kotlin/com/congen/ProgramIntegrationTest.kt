package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProgramIntegrationTest : BaseIntegrationTest() {
    @BeforeEach
    override fun setUp() {
        super.setUp()
        // Additional setup if needed
    }

    @Test
    fun `should create program`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        webTestClient.post()
            .uri("/program/?userId=$userId&name=${IntegrationTestHelpers.TEST_PROGRAM_NAME}&currentWeekNumber=1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".user_id").isEqualTo(userId)
            .jsonPath(".name").isEqualTo(IntegrationTestHelpers.TEST_PROGRAM_NAME)
            .jsonPath(".current_week_number").isEqualTo(1)
    }

    @Test
    fun `should get program by id`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        val programId =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = IntegrationTestHelpers.TEST_PROGRAM_NAME,
                currentWeekNumber = 1
            )
        webTestClient.get()
            .uri("/program/$programId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").value { value: Any ->
                assert(value.toString() == programId.toString())
            }
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.name").isEqualTo(IntegrationTestHelpers.TEST_PROGRAM_NAME)
            .jsonPath("$.current_week_number").isEqualTo(1)
    }

    @Test
    fun `should return 404 when program not found`() {
        webTestClient.get()
            .uri("/program/999")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get all programs`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        IntegrationTestHelpers.createTestProgram(webTestClient, userId, IntegrationTestHelpers.TEST_PROGRAM_NAME)
        IntegrationTestHelpers.createTestProgram(webTestClient, userId, "Another Program")
        webTestClient.get()
            .uri("/program/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `should update program`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)

        // First create a program
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, IntegrationTestHelpers.TEST_PROGRAM_NAME, 1)

        // Then update it
        webTestClient.patch()
            .uri("/program/$programId?name=Updated Program&currentWeekNumber=3")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(programId)
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.name").isEqualTo("Updated Program")
            .jsonPath("$.current_week_number").isEqualTo(3)
    }

    @Test
    fun `should return 404 when updating non-existent program`() {
        webTestClient.patch()
            .uri("/program/999?name=Updated Program&currentWeekNumber=3")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should delete program`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        val programId =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = IntegrationTestHelpers.TEST_PROGRAM_NAME,
                currentWeekNumber = 1
            )
        webTestClient.delete()
            .uri("/program/$programId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(programId)
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.name").isEqualTo(IntegrationTestHelpers.TEST_PROGRAM_NAME)
    }

    @Test
    fun `should return 404 when deleting non-existent program`() {
        webTestClient.delete()
            .uri("/program/999")
            .exchange()
            .expectStatus().isNotFound()
    }
}
