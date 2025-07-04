package com.congen.controllers

import com.congen.dal.UserExercisePreferenceDAL
import com.congen.model.UserExercisePreference
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
        return try {
            ResponseEntity.ok(
                userExercisePreferenceDAL.insertUserExercisePreference(userExercisePreference),
            )
        } catch (e: Exception) {
            logger.error(
                "Error saving user exercise preference: {} - {}",
                userExercisePreference.userId,
                userExercisePreference.exerciseName,
                e,
            )
            throw e
        }
    }

    @GetMapping("/{userId}")
    fun getByUser(
        @PathVariable("userId") userId: Int,
    ): Mono<ResponseEntity<List<UserExercisePreference>>> {
        return userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)
            .map {
                logger.debug("Found exercise preferences for user: {}", userId)
                ResponseEntity.ok(it)
            }
            .doOnError { e ->
                logger.error("Error getting user exercise preferences for user: {}", userId, e)
            }
    }

    @PostMapping("/update")
    fun update(
        @RequestBody userExercisePreference: UserExercisePreference,
    ): ResponseEntity<*> {
        logger.info("Updating user exercise preference: {} - {}", userExercisePreference.userId, userExercisePreference.exerciseName)
        return try {
            ResponseEntity.ok(
                userExercisePreferenceDAL.updateUserExercisePreference(userExercisePreference),
            )
        } catch (e: Exception) {
            logger.error(
                "Error updating user exercise preference: {} - {}",
                userExercisePreference.userId,
                userExercisePreference.exerciseName,
                e,
            )
            throw e
        }
    }

    @PostMapping("/delete")
    fun delete(
        @RequestBody userExercisePreference: UserExercisePreference,
    ): ResponseEntity<*> {
        logger.info("Deleting user exercise preference: {} - {}", userExercisePreference.userId, userExercisePreference.exerciseName)
        return try {
            ResponseEntity.ok(
                userExercisePreferenceDAL.deleteUserExercisePreference(userExercisePreference.userId, userExercisePreference.exerciseName),
            )
        } catch (e: Exception) {
            logger.error(
                "Error deleting user exercise preference: {} - {}",
                userExercisePreference.userId,
                userExercisePreference.exerciseName,
                e,
            )
            throw e
        }
    }
}
