package com.congen.controllers

import com.congen.exceptions.NoResultsFoundException
import com.congen.model.User
import com.congen.service.UserService
import com.congen.util.KeycloakUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant
import org.mockito.kotlin.never
import org.mockito.kotlin.any
import java.time.LocalDateTime
import org.mockito.kotlin.eq

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
        private const val USER_ID = 1
        private const val USER_ID_2 = 2
        private const val NON_EXISTENT_USER_ID = 999
        private const val NAME = "John Doe"
        private const val JANE_NAME = "Jane Smith"
        private const val AGE = 30
        private const val JANE_AGE = 25
        private const val HEIGHT = "180.5"
        private const val JANE_HEIGHT = "165.0"
        private const val WEIGHT = "75.0"
        private const val JANE_WEIGHT = "60.0"
    }

    @BeforeEach
    fun setUp() {
        userService = mock()
        keycloakUtil = mock()
        userController = UserController(userService, keycloakUtil)
    }

    @Test
    fun `save should create user profile after Keycloak registration`() {
        // Given
        val keycloakUserId = "test-keycloak-user-id"
        val name = "John Doe"
        val age = 30
        val height = BigDecimal("175.5")
        val weight = BigDecimal("80.0")
        val unit = "KG"
        val expectedUser = User(
            id = 1,
            name = name,
            age = age,
            height = height,
            weight = BigDecimal("80.0"),
            keycloakUserId = keycloakUserId,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(keycloakUserId))
        whenever(userService.createUser(
            eq(keycloakUserId),
            eq(name),
            eq(age),
            eq(height),
            eq(weight),
            eq(unit)
        )).thenReturn(Mono.just(expectedUser))

        // When
        val result = userController.save(name, age, height, weight, unit)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(expectedUser))
            .verifyComplete()

        verify(keycloakUtil).getCurrentUserId()
        verify(userService).createUser(
            keycloakUserId,
            name,
            age,
            height,
            weight,
            unit
        )
    }

    @Test
    fun `get should return user when found`() {
        val now = Instant.now()
        val user =
            User(
                id = USER_ID,
                name = NAME,
                age = AGE,
                height = BigDecimal(HEIGHT),
                weight = BigDecimal(WEIGHT),
                createdAt = now,
                updatedAt = now,
                keycloakUserId = null
            )

        whenever(userService.getUserById(USER_ID)).thenReturn(Mono.just(user))

        val result = userController.get(USER_ID)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(user))
            .verifyComplete()

        verify(userService).getUserById(USER_ID)
    }

    @Test
    fun `getAll should return all users`() {
        val now = Instant.now()
        val users =
            listOf(
                User(
                    id = USER_ID,
                    name = NAME,
                    age = AGE,
                    height = BigDecimal(HEIGHT),
                    weight = BigDecimal(WEIGHT),
                    createdAt = now,
                    updatedAt = now,
                    keycloakUserId = null
                ),
                User(
                    id = USER_ID_2,
                    name = JANE_NAME,
                    age = JANE_AGE,
                    height = BigDecimal(JANE_HEIGHT),
                    weight = BigDecimal(JANE_WEIGHT),
                    createdAt = now,
                    updatedAt = now,
                    keycloakUserId = null
                )
            )

        whenever(userService.getAllUsers()).thenReturn(Mono.just(users))

        val result = userController.getAll()

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(users))
            .verifyComplete()

        verify(userService).getAllUsers()
    }

    @Test
    fun `update should return updated user`() {
        val now = Instant.now()
        val user =
            User(
                id = USER_ID,
                name = NAME,
                age = AGE,
                height = BigDecimal(HEIGHT),
                weight = BigDecimal(WEIGHT),
                createdAt = now,
                updatedAt = now,
                keycloakUserId = null
            )

        whenever(userService.updateUser(USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT), "KG"))
            .thenReturn(Mono.just(user))

        val result = userController.update(USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT), "KG")

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(user))
            .verifyComplete()

        verify(userService).updateUser(USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT), "KG")
    }

    @Test
    fun `delete should return deleted user`() {
        val now = Instant.now()
        val user =
            User(
                id = USER_ID,
                name = NAME,
                age = AGE,
                height = BigDecimal(HEIGHT),
                weight = BigDecimal(WEIGHT),
                createdAt = now,
                updatedAt = now,
                keycloakUserId = null
            )

        whenever(userService.deleteUser(USER_ID)).thenReturn(Mono.just(user))

        val result = userController.delete(USER_ID)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(user))
            .verifyComplete()

        verify(userService).deleteUser(USER_ID)
    }

    @Test
    fun `should return 404 when user not found`() {
        val error = NoResultsFoundException("Not found")
        whenever(userService.getUserById(NON_EXISTENT_USER_ID)).thenReturn(Mono.error(error))

        val result = userController.get(NON_EXISTENT_USER_ID)

        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()

        verify(userService).getUserById(NON_EXISTENT_USER_ID)
    }

    @Test
    fun `should return 404 when updating non-existent user`() {
        val error = NoResultsFoundException("Not found")
        whenever(userService.updateUser(NON_EXISTENT_USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT), "KG"))
            .thenReturn(Mono.error(error))

        val result = userController.update(NON_EXISTENT_USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT), "KG")

        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()

        verify(userService).updateUser(NON_EXISTENT_USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT), "KG")
    }

    @Test
    fun `should return 404 when deleting non-existent user`() {
        val error = NoResultsFoundException("Not found")
        whenever(userService.deleteUser(NON_EXISTENT_USER_ID)).thenReturn(Mono.error(error))

        val result = userController.delete(NON_EXISTENT_USER_ID)

        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()

        verify(userService).deleteUser(NON_EXISTENT_USER_ID)
    }

    @Test
    fun `getCurrentUser should return current user profile`() {
        // Given
        val keycloakUserId = "test-keycloak-user-id"
        val now = Instant.now()
        val expectedUser = User(
            id = USER_ID,
            name = NAME,
            age = AGE,
            height = BigDecimal(HEIGHT),
            weight = BigDecimal(WEIGHT),
            createdAt = now,
            updatedAt = now,
            keycloakUserId = keycloakUserId
        )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(keycloakUserId))
        whenever(userService.getUserByKeycloakUserId(keycloakUserId)).thenReturn(Mono.just(expectedUser))

        // When
        val result = userController.getCurrentUser()

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(expectedUser))
            .verifyComplete()
        verify(keycloakUtil).getCurrentUserId()
        verify(userService).getUserByKeycloakUserId(keycloakUserId)
    }

    @Test
    fun `getCurrentUser should return 404 when user not found in database`() {
        // Given
        val keycloakUserId = "test-keycloak-user-id"
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(keycloakUserId))
        whenever(userService.getUserByKeycloakUserId(keycloakUserId))
            .thenReturn(Mono.error(NoResultsFoundException("User not found")))

        // When
        val result = userController.getCurrentUser()

        // Then
        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(keycloakUtil).getCurrentUserId()
        verify(userService).getUserByKeycloakUserId(keycloakUserId)
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
            .expectError(RuntimeException::class.java)
            .verify()
        verify(keycloakUtil).getCurrentUserId()
        verify(userService, never()).getUserByKeycloakUserId(any())
    }
}
