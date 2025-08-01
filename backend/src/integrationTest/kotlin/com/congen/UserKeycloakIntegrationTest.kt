package com.congen

import com.congen.model.WeightUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource

/**
 * Integration tests for user profile creation with Keycloak OAuth2 integration.
 *
 * These tests verify that authenticated users can create their profiles after
 * registering through Keycloak, without handling passwords in our backend.
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
    fun `should create user profile after Keycloak registration`() {
        val token = getValidToken("user")

        val userId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                name = testUserName,
                token = token
            )

        // Verify the user profile was created successfully
        webTestClient.get()
            .uri("/api/v1/user/me")
            .header("Authorization", "Bearer $token")
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
    fun `should return 401 when not authenticated`() {
        webTestClient.post()
            .uri(
                "/api/v1/user/?name=$testUserName" +
                    "&age=${IntegrationTestHelpers.TEST_USER_AGE}" +
                    "&height=${IntegrationTestHelpers.TEST_USER_HEIGHT}" +
                    "&weight=${IntegrationTestHelpers.TEST_USER_WEIGHT}"
            )
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `should return 422 when user age is 0`() {
        val token = getValidToken("user")

        webTestClient.post()
            .uri(
                "/api/v1/user/?name=$testUserName" +
                    "&age=0" +
                    "&height=${IntegrationTestHelpers.TEST_USER_HEIGHT}" +
                    "&weight=${IntegrationTestHelpers.TEST_USER_WEIGHT}"
            )
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("User age must be between 1 and 150"))
            }
    }

    @Test
    fun `should return 422 when user age is 151`() {
        val token = getValidToken("user")

        webTestClient.post()
            .uri(
                "/api/v1/user/?name=$testUserName" +
                    "&age=151" +
                    "&height=${IntegrationTestHelpers.TEST_USER_HEIGHT}" +
                    "&weight=${IntegrationTestHelpers.TEST_USER_WEIGHT}"
            )
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("User age must be between 1 and 150"))
            }
    }

    @Test
    fun `should return 422 when user height is 0`() {
        val token = getValidToken("user")

        webTestClient.post()
            .uri(
                "/api/v1/user/?name=$testUserName" +
                    "&age=${IntegrationTestHelpers.TEST_USER_AGE}" +
                    "&height=0" +
                    "&weight=${IntegrationTestHelpers.TEST_USER_WEIGHT}"
            )
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("User height must be between 0.01 and 300 cm"))
            }
    }

    @Test
    fun `should return 422 when user weight is 0`() {
        val token = getValidToken("user")

        webTestClient.post()
            .uri(
                "/api/v1/user/?name=$testUserName" +
                    "&age=${IntegrationTestHelpers.TEST_USER_AGE}" +
                    "&height=${IntegrationTestHelpers.TEST_USER_HEIGHT}" +
                    "&weight=0"
            )
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("User weight must be between 0.01 and 1000 kg"))
            }
    }

    @Test
    fun `should create user profile with weight conversion from LBS to KG`() {
        val token = getValidToken("user")
        val weightInLbs = 150.0
        val expectedWeightInKg = 68.04

        val userId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                name = testUserName,
                weight = weightInLbs,
                token = token,
                unit = WeightUnit.LBS
            )

        // Verify the user profile was created successfully with weight conversion
        webTestClient.get()
            .uri("/api/v1/user/me")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(userId)
            .jsonPath("$.name").isEqualTo(testUserName)
            .jsonPath("$.weight").isEqualTo(expectedWeightInKg)
    }

    @Test
    fun `should get user by id after profile creation`() {
        val token = getValidToken("user")

        // Create user profile first using helper method
        val userId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                name = testUserName,
                token = token
            )

        // Then get user by ID
        webTestClient.get()
            .uri("/api/v1/user/me")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".id").isEqualTo(userId)
            .jsonPath(".name").isEqualTo(testUserName)
            .jsonPath(".age").isEqualTo(IntegrationTestHelpers.TEST_USER_AGE)
    }

    @Test
    fun `should delete user`() {
        val userToken = getValidToken("user")

        // Create user profile first
        val userId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                name = testUserName,
                token = userToken
            )

        // Verify user exists in database
        webTestClient.get()
            .uri("/api/v1/user/me")
            .header("Authorization", "Bearer $userToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(userId)

        // Delete the user using service account token (admin privileges)
        val serviceToken = getValidToken("service")
        webTestClient.delete()
            .uri("/api/v1/user/$userId")
            .header("Authorization", "Bearer $serviceToken")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo(userId)
            .jsonPath("$.name").isEqualTo(testUserName)

        // Verify user is deleted from database by trying to access the user by ID
        webTestClient.get()
            .uri("/api/v1/user/$userId")
            .header("Authorization", "Bearer $serviceToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `should return 404 when deleting non-existent user`() {
        val serviceToken = getValidToken("service")
        val nonExistentUserId = 99999

        webTestClient.delete()
            .uri("/api/v1/user/$nonExistentUserId")
            .header("Authorization", "Bearer $serviceToken")
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.NOT_FOUND)
    }

    @Test
    fun `should return 401 when accessing me endpoint without authentication`() {
        webTestClient.get()
            .uri("/api/v1/user/me")
            .exchange()
            .expectStatus().isUnauthorized()
    }
}
