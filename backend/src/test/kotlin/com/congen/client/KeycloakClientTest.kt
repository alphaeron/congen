package com.congen.client

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.quality.Strictness
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class KeycloakClientTest {
    private lateinit var keycloakWebClient: WebClient
    private lateinit var keycloakClient: KeycloakClient

    private val keycloakUrl = "http://localhost:8080"
    private val realm = "congen"
    private val clientId = "congen-service"
    private val serviceAccountUsername = "service-account"
    private val clientSecret = "secret"
    private val managementUrl = "http://localhost:8080/health"
    private val testUserId = "test-user-id"
    private val testAccessToken = "test-access-token"

    @BeforeEach
    fun setUp() {
        keycloakWebClient = mock()
        keycloakClient =
            KeycloakClient(
                keycloakUrl = keycloakUrl,
                realm = realm,
                clientId = clientId,
                serviceAccountUsername = serviceAccountUsername,
                clientSecret = clientSecret,
                managementUrl = managementUrl,
                keycloakWebClient = keycloakWebClient
            )
    }

    @Test
    fun `deleteUser should delete user successfully`() {
        val tokenResponse = KeycloakClient.TokenResponse(testAccessToken)
        val mockRequestHeadersUriSpec = mock<WebClient.RequestHeadersUriSpec<*>>()
        val mockRequestBodyUriSpec = mock<WebClient.RequestBodyUriSpec>()
        val mockRequestBodySpec = mock<WebClient.RequestBodySpec>()
        val mockResponseSpec = mock<WebClient.ResponseSpec>()
        val mockDeleteRequestHeadersUriSpec = mock<WebClient.RequestHeadersUriSpec<*>>()
        val mockDeleteResponseSpec = mock<WebClient.ResponseSpec>()

        // Mock token request
        whenever(keycloakWebClient.post()).thenReturn(mockRequestBodyUriSpec)
        whenever(mockRequestBodyUriSpec.uri(any<String>())).thenReturn(mockRequestBodySpec)
        whenever(mockRequestBodySpec.header(any<String>(), any<String>())).thenReturn(mockRequestBodySpec)
        whenever(mockRequestBodySpec.bodyValue(any<String>())).thenReturn(mockRequestBodySpec)
        whenever(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec)
        whenever(mockResponseSpec.bodyToMono(KeycloakClient.TokenResponse::class.java))
            .thenReturn(Mono.just(tokenResponse))

        // Mock delete request
        whenever(keycloakWebClient.delete()).thenReturn(mockDeleteRequestHeadersUriSpec)
        whenever(mockDeleteRequestHeadersUriSpec.uri(any<String>())).thenReturn(mockDeleteRequestHeadersUriSpec)
        whenever(mockDeleteRequestHeadersUriSpec.header(any<String>(), any<String>())).thenReturn(mockDeleteRequestHeadersUriSpec)
        whenever(mockDeleteRequestHeadersUriSpec.retrieve()).thenReturn(mockDeleteResponseSpec)
        whenever(mockDeleteResponseSpec.toBodilessEntity()).thenReturn(Mono.just(ResponseEntity.ok().build<Void>()))

        val result = keycloakClient.deleteUser(testUserId)

        StepVerifier.create(result)
            .verifyComplete()

        verify(keycloakWebClient).post()
        verify(keycloakWebClient).delete()
    }

    @Test
    fun `deleteUser should handle token request failure`() {
        val mockRequestBodyUriSpec = mock<WebClient.RequestBodyUriSpec>()
        val mockRequestBodySpec = mock<WebClient.RequestBodySpec>()
        val mockResponseSpec = mock<WebClient.ResponseSpec>()

        whenever(keycloakWebClient.post()).thenReturn(mockRequestBodyUriSpec)
        whenever(mockRequestBodyUriSpec.uri(any<String>())).thenReturn(mockRequestBodySpec)
        whenever(mockRequestBodySpec.header(any<String>(), any<String>())).thenReturn(mockRequestBodySpec)
        whenever(mockRequestBodySpec.bodyValue(any<String>())).thenReturn(mockRequestBodySpec)
        whenever(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec)
        whenever(mockResponseSpec.bodyToMono(KeycloakClient.TokenResponse::class.java))
            .thenReturn(Mono.error(RuntimeException("Token request failed")))

        val result = keycloakClient.deleteUser(testUserId)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(keycloakWebClient).post()
    }

    @Test
    fun `deleteUser should handle delete request failure`() {
        val tokenResponse = KeycloakClient.TokenResponse(testAccessToken)
        val mockRequestBodyUriSpec = mock<WebClient.RequestBodyUriSpec>()
        val mockRequestBodySpec = mock<WebClient.RequestBodySpec>()
        val mockResponseSpec = mock<WebClient.ResponseSpec>()
        val mockDeleteRequestHeadersUriSpec = mock<WebClient.RequestHeadersUriSpec<*>>()
        val mockDeleteResponseSpec = mock<WebClient.ResponseSpec>()

        // Mock token request
        whenever(keycloakWebClient.post()).thenReturn(mockRequestBodyUriSpec)
        whenever(mockRequestBodyUriSpec.uri(any<String>())).thenReturn(mockRequestBodySpec)
        whenever(mockRequestBodySpec.header(any<String>(), any<String>())).thenReturn(mockRequestBodySpec)
        whenever(mockRequestBodySpec.bodyValue(any<String>())).thenReturn(mockRequestBodySpec)
        whenever(mockRequestBodySpec.retrieve()).thenReturn(mockResponseSpec)
        whenever(mockResponseSpec.bodyToMono(KeycloakClient.TokenResponse::class.java))
            .thenReturn(Mono.just(tokenResponse))

        // Mock delete request failure
        whenever(keycloakWebClient.delete()).thenReturn(mockDeleteRequestHeadersUriSpec)
        whenever(mockDeleteRequestHeadersUriSpec.uri(any<String>())).thenReturn(mockDeleteRequestHeadersUriSpec)
        whenever(mockDeleteRequestHeadersUriSpec.header(any<String>(), any<String>())).thenReturn(mockDeleteRequestHeadersUriSpec)
        whenever(mockDeleteRequestHeadersUriSpec.retrieve()).thenReturn(mockDeleteResponseSpec)
        whenever(mockDeleteResponseSpec.toBodilessEntity())
            .thenReturn(
                Mono.error(
                    WebClientResponseException.create(404, "Not Found", org.springframework.http.HttpHeaders.EMPTY, ByteArray(0), null)
                )
            )

        val result = keycloakClient.deleteUser(testUserId)

        StepVerifier.create(result)
            .expectError(WebClientResponseException::class.java)
            .verify()

        verify(keycloakWebClient).post()
        verify(keycloakWebClient).delete()
    }

    @Test
    fun `checkHealthLive should return successful health check`() {
        val mockRequestHeadersUriSpec = mock<WebClient.RequestHeadersUriSpec<*>>()
        val mockResponseSpec = mock<WebClient.ResponseSpec>()
        val expectedResponse = ResponseEntity.ok().build<Void>()

        whenever(keycloakWebClient.get()).thenReturn(mockRequestHeadersUriSpec)
        whenever(mockRequestHeadersUriSpec.uri(any<String>())).thenReturn(mockRequestHeadersUriSpec)
        whenever(mockRequestHeadersUriSpec.retrieve()).thenReturn(mockResponseSpec)
        whenever(mockResponseSpec.toBodilessEntity()).thenReturn(Mono.just(expectedResponse))

        val result = keycloakClient.checkHealthLive()

        StepVerifier.create(result)
            .expectNext(expectedResponse)
            .verifyComplete()

        verify(keycloakWebClient).get()
    }

    @Test
    fun `checkHealthLive should handle health check failure`() {
        val mockRequestHeadersUriSpec = mock<WebClient.RequestHeadersUriSpec<*>>()
        val mockResponseSpec = mock<WebClient.ResponseSpec>()

        whenever(keycloakWebClient.get()).thenReturn(mockRequestHeadersUriSpec)
        whenever(mockRequestHeadersUriSpec.uri(any<String>())).thenReturn(mockRequestHeadersUriSpec)
        whenever(mockRequestHeadersUriSpec.retrieve()).thenReturn(mockResponseSpec)
        whenever(mockResponseSpec.toBodilessEntity())
            .thenReturn(
                Mono.error(
                    WebClientResponseException.create(
                        503,
                        "Service Unavailable",
                        org.springframework.http.HttpHeaders.EMPTY,
                        ByteArray(0),
                        null
                    )
                )
            )

        val result = keycloakClient.checkHealthLive()

        StepVerifier.create(result)
            .expectError(WebClientResponseException::class.java)
            .verify()

        verify(keycloakWebClient).get()
    }

    @Test
    fun `TokenResponse should have correct access token`() {
        val tokenResponse = KeycloakClient.TokenResponse(testAccessToken)
        assert(tokenResponse.accessToken == testAccessToken)
    }
}
