package com.congen.controllers

import com.congen.dal.UserOneRepMaxDAL
import com.congen.model.UserOneRepMax
import com.congen.service.UserOneRepMaxService
import com.congen.util.ValidationUtil
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.math.BigDecimal

/**
 * REST controller for UserOneRepMax entity operations.
 *
 * This controller provides CRUD operations for user one rep max values in the Congen API.
 * User one rep max values allow users to track their maximum weight for different exercises,
 * which is used for workout generation and progression calculations.
 *
 * ## UserOneRepMax Entity
 *
 * A user one rep max represents:
 * - Association between a user and an exercise
 * - User's one rep max weight for the exercise
 * - Timestamp of when the 1RM was last updated
 * - Used for workout generation and progression calculations
 *
 * ## Endpoints
 *
 * - `PUT /user_one_rep_max/` - Create or update a user one rep max (upsert)
 * - `GET /user_one_rep_max/{userId}` - Retrieve all one rep max values for a user
 * - `GET /user_one_rep_max/{userId}/{exerciseName}` - Retrieve a specific one rep max
 * - `DELETE /user_one_rep_max/{userId}/{exerciseName}` - Delete a user one rep max
 *
 * ## Error Handling
 *
 * - **404 Not Found**: When a one rep max with the specified parameters doesn't exist
 * - **422 Unprocessable Entity**: When validation fails
 * - **500 Internal Server Error**: When database operations fail
 *
 * @property userOneRepMaxDAL Data access layer for user one rep max operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/user_one_rep_max")
class UserOneRepMaxController(
    private val userOneRepMaxDAL: UserOneRepMaxDAL,
    private val userOneRepMaxService: UserOneRepMaxService,
    private val validationUtil: ValidationUtil
) {
    /**
     * Get all one rep max records for a user.
     *
     * @param userId The user ID
     * @param unit Optional unit to convert weights to (kg or lbs)
     * @return Mono containing list of one rep max records
     */
    @GetMapping("/user/{user_id}")
    @PreAuthorize("hasRole('admin') or hasRole('service') or #userId == principal.subject")
    fun getOneRepMaxesByUserId(
        @PathVariable("user_id") userId: Int,
        @RequestParam(required = false) unit: String?
    ): Mono<ResponseEntity<List<UserOneRepMax>>> {
        return userOneRepMaxService.getAllByUser(userId, unit)
            .map { ResponseEntity.ok(it) }
    }

    /**
     * Get a specific one rep max record by user and exercise.
     *
     * @param userId The user ID
     * @param exerciseName The exercise name
     * @param unit Optional unit to convert weight to (kg or lbs)
     * @return Mono containing the one rep max record or empty if not found
     */
    @GetMapping("/user/{user_id}/exercise/{exercise_name}")
    @PreAuthorize("hasRole('admin') or hasRole('service') or #userId == principal.subject")
    fun getOneRepMaxByUserAndExercise(
        @PathVariable("user_id") userId: Int,
        @PathVariable("exercise_name") exerciseName: String,
        @RequestParam(required = false) unit: String?
    ): Mono<ResponseEntity<UserOneRepMax>> {
        return userOneRepMaxService.getByUserAndExercise(userId, exerciseName, unit)
            .map { ResponseEntity.ok(it) }
    }

    /**
     * Create or update a one rep max record.
     *
     * @param userId The user ID
     * @param exerciseName The exercise name
     * @param oneRepMax The one rep max weight value
     * @param unit The weight unit (kg or lbs)
     * @return Mono containing the created or updated one rep max record
     */
    @PutMapping("/")
    @PreAuthorize("hasRole('admin') or hasRole('service') or #userId == principal.subject")
    fun upsertOneRepMax(
        @RequestParam("user_id") userId: Int,
        @RequestParam("exercise_name") exerciseName: String,
        @RequestParam("one_rep_max") oneRepMax: BigDecimal,
        @RequestParam(required = false, defaultValue = "KG") unit: String?
    ): Mono<ResponseEntity<UserOneRepMax>> {
        return userOneRepMaxService.upsertOneRepMax(userId, exerciseName, oneRepMax, unit)
            .map { ResponseEntity.ok(it) }
    }

    /**
     * Delete a one rep max record.
     *
     * @param userId The user ID
     * @param exerciseName The exercise name
     * @return Mono containing confirmation of deletion
     */
    @DeleteMapping("/user/{user_id}/exercise/{exercise_name}")
    @PreAuthorize("hasRole('admin') or hasRole('service') or #userId == principal.subject")
    fun deleteOneRepMax(
        @PathVariable("user_id") userId: Int,
        @PathVariable("exercise_name") exerciseName: String
    ): Mono<ResponseEntity<UserOneRepMax>> {
        return userOneRepMaxService.deleteOneRepMax(userId, exerciseName)
            .map { ResponseEntity.ok(it) }
    }
}
