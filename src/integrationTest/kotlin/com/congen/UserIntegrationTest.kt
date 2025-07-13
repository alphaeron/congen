package com.congen

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

class UserIntegrationTest : BaseIntegrationTest() {
    @BeforeEach
    override fun setUp() {
        super.setUp()
        val unique = System.nanoTime()
        val userName = "UserIntegrationTest User $unique"
        val userId = IntegrationTestHelpers.createTestUserWithId(webTestClient, userName)
        IntegrationTestHelpers.createAllReferenceDataForUser(webTestClient, userId)
        this.testUserName = userName
    }

    private lateinit var testUserName: String

    @Test
    fun `should return 422 when user age is 0`() {
        webTestClient.post()
            .uri(
                "/user/?name=$testUserName&age=0&height=${IntegrationTestHelpers.TEST_USER_HEIGHT}&weight=${IntegrationTestHelpers.TEST_USER_WEIGHT}"
            )
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("User age must be between 1 and 150"))
            }
    }

    @Test
    fun `should return 422 when user age is 151`() {
        webTestClient.post()
            .uri(
                "/user/?name=$testUserName&age=151&height=${IntegrationTestHelpers.TEST_USER_HEIGHT}&weight=${IntegrationTestHelpers.TEST_USER_WEIGHT}"
            )
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("User age must be between 1 and 150"))
            }
    }

    @Test
    fun `should return 422 when user height is 0`() {
        webTestClient.post()
            .uri(
                "/user/?name=$testUserName&age=${IntegrationTestHelpers.TEST_USER_AGE}&height=0&weight=${IntegrationTestHelpers.TEST_USER_WEIGHT}"
            )
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("User height must be between 0.01 and 300 cm"))
            }
    }

    @Test
    fun `should return 422 when user weight is 0`() {
        webTestClient.post()
            .uri(
                "/user/?name=$testUserName&age=${IntegrationTestHelpers.TEST_USER_AGE}&height=${IntegrationTestHelpers.TEST_USER_HEIGHT}&weight=0"
            )
            .exchange()
            .expectStatus().isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY)
            .expectBody()
            .jsonPath("$.error").value<String> { error ->
                assert(error.contains("User weight must be between 0.01 and 1000 kg"))
            }
    }

    @Test
    fun `should accept valid user data`() {
        webTestClient.post()
            .uri(
                "/user/?name=$testUserName&age=${IntegrationTestHelpers.TEST_USER_AGE}&height=${IntegrationTestHelpers.TEST_USER_HEIGHT}&weight=${IntegrationTestHelpers.TEST_USER_WEIGHT}"
            )
            .exchange()
            .expectStatus().isOk()
    }

    @Test
    fun `should get user by id`() {
        val userId = IntegrationTestHelpers.createTestUser(webTestClient)
        webTestClient.get()
            .uri("/user/$userId")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath(".id").isEqualTo(userId)
            .jsonPath(".name").isEqualTo(IntegrationTestHelpers.TEST_USER_NAME)
            .jsonPath(".age").isEqualTo(IntegrationTestHelpers.TEST_USER_AGE)
    }

    @Test
    fun `should get all users`() {
        webTestClient.get()
            .uri("/user/")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$").isArray()
    }
}
