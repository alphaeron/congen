package com.congen.controllers

import com.congen.dal.UserDAL
import com.congen.mockUser
import com.congen.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

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
        MockitoAnnotations.openMocks(this)
        userController = UserController(userDAL)
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
        whenever(userDAL.insertUser(NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT))).thenReturn(Mono.just(savedUser))
        val result = userController.save(NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT))
        StepVerifier.create(result).expectNext(ResponseEntity.ok(savedUser)).verifyComplete()
        verify(userDAL).insertUser(NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT))
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
        whenever(userDAL.selectUserById(USER_ID)).thenReturn(Mono.just(user))
        val result = userController.get(USER_ID)
        StepVerifier.create(result).expectNext(ResponseEntity.ok(user)).verifyComplete()
        verify(userDAL).selectUserById(USER_ID)
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
        whenever(userDAL.selectUsers()).thenReturn(Mono.just(users))
        val result = userController.getAll()
        StepVerifier.create(result).expectNext(ResponseEntity.ok(users)).verifyComplete()
        verify(userDAL).selectUsers()
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
        whenever(userDAL.updateUser(USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT))).thenReturn(Mono.just(user))
        val result = userController.update(USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT))
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<User>
        StepVerifier.create(body).expectNext(user).verifyComplete()
        verify(userDAL).updateUser(USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT))
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
        whenever(userDAL.deleteUser(USER_ID)).thenReturn(Mono.just(user))
        val result = userController.delete(USER_ID)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<User>
        StepVerifier.create(body).expectNext(user).verifyComplete()
        verify(userDAL).deleteUser(USER_ID)
    }

    @Test
    fun `should return error when user not found`() {
        whenever(userDAL.selectUserById(NON_EXISTENT_USER_ID)).thenReturn(Mono.error(RuntimeException("Not found")))
        val result = userController.get(NON_EXISTENT_USER_ID)
        StepVerifier.create(result).expectError(RuntimeException::class.java).verify()
    }

    @Test
    fun `should return error when updating non-existent user`() {
        whenever(
            userDAL.updateUser(NON_EXISTENT_USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT))
        ).thenReturn(Mono.error(RuntimeException("Not found")))
        val result = userController.update(NON_EXISTENT_USER_ID, NAME, AGE, BigDecimal(HEIGHT), BigDecimal(WEIGHT))
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<User>
        StepVerifier.create(body).expectError(RuntimeException::class.java).verify()
    }

    @Test
    fun `should return error when deleting non-existent user`() {
        whenever(userDAL.deleteUser(NON_EXISTENT_USER_ID)).thenReturn(Mono.error(RuntimeException("Not found")))
        val result = userController.delete(NON_EXISTENT_USER_ID)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<User>
        StepVerifier.create(body).expectError(RuntimeException::class.java).verify()
    }

    @Test
    fun `should handle DAL error gracefully for getAll`() {
        whenever(userDAL.selectUsers()).thenReturn(Mono.error(RuntimeException("Database error")))
        val result = userController.getAll()
        StepVerifier.create(result).expectError(RuntimeException::class.java).verify()
    }
}
