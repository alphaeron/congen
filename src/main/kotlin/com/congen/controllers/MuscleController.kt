package com.congen.controllers

import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.MuscleDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.ExerciseMuscle
import com.congen.model.Muscle
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
@RequestMapping("/muscle")
class MuscleController(
    private val muscleDAL: MuscleDAL,
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(MuscleController::class.java)
    }

    @PostMapping("/")
    fun save(
        @RequestBody muscle: Muscle,
    ): ResponseEntity<*> {
        logger.info("Saving muscle: {}", muscle.name)
        return ResponseEntity.ok(
            muscleDAL.insertMuscle(muscle),
        )
    }

    @GetMapping("/{name}")
    fun get(
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<Muscle>> {
        return muscleDAL.selectMuscleByName(name)
            .map {
                logger.debug("Found muscle: {}", name)
                ResponseEntity.ok(it)
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Muscle not found: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting muscle: {}", name, e)
            }
    }

    @GetMapping("/{name}/exercise")
    fun getExercise(
        @PathVariable("name") name: String,
    ): Mono<ResponseEntity<List<ExerciseMuscle>>> {
        // First check if the muscle exists
        return muscleDAL.selectMuscleByName(name)
            .flatMap { _ ->
                // Muscle exists, now get its exercises
                exerciseMuscleDAL.selectExerciseMuscleByMuscle(name)
                    .flatMap { exercises ->
                        if (exercises.isEmpty()) {
                            logger.warn("No exercises found for muscle: {}", name)
                            Mono.just(ResponseEntity.notFound().build())
                        } else {
                            logger.debug("Found {} exercises for muscle: {}", exercises.size, name)
                            Mono.just(ResponseEntity.ok(exercises))
                        }
                    }
            }
            .onErrorResume(NoResultsFoundException::class.java) {
                logger.warn("Muscle not found: {}", name)
                Mono.just(ResponseEntity.notFound().build())
            }
            .doOnError { e ->
                logger.error("Error getting exercises for muscle: {}", name, e)
            }
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all muscles")
        return ResponseEntity.ok(
            muscleDAL.selectMuscles(),
        )
    }
}
