package com.congen

import com.congen.controllers.EquipmentController
import com.congen.controllers.ExerciseController
import com.congen.controllers.ExerciseEquipmentController
import com.congen.controllers.ExerciseMuscleController
import com.congen.controllers.HealthController
import com.congen.controllers.MuscleController
import com.congen.dal.EquipmentDAL
import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.MuscleDAL
import com.congen.service.HealthCheckService
import org.junit.jupiter.api.Test
import com.congen.model.HealthCheck
import com.congen.model.HealthCheckResponse
import com.congen.model.HealthStatus
import org.junit.jupiter.api.BeforeEach
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono
import java.time.Instant

@WebFluxTest(
    controllers = [
        EquipmentController::class,
        ExerciseController::class,
        MuscleController::class,
        ExerciseEquipmentController::class,
        ExerciseMuscleController::class,
        HealthController::class,
    ],
)
class CongenApplicationTests {
    @Autowired
    private lateinit var webTestClient: WebTestClient

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
    fun setUp() {
        val mockHealthResponse = HealthCheckResponse(
            status = HealthStatus.PASS,
            version = "1.0.0",
            releaseId = "test-release",
            checks = mapOf(
                "database" to listOf(
                    HealthCheck(
                        componentId = "postgres",
                        componentType = "database",
                        status = HealthStatus.PASS,
                        output = "Database connection successful",
                        links = mapOf("self" to "/health"),
                        time = Instant.now()
                    )
                ),
                "application" to listOf(
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
    fun `should return 404 for non-existent endpoints`() {
        webTestClient.get()
            .uri("/non-existent")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should handle health check endpoint`() {
        webTestClient.get()
            .uri("/health/")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.status").exists()
            .jsonPath("$.version").exists()
            .jsonPath("$.serviceId").isEqualTo("congen")
            .jsonPath("$.checks").exists()
    }

    @Test
    fun `should handle invalid JSON gracefully`() {
        webTestClient.post()
            .uri("/equipment/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("invalid json")
            .exchange()
            .expectStatus().isBadRequest
    }

    @Test
    fun `should handle missing required fields gracefully`() {
        val invalidEquipment = mapOf("description" to "Missing name field")
		
        webTestClient.post()
            .uri("/equipment/")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(invalidEquipment)
            .exchange()
            .expectStatus().isBadRequest
    }
}
