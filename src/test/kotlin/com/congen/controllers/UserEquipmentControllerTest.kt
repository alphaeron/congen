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

class UserEquipmentControllerTest {
    private lateinit var userEquipmentDAL: UserEquipmentDAL
    private lateinit var userEquipmentController: UserEquipmentController

    @BeforeEach
    fun setUp() {
        userEquipmentDAL = mock()
        userEquipmentController = UserEquipmentController(userEquipmentDAL)
    }

    @Test
    fun `save should return saved user equipment`() {
        val userEquipment = UserEquipment(userId = 1, equipmentName = "Barbell")
        whenever(
            userEquipmentDAL.insertUserEquipment(userEquipment.userId, userEquipment.equipmentName)
        ).thenReturn(Mono.just(userEquipment))
        val result = userEquipmentController.save(userEquipment.userId, userEquipment.equipmentName)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserEquipment>).expectNext(userEquipment).verifyComplete()
        verify(userEquipmentDAL).insertUserEquipment(userEquipment.userId, userEquipment.equipmentName)
    }

    @Test
    fun `getByUser should return user equipment when found`() {
        val userEquipmentList = listOf(UserEquipment(userId = 1, equipmentName = "Barbell"))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(1)).thenReturn(Mono.just(userEquipmentList))
        val result = userEquipmentController.getByUser(1)
        StepVerifier.create(result).expectNext(ResponseEntity.ok(userEquipmentList)).verifyComplete()
        verify(userEquipmentDAL).selectUserEquipmentByUser(1)
    }

    @Test
    fun `delete should return deleted user equipment`() {
        val userEquipment = UserEquipment(userId = 1, equipmentName = "Barbell")
        whenever(userEquipmentDAL.deleteUserEquipment(1, "Barbell")).thenReturn(Mono.just(userEquipment))
        val result = userEquipmentController.delete(userEquipment)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserEquipment>).expectNext(userEquipment).verifyComplete()
        verify(userEquipmentDAL).deleteUserEquipment(1, "Barbell")
    }
}
