package com.congen.controllers

import com.congen.dal.UserDAL
import com.congen.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDateTime
import kotlin.test.assertEquals

/**
 * Unit tests for UserController.
 *
 * These tests verify the REST API endpoints for user operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UserControllerTest {
    @Mock
    private lateinit var userDAL: UserDAL

    private lateinit var userController: UserController

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        userController = UserController(userDAL)
    }

    @Test
    fun `save should return created user`() {
        val now = LocalDateTime.now()
        val name = "John Doe"
        val age = 30
        val height = BigDecimal("180.5")
        val weight = BigDecimal("75.0")
        val user = User(id = 0, name = name, age = age, height = height, weight = weight, createdAt = now, updatedAt = now)
        val savedUser = user.copy(id = 1)
        whenever(userDAL.insertUser(name, age, height, weight)).thenReturn(Mono.just(savedUser))
        val result = userController.save(name, age, height, weight)
        StepVerifier.create(result).expectNext(ResponseEntity.ok(savedUser)).verifyComplete()
        verify(userDAL).insertUser(name, age, height, weight)
    }

    @Test
    fun `get should return user when found`() {
        val now = LocalDateTime.now()
        val user = User(id = 1, name = "John Doe", age = 30, height = BigDecimal("180.5"), weight = BigDecimal("75.0"), createdAt = now, updatedAt = now)
        whenever(userDAL.selectUserById(1)).thenReturn(Mono.just(user))
        val result = userController.get(1)
        StepVerifier.create(result).expectNext(ResponseEntity.ok(user)).verifyComplete()
        verify(userDAL).selectUserById(1)
    }

    @Test
    fun `getAll should return all users`() {
        val now = LocalDateTime.now()
        val users = listOf(User(id = 1, name = "John Doe", age = 30, height = BigDecimal("180.5"), weight = BigDecimal("75.0"), createdAt = now, updatedAt = now))
        whenever(userDAL.selectUsers()).thenReturn(Mono.just(users))
        val result = userController.getAll()
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(users))
            .verifyComplete()
        verify(userDAL).selectUsers()
    }

    @Test
    fun `update should return updated user`() {
        val now = LocalDateTime.now()
        val name = "John Doe"
        val age = 31
        val height = BigDecimal("180.5")
        val weight = BigDecimal("75.0")
        val user = User(id = 0, name = name, age = age, height = height, weight = weight, createdAt = now, updatedAt = now)
        val updatedUser = user.copy(id = 1)
        whenever(userDAL.updateUser(1, name, age, height, weight)).thenReturn(Mono.just(updatedUser))
        val result = userController.update(1, name, age, height, weight)
        assertEquals(ResponseEntity.ok(Mono.just(updatedUser)), result)
        verify(userDAL).updateUser(1, name, age, height, weight)
    }

    @Test
    fun `delete should return deleted user`() {
        val now = LocalDateTime.now()
        val user = User(id = 1, name = "John Doe", age = 30, height = BigDecimal("180.5"), weight = BigDecimal("75.0"), createdAt = now, updatedAt = now)
        whenever(userDAL.deleteUser(1)).thenReturn(Mono.just(user))
        val result = userController.delete(1)
        assertEquals(ResponseEntity.ok(Mono.just(user)), result)
        verify(userDAL).deleteUser(1)
    }

    @Test
    fun `should get all users`() {
        val now = LocalDateTime.now()
        val users = listOf(
            User(
                id = 1,
                name = "John Doe",
                age = 30,
                height = BigDecimal("180.5"),
                weight = BigDecimal("75.0"),
                createdAt = now,
                updatedAt = now
            ),
            User(
                id = 2,
                name = "Jane Smith",
                age = 25,
                height = BigDecimal("165.0"),
                weight = BigDecimal("60.0"),
                createdAt = now,
                updatedAt = now
            )
        )

        whenever(userDAL.selectUsers()).thenReturn(Mono.just(users))

        val result = userController.getAll()

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(users))
            .verifyComplete()
    }

    @Test
    fun `should get user by id`() {
        val now = LocalDateTime.now()
        val user = User(
            id = 1,
            name = "John Doe",
            age = 30,
            height = BigDecimal("180.5"),
            weight = BigDecimal("75.0"),
            createdAt = now,
            updatedAt = now
        )

        whenever(userDAL.selectUserById(1)).thenReturn(Mono.just(user))

        val result = userController.get(1)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(user))
            .verifyComplete()
    }

    @Test
    fun `should return error when user not found`() {
        whenever(userDAL.selectUserById(999)).thenReturn(Mono.error(RuntimeException("Not found")))

        val result = userController.get(999)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should create user`() {
        val now = LocalDateTime.now()
        val name = "John Doe"
        val age = 30
        val height = BigDecimal("180.5")
        val weight = BigDecimal("75.0")
        val user = User(
            id = 0,
            name = name,
            age = age,
            height = height,
            weight = weight,
            createdAt = now,
            updatedAt = now
        )
        val savedUser = user.copy(id = 1)
        whenever(userDAL.insertUser(name, age, height, weight)).thenReturn(Mono.just(savedUser))

        val result = userController.save(name, age, height, weight)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(savedUser))
            .verifyComplete()
    }

    @Test
    fun `should update user`() {
        val now = LocalDateTime.now()
        val id = 1
        val name = "John Doe"
        val age = 30
        val height = BigDecimal("180.5")
        val weight = BigDecimal("75.0")
        val user = User(
            id = id,
            name = name,
            age = age,
            height = height,
            weight = weight,
            createdAt = now,
            updatedAt = now
        )
        whenever(userDAL.updateUser(id, name, age, height, weight)).thenReturn(Mono.just(user))

        val result = userController.update(id, name, age, height, weight)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<User>)
            .expectNext(user)
            .verifyComplete()
    }

    @Test
    fun `should return error when updating non-existent user`() {
        whenever(userDAL.updateUser(999, "John Doe", 30, BigDecimal("180.5"), BigDecimal("75.0"))).thenReturn(Mono.error(RuntimeException("Not found")))

        val result = userController.update(999, "John Doe", 30, BigDecimal("180.5"), BigDecimal("75.0"))

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<User>)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should delete user`() {
        val now = LocalDateTime.now()
        val user = User(
            id = 1,
            name = "John Doe",
            age = 30,
            height = BigDecimal("180.5"),
            weight = BigDecimal("75.0"),
            createdAt = now,
            updatedAt = now
        )
        whenever(userDAL.deleteUser(1)).thenReturn(Mono.just(user))

        val result = userController.delete(1)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<User>)
            .expectNext(user)
            .verifyComplete()
    }

    @Test
    fun `should return error when deleting non-existent user`() {
        whenever(userDAL.deleteUser(999)).thenReturn(Mono.error(RuntimeException("Not found")))

        val result = userController.delete(999)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<User>)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully`() {
        whenever(userDAL.selectUsers()).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userController.getAll()

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }
}
