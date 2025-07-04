package com.congen.controllers

import com.congen.dal.SetSchemeDAL
import com.congen.model.SetScheme
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/set-schemes")
class SetSchemeController(
    private val setSchemeDAL: SetSchemeDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(SetSchemeController::class.java)
    }

    @PostMapping("/")
    fun save(
        @RequestBody setScheme: SetScheme,
    ): Mono<ResponseEntity<SetScheme>> {
        logger.info("Saving set scheme for exercise: {}, set: {}", setScheme.programmedExerciseId, setScheme.setNumber)
        return setSchemeDAL.insertSetScheme(setScheme)
            .map { savedScheme ->
                logger.debug("Saved set scheme with id: {}", savedScheme.id)
                ResponseEntity.ok(savedScheme)
            }
            .doOnError { e ->
                logger.error("Error saving set scheme for exercise: {}, set: {}", setScheme.programmedExerciseId, setScheme.setNumber, e)
            }
    }

    @GetMapping("/{id}")
    fun get(
        @PathVariable("id") id: Long,
    ): Mono<ResponseEntity<SetScheme>> {
        return setSchemeDAL.selectSetSchemeById(id)
            .map {
                logger.debug("Found set scheme: {}", id)
                ResponseEntity.ok(it)
            }
            .doOnError { e ->
                logger.error("Error getting set scheme: {}", id, e)
            }
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all set schemes")
        return try {
            ResponseEntity.ok(
                setSchemeDAL.selectSetSchemes(),
            )
        } catch (e: Exception) {
            logger.error("Error getting all set schemes", e)
            throw e
        }
    }

    @GetMapping("/exercise/{programmedExerciseId}")
    fun getByProgrammedExerciseId(
        @PathVariable("programmedExerciseId") programmedExerciseId: Long,
    ): ResponseEntity<*> {
        logger.debug("Getting set schemes for programmed exercise: {}", programmedExerciseId)
        return try {
            ResponseEntity.ok(
                setSchemeDAL.selectSetSchemesByProgrammedExerciseId(programmedExerciseId),
            )
        } catch (e: Exception) {
            logger.error("Error getting set schemes for programmed exercise: {}", programmedExerciseId, e)
            throw e
        }
    }

    @PatchMapping("/")
    fun update(
        @RequestBody setScheme: SetScheme,
    ): ResponseEntity<*> {
        logger.info("Updating set scheme: {}", setScheme.id)
        return try {
            ResponseEntity.ok(
                setSchemeDAL.updateSetScheme(setScheme),
            )
        } catch (e: Exception) {
            logger.error("Error updating set scheme: {}", setScheme.id, e)
            throw e
        }
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable("id") id: Long,
    ): ResponseEntity<*> {
        logger.info("Deleting set scheme: {}", id)
        return try {
            ResponseEntity.ok(
                setSchemeDAL.deleteSetScheme(id),
            )
        } catch (e: Exception) {
            logger.error("Error deleting set scheme: {}", id, e)
            throw e
        }
    }
}
