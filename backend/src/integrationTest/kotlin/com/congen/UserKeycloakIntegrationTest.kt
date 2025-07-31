package com.congen

import com.congen.model.WeightUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource

/**
 * Integration tests for user creation with Keycloak integration.
 *
 * These tests use a real Keycloak instance via Testcontainers to test the actual
 * user creation flow, including Keycloak account creation and database profile creation.
 */
@TestPropertySource(
    properties = [
        "spring.profiles.active=integration-test"
    ]
)
class UserKeycloakIntegrationTest : BaseIntegrationTest() {
    private lateinit var testUserName: String

    @BeforeEach
    override fun setUp() {
        super.setUp()
        val unique = System.nanoTime()
        testUserName = "UserKeycloakIntegrationTest User $unique"
    }

    @Test
    fun `should create user with Keycloak integration when email and password provided`() {
        val email = "test.user.${System.nanoTime()}@example.com"
        val password = "SecurePassword123!"

        val userId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                name = testUserName,
                email = email,
                password = password
            )

        // Verify the user was created successfully
        webTestClient.get()
            .uri("/api/v1/user/$userId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(userId)
            .jsonPath("$.name").isEqualTo(testUserName)
            .jsonPath("$.age").isEqualTo(IntegrationTestHelpers.TEST_USER_AGE)
            .jsonPath("$.height").isEqualTo(IntegrationTestHelpers.TEST_USER_HEIGHT)
            .jsonPath("$.weight").isEqualTo(IntegrationTestHelpers.TEST_USER_WEIGHT)
    }

    @Test
    fun `should return 400 when email is missing`() {
        val password = "SecurePassword123!"

        webTestClient.post()
            .uri(
                "/api/v1/user/?name=$testUserName" +
                    "&age=${IntegrationTestHelpers.TEST_USER_AGE}" +
                    "&height=${IntegrationTestHelpers.TEST_USER_HEIGHT}" +
                    "&weight=${IntegrationTestHelpers.TEST_USER_WEIGHT}" +
                    "&password=$password"
            )
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return 400 when password is missing`() {
        val email = "test.user.${System.nanoTime()}@example.com"

        webTestClient.post()
            .uri(
                "/api/v1/user/?name=$testUserName" +
                    "&age=${IntegrationTestHelpers.TEST_USER_AGE}" +
                    "&height=${IntegrationTestHelpers.TEST_USER_HEIGHT}" +
                    "&weight=${IntegrationTestHelpers.TEST_USER_WEIGHT}" +
                    "&email=$email"
            )
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return 400 when email is invalid format`() {
        val email = "invalid-email-format"
        val password = "SecurePassword123!"

        webTestClient.post()
            .uri(
                "/api/v1/user/?name=$testUserName" +
                    "&age=${IntegrationTestHelpers.TEST_USER_AGE}" +
                    "&height=${IntegrationTestHelpers.TEST_USER_HEIGHT}" +
                    "&weight=${IntegrationTestHelpers.TEST_USER_WEIGHT}" +
                    "&email=$email" +
                    "&password=$password"
            )
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.BAD_REQUEST)
    }

    @Test
    fun `should return 422 when user age is 0 with Keycloak integration`() {
        val email = "test.user.${System.nanoTime()}@example.com"
        val password = "SecurePassword123!"

        webTestClient.post()
            .uri(
                "/api/v1/user/?name=$testUserName" +
                    "&age=0" +
                    "&height=${IntegrationTestHelpers.TEST_USER_HEIGHT}" +
                    "&weight=${IntegrationTestHelpers.TEST_USER_WEIGHT}" +
                    "&email=$email" +
                    "&password=$password"
            )
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("User age must be between 1 and 150"))
            }
    }

    @Test
    fun `should return 422 when user age is 151 with Keycloak integration`() {
        val email = "test.user.${System.nanoTime()}@example.com"
        val password = "SecurePassword123!"

        webTestClient.post()
            .uri(
                "/api/v1/user/?name=$testUserName" +
                    "&age=151" +
                    "&height=${IntegrationTestHelpers.TEST_USER_HEIGHT}" +
                    "&weight=${IntegrationTestHelpers.TEST_USER_WEIGHT}" +
                    "&email=$email" +
                    "&password=$password"
            )
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("User age must be between 1 and 150"))
            }
    }

    @Test
    fun `should return 422 when user height is 0 with Keycloak integration`() {
        val email = "test.user.${System.nanoTime()}@example.com"
        val password = "SecurePassword123!"

        webTestClient.post()
            .uri(
                "/api/v1/user/?name=$testUserName" +
                    "&age=${IntegrationTestHelpers.TEST_USER_AGE}" +
                    "&height=0" +
                    "&weight=${IntegrationTestHelpers.TEST_USER_WEIGHT}" +
                    "&email=$email" +
                    "&password=$password"
            )
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("User height must be between 0.01 and 300 cm"))
            }
    }

    @Test
    fun `should return 422 when user weight is 0 with Keycloak integration`() {
        val email = "test.user.${System.nanoTime()}@example.com"
        val password = "SecurePassword123!"

        webTestClient.post()
            .uri(
                "/api/v1/user/?name=$testUserName" +
                    "&age=${IntegrationTestHelpers.TEST_USER_AGE}" +
                    "&height=${IntegrationTestHelpers.TEST_USER_HEIGHT}" +
                    "&weight=0" +
                    "&email=$email" +
                    "&password=$password"
            )
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("User weight must be between 0.01 and 1000 kg"))
            }
    }

    @Test
    fun `should create user with weight conversion from LBS to KG`() {
        val email = "test.user.${System.nanoTime()}@example.com"
        val password = "SecurePassword123!"
        val weightInLbs = 150.0
        val expectedWeightInKg = 68.04

        val userId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                name = testUserName,
                weight = weightInLbs,
                email = email,
                password = password,
                unit = WeightUnit.LBS
            )

        // Verify the user was created successfully with weight conversion
        webTestClient.get()
            .uri("/api/v1/user/$userId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(userId)
            .jsonPath("$.name").isEqualTo(testUserName)
            .jsonPath("$.weight").isEqualTo(expectedWeightInKg)
    }

    @Test
    fun `should get user by id after Keycloak creation`() {
        val email = "test.user.${System.nanoTime()}@example.com"
        val password = "SecurePassword123!"

        // Create user first using helper method
        val userId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                name = testUserName,
                email = email,
                password = password
            )

        // Then get user by ID
        webTestClient.get()
            .uri("/api/v1/user/$userId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".id").isEqualTo(userId)
            .jsonPath(".name").isEqualTo(testUserName)
            .jsonPath(".age").isEqualTo(IntegrationTestHelpers.TEST_USER_AGE)
    }

    @Test
    fun `should delete user`() {
        val email = "test.user.${System.nanoTime()}@example.com"
        val password = "SecurePassword123!"

        // Create user first
        val userId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                name = testUserName,
                email = email,
                password = password
            )

        // Verify user exists in database
        webTestClient.get()
            .uri("/api/v1/user/$userId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(userId)

        // Delete the user
        webTestClient.delete()
            .uri("/api/v1/user/$userId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(userId)
            .jsonPath("$.name").isEqualTo(testUserName)

        // Verify user is deleted from database
        webTestClient.get()
            .uri("/api/v1/user/$userId")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)

        // Note: We cannot directly verify Keycloak deletion in integration tests
        // because we don't have access to Keycloak's internal state.
        // The deletion is verified indirectly by ensuring the database deletion
        // succeeds, which indicates the Keycloak deletion also succeeded
        // (since the service method handles both operations atomically).
    }

    @Test
    fun `should return 404 when deleting non-existent user`() {
        val nonExistentUserId = 99999

        webTestClient.delete()
            .uri("/api/v1/user/$nonExistentUserId")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `should return 403 when accessing me endpoint without authentication`() {
        webTestClient.get()
            .uri("/api/v1/user/me")
            .exchange()
            .expectStatus().isForbidden()
    }
}
