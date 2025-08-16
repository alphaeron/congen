package com.congen.controllers

import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

/**
 * Controller for CSRF token management.
 *
 * This controller provides CSRF tokens to the frontend for CSRF protection.
 * It's only active when CSRF protection is enabled (production/staging).
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/csrf")
class CsrfController {

    /**
     * Provides CSRF token to the frontend.
     *
     * This endpoint returns the current CSRF token that the frontend
     * should include in state-changing requests (POST, PUT, DELETE).
     * Only authenticated users can access this endpoint.
     *
     * @param exchange The server web exchange containing the CSRF token
     * @return CSRF token response
     */
    @GetMapping("/")
    @PreAuthorize("isAuthenticated()")
    fun getCsrfToken(exchange: ServerWebExchange): Mono<ResponseEntity<Map<String, String>>> {
        val csrfToken = exchange.getAttribute(CsrfToken::class.java.name) as? CsrfToken
        
        return if (csrfToken != null) {
            Mono.just(ResponseEntity.ok(mapOf("token" to csrfToken.token)))
        } else {
            // This should not happen when CSRF protection is enabled
            // Return an error response instead of empty token
            Mono.just(ResponseEntity.status(500).body(mapOf("error" to "CSRF token not available")))
        }
    }
}
