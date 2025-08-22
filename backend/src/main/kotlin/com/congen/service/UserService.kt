package com.congen.service

import com.congen.client.KeycloakClient
import com.congen.dal.UserDAL
import com.congen.dal.UserProgramPreferencesDAL
import com.congen.exceptions.ValidationException
import com.congen.model.User
import com.congen.util.KeycloakUtil
import com.congen.util.UnitConverter
import com.congen.util.ValidationUtil
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
    private val userProgramPreferencesDAL: UserProgramPreferencesDAL,
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
        logger.info("Creating user profile from Keycloak information")

        return keycloakUtil.getCurrentUserId()
            .flatMap { keycloakId ->
                keycloakUtil.getCurrentUserName()
                    .switchIfEmpty(Mono.error(ValidationException("User name not available from Keycloak token")))
                    .flatMap { name ->
                        if (name.isNullOrBlank()) {
                            Mono.error(ValidationException("User name not available from Keycloak token"))
                        } else {
                            logger.info("Creating user profile after Keycloak registration: {}", name)

                            // Validate user name
                            ValidationUtil.validateUserName(name)

                            userDAL.insertUser(keycloakId, name)
                                .flatMap { user ->
                                    // Automatically create consent record for basic service provision
                                    gdprComplianceService.updateUserConsent(keycloakId, true)
                                        .then(
                                            // Create default program preferences (4 days per week, 60 minutes per session)
                                            userProgramPreferencesDAL.insertUserProgramPreferences(
                                                keycloakId,
                                                4,
                                                60,
                                            )
                                        )
                                        .thenReturn(user)
                                }
                                .doOnSuccess { logger.debug("Created user profile with Keycloak ID: {}", it.keycloakId) }
                                .doOnError { e -> logger.error("Error creating user profile: {}", name, e) }
                        }
                    }
            }
            .doOnSuccess { user ->
                if (user != null) {
                    logger.debug("Created user profile from Keycloak with ID: {}", user.keycloakId)
                }
            }
            .doOnError { e -> logger.error("Error creating user profile from Keycloak", e) }
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
