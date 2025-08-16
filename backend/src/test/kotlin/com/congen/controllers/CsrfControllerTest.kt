package com.congen.controllers

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Unit tests for CsrfController.
 *
 * Tests CSRF token endpoint functionality and security.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class CsrfControllerTest {

    private lateinit var csrfController: CsrfController
    private lateinit var mockExchange: ServerWebExchange
    private lateinit var mockCsrfToken: CsrfToken

    @BeforeEach
    fun setUp() {
        csrfController = CsrfController()
        mockExchange = mock()
        mockCsrfToken = mock()
    }

    @Test
    fun `should return CSRF token when available`() {
        // Given
        val tokenValue = "test-csrf-token"
        whenever(mockCsrfToken.token).thenReturn(tokenValue)
        whenever(mockExchange.getAttribute<CsrfToken>(CsrfToken::class.java.name)).thenReturn(mockCsrfToken)

        // When
        val result = csrfController.getCsrfToken(mockExchange)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.OK &&
                response.body?.get("token") == tokenValue
            }
            .verifyComplete()
    }

    @Test
    fun `should return error when CSRF token is not available`() {
        // Given
        whenever(mockExchange.getAttribute<CsrfToken?>(CsrfToken::class.java.name)).thenReturn(null)

        // When
        val result = csrfController.getCsrfToken(mockExchange)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR &&
                response.body?.get("error") == "CSRF token not available"
            }
            .verifyComplete()
    }

    @Test
    fun `should handle null CSRF token attribute`() {
        // Given
        whenever(mockExchange.getAttribute<CsrfToken?>(CsrfToken::class.java.name)).thenReturn(null)

        // When
        val result = csrfController.getCsrfToken(mockExchange)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { response ->
                response.statusCode == HttpStatus.INTERNAL_SERVER_ERROR &&
                response.body?.get("error") == "CSRF token not available"
            }
            .verifyComplete()
    }
}
