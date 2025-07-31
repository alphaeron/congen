package com.congen.service

import com.congen.client.KeycloakClient
import com.congen.dal.UserDAL
import com.congen.exceptions.ValidationException
import com.congen.model.User
import com.congen.model.WeightUnit
import com.congen.util.UnitConverter
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * Service for user business logic: validation, conversion, and DAL operations.
 * Handles creation, retrieval, update, and deletion of users.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class UserService(
    private val userDAL: UserDAL,
    private val unitConverter: UnitConverter,
    private val keycloakClient: KeycloakClient
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserService::class.java)
    }

    /**
     * Creates a new user after validation and unit conversion.
     * @throws ValidationException if validation fails
     */
    fun createUser(
        name: String,
        age: Int,
        height: BigDecimal,
        weight: BigDecimal,
        unit: String?,
        email: String,
        password: String
    ): Mono<User> {
        logger.info("Creating user: {}", name)
        return Mono.fromCallable {
            val weightUnit = WeightUnit.fromString(unit)
            ValidationUtil.validateUserWeightWithUnit(weight, weightUnit, unitConverter)
        }
            .flatMap { weightInKg ->
                val nameParts = name.split(" ", limit = 2)
                val firstName = nameParts.firstOrNull() ?: name
                val lastName = nameParts.getOrNull(1) ?: ""
                keycloakClient.createUser(
                    username = email,
                    email = email,
                    firstName = firstName,
                    lastName = lastName,
                    password = password
                ).flatMap { keycloakUserId ->
                    userDAL.insertUser(name, age, height, weightInKg, keycloakUserId)
                }
            }
            .doOnSuccess { logger.debug("Created user with id: {}", it.id) }
            .doOnError { e -> logger.error("Error creating user: {}", name, e) }
    }

    /**
     * Retrieves a user by ID.
     */
    fun getUserById(id: Int): Mono<User> {
        return userDAL.selectUserById(id)
            .doOnSuccess { logger.debug("Found user: {}", id) }
            .doOnError { e -> logger.error("Error getting user: {}", id, e) }
    }

    /**
     * Retrieves a user by their Keycloak user ID.
     */
    fun getUserByKeycloakUserId(keycloakUserId: String): Mono<User> {
        return userDAL.selectUserByKeycloakUserId(keycloakUserId)
            .doOnSuccess { logger.debug("Found user by Keycloak ID: {}", keycloakUserId) }
            .doOnError { e -> logger.error("Error getting user by Keycloak ID: {}", keycloakUserId, e) }
    }

    /**
     * Retrieves all users.
     */
    fun getAllUsers(): Mono<List<User>> {
        logger.debug("Getting all users")
        return userDAL.selectUsers()
    }

    /**
     * Updates an existing user after validation and unit conversion.
     * @throws ValidationException if validation fails
     */
    fun updateUser(
        id: Int,
        name: String,
        age: Int,
        height: BigDecimal,
        weight: BigDecimal,
        unit: String?
    ): Mono<User> {
        logger.info("Updating user: {}", id)
        return Mono.fromCallable {
            val weightUnit = WeightUnit.fromString(unit)
            ValidationUtil.validateUserWeightWithUnit(weight, weightUnit, unitConverter)
        }
            .flatMap { weightInKg ->
                userDAL.updateUser(id, name, age, height, weightInKg)
            }
            .doOnSuccess { logger.debug("Updated user: {}", id) }
            .doOnError { e -> logger.error("Error updating user: {}", id, e) }
    }

    /**
     * Deletes a user by ID.
     */
    fun deleteUser(id: Int): Mono<User> {
        logger.info("Deleting user: {}", id)
        return userDAL.selectUserById(id)
            .flatMap { user ->
                user.keycloakUserId?.let { keycloakUserId ->
                    // Delete from Keycloak first, then from database
                    keycloakClient.deleteUser(keycloakUserId)
                        .then(userDAL.deleteUser(id))
                } ?: userDAL.deleteUser(id)
            }
            .doOnSuccess { logger.debug("Deleted user: {}", id) }
            .doOnError { e -> logger.error("Error deleting user: {}", id, e) }
    }
}
