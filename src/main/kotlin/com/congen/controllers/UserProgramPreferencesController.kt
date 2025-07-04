package com.congen.controllers

import com.congen.dal.UserProgramPreferencesDAL
import com.congen.model.UserProgramPreferences
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
@RequestMapping("/user_program_preferences")
class UserProgramPreferencesController(
    private val userProgramPreferencesDAL: UserProgramPreferencesDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserProgramPreferencesController::class.java)
    }

    @PostMapping("/")
    fun save(
        @RequestBody userProgramPreferences: UserProgramPreferences,
    ): ResponseEntity<*> {
        logger.info("Saving user program preferences: {}", userProgramPreferences.userId)
        return try {
            ResponseEntity.ok(
                userProgramPreferencesDAL.insertUserProgramPreferences(userProgramPreferences),
            )
        } catch (e: Exception) {
            logger.error("Error saving user program preferences: {}", userProgramPreferences.userId, e)
            throw e
        }
    }

    @GetMapping("/{userId}")
    fun get(
        @PathVariable("userId") userId: Int,
    ): Mono<ResponseEntity<UserProgramPreferences>> {
        return userProgramPreferencesDAL.selectUserProgramPreferences(userId)
            .map {
                logger.debug("Found program preferences for user: {}", userId)
                ResponseEntity.ok(it)
            }
            .doOnError { e ->
                logger.error("Error getting user program preferences for user: {}", userId, e)
            }
    }

    @PostMapping("/update")
    fun update(
        @RequestBody userProgramPreferences: UserProgramPreferences,
    ): ResponseEntity<*> {
        logger.info("Updating user program preferences: {}", userProgramPreferences.userId)
        return try {
            ResponseEntity.ok(
                userProgramPreferencesDAL.updateUserProgramPreferences(userProgramPreferences),
            )
        } catch (e: Exception) {
            logger.error("Error updating user program preferences: {}", userProgramPreferences.userId, e)
            throw e
        }
    }

    @PostMapping("/delete/{userId}")
    fun delete(
        @PathVariable("userId") userId: Int,
    ): ResponseEntity<*> {
        logger.info("Deleting user program preferences: {}", userId)
        return try {
            ResponseEntity.ok(
                userProgramPreferencesDAL.deleteUserProgramPreferences(userId),
            )
        } catch (e: Exception) {
            logger.error("Error deleting user program preferences: {}", userId, e)
            throw e
        }
    }
}
