package com.congen.controllers

import com.congen.dal.WorkoutStageDAL
import com.congen.model.WorkoutStage
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
@RequestMapping("/workout-stages")
class WorkoutStageController(
    private val workoutStageDAL: WorkoutStageDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(WorkoutStageController::class.java)
    }

    @PostMapping("/")
    fun save(
        @RequestBody workoutStage: WorkoutStage,
    ): ResponseEntity<*> {
        logger.info("Saving workout stage for workout: {}, position: {}", workoutStage.programmedWorkoutId, workoutStage.position)
        return try {
            ResponseEntity.ok(
                workoutStageDAL.insertWorkoutStage(workoutStage),
            )
        } catch (e: Exception) {
            logger.error(
                "Error saving workout stage for workout: {}, position: {}",
                workoutStage.programmedWorkoutId,
                workoutStage.position,
                e,
            )
            throw e
        }
    }

    @GetMapping("/{id}")
    fun get(
        @PathVariable("id") id: Long,
    ): Mono<ResponseEntity<WorkoutStage>> {
        return workoutStageDAL.selectWorkoutStageById(id)
            .map {
                logger.debug("Found workout stage: {}", id)
                ResponseEntity.ok(it)
            }
            .doOnError { e ->
                logger.error("Error getting workout stage: {}", id, e)
            }
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all workout stages")
        return try {
            ResponseEntity.ok(
                workoutStageDAL.selectWorkoutStages(),
            )
        } catch (e: Exception) {
            logger.error("Error getting all workout stages", e)
            throw e
        }
    }

    @GetMapping("/workout/{programmedWorkoutId}")
    fun getByProgrammedWorkoutId(
        @PathVariable("programmedWorkoutId") programmedWorkoutId: Long,
    ): ResponseEntity<*> {
        logger.debug("Getting workout stages for programmed workout: {}", programmedWorkoutId)
        return try {
            ResponseEntity.ok(
                workoutStageDAL.selectWorkoutStagesByProgrammedWorkoutId(programmedWorkoutId),
            )
        } catch (e: Exception) {
            logger.error("Error getting workout stages for programmed workout: {}", programmedWorkoutId, e)
            throw e
        }
    }

    @PutMapping("/")
    fun update(
        @RequestBody workoutStage: WorkoutStage,
    ): ResponseEntity<*> {
        logger.info("Updating workout stage: {}", workoutStage.id)
        return try {
            ResponseEntity.ok(
                workoutStageDAL.updateWorkoutStage(workoutStage),
            )
        } catch (e: Exception) {
            logger.error("Error updating workout stage: {}", workoutStage.id, e)
            throw e
        }
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable("id") id: Long,
    ): ResponseEntity<*> {
        logger.info("Deleting workout stage: {}", id)
        return try {
            ResponseEntity.ok(
                workoutStageDAL.deleteWorkoutStage(id),
            )
        } catch (e: Exception) {
            logger.error("Error deleting workout stage: {}", id, e)
            throw e
        }
    }
}
