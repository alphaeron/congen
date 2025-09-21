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
        val now = Instant.now()
        val expectedUser =
            User(
                keycloakId = "test-keycloak-user-id",
                name = "Test User",
                age = null,
                weight = null,
                height = null,
                createdAt = now,
                updatedAt = now
            )
        whenever(userService.insertUser()).thenReturn(Mono.just(expectedUser))

        val result = userController.createUser()

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(expectedUser))
            .verifyComplete()
        verify(userService).insertUser()
    }

    @Test
    fun `createUser should propagate validation error`() {
        val validationException = ValidationException("User name not available from Keycloak token")
        whenever(userService.insertUser()).thenReturn(Mono.error(validationException))

        val result = userController.createUser()

        StepVerifier.create(result)
            .expectError(ValidationException::class.java)
            .verify()
        verify(userService).insertUser()
    }

    @Test
    fun `createUser should propagate database error`() {
        val databaseException = DatabaseException("Database connection failed")
        whenever(userService.insertUser()).thenReturn(Mono.error(databaseException))

        val result = userController.createUser()

        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
        verify(userService).insertUser()
    }

    @Test
    fun `getCurrentUser should return current user profile`() {
        val now = Instant.now()
        val expectedUser =
            User(
                keycloakId = KEYCLOAK_USER_ID,
                name = NAME,
                age = null,
                weight = null,
                height = null,
                createdAt = now,
                updatedAt = now
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(KEYCLOAK_USER_ID))
        whenever(userService.selectUserByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Mono.just(expectedUser))

        val result = userController.getCurrentUser()

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(expectedUser))
            .verifyComplete()
        verify(keycloakUtil).getCurrentUserId()
        verify(userService).selectUserByKeycloakId(KEYCLOAK_USER_ID)
    }

    @Test
    fun `getCurrentUser should return 404 when user not found in database`() {
        val error = NoResultsFoundException("User not found")
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(KEYCLOAK_USER_ID))
        whenever(userService.selectUserByKeycloakId(KEYCLOAK_USER_ID)).thenReturn(Mono.error(error))

        val result = userController.getCurrentUser()

        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(keycloakUtil).getCurrentUserId()
        verify(userService).selectUserByKeycloakId(KEYCLOAK_USER_ID)
    }

    @Test
    fun `getCurrentUser should propagate keycloak error`() {
        val error = RuntimeException("Keycloak error")
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.error(error))

        val result = userController.getCurrentUser()

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
        verify(keycloakUtil).getCurrentUserId()
        verify(userService, never()).selectUserByKeycloakId(any())
    }

    @Test
    fun `updateCurrentUser should update user profile successfully`() {
        val keycloakId = "test-keycloak-id"
        val newName = "Updated User Name"
        val updatedUser =
            User(
                keycloakId = keycloakId,
                name = newName,
                age = null,
                weight = null,
                height = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

        whenever(userService.updateUser(newName, null, null, null)).thenReturn(Mono.just(updatedUser))

        val result = userController.updateCurrentUser(newName, null, null, null)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(updatedUser))
            .verifyComplete()
        verify(userService).updateUser(newName, null, null, null)
    }

    @Test
    fun `updateCurrentUser should propagate validation error`() {
        val newName = "Updated User Name"
        val error = ValidationException("Invalid name")

        whenever(userService.updateUser(newName, null, null, null)).thenReturn(Mono.error(error))

        val result = userController.updateCurrentUser(newName, null, null, null)

        StepVerifier.create(result)
            .expectError(ValidationException::class.java)
            .verify()
        verify(userService).updateUser(newName, null, null, null)
    }

    @Test
    fun `updateCurrentUser should propagate keycloak error`() {
        val newName = "Updated User Name"
        val error = RuntimeException("Keycloak error")

        whenever(userService.updateUser(newName, null, null, null)).thenReturn(Mono.error(error))

        val result = userController.updateCurrentUser(newName, null, null, null)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
        verify(userService).updateUser(newName, null, null, null)
    }
}
