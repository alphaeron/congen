package com.congen.controllers

import com.congen.dal.ProgrammedExerciseDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.ProgrammedExercise
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/programmed-exercise")
class ProgrammedExerciseController(
    private val programmedExerciseDAL: ProgrammedExerciseDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ProgrammedExerciseController::class.java)
    }

    @PostMapping("/")
    fun save(
        @RequestBody programmedExercise: ProgrammedExercise,
    ): ResponseEntity<*> {
        logger.info("Saving programmed exercise: {} for stage: {}", programmedExercise.exerciseName, programmedExercise.workoutStageId)
        return try {
            ResponseEntity.ok(
                programmedExerciseDAL.insertProgrammedExercise(programmedExercise),
            )
        } catch (e: Exception) {
            logger.error(
                "Error saving programmed exercise: {} for stage: {}",
                programmedExercise.exerciseName,
                programmedExercise.workoutStageId,
                e,
            )
            throw e
        }
    }

    @GetMapping("/{id}")
    fun get(
        @PathVariable("id") id: Long,
    ): Mono<ResponseEntity<ProgrammedExercise>> {
        return programmedExerciseDAL.selectProgrammedExerciseById(id)
            .map {
                logger.debug("Found programmed exercise: {}", id)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Programmed exercise not found: {}", id)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting programmed exercise: {}", id, e)
            }
    }

    @GetMapping("/stage/{workoutStageId}")
    fun getByStage(
        @PathVariable("workoutStageId") workoutStageId: Long,
    ): ResponseEntity<*> {
        logger.debug("Getting programmed exercises for stage: {}", workoutStageId)
        return try {
            ResponseEntity.ok(
                programmedExerciseDAL.selectProgrammedExercisesByWorkoutStageId(workoutStageId),
            )
        } catch (e: Exception) {
            logger.error("Error getting programmed exercises for stage: {}", workoutStageId, e)
            throw e
        }
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all programmed exercises")
        return try {
            ResponseEntity.ok(
                programmedExerciseDAL.selectProgrammedExercises(),
            )
        } catch (e: Exception) {
            logger.error("Error getting all programmed exercises", e)
            throw e
        }
    }

    @PutMapping("/{id}")
    fun update(
        @PathVariable("id") id: Long,
        @RequestBody programmedExercise: ProgrammedExercise,
    ): Mono<ResponseEntity<ProgrammedExercise>> {
        val updatedProgrammedExercise = programmedExercise.copy(id = id)
        return programmedExerciseDAL.updateProgrammedExercise(updatedProgrammedExercise)
            .map {
                logger.debug("Updated programmed exercise: {}", id)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Programmed exercise not found for update: {}", id)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error updating programmed exercise: {}", id, e)
            }
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable("id") id: Long,
    ): Mono<ResponseEntity<ProgrammedExercise>> {
        return programmedExerciseDAL.deleteProgrammedExercise(id)
            .map {
                logger.debug("Deleted programmed exercise: {}", id)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Programmed exercise not found for deletion: {}", id)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error deleting programmed exercise: {}", id, e)
            }
    }
}
