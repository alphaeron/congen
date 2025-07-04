package com.congen.controllers

import com.congen.dal.UserDAL
import com.congen.model.User
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
@RequestMapping("/user")
class UserController(
    private val userDAL: UserDAL,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserController::class.java)
    }

    @PostMapping("/")
    fun save(
        @RequestBody user: User,
    ): ResponseEntity<*> {
        logger.info("Saving user: {}", user.name)
        return try {
            ResponseEntity.ok(
                userDAL.insertUser(user),
            )
        } catch (e: Exception) {
            logger.error("Error saving user: {}", user.name, e)
            throw e
        }
    }

    @GetMapping("/{id}")
    fun get(
        @PathVariable("id") id: Int,
    ): Mono<ResponseEntity<User>> {
        return userDAL.selectUserById(id)
            .map {
                logger.debug("Found user: {}", id)
                ResponseEntity.ok(it)
            }
            .doOnError { e ->
                logger.error("Error getting user: {}", id, e)
            }
    }

    @GetMapping("/")
    fun getAll(): ResponseEntity<*> {
        logger.debug("Getting all users")
        return try {
            ResponseEntity.ok(
                userDAL.selectUsers(),
            )
        } catch (e: Exception) {
            logger.error("Error getting all users", e)
            throw e
        }
    }

    @PostMapping("/update")
    fun update(
        @RequestBody user: User,
    ): ResponseEntity<*> {
        logger.info("Updating user: {}", user.id)
        return try {
            ResponseEntity.ok(
                userDAL.updateUser(user),
            )
        } catch (e: Exception) {
            logger.error("Error updating user: {}", user.id, e)
            throw e
        }
    }

    @PostMapping("/delete/{id}")
    fun delete(
        @PathVariable("id") id: Int,
    ): ResponseEntity<*> {
        logger.info("Deleting user: {}", id)
        return try {
            ResponseEntity.ok(
                userDAL.deleteUser(id),
            )
        } catch (e: Exception) {
            logger.error("Error deleting user: {}", id, e)
            throw e
        }
    }
}
