package com.congen

import com.congen.dal.EquipmentDAL
import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.MuscleDAL
import com.congen.model.HealthCheck
import com.congen.model.HealthCheckResponse
import com.congen.model.HealthStatus
import com.congen.service.HealthCheckService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import reactor.core.publisher.Mono
import java.time.Instant

class CongenApplicationTests : BaseIntegrationTest() {
    @MockBean
    private lateinit var equipmentDAL: EquipmentDAL

    @MockBean
    private lateinit var exerciseDAL: ExerciseDAL

    @MockBean
    private lateinit var muscleDAL: MuscleDAL

    @MockBean
    private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL

    @MockBean
    private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL

    @MockBean
    private lateinit var healthCheckService: HealthCheckService

    @BeforeEach
    override fun setUp() {
        super.setUp()

        val mockHealthResponse =
            HealthCheckResponse(
                status = HealthStatus.PASS,
                version = "1.0.0",
                releaseId = "test-release",
                checks =
                    mapOf(
                        "database" to
                            listOf(
                                HealthCheck(
                                    componentId = "postgres",
                                    componentType = "database",
                                    status = HealthStatus.PASS,
                                    output = "Database connection successful",
                                    links = mapOf("self" to "/health"),
                                    time = Instant.now()
                                )
                            ),
                        "application" to
                            listOf(
                                HealthCheck(
                                    componentId = "congen-api",
                                    componentType = "service",
                                    status = HealthStatus.PASS,
                                    output = "Application is running",
                                    links = mapOf("self" to "/health"),
                                    time = Instant.now()
                                )
                            )
                    )
            )

        `when`(healthCheckService.performHealthCheck()).thenReturn(Mono.just(mockHealthResponse))
    }

    @Test
    fun `should return 404 for non-existent prefixed endpoints`() {
        val token = getValidToken("user")
        webTestClient.get()
            .uri("/api/v1/non-existent")
            .header("Authorization", "Bearer $token")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should handle health check endpoint`() {
        webTestClient.get()
            .uri("/api/v1/health/")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").exists()
            .jsonPath("$.version").exists()
            .jsonPath("$.service_id").isEqualTo("congen")
            .jsonPath("$.checks").exists()
    }

    @Test
    fun `should handle invalid JSON gracefully`() {
        val token = getValidToken("user")
        webTestClient.post()
            .uri("/api/v1/equipment/")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue("invalid json")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should handle missing required fields gracefully`() {
        val token = getValidToken("user")
        val invalidEquipment = mapOf("description" to "Missing name field")

        webTestClient.post()
            .uri("/api/v1/equipment/")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $token")
            .bodyValue(invalidEquipment)
            .exchange()
            .expectStatus().isBadRequest
    }
}
