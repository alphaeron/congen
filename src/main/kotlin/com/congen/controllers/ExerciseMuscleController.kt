package com.congen.controllers

import com.congen.dal.ExerciseMuscleDAL
import com.congen.exceptions.NoResultsFoundException
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
@RequestMapping("/exercise_muscle")
class ExerciseMuscleController(
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExerciseMuscleController::class.java)
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all exercise muscle relationships")
        return try {
            ResponseEntity.ok(
                exerciseMuscleDAL.selectAllExerciseMuscle(),
            )
        } catch (e: Exception) {
            logger.error("Error getting all exercise muscle relationships", e)
            throw e
        }
    }

    @GetMapping("/exercise/{exerciseName}/muscle/{muscleName}")
    fun getExerciseMuscle(
        @PathVariable exerciseName: String,
        @PathVariable muscleName: String,
    ): Mono<ResponseEntity<ExerciseMuscle>> {
        return exerciseMuscleDAL.selectExerciseMuscle(exerciseName, muscleName)
            .map {
                logger.debug("Found exercise muscle relationship: {} - {}", exerciseName, muscleName)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Exercise muscle relationship not found: {} - {}", exerciseName, muscleName)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting exercise muscle relationship: {} - {}", exerciseName, muscleName, e)
            }
    }

    @PostMapping("/")
    fun save(
        @RequestBody exerciseMuscle: ExerciseMuscle,
    ): ResponseEntity<*> {
        logger.info("Saving exercise muscle relationship: {} - {}", exerciseMuscle.exerciseName, exerciseMuscle.muscleName)
        return try {
            ResponseEntity.ok(
                exerciseMuscleDAL.insertExerciseMuscle(exerciseMuscle),
            )
        } catch (e: Exception) {
            logger.error("Error saving exercise muscle relationship: {} - {}", exerciseMuscle.exerciseName, exerciseMuscle.muscleName, e)
            throw e
        }
    }
}
