package com.congen.service

import com.congen.client.KeycloakClient
import com.congen.dal.UserDAL
import com.congen.exceptions.ValidationException
import com.congen.model.User
import com.congen.util.KeycloakUtil
import com.congen.util.UnitConverter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

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
    private val keycloakClient: KeycloakClient,
    private val keycloakUtil: KeycloakUtil,
    private val gdprComplianceService: GdprComplianceService
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserService::class.java)
    }

    /**
     * Creates a new user profile after Keycloak registration.
     * This method automatically extracts user information from the JWT token
     * and creates their profile in our database. It also automatically creates
     * a consent record with true consent for basic service provision.
     *
     * @return The created user profile
     * @throws ValidationException if validation fails or name is not available
     */
    fun insertUser(): Mono<User> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { keycloakId ->
                keycloakUtil.getCurrentUserName()
                    .switchIfEmpty(Mono.error(ValidationException("User name not available from Keycloak token")))
                    .flatMap { name ->
                        if (name.isNullOrBlank()) {
                            Mono.error(ValidationException("User name not available from Keycloak token"))
                        } else {
                            logger.info("Creating user profile after Keycloak registration: {}", name)

                            userDAL.insertUser(keycloakId, name)
                                .flatMap { user ->
                                    // Automatically create consent record for basic service provision
                                    gdprComplianceService.updateUserConsent(keycloakId, true)
                                        .thenReturn(user)
                                }
                                .doOnSuccess { logger.debug("Created user profile with Keycloak ID: {}", it.keycloakId) }
                                .doOnError { e -> logger.error("Error creating user profile: {}", name, e) }
                        }
                    }
            }
    }

    /**
     * Updates the current user's profile information.
     *
     * This method updates the user's profile in the application database.
     * The user must be authenticated and can only update their own profile.
     *
     * @param name The new name for the user
     * @param age The new age for the user (optional)
     * @param weight The new weight for the user (optional)
     * @param height The new height for the user (optional)
     * @return The updated user
     * @throws ValidationException if user data fails validation
     * @throws NoResultsFoundException if user profile does not exist
     */
    fun updateUser(
        name: String,
        age: Int? = null,
        weight: Int? = null,
        height: Int? = null,
        gender: String? = null
    ): Mono<User> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { keycloakUserId ->
                userDAL.updateUser(keycloakUserId, name, age, weight, height, gender)
            }
    }

    /**
     * Retrieves a user by their Keycloak ID.
     */
    fun selectUserByKeycloakId(keycloakId: String): Mono<User> {
        return userDAL.selectUserByKeycloakId(keycloakId)
            .doOnSuccess { logger.debug("Found user by Keycloak ID: {}", keycloakId) }
            .doOnError { e -> logger.error("Error getting user by Keycloak ID: {}", keycloakId, e) }
    }
}
