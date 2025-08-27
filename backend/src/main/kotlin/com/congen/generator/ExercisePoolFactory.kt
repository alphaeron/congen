package com.congen.generator

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Factory for creating user exercise pools, handling all filtering logic
 * including equipment availability, user preferences, and exercise characteristics.
 *
 * This class consolidates all exercise filtering logic that was previously scattered
 * across multiple services, providing a single source of truth for determining
 * which exercises are available to a user.
 *
 * @param exerciseEquipmentDAL Data access layer for exercise equipment relationships
 * @param exerciseMuscleDAL Data access layer for exercise muscle relationships
 * @param exerciseWorkoutTypeDAL Data access layer for exercise workout type relationships
 * @param exerciseMatchingService Service for exercise matching and scoring
 * @param exerciseDAL Data access layer for exercise operations
 * @param userEquipmentDAL Data access layer for user equipment operations
 * @param userExercisePreferenceDAL Data access layer for user exercise preference operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class ExercisePoolFactory(
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
    private val exerciseWorkoutTypeDAL: ExerciseWorkoutTypeDAL,
    private val exerciseMatchingService: ExerciseMatchingService,
    private val exerciseDAL: ExerciseDAL,
    private val userEquipmentDAL: UserEquipmentDAL,
    private val userExercisePreferenceDAL: UserExercisePreferenceDAL
) {
    companion object {
        private val logger = LoggerFactory.getLogger(ExercisePoolFactory::class.java)
    }

    /**
     * Creates a user exercise pool for the specified user by fetching all necessary data.
     *
     * @param userId The user ID to create the pool for
     * @return Mono containing the user's exercise pool
     */
    fun createPoolForUser(userId: String): Mono<UserExercisePool> {
        return Mono.zip(
            exerciseDAL.selectExercises(),
            userEquipmentDAL.selectUserEquipmentByUser(userId),
            userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId)
        ).map { tuple ->
            val allExercises = tuple.t1
            val userEquipment = tuple.t2
            val preferences = tuple.t3

            UserExercisePool(
                allExercises = allExercises,
                preferences = preferences,
                userEquipment = userEquipment,
                exerciseEquipmentDAL = exerciseEquipmentDAL,
            )
        }
    }
}
