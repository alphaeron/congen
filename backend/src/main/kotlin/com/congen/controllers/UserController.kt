package com.congen.controllers

import com.congen.exceptions.DatabaseException
import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import com.congen.model.User
import com.congen.service.UserService
import com.congen.util.KeycloakUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for user management operations.
 *
 * Provides endpoints for creating, retrieving, updating, and managing user profiles.
 * All operations require proper authentication and authorization.
 *
 * @param userService Service for user operations
 * @param keycloakUtil Utility for Keycloak operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user")
@Tag(name = "User Management", description = "APIs for managing user profiles")
class UserController(
    private val userService: UserService,
    private val keycloakUtil: KeycloakUtil
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserController::class.java)
    }

    /**
     * Creates a new user profile after Keycloak registration.
     *
     * This endpoint allows public user registration. It creates a user profile
     * in the application database linked to the authenticated user's Keycloak ID.
     * The user must be authenticated and the profile will be linked to their Keycloak user ID.
     * User information (name) is automatically extracted from the JWT token.
     *
     * @return The created user with assigned ID and timestamps
     *
     * @throws ValidationException if user data fails validation or name is not available
     * @throws DatabaseException if database operation fails
     */
    @PostMapping("/")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create user profile after Keycloak registration",
        description =
            "Creates a user profile using information automatically extracted from the JWT token. " +
                "The user must be authenticated and the profile will be linked to their Keycloak user ID."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User profile created successfully",
                content = [Content(schema = Schema(implementation = User::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - validation error or name not available from token"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - user not authenticated"
            ),
            ApiResponse(
                responseCode = "409",
                description = "Conflict - user profile already exists"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun createUser(): Mono<ResponseEntity<User>> {
        return userService.insertUser()
            .map { ResponseEntity.ok(it) }
    }

    /**
     * Retrieves the current authenticated user's profile.
     *
     * This endpoint allows authenticated users to retrieve their own profile information.
     * The user must be authenticated and can only access their own profile.
     *
     * @return The current user's profile
     *
     * @throws NoResultsFoundException if user profile does not exist
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get current user profile",
        description =
            "Retrieves the current authenticated user's profile. " +
                "The user must be authenticated and can only access their own profile."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Current user profile retrieved successfully",
                content = [Content(schema = Schema(implementation = User::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - user not authenticated"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - user profile does not exist"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getCurrentUser(): Mono<ResponseEntity<User>> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { keycloakUserId ->
                userService.selectUserByKeycloakId(keycloakUserId)
            }
            .map { ResponseEntity.ok(it) }
    }

    /**
     * Updates the current authenticated user's profile.
     *
     * This endpoint allows authenticated users to update their own profile information.
     * The user must be authenticated and can only update their own profile.
     *
     * @param name The new name for the user
     * @param age The new age for the user (optional)
     * @param weight The new weight for the user in pounds (optional)
     * @param height The new height for the user in inches (optional)
     * @return The updated user profile
     *
     * @throws ValidationException if the provided data fails validation
     * @throws NoResultsFoundException if user profile does not exist
     */
    @PatchMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Update current user profile",
        description =
            "Updates the current authenticated user's profile. " +
                "The user must be authenticated and can only update their own profile. " +
                "All personal data is encrypted at rest for GDPR compliance."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User profile updated successfully",
                content = [Content(schema = Schema(implementation = User::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - validation error"
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - user not authenticated"
            ),
            ApiResponse(
                responseCode = "404",
                description = "Not found - user profile does not exist"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun updateCurrentUser(
        @RequestParam("name") name: String,
        @RequestParam("age", required = false) age: Int?,
        @RequestParam("weight", required = false) weight: Int?,
        @RequestParam("height", required = false) height: Int?,
        @RequestParam("gender", required = false) gender: String?
    ): Mono<ResponseEntity<User>> {
        return userService.updateUser(name, age, weight, height, gender)
            .map { ResponseEntity.ok(it) }
    }
}
