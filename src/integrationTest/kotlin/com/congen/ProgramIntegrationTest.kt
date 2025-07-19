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
            .uri("/api/v1/program/?userId=$userId&name=${IntegrationTestHelpers.TEST_PROGRAM_NAME}")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".user_id").isEqualTo(userId)
            .jsonPath(".name").isEqualTo(IntegrationTestHelpers.TEST_PROGRAM_NAME)
            .jsonPath(".current_week_number").isEqualTo(1)
            .jsonPath(".is_active").isEqualTo(true)
    }

    @Test
    fun `should get program by id`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        val programId =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = IntegrationTestHelpers.TEST_PROGRAM_NAME
            )
        webTestClient.get()
            .uri("/api/v1/program/$programId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").value { value: Any ->
                assert(value.toString() == programId.toString())
            }
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.name").isEqualTo(IntegrationTestHelpers.TEST_PROGRAM_NAME)
            .jsonPath("$.current_week_number").isEqualTo(1)
            .jsonPath("$.is_active").isEqualTo(true)
    }

    @Test
    fun `should return 404 when program not found`() {
        webTestClient.get()
            .uri("/api/v1/program/999")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get all programs`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        IntegrationTestHelpers.createTestProgram(webTestClient, userId, "Test Program")
        // Create second program as inactive to avoid unique constraint violation
        webTestClient.post()
            .uri("/api/v1/program/?userId=$userId&name=Another Program&isActive=false")
            .exchange()
            .expectStatus().isOk()
        webTestClient.get()
            .uri("/api/v1/program/")
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
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, IntegrationTestHelpers.TEST_PROGRAM_NAME)

        // Then update it
        webTestClient.patch()
            .uri("/api/v1/program/$programId?name=Updated Program&currentWeekNumber=3&isActive=false")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(programId)
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.name").isEqualTo("Updated Program")
            .jsonPath("$.current_week_number").isEqualTo(3)
            .jsonPath("$.is_active").isEqualTo(false)
    }

    @Test
    fun `should return 404 when updating non-existent program`() {
        webTestClient.patch()
            .uri("/api/v1/program/999?name=Updated Program&currentWeekNumber=3&isActive=true")
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
                name = IntegrationTestHelpers.TEST_PROGRAM_NAME
            )
        webTestClient.delete()
            .uri("/api/v1/program/$programId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(programId)
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.name").isEqualTo(IntegrationTestHelpers.TEST_PROGRAM_NAME)
            .jsonPath("$.is_active").isEqualTo(true)
    }

    @Test
    fun `should return 404 when deleting non-existent program`() {
        webTestClient.delete()
            .uri("/api/v1/program/999")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get programs by user id without filter`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        IntegrationTestHelpers.createTestProgram(webTestClient, userId, "Test Program 1")
        // Create second program as inactive to avoid unique constraint violation
        webTestClient.post()
            .uri("/api/v1/program/?userId=$userId&name=Test Program 2&isActive=false")
            .exchange()
            .expectStatus().isOk()

        webTestClient.get()
            .uri("/api/v1/program/user/$userId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `should get active programs by user id`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        IntegrationTestHelpers.createTestProgram(webTestClient, userId, "Active Program")

        webTestClient.get()
            .uri("/api/v1/program/user/$userId?isActive=true")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].is_active").isEqualTo(true)
    }

    @Test
    fun `should get inactive programs by user id`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, "Test Program")

        // Deactivate the program
        webTestClient.patch()
            .uri("/api/v1/program/$programId?name=Test Program&currentWeekNumber=1&isActive=false")
            .exchange()
            .expectStatus().isOk()

        webTestClient.get()
            .uri("/api/v1/program/user/$userId?isActive=false")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].is_active").isEqualTo(false)
    }

    @Test
    fun `should deactivate other programs when creating new active program`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)

        // Create first program (should be active by default)
        val programId1 = IntegrationTestHelpers.createTestProgram(webTestClient, userId, "First Program")

        // Create second program (should deactivate the first one)
        val programId2 = IntegrationTestHelpers.createTestProgram(webTestClient, userId, "Second Program")

        // Check that only the second program is active
        webTestClient.get()
            .uri("/api/v1/program/user/$userId?isActive=true")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].id").isEqualTo(programId2)
            .jsonPath("$[0].is_active").isEqualTo(true)

        // Check that the first program is now inactive
        webTestClient.get()
            .uri("/api/v1/program/user/$userId?isActive=false")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].id").isEqualTo(programId1)
            .jsonPath("$[0].is_active").isEqualTo(false)
    }

    @Test
    fun `should create inactive program without deactivating others`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)

        // Create first program (should be active by default)
        val programId1 = IntegrationTestHelpers.createTestProgram(webTestClient, userId, "First Program")

        // Create second program as inactive
        webTestClient.post()
            .uri("/api/v1/program/?userId=$userId&name=Second Program&isActive=false")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".is_active").isEqualTo(false)

        // Check that the first program is still active
        webTestClient.get()
            .uri("/api/v1/program/user/$userId?isActive=true")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].id").isEqualTo(programId1)
            .jsonPath("$[0].is_active").isEqualTo(true)
    }
}
