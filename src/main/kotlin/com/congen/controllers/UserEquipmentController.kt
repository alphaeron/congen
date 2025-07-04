package com.congen.controllers

import com.congen.dal.UserEquipmentDAL
import com.congen.model.UserEquipment
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/user_equipment")
class UserEquipmentController(
    private val userEquipmentDAL: UserEquipmentDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserEquipmentController::class.java)
    }

    @PostMapping("/")
    fun save(
        @RequestBody userEquipment: UserEquipment,
    ): ResponseEntity<*> {
        logger.info("Saving user equipment: {} - {}", userEquipment.userId, userEquipment.equipmentName)
        return try {
            ResponseEntity.ok(
                userEquipmentDAL.insertUserEquipment(userEquipment),
            )
        } catch (e: Exception) {
            logger.error("Error saving user equipment: {} - {}", userEquipment.userId, userEquipment.equipmentName, e)
            throw e
        }
    }

    @GetMapping("/{userId}")
    fun getByUser(
        @PathVariable("userId") userId: Int,
    ): Mono<ResponseEntity<List<UserEquipment>>> {
        return userEquipmentDAL.selectUserEquipmentByUser(userId)
            .map {
                logger.debug("Found equipment for user: {}", userId)
                ResponseEntity.ok(it)
            }
            .doOnError { e ->
                logger.error("Error getting user equipment for user: {}", userId, e)
            }
    }

    @PostMapping("/delete")
    fun delete(
        @RequestBody userEquipment: UserEquipment,
    ): ResponseEntity<*> {
        logger.info("Deleting user equipment: {} - {}", userEquipment.userId, userEquipment.equipmentName)
        return try {
            ResponseEntity.ok(
                userEquipmentDAL.deleteUserEquipment(userEquipment.userId, userEquipment.equipmentName),
            )
        } catch (e: Exception) {
            logger.error("Error deleting user equipment: {} - {}", userEquipment.userId, userEquipment.equipmentName, e)
            throw e
        }
    }
}
