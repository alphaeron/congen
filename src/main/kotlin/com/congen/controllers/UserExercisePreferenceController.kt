package com.congen.controllers

import com.congen.dal.UserExercisePreferenceDAL
import com.congen.model.UserExercisePreference
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
@RequestMapping("/user_exercise_preference")
class UserExercisePreferenceController(
    private val userExercisePreferenceDAL: UserExercisePreferenceDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserExercisePreferenceController::class.java)
    }

    @PostMapping("/")
    fun save(
        @RequestBody userExercisePreference: UserExercisePreference,
    ): ResponseEntity<*> {
        logger.info("Saving user exercise preference: {} - {}", userExercisePreference.userId, userExercisePreference.exerciseName)
        return ResponseEntity.ok(
            userExercisePreferenceDAL.insertUserExercisePreference(userExercisePreference),
        )
    }

    @GetMapping("/{userId}")
    fun getByUser(
        @PathVariable("userId") userId: Int,
    ): Mono<ResponseEntity<List<UserExercisePreference>>> {
        return userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)
            .map { preferences ->
                logger.debug("Found exercise preferences for user: {}", userId)
                ResponseEntity.ok(preferences)
            }
            .doOnError { e ->
                logger.error("Error getting user exercise preferences for user: {}", userId, e)
            }
    }

    @PatchMapping("/")
    fun update(
        @RequestBody userExercisePreference: UserExercisePreference,
    ): ResponseEntity<*> {
        logger.info("Updating user exercise preference: {} - {}", userExercisePreference.userId, userExercisePreference.exerciseName)
        return ResponseEntity.ok(
            userExercisePreferenceDAL.updateUserExercisePreference(userExercisePreference),
        )
    }

    @DeleteMapping("/")
    fun delete(
        @RequestBody userExercisePreference: UserExercisePreference,
    ): ResponseEntity<*> {
        logger.info("Deleting user exercise preference: {} - {}", userExercisePreference.userId, userExercisePreference.exerciseName)
        return ResponseEntity.ok(
            userExercisePreferenceDAL.deleteUserExercisePreference(userExercisePreference.userId, userExercisePreference.exerciseName),
        )
    }
}
