package com.congen.controllers

import com.congen.dal.UserEquipmentDAL
import com.congen.model.UserEquipment
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDateTime

/**
 * Unit tests for UserEquipmentController.
 *
 * These tests verify the REST API endpoints for user equipment operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UserEquipmentControllerTest {
    private lateinit var userEquipmentDAL: UserEquipmentDAL
    private lateinit var userEquipmentController: UserEquipmentController

    @BeforeEach
    fun setUp() {
        userEquipmentDAL = mock()
        userEquipmentController = UserEquipmentController(userEquipmentDAL)
    }

    @Test
    fun `save should return created user equipment`() {
        val userId = 1
        val equipmentName = "Barbell"
        val now = LocalDateTime.now()
        val userEquipment = UserEquipment(
            userId = userId,
            equipmentName = equipmentName,
            createdAt = now
        )
        val savedUserEquipment = userEquipment
        whenever(userEquipmentDAL.insertUserEquipment(userId, equipmentName)).thenReturn(Mono.just(savedUserEquipment))

        val result = userEquipmentController.save(userId, equipmentName)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserEquipment>)
            .expectNext(savedUserEquipment)
            .verifyComplete()

        verify(userEquipmentDAL).insertUserEquipment(userId, equipmentName)
    }

    @Test
    fun `getByUser should return user equipment when found`() {
        val userId = 1
        val now = LocalDateTime.now()
        val userEquipment = listOf(
            UserEquipment(
                userId = userId,
                equipmentName = "Barbell",
                createdAt = now
            ),
            UserEquipment(
                userId = userId,
                equipmentName = "Dumbbells",
                createdAt = now
            )
        )

        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.just(userEquipment))

        val result = userEquipmentController.getByUser(userId)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userEquipment))
            .verifyComplete()

        verify(userEquipmentDAL).selectUserEquipmentByUser(userId)
    }

    @Test
    fun `delete should return deleted user equipment`() {
        val userId = 1
        val equipmentName = "Barbell"
        val now = LocalDateTime.now()
        val userEquipment = UserEquipment(
            userId = userId,
            equipmentName = equipmentName,
            createdAt = now
        )

        whenever(userEquipmentDAL.deleteUserEquipment(userId, equipmentName)).thenReturn(Mono.just(userEquipment))

        val result = userEquipmentController.delete(userEquipment)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserEquipment>)
            .expectNext(userEquipment)
            .verifyComplete()

        verify(userEquipmentDAL).deleteUserEquipment(userId, equipmentName)
    }

    @Test
    fun `should handle DAL error gracefully for save`() {
        val userId = 1
        val equipmentName = "Barbell"

        whenever(userEquipmentDAL.insertUserEquipment(userId, equipmentName)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userEquipmentController.save(userId, equipmentName)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserEquipment>)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for getByUser`() {
        val userId = 1

        whenever(userEquipmentDAL.selectUserEquipmentByUser(userId)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userEquipmentController.getByUser(userId)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for delete`() {
        val userId = 1
        val equipmentName = "Barbell"
        val now = LocalDateTime.now()
        val userEquipment = UserEquipment(
            userId = userId,
            equipmentName = equipmentName,
            createdAt = now
        )

        whenever(userEquipmentDAL.deleteUserEquipment(userId, equipmentName)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userEquipmentController.delete(userEquipment)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserEquipment>)
            .expectError(RuntimeException::class.java)
            .verify()
    }
}
