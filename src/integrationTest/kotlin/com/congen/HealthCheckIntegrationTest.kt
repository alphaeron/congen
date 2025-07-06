package com.congen

import org.junit.jupiter.api.Test

class HealthCheckIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `health check should return proper structure and status`() {
        webTestClient.get()
            .uri("/health/")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").isEqualTo("pass")
            .jsonPath("$.version").exists()
            .jsonPath("$.release_id").exists()
            .jsonPath("$.serviceId").isEqualTo("congen")
            .jsonPath("$.description").isEqualTo("Congen Exercise API Health Check")
            .jsonPath("$.checks.database").exists()
            .jsonPath("$.checks.application").exists()
            .jsonPath("$.checks.database[0].component_id").isEqualTo("postgres")
            .jsonPath("$.checks.database[0].component_type").isEqualTo("database")
            .jsonPath("$.checks.application[0].component_id").isEqualTo("congen-api")
            .jsonPath("$.checks.application[0].component_type").isEqualTo("service")
    }

    @Test
    fun `health check should include database response time`() {
        webTestClient.get()
            .uri("/health/")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.checks.database[0].observed_value").exists()
            .jsonPath("$.checks.database[0].observed_unit").isEqualTo("ms")
    }
}
