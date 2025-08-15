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
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)
        webTestClient.post()
            .uri("/api/v1/program/?user_id=$userId&name=${IntegrationTestHelpers.TEST_PROGRAM_NAME}")
            .header("Authorization", "Bearer $token")
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
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)
        val programId =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = IntegrationTestHelpers.TEST_PROGRAM_NAME,
                token = token
            )
        webTestClient.get()
            .uri("/api/v1/program/$programId")
            .header("Authorization", "Bearer $token")
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
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/program/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get all programs`() {
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)
        IntegrationTestHelpers.createTestProgram(webTestClient, userId, "Test Program", token = token)
        // Create second program as inactive to avoid unique constraint violation
        webTestClient.post()
            .uri("/api/v1/program/?user_id=$userId&name=Another Program&is_active=false")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
        webTestClient.get()
            .uri("/api/v1/program/")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `should update program`() {
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)

        // First create a program
        val programId =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                IntegrationTestHelpers.TEST_PROGRAM_NAME,
                token = token
            )

        // Then update it
        webTestClient.patch()
            .uri("/api/v1/program/$programId?name=Updated Program&current_week_number=3&is_active=false")
            .header("Authorization", "Bearer $token")
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
        val token = getValidToken("user")
        webTestClient.patch()
            .uri("/api/v1/program/999?name=Updated Program&current_week_number=3&is_active=true")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should delete program`() {
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)
        val programId =
            IntegrationTestHelpers.createTestProgram(
                webTestClient,
                userId,
                name = IntegrationTestHelpers.TEST_PROGRAM_NAME,
                token = token
            )
        webTestClient.delete()
            .uri("/api/v1/program/$programId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(programId)
            .jsonPath("$.user_id").isEqualTo(userId)
            .jsonPath("$.name").isEqualTo(IntegrationTestHelpers.TEST_PROGRAM_NAME)
    }

    @Test
    fun `should return 404 when deleting non-existent program`() {
        val token = getValidToken("user")
        webTestClient.delete()
            .uri("/api/v1/program/999")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound()
    }

    @Test
    fun `should get programs by user id without filter`() {
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)
        IntegrationTestHelpers.createTestProgram(webTestClient, userId, "Test Program 1", token = token)
        // Create second program as inactive to avoid unique constraint violation
        webTestClient.post()
            .uri("/api/v1/program/?user_id=$userId&name=Test Program 2&is_active=false")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        webTestClient.get()
            .uri("/api/v1/program/user/$userId")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `should get active programs by user id`() {
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)
        IntegrationTestHelpers.createTestProgram(webTestClient, userId, "Active Program", token = token)

        webTestClient.get()
            .uri("/api/v1/program/user/$userId?is_active=true")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].is_active").isEqualTo(true)
    }

    @Test
    fun `should get inactive programs by user id`() {
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)
        val programId = IntegrationTestHelpers.createTestProgram(webTestClient, userId, "Test Program", token = token)

        // Deactivate the program
        webTestClient.patch()
            .uri("/api/v1/program/$programId?name=Test Program&current_week_number=1&is_active=false")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()

        webTestClient.get()
            .uri("/api/v1/program/user/$userId?is_active=false")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].is_active").isEqualTo(false)
    }

    @Test
    fun `should deactivate other programs when creating new active program`() {
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)

        // Create first program (should be active by default)
        val programId1 = IntegrationTestHelpers.createTestProgram(webTestClient, userId, "First Program", token = token)
        // Verify the first program is active
        webTestClient.get()
            .uri("/api/v1/program/user/$userId?is_active=true")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].id").isEqualTo(programId1)
            .jsonPath("$[0].is_active").isEqualTo(true)
        
        // Create second program (should deactivate the first one)
        val programId2 = IntegrationTestHelpers.createTestProgram(webTestClient, userId, "Second Program", token = token)

        // Check that only the second program is active
        webTestClient.get()
            .uri("/api/v1/program/user/$userId?is_active=true")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].id").isEqualTo(programId2)
            .jsonPath("$[0].is_active").isEqualTo(true)

        // Check that the first program is now inactive
        webTestClient.get()
            .uri("/api/v1/program/user/$userId?is_active=false")
            .header("Authorization", "Bearer $token")
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
        val token = getValidToken("user")
        val userId = IntegrationTestHelpers.createTestUser(webTestClient, token = token)
        // Create user consent for GDPR compliance
        IntegrationTestHelpers.createUserConsent(webTestClient, token)

        // Create first program (should be active by default)
        val programId1 = IntegrationTestHelpers.createTestProgram(webTestClient, userId, "First Program", token = token)

        // Create second program as inactive
        webTestClient.post()
            .uri("/api/v1/program/?user_id=$userId&name=Second Program&is_active=false")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".is_active").isEqualTo(false)

        // Check that the first program is still active
        webTestClient.get()
            .uri("/api/v1/program/user/$userId?is_active=true")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
            .jsonPath("$.length()").isEqualTo(1)
            .jsonPath("$[0].id").isEqualTo(programId1)
            .jsonPath("$[0].is_active").isEqualTo(true)
    }
}
