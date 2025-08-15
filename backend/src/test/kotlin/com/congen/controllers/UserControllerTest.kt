package com.congen.controllers

import com.congen.exceptions.DatabaseException
import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import com.congen.model.User
import com.congen.service.UserService
import com.congen.util.KeycloakUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

@TestPropertySource(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration"
    ]
)
class UserControllerTest {
    private lateinit var userService: UserService
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var userController: UserController

    companion object {
        private const val KEYCLOAK_USER_ID = "test-keycloak-user-id"
        private const val KEYCLOAK_USER_ID_2 = "test-keycloak-user-id-2"
        private const val NON_EXISTENT_KEYCLOAK_USER_ID = "non-existent-keycloak-user-id"
        private const val NAME = "John Doe"
        private const val JANE_NAME = "Jane Smith"
    }

    @BeforeEach
    fun setUp() {
        userService = mock()
        keycloakUtil = mock()
        userController = UserController(userService, keycloakUtil)
    }

    @Test
    fun `createUser should create user profile from Keycloak information successfully`() {
        // Given
        val now = Instant.now()
        val expectedUser =
            User(
                keycloakId = "test-keycloak-user-id",
                name = "Test User",
                createdAt = now,
                updatedAt = now
            )
        whenever(userService.createUser()).thenReturn(Mono.just(expectedUser))

        // When
        val result = userController.createUser()

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(expectedUser))
            .verifyComplete()
        verify(userService).createUser()
    }

    @Test
    fun `createUser should propagate validation error`() {
        // Given
        val validationException = ValidationException("User name not available from Keycloak token")
        whenever(userService.createUser()).thenReturn(Mono.error(validationException))

        // When
        val result = userController.createUser()

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.badRequest().build<User>())
            .verifyComplete()
        verify(userService).createUser()
    }

    @Test
    fun `createUser should propagate database error`() {
        // Given
        val databaseException = DatabaseException("Database connection failed")
        whenever(userService.createUser()).thenReturn(Mono.error(databaseException))

        // When
        val result = userController.createUser()

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.internalServerError().build<User>())
            .verifyComplete()
        verify(userService).createUser()
    }

    @Test
    fun `getCurrentUser should return current user profile`() {
        // Given
        val now = Instant.now()
        val expectedUser =
            User(
                keycloakId = KEYCLOAK_USER_ID,
                name = NAME,
                createdAt = now,
                updatedAt = now
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(KEYCLOAK_USER_ID))
        whenever(userService.getUserByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Mono.just(expectedUser))

        // When
        val result = userController.getCurrentUser()

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(expectedUser))
            .verifyComplete()
        verify(keycloakUtil).getCurrentUserId()
        verify(userService).getUserByKeycloakId(KEYCLOAK_USER_ID)
    }

    @Test
    fun `getCurrentUser should return 404 when user not found in database`() {
        // Given
        val error = NoResultsFoundException("User not found")
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(KEYCLOAK_USER_ID))
        whenever(userService.getUserByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Mono.error(error))

        // When
        val result = userController.getCurrentUser()

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build<User>())
            .verifyComplete()
        verify(keycloakUtil).getCurrentUserId()
        verify(userService).getUserByKeycloakId(KEYCLOAK_USER_ID)
    }

    @Test
    fun `getCurrentUser should propagate keycloak error`() {
        // Given
        val error = RuntimeException("Keycloak error")
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.error(error))

        // When
        val result = userController.getCurrentUser()

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.internalServerError().build<User>())
            .verifyComplete()
        verify(keycloakUtil).getCurrentUserId()
        verify(userService, never()).getUserByKeycloakId(any())
    }
}
