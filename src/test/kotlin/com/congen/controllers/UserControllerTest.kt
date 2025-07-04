package com.congen.controllers

import com.congen.dal.UserDAL
import com.congen.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal

class UserControllerTest {
    private lateinit var userDAL: UserDAL
    private lateinit var userController: UserController

    @BeforeEach
    fun setUp() {
        userDAL = mock()
        userController = UserController(userDAL)
    }

    @Test
    fun `save should return saved user`() {
        val user = User(name = "John Doe", age = 30, height = BigDecimal("180.5"), weight = BigDecimal("75.0"))
        whenever(userDAL.insertUser(user)).thenReturn(Mono.just(user))
        val result = userController.save(user)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<User>).expectNext(user).verifyComplete()
        verify(userDAL).insertUser(user)
    }

    @Test
    fun `get should return user when found`() {
        val user = User(id = 1, name = "John Doe", age = 30, height = BigDecimal("180.5"), weight = BigDecimal("75.0"))
        whenever(userDAL.selectUserById(1)).thenReturn(Mono.just(user))
        val result = userController.get(1)
        StepVerifier.create(result).expectNext(ResponseEntity.ok(user)).verifyComplete()
        verify(userDAL).selectUserById(1)
    }

    @Test
    fun `getAll should return all users`() {
        val users = listOf(User(id = 1, name = "John Doe", age = 30, height = BigDecimal("180.5"), weight = BigDecimal("75.0")))
        whenever(userDAL.selectUsers()).thenReturn(Mono.just(users))
        val result = userController.getAll()
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<User>>).expectNext(users).verifyComplete()
        verify(userDAL).selectUsers()
    }

    @Test
    fun `update should return updated user`() {
        val user = User(id = 1, name = "John Doe", age = 31, height = BigDecimal("180.5"), weight = BigDecimal("75.0"))
        whenever(userDAL.updateUser(user)).thenReturn(Mono.just(user))
        val result = userController.update(user)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<User>).expectNext(user).verifyComplete()
        verify(userDAL).updateUser(user)
    }

    @Test
    fun `delete should return deleted user`() {
        val user = User(id = 1, name = "John Doe", age = 30, height = BigDecimal("180.5"), weight = BigDecimal("75.0"))
        whenever(userDAL.deleteUser(1)).thenReturn(Mono.just(user))
        val result = userController.delete(1)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<User>).expectNext(user).verifyComplete()
        verify(userDAL).deleteUser(1)
    }
}
