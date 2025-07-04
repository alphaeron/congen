package com.congen

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class HealthCheckIntegrationTest {
    @Autowired
    private lateinit var webTestClient: WebTestClient

    @Test
    fun `health check should return proper structure and status`() {
        webTestClient.get()
            .uri("/health/")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("pass")
            .jsonPath("$.version").exists()
            .jsonPath("$.releaseId").exists()
            .jsonPath("$.serviceId").isEqualTo("congen")
            .jsonPath("$.description").isEqualTo("Congen Exercise API Health Check")
            .jsonPath("$.checks.database").exists()
            .jsonPath("$.checks.application").exists()
            .jsonPath("$.checks.database[0].componentId").isEqualTo("postgres")
            .jsonPath("$.checks.database[0].componentType").isEqualTo("database")
            .jsonPath("$.checks.application[0].componentId").isEqualTo("congen-api")
            .jsonPath("$.checks.application[0].componentType").isEqualTo("service")
    }

    @Test
    fun `health check should include database response time`() {
        webTestClient.get()
            .uri("/health/")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.checks.database[0].observedValue").exists()
            .jsonPath("$.checks.database[0].observedUnit").isEqualTo("ms")
    }
}
