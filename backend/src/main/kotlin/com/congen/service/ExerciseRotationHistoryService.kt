package com.congen.service

import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.model.ExerciseRotationHistory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

/**
 * Service for managing exercise rotation history operations.
 *
 * This service is a thin wrapper around [ExerciseRotationHistoryDAL], exposing the same methods
 * for use by controllers and other services.
 *
 * @property exerciseRotationHistoryDAL Data access layer for exercise rotation history operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class ExerciseRotationHistoryService(
    private val exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL
) {
    /**
     * Retrieves an exercise rotation history record by its unique identifier.
     * @see ExerciseRotationHistoryDAL.selectById
     */
    fun selectById(id: Long): Mono<ExerciseRotationHistory> = exerciseRotationHistoryDAL.selectById(id)

    /**
     * Retrieves all exercise rotation history records.
     * @see ExerciseRotationHistoryDAL.selectAll
     */
    fun selectAll(): Mono<List<ExerciseRotationHistory>> = exerciseRotationHistoryDAL.selectAll()

    /**
     * Retrieves exercise rotation history records for a specific accessory type.
     * @see ExerciseRotationHistoryDAL.selectByIsAccessory
     */
    fun selectByIsAccessory(isAccessory: Boolean): Mono<List<ExerciseRotationHistory>> =
        exerciseRotationHistoryDAL.selectByIsAccessory(isAccessory)

    /**
     * Retrieves all exercise rotation history records for a specific user, optionally filtered by accessory type.
     * @param userId The Keycloak user ID
     * @param isAccessory Optional filter for accessory exercises
     * @return Mono containing a list of exercise rotation history records for the user
     */
    fun selectByUserId(
        userId: String,
        isAccessory: Boolean? = null
    ): Mono<List<ExerciseRotationHistory>> = exerciseRotationHistoryDAL.selectByUserId(userId, isAccessory)

    /**
     * Inserts a new exercise rotation history record into the database.
     * @see ExerciseRotationHistoryDAL.insert
     */
    fun insert(
        userId: String,
        exerciseName: String,
        isAccessory: Boolean
    ): Mono<ExerciseRotationHistory> = exerciseRotationHistoryDAL.insert(userId, exerciseName, isAccessory)

    /**
     * Updates an existing exercise rotation history record in the database.
     * @see ExerciseRotationHistoryDAL.update
     */
    fun update(
        id: Long,
        userId: String,
        exerciseName: String,
        isAccessory: Boolean
    ): Mono<ExerciseRotationHistory> = exerciseRotationHistoryDAL.update(id, userId, exerciseName, isAccessory)

    /**
     * Deletes an exercise rotation history record from the database.
     * @see ExerciseRotationHistoryDAL.deleteById
     */
    fun deleteById(id: Long): Mono<ExerciseRotationHistory> = exerciseRotationHistoryDAL.deleteById(id)

    /**
     * Deletes all exercise rotation history records for a specific user.
     * @see ExerciseRotationHistoryDAL.deleteByUserId
     */
    fun deleteByUserId(userId: String): Mono<Int> = exerciseRotationHistoryDAL.deleteByUserId(userId)

    /**
     * Checks if the given user is the owner of the exercise rotation history record.
     *
     * @param historyId The ID of the exercise rotation history record
     * @param userId The Keycloak user ID to check ownership against (as String)
     * @return Mono<Boolean> true if the user owns the record, false otherwise
     */
    fun isOwner(
        historyId: Long,
        userId: String
    ): Mono<Boolean> {
        return exerciseRotationHistoryDAL.selectById(historyId)
            .map { record -> record.userId.toString() == userId }
            .onErrorReturn(false)
    }
}
