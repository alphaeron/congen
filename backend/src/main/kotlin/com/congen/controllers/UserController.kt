package com.congen.controllers

import com.congen.exceptions.DatabaseException
import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import com.congen.model.User
import com.congen.service.UserService
import com.congen.util.KeycloakUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

/**
 * REST controller for user management operations.
 *
 * Provides endpoints for creating, retrieving, updating, and managing user profiles.
 * All operations require proper authentication and authorization.
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
                content = [Content(schema = io.swagger.v3.oas.annotations.media.Schema(implementation = User::class))]
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
        return userService.createUser()
            .map { ResponseEntity.ok(it) }
            .onErrorResume(ValidationException::class.java) { e ->
                logger.warn("Validation error creating user: {}", e.message)
                Mono.just(ResponseEntity.badRequest().build())
            }
            .onErrorResume(DatabaseException::class.java) { e ->
                logger.error("Database error creating user", e)
                Mono.just(ResponseEntity.internalServerError().build())
            }
            .onErrorResume { e ->
                logger.error("Unexpected error creating user", e)
                Mono.just(ResponseEntity.internalServerError().build())
            }
    }

    /**
     * Retrieves all users in the system.
     *
     * This endpoint allows retrieving all user profiles in the system.
     * The user must be authenticated and have admin privileges.
     *
     * @return List of all users
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/")
    @PreAuthorize("hasRole('admin') or hasRole('service')")
    @Operation(
        summary = "Get all users",
        description =
            "Retrieves all user profiles in the system. " +
                "Only users with admin or service roles can access this endpoint."
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "All users retrieved successfully",
                content = [Content(schema = io.swagger.v3.oas.annotations.media.Schema(implementation = Array<User>::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - user not authenticated"
            ),
            ApiResponse(
                responseCode = "403",
                description = "Forbidden - user not authorized"
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error"
            )
        ]
    )
    fun getAll(): Mono<ResponseEntity<List<User>>> {
        return userService.getAllUsers()
            .map { ResponseEntity.ok(it) }
            .onErrorResume(DatabaseException::class.java) { e ->
                logger.error("Database error getting all users", e)
                Mono.just(ResponseEntity.internalServerError().build())
            }
            .onErrorResume { e ->
                logger.error("Unexpected error getting all users", e)
                Mono.just(ResponseEntity.internalServerError().build())
            }
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
                content = [Content(schema = io.swagger.v3.oas.annotations.media.Schema(implementation = User::class))]
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
                userService.getUserByKeycloakId(keycloakUserId)
            }
            .map { ResponseEntity.ok(it) }
            .onErrorResume(NoResultsFoundException::class.java) { e ->
                logger.warn("User profile not found for current user: {}", e.message)
                Mono.just(ResponseEntity.notFound().build())
            }
            .onErrorResume { e ->
                logger.error("Unexpected error getting current user", e)
                Mono.just(ResponseEntity.internalServerError().build())
            }
    }
}
