package com.congen.controllers

import com.congen.dal.UserProgramPreferencesDAL
import com.congen.model.UserProgramPreferences
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
@RequestMapping("/user-program-preferences")
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
        return ResponseEntity.ok(
            userProgramPreferencesDAL.insertUserProgramPreferences(userProgramPreferences),
        )
    }

    @GetMapping("/{userId}")
    fun get(
        @PathVariable("userId") userId: Int,
    ): Mono<ResponseEntity<UserProgramPreferences>> {
        return userProgramPreferencesDAL.selectUserProgramPreferences(userId)
            .map {
                logger.debug("Found user program preferences: {}", userId)
                ResponseEntity.ok(it)
            }
            .doOnError { e ->
                logger.error("Error getting user program preferences: {}", userId, e)
            }
    }

    @PatchMapping("/")
    fun update(
        @RequestBody userProgramPreferences: UserProgramPreferences,
    ): ResponseEntity<*> {
        logger.info("Updating user program preferences: {}", userProgramPreferences.userId)
        return ResponseEntity.ok(
            userProgramPreferencesDAL.updateUserProgramPreferences(userProgramPreferences),
        )
    }

    @DeleteMapping("/{userId}")
    fun delete(
        @PathVariable("userId") userId: Int,
    ): ResponseEntity<*> {
        logger.info("Deleting user program preferences: {}", userId)
        return ResponseEntity.ok(
            userProgramPreferencesDAL.deleteUserProgramPreferences(userId),
        )
    }
}
