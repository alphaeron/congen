package com.congen.controllers

import com.congen.model.User
import com.congen.service.UserService
import com.congen.util.KeycloakUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * REST controller for managing user operations.
 *
 * This controller provides endpoints for creating, reading, updating, and deleting
 * user profiles. Users represent individuals who use the workout generation system
 * and can have associated preferences, equipment, and program selections.
 *
 * ## User Model
 *
 * Users contain basic profile information including:
 * - Personal details (name, age, height, weight)
 * - Fitness preferences and goals
 * - Associated equipment and exercise preferences
 *
 * ## Validation Rules
 *
 * - **Name**: Required, non-empty string
 * - **Age**: 1-150 years
 * - **Height**: 0.01-300 cm
 * - **Weight**: 0.01-1000 kg
 *
 * ## Error Handling
 *
 * - `400 Bad Request`: Invalid input data
 * - `404 Not Found`: User not found
 * - `422 Unprocessable Entity`: Validation errors
 * - `500 Internal Server Error`: Database or system errors
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user")
@Tag(
    name = "User Management",
    description = "Operations for managing user profiles and preferences",
)
class UserController(
    private val userService: UserService,
    private val keycloakUtil: KeycloakUtil,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(UserController::class.java)
    }

    /**
     * Creates a new user profile (public registration endpoint).
     *
     * This endpoint allows public user registration. It creates a user profile
     * in the application database linked to the authenticated user's Keycloak ID.
     * The user must be authenticated and the profile will be linked to their Keycloak user ID.
     *
     * @param name The user's full name
     * @param age The user's age in years
     * @param height The user's height in centimeters
     * @param weight The user's weight in kilograms
     * @param unit The weight unit (optional, defaults to KG)
     * @return The created user with assigned ID and timestamps
     *
     * @throws ValidationException if user data fails validation
     * @throws DatabaseException if database operation fails
     */
    @PostMapping("/")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Create user profile after Keycloak registration",
        description =
            "Creates a user profile in the application database after successful Keycloak registration. " +
                "The user must be authenticated and the profile will be linked to their Keycloak user ID.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User profile created successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "422",
                description = "Validation error",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
        ],
    )
    fun save(
        @Parameter(
            description = "User's full name",
            required = true,
            example = "John Doe",
        )
        @RequestParam name: String,
        @Parameter(
            description = "User's age in years",
            required = true,
            example = "30",
        )
        @RequestParam age: Int,
        @Parameter(
            description = "User's height in centimeters",
            required = true,
            example = "175.5",
        )
        @RequestParam height: BigDecimal,
        @Parameter(
            description = "User's weight in kilograms",
            required = true,
            example = "80.0",
        )
        @RequestParam weight: BigDecimal,
        @RequestParam(required = false, defaultValue = "KG") unit: String?,
    ): Mono<ResponseEntity<User>> {
        logger.info("Creating user profile for authenticated user: {}", name)
        return keycloakUtil.getCurrentUserId()
            .flatMap { keycloakUserId ->
                userService.createUser(
                    keycloakUserId,
                    name,
                    age,
                    height,
                    weight,
                    unit
                )
            }
            .map { savedUser ->
                logger.debug("Created user profile with id: {}", savedUser.id)
                ResponseEntity.ok(savedUser)
            }
    }

    /**
     * Retrieves the current user's profile.
     *
     * This endpoint fetches the profile information of the currently authenticated user.
     * The user is identified by their Keycloak user ID from the authentication context.
     *
     * @return The current user's profile
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "Get current user profile",
        description = "Retrieves the profile information of the currently authenticated user.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Current user profile retrieved successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "Current user not found in database",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
        ],
    )
    fun getCurrentUser(): Mono<ResponseEntity<User>> {
        return keycloakUtil.getCurrentUserId()
            .flatMap { keycloakUserId ->
                logger.debug("Getting current user profile for Keycloak user ID: {}", keycloakUserId)
                userService.getUserByKeycloakUserId(keycloakUserId)
            }
            .map { ResponseEntity.ok(it) }
    }

    /**
     * Retrieves a user by their unique identifier.
     *
     * This endpoint fetches a specific user's profile information by their ID.
     * If the user is not found, a 404 error will be returned.
     *
     * @param id The unique identifier of the user to retrieve
     * @return The user profile if found, or 404 if not found
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('admin') or hasRole('service') or #id == principal.subject")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a specific user's profile information by their unique identifier.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User found successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
        ],
    )
    fun get(
        @Parameter(
            description = "Unique identifier of the user",
            required = true,
            example = "1",
        )
        @PathVariable("id") id: Int,
    ): Mono<ResponseEntity<User>> {
        return userService.getUserById(id)
            .map { ResponseEntity.ok(it) }
    }

    /**
     * Retrieves all users in the system.
     *
     * This endpoint returns a list of all user profiles. The response includes
     * basic user information for each user in the system.
     *
     * @return List of all users
     *
     * @throws DatabaseException if database operation fails
     */
    @GetMapping("/")
    @PreAuthorize("hasRole('admin') or hasRole('service')")
    @Operation(
        summary = "Get all users",
        description = "Retrieves a list of all user profiles in the system.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Users retrieved successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
        ],
    )
    fun getAll(): Mono<ResponseEntity<List<User>>> {
        logger.debug("Getting all users")
        return userService.getAllUsers()
            .map { ResponseEntity.ok(it) }
    }

    /**
     * Updates an existing user profile.
     *
     * This endpoint updates a user's profile information. The user must exist
     * and have a valid ID. All provided fields will be updated.
     *
     * @param id The unique identifier of the user to update
     * @param name The updated user's full name
     * @param age The updated user's age in years
     * @param height The updated user's height in centimeters
     * @param weight The updated user's weight in kilograms
     * @return The updated user profile
     *
     * @throws ValidationException if user data fails validation
     * @throws DatabaseException if database operation fails
     */
    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('admin') or hasRole('service') or #id == principal.subject")
    @Operation(
        summary = "Update user",
        description =
            "Updates an existing user's profile information. " +
                "The user must exist and have a valid ID.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User updated successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
        ],
    )
    fun update(
        @Parameter(
            description = "Unique identifier of the user to update",
            required = true,
            example = "1",
        )
        @PathVariable("id") id: Int,
        @Parameter(
            description = "User's full name",
            required = true,
            example = "John Doe",
        )
        @RequestParam name: String,
        @Parameter(
            description = "User's age in years",
            required = true,
            example = "30",
        )
        @RequestParam age: Int,
        @Parameter(
            description = "User's height in centimeters",
            required = true,
            example = "175.5",
        )
        @RequestParam height: BigDecimal,
        @Parameter(
            description = "User's weight in kilograms",
            required = true,
            example = "80.0",
        )
        @RequestParam weight: BigDecimal,
        @RequestParam(required = false, defaultValue = "KG") unit: String?,
    ): Mono<ResponseEntity<User>> {
        return userService.updateUser(id, name, age, height, weight, unit)
            .map { ResponseEntity.ok(it) }
    }

    /**
     * Deletes a user profile.
     *
     * This endpoint permanently removes a user from the system. This action
     * cannot be undone and will also remove associated preferences and data.
     *
     * @param id The unique identifier of the user to delete
     * @return Confirmation of deletion
     *
     * @throws DatabaseException if database operation fails
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('admin') or hasRole('service') or #id == principal.subject")
    @Operation(
        summary = "Delete user",
        description =
            "Permanently removes a user from the system. " +
                "This action cannot be undone and will also remove associated preferences and data.",
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "User deleted successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [
                    Content(
                        mediaType = "application/json",
                    ),
                ],
            ),
        ],
    )
    fun delete(
        @Parameter(
            description = "Unique identifier of the user to delete",
            required = true,
            example = "1",
        )
        @PathVariable("id") id: Int,
    ): Mono<ResponseEntity<User>> {
        return userService.deleteUser(id)
            .map { ResponseEntity.ok(it) }
    }
}
