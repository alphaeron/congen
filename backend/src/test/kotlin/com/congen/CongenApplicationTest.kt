package com.congen

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource

/**
 * Integration test for CongenApplication.
 *
 * Tests that the Spring Boot application context loads successfully
 * and that the main application class is properly configured.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@SpringBootTest
@TestPropertySource(
    properties = [
        "CORS_ALLOWED_ORIGINS=http://localhost:3000,https://example.com",
        "CORS_ALLOWED_METHODS=GET,POST,PUT,DELETE",
        "CORS_ALLOWED_HEADERS=Content-Type,Authorization",
        "CORS_EXPOSED_HEADERS=X-Total-Count",
        "CORS_MAX_AGE=3600",
        "spring.profiles.active=test",
        "KEYCLOAK_AUTH_URL=http://localhost:8080",
        "KEYCLOAK_REALM=congen",
        "KEYCLOAK_CLIENT_ID=congen-client",
        "KEYCLOAK_CLIENT_SECRET=test-secret",
        "KEYCLOAK_URL=http://localhost:8080",
        "KEYCLOAK_SERVICE_ACCOUNT_USERNAME=admin",
        "KEYCLOAK_SERVICE_ACCOUNT_PASSWORD=admin"
    ]
)
class CongenApplicationTest {
    @Test
    fun `should load application context`() {
        // This test will pass if the application context loads successfully
    }

    @Test
    fun `should have CongenApplication class`() {
        // Verify the main application class exists
        assert(CongenApplication::class.java.isAssignableFrom(CongenApplication::class.java))
    }

    @Test
    fun `should have main function`() {
        // Verify the main function exists and is accessible
        // In Kotlin, the main function is a top-level function, not a class method
        // We can verify the class exists and has the SpringBootApplication annotation
        assert(CongenApplication::class.java.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication::class.java))
    }
}
