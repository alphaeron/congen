package com.congen.controllers

import com.congen.dal.ProgramDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Program
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
@RequestMapping("/program")
class ProgramController(
    private val programDAL: ProgramDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ProgramController::class.java)
    }

    @PostMapping("/")
    fun save(
        @RequestBody program: Program,
    ): Mono<ResponseEntity<Program>> {
        logger.info("Saving program: {}", program.name)
        return programDAL.insertProgram(program)
            .map { savedProgram ->
                logger.debug("Saved program with id: {}", savedProgram.id)
                ResponseEntity.ok(savedProgram)
            }
            .doOnError { e ->
                logger.error("Error saving program: {}", program.name, e)
            }
    }

    @GetMapping("/{id}")
    fun get(
        @PathVariable("id") id: Long,
    ): Mono<ResponseEntity<Program>> {
        return programDAL.selectProgramById(id)
            .map {
                logger.debug("Found program: {}", id)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Program not found: {}", id)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting program: {}", id, e)
            }
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all programs")
        return try {
            ResponseEntity.ok(
                programDAL.selectPrograms(),
            )
        } catch (e: Exception) {
            logger.error("Error getting all programs", e)
            throw e
        }
    }

    @PatchMapping("/{id}")
    fun update(
        @PathVariable("id") id: Long,
        @RequestBody program: Program,
    ): Mono<ResponseEntity<Program>> {
        val updatedProgram = program.copy(id = id)
        return programDAL.updateProgram(updatedProgram)
            .map {
                logger.debug("Updated program: {}", id)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Program not found for update: {}", id)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error updating program: {}", id, e)
            }
    }

    @DeleteMapping("/{id}")
    fun delete(
        @PathVariable("id") id: Long,
    ): Mono<ResponseEntity<Program>> {
        return programDAL.deleteProgram(id)
            .map {
                logger.debug("Deleted program: {}", id)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Program not found for deletion: {}", id)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error deleting program: {}", id, e)
            }
    }
}
