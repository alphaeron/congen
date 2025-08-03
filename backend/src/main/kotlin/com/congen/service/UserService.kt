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
     * Creates a new user profile after Keycloak registration.
     * This method is called after a user has registered through Keycloak
     * and we need to create their profile in our database.
     *
     * @param keycloakId The Keycloak user ID
     * @param name The user's full name
     * @param age The user's age in years
     * @param height The user's height in centimeters
     * @param weight The user's weight in kilograms
     * @param unit The weight unit (optional, defaults to KG)
     * @return The created user profile
     * @throws ValidationException if validation fails
     */
    fun createUser(
        keycloakId: String,
        name: String,
        age: Int,
        height: BigDecimal,
        weight: BigDecimal,
        unit: String?
    ): Mono<User> {
        logger.info("Creating user profile after Keycloak registration: {}", name)
        return Mono.fromCallable {
            val weightUnit = WeightUnit.fromString(unit)
            ValidationUtil.validateUserWeightWithUnit(weight, weightUnit, unitConverter)
        }
            .flatMap { weightInKg ->
                userDAL.insertUser(keycloakId, name, age, height, weightInKg)
            }
            .doOnSuccess { logger.debug("Created user profile with Keycloak ID: {}", it.keycloakId) }
            .doOnError { e -> logger.error("Error creating user profile: {}", name, e) }
    }

    /**
     * Retrieves a user by their Keycloak ID.
     */
    fun getUserByKeycloakId(keycloakId: String): Mono<User> {
        return userDAL.selectUserByKeycloakId(keycloakId)
            .doOnSuccess { logger.debug("Found user by Keycloak ID: {}", keycloakId) }
            .doOnError { e -> logger.error("Error getting user by Keycloak ID: {}", keycloakId, e) }
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
        keycloakId: String,
        name: String,
        age: Int,
        height: BigDecimal,
        weight: BigDecimal,
        unit: String?
    ): Mono<User> {
        logger.info("Updating user with Keycloak ID: {}", keycloakId)
        return Mono.fromCallable {
            val weightUnit = WeightUnit.fromString(unit)
            ValidationUtil.validateUserWeightWithUnit(weight, weightUnit, unitConverter)
        }
            .flatMap { weightInKg ->
                userDAL.updateUser(keycloakId, name, age, height, weightInKg)
            }
            .doOnSuccess { logger.debug("Updated user with Keycloak ID: {}", keycloakId) }
            .doOnError { e -> logger.error("Error updating user with Keycloak ID: {}", keycloakId, e) }
    }

    /**
     * Deletes a user by their Keycloak ID.
     */
    fun deleteUser(keycloakId: String): Mono<User> {
        logger.info("Deleting user with Keycloak ID: {}", keycloakId)
        return userDAL.selectUserByKeycloakId(keycloakId)
            .flatMap {
                // Delete from Keycloak first, then from database
                keycloakClient.deleteUser(keycloakId)
                    .then(userDAL.deleteUser(keycloakId))
            }
            .doOnSuccess { logger.debug("Deleted user with Keycloak ID: {}", keycloakId) }
            .doOnError { e -> logger.error("Error deleting user with Keycloak ID: {}", keycloakId, e) }
    }
}
