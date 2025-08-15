package com.congen.controllers

import com.congen.BaseIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient

/**
 * Integration tests for DataRetentionController.
 *
 * Tests all data retention endpoints with proper authentication
 * and authorization for TTL management and cleanup operations.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class DataRetentionIntegrationTest : BaseIntegrationTest() {
    @Autowired
    protected override lateinit var webTestClient: WebTestClient

    @Test
    fun `GET policies should return retention policies for admin`() {
        val adminToken = getValidToken("admin")

        webTestClient
            .get()
            .uri("/api/v1/admin/data_retention/policies")
            .header("Authorization", "Bearer $adminToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$").isArray
            .jsonPath("$.length()").isNumber
    }

    @Test
    fun `GET policies should deny access for non-admin user`() {
        val userToken = getValidToken("user")

        webTestClient
            .get()
            .uri("/api/v1/admin/data_retention/policies")
            .header("Authorization", "Bearer $userToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `GET policies should require authentication`() {
        webTestClient
            .get()
            .uri("/api/v1/admin/data_retention/policies")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isUnauthorized
    }

    @Test
    fun `PUT policies should update retention policy for admin`() {
        val adminToken = getValidToken("admin")

        webTestClient
            .put()
            .uri(
                "/api/v1/admin/data_retention/policies?dataType=AUDIT_LOGS&retentionPeriodDays=365&description=Updated audit log retention"
            )
            .header("Authorization", "Bearer $adminToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.data_type").isEqualTo("AUDIT_LOGS")
            .jsonPath("$.retention_period_days").isEqualTo(365)
            .jsonPath("$.description").isEqualTo("Updated audit log retention")
    }

    @Test
    fun `PUT policies should deny access for non-admin user`() {
        val userToken = getValidToken("user")

        webTestClient
            .put()
            .uri(
                "/api/v1/admin/data_retention/policies?dataType=AUDIT_LOGS&retentionPeriodDays=365&description=Updated audit log retention"
            )
            .header("Authorization", "Bearer $userToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `PUT policies should reject invalid policy data`() {
        val adminToken = getValidToken("admin")

        webTestClient
            .put()
            .uri("/api/v1/admin/data_retention/policies?dataType=&retentionPeriodDays=-1&description=Invalid policy")
            .header("Authorization", "Bearer $adminToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `GET cleanup_estimate should return cleanup estimation for admin`() {
        val adminToken = getValidToken("admin")

        webTestClient
            .get()
            .uri("/api/v1/admin/data_retention/cleanup_estimate")
            .header("Authorization", "Bearer $adminToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.estimated_deletions").exists()
            .jsonPath("$.estimated_deletions").isArray
    }

    @Test
    fun `GET cleanup_estimate with dataType parameter should return filtered estimation`() {
        val adminToken = getValidToken("admin")

        webTestClient
            .get()
            .uri("/api/v1/admin/data_retention/cleanup_estimate?dataType=AUDIT_LOGS")
            .header("Authorization", "Bearer $adminToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.estimated_deletions").exists()
            .jsonPath("$.estimated_deletions").isArray
    }

    @Test
    fun `GET cleanup_estimate should deny access for non-admin user`() {
        val userToken = getValidToken("user")

        webTestClient
            .get()
            .uri("/api/v1/admin/data_retention/cleanup_estimate")
            .header("Authorization", "Bearer $userToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `POST cleanup should execute cleanup for admin`() {
        val adminToken = getValidToken("admin")

        webTestClient
            .post()
            .uri("/api/v1/admin/data_retention/cleanup")
            .header("Authorization", "Bearer $adminToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.cleanup_results").exists()
            .jsonPath("$.cleanup_results").isArray
            .jsonPath("$.summary").exists()
            .jsonPath("$.summary.total_deleted").isNumber
            .jsonPath("$.summary.data_types_processed").isNumber
            .jsonPath("$.summary.execution_time").exists()
    }

    @Test
    fun `POST cleanup should deny access for non-admin user`() {
        val userToken = getValidToken("user")

        webTestClient
            .post()
            .uri("/api/v1/admin/data_retention/cleanup")
            .header("Authorization", "Bearer $userToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isForbidden
    }

    @Test
    fun `integration workflow should work end-to-end`() {
        val adminToken = getValidToken("admin")

        // 1. Get current policies
        webTestClient
            .get()
            .uri("/api/v1/admin/data_retention/policies")
            .header("Authorization", "Bearer $adminToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk

        // 2. Update a policy
        webTestClient
            .put()
            .uri(
                "/api/v1/admin/data_retention/policies" +
                    "?dataType=AUDIT_LOGS&retentionPeriodDays=730" +
                    "&description=Extended audit log retention for compliance"
            )
            .header("Authorization", "Bearer $adminToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk

        // 3. Get cleanup estimate
        webTestClient
            .get()
            .uri("/api/v1/admin/data_retention/cleanup_estimate")
            .header("Authorization", "Bearer $adminToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `API should handle malformed JSON gracefully`() {
        val adminToken = getValidToken("admin")

        webTestClient
            .put()
            .uri("/api/v1/admin/data_retention/policies")
            .header("Authorization", "Bearer $adminToken")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("{invalid json}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `API should handle missing request body gracefully`() {
        val adminToken = getValidToken("admin")

        webTestClient
            .put()
            .uri("/api/v1/admin/data_retention/policies")
            .header("Authorization", "Bearer $adminToken")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `GET cleanup_estimate should handle invalid dataType parameter`() {
        val adminToken = getValidToken("admin")

        webTestClient
            .get()
            .uri("/api/v1/admin/data_retention/cleanup_estimate?dataType=INVALID_TYPE")
            .header("Authorization", "Bearer $adminToken")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isBadRequest
    }
}
