package com.congen.controllers

import com.congen.dal.EquipmentDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Equipment
import com.congen.model.ExerciseEquipment
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
@RequestMapping("/equipment")
class EquipmentController(
    private val equipmentDAL: EquipmentDAL,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(EquipmentController::class.java)
    }

    @PostMapping("/")
    fun save(
        @RequestBody equipment: Equipment,
    ): ResponseEntity<*> {
        logger.info("Saving equipment: {}", equipment.name)
        return try {
            ResponseEntity.ok(
                equipmentDAL.insertEquipment(equipment),
            )
        } catch (e: Exception) {
            logger.error("Error saving equipment: {}", equipment.name, e)
            throw e
        }
    }

    @GetMapping("/{name}")
    fun get(
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<Equipment>> {
        return equipmentDAL.selectEquipmentByName(name)
            .map {
                logger.debug("Found equipment: {}", name)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Equipment not found: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting equipment: {}", name, e)
            }
    }

    @GetMapping("/{name}/exercise")
    fun getExercise(
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<List<ExerciseEquipment>>> {
        // First check if the equipment exists
        return equipmentDAL.selectEquipmentByName(name)
            .flatMap { _ ->
                // Equipment exists, now get its exercises
                exerciseEquipmentDAL.selectExerciseEquipmentByEquipment(name)
                    .flatMap { exercises ->
                        if (exercises.isEmpty()) {
                            logger.warn("No exercises found for equipment: {}", name)
                            Mono.just(ResponseEntity.notFound().build())
                        } else {
                            logger.debug("Found {} exercises for equipment: {}", exercises.size, name)
                            Mono.just(ResponseEntity.ok(exercises))
                        }
                    }
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Equipment not found: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting exercises for equipment: {}", name, e)
            }
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all equipment")
        return try {
            ResponseEntity.ok(
                equipmentDAL.selectEquipment(),
            )
        } catch (e: Exception) {
            logger.error("Error getting all equipment", e)
            throw e
        }
    }
}
