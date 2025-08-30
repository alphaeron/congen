package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.model.ProgramPreferences
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for ProgramPreferences entity operations.
 *
 * This class provides database operations for the ProgramPreferences entity in the Congen application.
 * ProgramPreferences represents program-specific workout preferences including frequency and duration.
 *
 * ## ProgramPreferences Entity
 *
 * ProgramPreferences represents:
 * - Program-specific workout frequency (days per week)
 * - Session duration preferences
 * - Association with a specific program
 *
 * ## Database Operations
 *
 * - **Select by program ID**: Retrieve preferences for a specific program
 * - **Select by user ID**: Retrieve preferences for all programs owned by a user
 * - **Insert**: Create new program preferences
 * - **Update**: Modify existing program preferences
 * - **Delete**: Remove program preferences
 *
 * ## Error Handling
 *
 * - **NoResultsFoundException**: When preferences for specified program don't exist
 * - **DatabaseException**: When database operations fail
 *
 * @param postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class ProgramPreferencesDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ProgramPreferencesDAL::class.java)
    }

    /**
     * Retrieves program preferences for a specific program.
     *
     * This method queries the database to find the program preferences for the specified program.
     * If no preferences exist, a NoResultsFoundException is thrown.
     *
     * @param programId The ID of the program
     * @return Mono containing the program preferences if found
     * @throws NoResultsFoundException when the preferences don't exist
     */
    @Cacheable(
        ttl = CacheTTL.LONG_TERM,
        keyStrategy = CacheKeyStrategy.RELATIONSHIP,
        entityName = "program_preferences"
    )
    fun selectProgramPreferences(programId: Long): Mono<ProgramPreferences> {
        logger.debug("Selecting program preferences for program: {}", programId)
        return postgresClient.selectIndividual(
            "SELECT * FROM program_preferences WHERE program_id=$1",
            programId,
        )
    }

    /**
     * Creates new program preferences in the database.
     *
     * This method validates and inserts new program preferences for the specified program.
     * The program ID must be unique in the program_preferences table.
     *
     * @param programId The ID of the program these preferences belong to
     * @param programDaysPerWeek The number of days per week for the program
     * @param sessionTimeLengthInMinutes The session time length in minutes
     * @return Mono containing the created program preferences
     * @throws DatabaseException when the preferences already exist or database operation fails
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.RELATIONSHIP,
        entityName = "program_preferences"
    )
    fun insertProgramPreferences(
        programId: Long,
        programDaysPerWeek: Int,
        sessionTimeLengthInMinutes: Int
    ): Mono<ProgramPreferences> {
        logger.debug("Inserting program preferences for program: {}", programId)

        // Validate all CHECK constraints
        ValidationUtil.validateProgramDaysPerWeek(programDaysPerWeek)
        ValidationUtil.validateSessionTimeLength(sessionTimeLengthInMinutes)

        return postgresClient.update(
            """
            INSERT INTO program_preferences
                (program_id, program_days_per_week, session_time_length_in_minutes)
            VALUES
                ($1, $2, $3)
            """.trimIndent(),
            programId,
            programDaysPerWeek,
            sessionTimeLengthInMinutes,
        )
    }

    /**
     * Updates existing program preferences in the database.
     *
     * This method updates only the session time length for the specified program.
     * Program days per week cannot be modified as it would affect workout scheduling.
     * If no preferences exist for the given program, a NoResultsFoundException is thrown.
     *
     * @param programId The ID of the program to update preferences for
     * @param sessionTimeLengthInMinutes The updated session time length in minutes
     * @return Mono containing the updated program preferences
     * @throws NoResultsFoundException if no preferences exist for the given program
     * @throws ValidationException if attempting to modify program days per week
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.RELATIONSHIP,
        entityName = "program_preferences"
    )
    fun updateProgramPreferences(
        programId: Long,
        sessionTimeLengthInMinutes: Int
    ): Mono<ProgramPreferences> {
        logger.debug("Updating program preferences for program: {}", programId)

        // Validate session time length
        ValidationUtil.validateSessionTimeLength(sessionTimeLengthInMinutes)

        return postgresClient.update(
            """
            UPDATE program_preferences
            SET session_time_length_in_minutes=$2, updated_at=NOW()
            WHERE program_id=$1
            """.trimIndent(),
            programId,
            sessionTimeLengthInMinutes,
        )
    }

    /**
     * Deletes program preferences from the database.
     *
     * This method removes the program preferences record for the specified program
     * from the database. If no preferences exist for the given program, a
     * NoResultsFoundException is thrown. The method returns the deleted preferences
     * data for confirmation.
     *
     * @param programId The ID of the program to delete preferences for
     * @return Mono containing the deleted program preferences
     * @throws NoResultsFoundException if no preferences exist for the given program
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.RELATIONSHIP,
        entityName = "program_preferences"
    )
    fun deleteProgramPreferences(programId: Long): Mono<ProgramPreferences> {
        logger.debug("Deleting program preferences for program: {}", programId)
        return postgresClient.update(
            "DELETE FROM program_preferences WHERE program_id=$1",
            programId,
        )
    }
}
