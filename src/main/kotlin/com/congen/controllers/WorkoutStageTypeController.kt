package com.congen.controllers

import com.congen.dal.WorkoutStageTypeDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.WorkoutStageType
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/workout-stage-type")
class WorkoutStageTypeController(
    private val workoutStageTypeDAL: WorkoutStageTypeDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(WorkoutStageTypeController::class.java)
    }

    @GetMapping("/{id}")
    fun get(
        @PathVariable("id") id: Int,
    ): Mono<ResponseEntity<WorkoutStageType>> {
        return workoutStageTypeDAL.selectWorkoutStageTypeById(id)
            .map {
                logger.debug("Found workout stage type: {}", id)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Workout stage type not found: {}", id)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting workout stage type: {}", id, e)
            }
    }

    @GetMapping("/name/{name}")
    fun getByName(
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<WorkoutStageType>> {
        return workoutStageTypeDAL.selectWorkoutStageTypeByName(name)
            .map {
                logger.debug("Found workout stage type by name: {}", name)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Workout stage type not found by name: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting workout stage type by name: {}", name, e)
            }
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all workout stage types")
        return try {
            ResponseEntity.ok(
                workoutStageTypeDAL.selectWorkoutStageTypes(),
            )
        } catch (e: Exception) {
            logger.error("Error getting all workout stage types", e)
            throw e
        }
    }
}
