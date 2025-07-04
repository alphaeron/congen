package com.congen.controllers

import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.model.ProgrammedWorkout
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
@RequestMapping("/programmed-workouts")
class ProgrammedWorkoutController(
    private val programmedWorkoutDAL: ProgrammedWorkoutDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ProgrammedWorkoutController::class.java)
    }

    @PostMapping("/")
    fun save(
        @RequestBody programmedWorkout: ProgrammedWorkout,
    ): ResponseEntity<*> {
        logger.info("Saving programmed workout: {}", programmedWorkout.name)
        return try {
            ResponseEntity.ok(
                programmedWorkoutDAL.insertProgrammedWorkout(programmedWorkout),
            )
        } catch (e: Exception) {
            logger.error("Error saving programmed workout: {}", programmedWorkout.name, e)
            throw e
        }
    }

    @GetMapping("/{id}")
    fun get(
        @PathVariable("id") id: Long,
    ): Mono<ResponseEntity<ProgrammedWorkout>> {
        return programmedWorkoutDAL.selectProgrammedWorkoutById(id)
            .map {
                logger.debug("Found programmed workout: {}", id)
                ResponseEntity.ok(it)
            }
            .doOnError { e ->
                logger.error("Error getting programmed workout: {}", id, e)
            }
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all programmed workouts")
        return try {
            ResponseEntity.ok(
                programmedWorkoutDAL.selectProgrammedWorkouts(),
            )
        } catch (e: Exception) {
            logger.error("Error getting all programmed workouts", e)
            throw e
        }
    }

    @GetMapping("/program/{programId}")
    fun getByProgramId(
        @PathVariable("programId") programId: Long,
    ): ResponseEntity<*> {
        logger.debug("Getting programmed workouts for program: {}", programId)
        return try {
            ResponseEntity.ok(
                programmedWorkoutDAL.selectProgrammedWorkoutsByProgramId(programId),
            )
        } catch (e: Exception) {
            logger.error("Error getting programmed workouts for program: {}", programId, e)
            throw e
        }
    }

    @PutMapping("/")
    fun update(
        @RequestBody programmedWorkout: ProgrammedWorkout,
    ): ResponseEntity<*> {
        logger.info("Updating programmed workout: {}", programmedWorkout.id)
        return try {
            ResponseEntity.ok(
                programmedWorkoutDAL.updateProgrammedWorkout(programmedWorkout),
            )
        } catch (e: Exception) {
            logger.error("Error updating programmed workout: {}", programmedWorkout.id, e)
            throw e
        }
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable("id") id: Long,
    ): ResponseEntity<*> {
        logger.info("Deleting programmed workout: {}", id)
        return try {
            ResponseEntity.ok(
                programmedWorkoutDAL.deleteProgrammedWorkout(id),
            )
        } catch (e: Exception) {
            logger.error("Error deleting programmed workout: {}", id, e)
            throw e
        }
    }
}
