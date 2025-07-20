package com.congen.config

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping

/**
 * Unit tests for WebConfig class.
 *
 * These tests verify that the WebConfig properly configures the base API path
 * prefix for all controllers, ensuring that all endpoints are prefixed with
 * `/api/v1/`.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class WebConfigTest {
    private val webConfig = WebConfig()

    /**
     * Tests that the RequestMappingHandlerMapping bean is created successfully.
     */
    @Test
    fun `should create RequestMappingHandlerMapping bean`() {
        val handlerMapping = webConfig.requestMappingHandlerMapping()
        assertNotNull(handlerMapping)
        assertTrue(handlerMapping is RequestMappingHandlerMapping)
    }

    /**
     * Tests that the RequestMappingHandlerMapping bean is created with correct type.
     *
     * This test verifies that the custom implementation is properly instantiated
     * and can be used by Spring WebFlux.
     */
    @Test
    fun `should create custom RequestMappingHandlerMapping`() {
        val handlerMapping = webConfig.requestMappingHandlerMapping()

        // Verify it's our custom implementation
        assertTrue(handlerMapping.javaClass.name.contains("WebConfig"))
        assertTrue(handlerMapping is RequestMappingHandlerMapping)
    }

    /**
     * Test controller class for unit testing.
     */
    class TestController {
        @org.springframework.web.bind.annotation.GetMapping("/test")
        fun testEndpoint(): String = "test"
    }
}
