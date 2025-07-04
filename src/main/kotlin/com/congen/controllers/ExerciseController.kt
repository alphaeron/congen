package com.congen.controllers

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
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
@RequestMapping("/exercise")
class ExerciseController(
    private val exerciseDAL: ExerciseDAL,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseController::class.java)
    }

    @PostMapping("/")
    fun save(
        @RequestBody exercise: Exercise,
    ): ResponseEntity<*> {
        logger.info("Saving exercise: {}", exercise.name)
        return try {
            ResponseEntity.ok(
                exerciseDAL.insertExercise(exercise),
            )
        } catch (e: Exception) {
            logger.error("Error saving exercise: {}", exercise.name, e)
            throw e
        }
    }

    @GetMapping("/{name}")
    fun get(
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<Exercise>> {
        return exerciseDAL.selectExerciseByName(name)
            .map {
                logger.debug("Found exercise: {}", name)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Exercise not found: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting exercise: {}", name, e)
            }
    }

    @GetMapping("/{name}/muscle")
    fun getMuscle(
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<List<ExerciseMuscle>>> {
        return exerciseDAL.selectExerciseByName(name)
            .flatMap { _ ->
                exerciseMuscleDAL.selectExerciseMuscleByExercise(name)
                    .flatMap { muscles ->
                        if (muscles.isEmpty()) {
                            logger.warn("No muscles found for exercise: {}", name)
                            Mono.just(ResponseEntity.notFound().build())
                        } else {
                            logger.debug("Found {} muscles for exercise: {}", muscles.size, name)
                            Mono.just(ResponseEntity.ok(muscles))
                        }
                    }
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Exercise not found: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting muscles for exercise: {}", name, e)
            }
    }

    @GetMapping("/{name}/equipment")
    fun getEquipment(
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<List<ExerciseEquipment>>> {
        return exerciseDAL.selectExerciseByName(name)
            .flatMap { _ ->
                exerciseEquipmentDAL.selectExerciseEquipmentByExercise(name)
                    .flatMap { equipment ->
                        if (equipment.isEmpty()) {
                            logger.warn("No equipment found for exercise: {}", name)
                            Mono.just(ResponseEntity.notFound().build())
                        } else {
                            logger.debug("Found {} equipment for exercise: {}", equipment.size, name)
                            Mono.just(ResponseEntity.ok(equipment))
                        }
                    }
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Exercise not found: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting equipment for exercise: {}", name, e)
            }
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all exercises")
        return try {
            ResponseEntity.ok(
                exerciseDAL.selectExercises(),
            )
        } catch (e: Exception) {
            logger.error("Error getting all exercises", e)
            throw e
        }
    }
}
