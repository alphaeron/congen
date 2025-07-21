package com.congen.controllers

import com.congen.dal.UserEquipmentDAL
import com.congen.exceptions.DatabaseQueryException
import com.congen.mockUserEquipment
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
import java.time.Instant

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

    companion object {
        private const val USER_ID = 1
        private const val EQUIPMENT_NAME = "Barbell"
        private const val DUMBBELLS = "Dumbbells"
    }

    @BeforeEach
    fun setUp() {
        userEquipmentDAL = mock()
        userEquipmentController = UserEquipmentController(userEquipmentDAL)
    }

    @Test
    fun `save should return created user equipment`() {
        val now = Instant.now()
        val userEquipment = mockUserEquipment(userId = USER_ID, equipmentName = EQUIPMENT_NAME, createdAt = now)
        whenever(userEquipmentDAL.insertUserEquipment(USER_ID, EQUIPMENT_NAME)).thenReturn(Mono.just(userEquipment))
        val result = userEquipmentController.save(USER_ID, EQUIPMENT_NAME)
        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<UserEquipment>)
            .expectNext(userEquipment)
            .verifyComplete()
        verify(userEquipmentDAL).insertUserEquipment(USER_ID, EQUIPMENT_NAME)
    }

    @Test
    fun `getByUser should return user equipment when found`() {
        val now = Instant.now()
        val userEquipment = mockUserEquipment(userId = USER_ID, equipmentName = EQUIPMENT_NAME, createdAt = now)
        val userEquipmentList =
            listOf(
                userEquipment,
                mockUserEquipment(userId = USER_ID, equipmentName = DUMBBELLS, createdAt = now)
            )
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.just(userEquipmentList))
        val result = userEquipmentController.getByUser(USER_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userEquipmentList))
            .verifyComplete()
        verify(userEquipmentDAL).selectUserEquipmentByUser(USER_ID)
    }

    @Test
    fun `delete should return deleted user equipment`() {
        val now = Instant.now()
        val userEquipment = mockUserEquipment(userId = USER_ID, equipmentName = EQUIPMENT_NAME, createdAt = now)
        whenever(userEquipmentDAL.deleteUserEquipment(USER_ID, EQUIPMENT_NAME)).thenReturn(Mono.just(userEquipment))
        val result = userEquipmentController.delete(userEquipment)
        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<UserEquipment>)
            .expectNext(userEquipment)
            .verifyComplete()
        verify(userEquipmentDAL).deleteUserEquipment(USER_ID, EQUIPMENT_NAME)
    }

    @Test
    fun `should handle DAL error gracefully for save`() {
        whenever(userEquipmentDAL.insertUserEquipment(USER_ID, EQUIPMENT_NAME))
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = userEquipmentController.save(USER_ID, EQUIPMENT_NAME)
        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<UserEquipment>)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for getByUser`() {
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID))
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = userEquipmentController.getByUser(USER_ID)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for delete`() {
        val now = Instant.now()
        val userEquipment = mockUserEquipment(userId = USER_ID, equipmentName = EQUIPMENT_NAME, createdAt = now)
        whenever(userEquipmentDAL.deleteUserEquipment(USER_ID, EQUIPMENT_NAME))
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = userEquipmentController.delete(userEquipment)
        assert(result.statusCode == HttpStatus.OK)
        StepVerifier.create(result.body as Mono<UserEquipment>)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }
}
