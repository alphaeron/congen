package com.congen.config

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.test.context.TestPropertySource
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
@TestPropertySource(
    properties = [
        "spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/auth/realms/test",
        "spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/auth/realms/test/protocol/openid-connect/certs",
        "spring.security.oauth2.resourceserver.jwt.audiences=test-client"
    ]
)
class SecurityConfigTest {
    private lateinit var securityConfig: SecurityConfig

    @BeforeEach
    fun setUp() {
        securityConfig = SecurityConfig()
    }

    @Test
    fun `should have correct annotations`() {
        assertTrue(hasWebFluxSecurityAnnotation())
    }

    @Test
    fun `should have springSecurityFilterChain method`() {
        assertNotNull(SecurityConfig::springSecurityFilterChain)
    }

    @Test
    fun `should have SecurityConfig class`() {
        assertNotNull(securityConfig)
    }

    private fun hasWebFluxSecurityAnnotation(): Boolean {
        return SecurityConfig::class.java.isAnnotationPresent(EnableWebFluxSecurity::class.java)
    }
}
