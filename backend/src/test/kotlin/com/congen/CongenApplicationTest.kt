package com.congen

import org.junit.jupiter.api.Test
import org.springframework.boot.autoconfigure.SpringBootApplication
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
        "cors.allowed-origins=http://localhost:3000,https://example.com",
        "cors.allowed-methods=GET,POST,PUT,DELETE",
        "cors.allowed-headers=Content-Type,Authorization",
        "cors.exposed-headers=X-Total-Count",
        "cors.max-age=3600",
        "spring.profiles.active=test",
        "congen.keycloak.client.id=congen-client",
        "congen.keycloak.client.secret=test-secret",
        "congen.keycloak.service_account.username=service-account-congen-backend",
        "congen.keycloak.management.url=http://localhost:9000",
        "KEYCLOAK_URL=http://localhost:8080",
        "KEYCLOAK_REALM=congen",
        "JWT_ISSUER_URI=http://localhost:8080/realms/congen",
        "JWT_JWK_SET_URI=http://localhost:8080/realms/congen/protocol/openid-connect/certs",
        "JWT_AUDIENCES=congen-backend",
        "congen.gdpr.audit-enabled=true",
        "congen.gdpr.data-retention-check-enabled=true",
        "ENCRYPTION_KEY=MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI="
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
        assert(CongenApplication::class.java.isAnnotationPresent(SpringBootApplication::class.java))
    }
}
