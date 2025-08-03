package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.UserProgramPreferences
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for UserProgramPreferences entity operations.
 *
 * This class provides database operations for the UserProgramPreferences entity in the Congen application.
 * UserProgramPreferences represents a user's preferences for workout programs, such as days per week and session length.
 *
 * ## UserProgramPreferences Entity
 *
 * UserProgramPreferences represents:
 * - User's preferences for workout program structure
 * - Days per week and session time length
 * - Used for generating personalized workout programs
 *
 * ## Database Operations
 *
 * - **Select by user**: Retrieve program preferences for a specific user
 * - **Insert**: Create new user program preferences
 * - **Update**: Modify existing user program preferences
 * - **Delete**: Remove user program preferences
 *
 * ## Error Handling
 *
 * - **NoResultsFoundException**: When user program preferences don't exist
 * - **DatabaseException**: When database operations fail
 *
 * @property postgresClient PostgreSQL client for database operations
 * @property programmedWorkoutDAL Data access layer for programmed workout operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class UserProgramPreferencesDAL(
    private val postgresClient: PostgresClient,
    private val programmedWorkoutDAL: ProgrammedWorkoutDAL,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(UserProgramPreferencesDAL::class.java)
    }

    /**
     * Retrieves program preferences for a specific user.
     *
     * This method queries the database to find the program preferences for the specified user.
     * If no preferences exist, a NoResultsFoundException is thrown.
     *
     * @param userId The Keycloak identifier of the user
     * @return Mono containing the user program preferences if found
     * @throws NoResultsFoundException when the preferences don't exist
     */
    fun selectUserProgramPreferences(userId: String): Mono<UserProgramPreferences> {
        logger.debug("Selecting program preferences for user: {}", userId)
        return postgresClient.selectIndividual(
            "SELECT * FROM user_program_preferences WHERE user_id=$1",
            userId,
        )
    }

    /**
     * Creates new user program preferences in the database.
     *
     * This method validates and inserts new program preferences for the specified user.
     * The user ID must be unique in the user_program_preferences table.
     *
     * @param userId The Keycloak identifier of the user
     * @param programDaysPerWeek The number of days per week for the program
     * @param sessionTimeLengthInMinutes The session time length in minutes
     * @return Mono containing the created user program preferences
     * @throws DatabaseException when the preferences already exist or database operation fails
     */
    fun insertUserProgramPreferences(
        userId: String,
        programDaysPerWeek: Int,
        sessionTimeLengthInMinutes: Int,
    ): Mono<UserProgramPreferences> {
        logger.debug("Inserting user program preferences: {}", userId)

        // Validate all CHECK constraints
        ValidationUtil.validateProgramDaysPerWeek(programDaysPerWeek)
        ValidationUtil.validateSessionTimeLength(sessionTimeLengthInMinutes)

        return postgresClient.update(
            """
            INSERT INTO user_program_preferences
                (user_id, program_days_per_week, session_time_length_in_minutes)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            userId,
            programDaysPerWeek,
            sessionTimeLengthInMinutes,
        )
    }

    /**
     * Updates existing user program preferences in the database.
     *
     * This method validates and updates the program preferences for the specified user.
     * If no preferences exist, a NoResultsFoundException is thrown.
     * Program days per week cannot be changed if the user has existing workouts to prevent
     * day numbering conflicts and maintain program consistency.
     *
     * @param userId The Keycloak identifier of the user
     * @param programDaysPerWeek The number of days per week for the program
     * @param sessionTimeLengthInMinutes The session time length in minutes
     * @return Mono containing the updated user program preferences
     * @throws NoResultsFoundException when the preferences don't exist
     * @throws ValidationException when program days per week cannot be changed due to existing workouts
     */
    fun updateUserProgramPreferences(
        userId: String,
        programDaysPerWeek: Int,
        sessionTimeLengthInMinutes: Int,
    ): Mono<UserProgramPreferences> {
        logger.debug("Updating user program preferences: {}", userId)

        // Validate all CHECK constraints
        ValidationUtil.validateProgramDaysPerWeek(programDaysPerWeek)
        ValidationUtil.validateSessionTimeLength(sessionTimeLengthInMinutes)

        // Check if user has existing workouts and validate program days per week change
        return programmedWorkoutDAL.hasUserExistingWorkouts(userId)
            .flatMap { hasExistingWorkouts ->
                if (hasExistingWorkouts) {
                    // Get current preferences to check if program days per week is being changed
                    selectUserProgramPreferences(userId)
                        .flatMap { currentPreferences ->
                            ValidationUtil.validateProgramDaysPerWeekChange(
                                userId = userId,
                                newProgramDaysPerWeek = programDaysPerWeek,
                                currentProgramDaysPerWeek = currentPreferences.programDaysPerWeek
                            )
                            // If validation passes, proceed with update
                            postgresClient.update(
                                """
                                UPDATE user_program_preferences
                                SET program_days_per_week=$2, session_time_length_in_minutes=$3, updated_at=NOW()
                                WHERE user_id=$1
                                """.trimIndent(),
                                userId,
                                programDaysPerWeek,
                                sessionTimeLengthInMinutes,
                            )
                        }
                } else {
                    // No existing workouts, safe to update
                    postgresClient.update(
                        """
                        UPDATE user_program_preferences
                        SET program_days_per_week=$2, session_time_length_in_minutes=$3, updated_at=NOW()
                        WHERE user_id=$1
                        """.trimIndent(),
                        userId,
                        programDaysPerWeek,
                        sessionTimeLengthInMinutes,
                    )
                }
            }
    }

    /**
     * Deletes user program preferences from the database.
     *
     * This method removes the program preferences for the specified user.
     * If no preferences exist, a NoResultsFoundException is thrown.
     *
     * @param userId The Keycloak identifier of the user
     * @return Mono containing the deleted user program preferences
     * @throws NoResultsFoundException when the preferences don't exist
     */
    fun deleteUserProgramPreferences(userId: String): Mono<UserProgramPreferences> {
        logger.debug("Deleting user program preferences: {}", userId)
        return postgresClient.update(
            """
            DELETE FROM user_program_preferences
            WHERE user_id=$1
            """.trimIndent(),
            userId,
        )
    }
}
