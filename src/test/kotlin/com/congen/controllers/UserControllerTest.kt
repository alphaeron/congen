package com.congen.controllers

import com.congen.mockUser
import com.congen.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

/**
 * Unit tests for UserController.
 *
 * These tests verify the REST API endpoints for user operations,
 * including HTTP response construction and delegation to UserService.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UserControllerTest {
    private lateinit var userController: UserController
    private lateinit var userService: UserService

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
        userController = UserController(userService)
    }

    @Test
    fun `save should return created user`() {
        val now = Instant.now()
        val user =
            mockUser(
                id = 0,
                name = NAME,
                age = AGE,
                height = BigDecimal(HEIGHT),
                weight = BigDecimal(WEIGHT),
                createdAt = now,
                updatedAt = now
            )
        val savedUser = user.copy(id = USER_ID)

        whenever(userService.createUser(eq(NAME), eq(AGE), eq(BigDecimal(HEIGHT)), eq(BigDecimal(WEIGHT)), eq("KG")))
            .thenReturn(Mono.just(savedUser))

        val result = userController.save(NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT), "KG")

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(savedUser))
            .verifyComplete()

        verify(userService).createUser(NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT), "KG")
    }

    @Test
    fun `get should return user when found`() {
        val now = Instant.now()
        val user =
            mockUser(
                id = USER_ID,
                name = NAME,
                age = AGE,
                height = BigDecimal(HEIGHT),
                weight = BigDecimal(WEIGHT),
                createdAt = now,
                updatedAt = now
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
                mockUser(
                    id = USER_ID,
                    name = NAME,
                    age = AGE,
                    height = BigDecimal(HEIGHT),
                    weight = BigDecimal(WEIGHT),
                    createdAt = now,
                    updatedAt = now
                ),
                mockUser(
                    id = USER_ID_2,
                    name = JANE_NAME,
                    age = JANE_AGE,
                    height = BigDecimal(JANE_HEIGHT),
                    weight = BigDecimal(JANE_WEIGHT),
                    createdAt = now,
                    updatedAt = now
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
            mockUser(
                id = USER_ID,
                name = NAME,
                age = AGE,
                height = BigDecimal(HEIGHT),
                weight = BigDecimal(WEIGHT),
                createdAt = now,
                updatedAt = now
            )

        whenever(userService.updateUser(eq(USER_ID), eq(NAME), eq(AGE), eq(BigDecimal(HEIGHT)), eq(BigDecimal(WEIGHT)), eq("KG")))
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
            mockUser(
                id = USER_ID,
                name = NAME,
                age = AGE,
                height = BigDecimal(HEIGHT),
                weight = BigDecimal(WEIGHT),
                createdAt = now,
                updatedAt = now
            )

        whenever(userService.deleteUser(USER_ID)).thenReturn(Mono.just(user))

        val result = userController.delete(USER_ID)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(user))
            .verifyComplete()

        verify(userService).deleteUser(USER_ID)
    }

    @Test
    fun `should propagate error when user not found`() {
        val error = RuntimeException("Not found")
        whenever(userService.getUserById(NON_EXISTENT_USER_ID)).thenReturn(Mono.error(error))

        val result = userController.get(NON_EXISTENT_USER_ID)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(userService).getUserById(NON_EXISTENT_USER_ID)
    }

    @Test
    fun `should propagate error when updating non-existent user`() {
        val error = RuntimeException("Not found")
        whenever(
            userService.updateUser(eq(NON_EXISTENT_USER_ID), eq(NAME), eq(AGE), eq(BigDecimal(HEIGHT)), eq(BigDecimal(WEIGHT)), eq("KG"))
        )
            .thenReturn(Mono.error(error))

        val result = userController.update(NON_EXISTENT_USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT), "KG")

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(userService).updateUser(NON_EXISTENT_USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT), "KG")
    }

    @Test
    fun `should propagate error when deleting non-existent user`() {
        val error = RuntimeException("Not found")
        whenever(userService.deleteUser(NON_EXISTENT_USER_ID)).thenReturn(Mono.error(error))

        val result = userController.delete(NON_EXISTENT_USER_ID)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(userService).deleteUser(NON_EXISTENT_USER_ID)
    }

    @Test
    fun `should propagate service error gracefully for getAll`() {
        val error = RuntimeException("Database error")
        whenever(userService.getAllUsers()).thenReturn(Mono.error(error))

        val result = userController.getAll()

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(userService).getAllUsers()
    }
}
