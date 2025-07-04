package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.SetScheme
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for SetScheme entities.
 *
 * This class provides database operations for SetScheme entities, including CRUD
 * operations and comprehensive data validation. It uses the reactive PostgreSQL
 * client for all database interactions and includes extensive validation of set
 * scheme data before database operations.
 *
 * ## Operations
 *
 * - **Read**: Select set scheme by ID, select by programmed exercise ID, select all
 * - **Create**: Insert new set scheme with validation
 * - **Update**: Update existing set scheme with validation
 * - **Delete**: Delete set scheme by ID
 *
 * ## Validation
 *
 * All set scheme data is validated before database operations using [ValidationUtil]:
 * - Set number validation (> 0)
 * - Tempo validation (single digit 0-9) for eccentric, isometric, and concentric
 * - Weight validation (> 0) for target and performed weights
 * - Rep count validation (1-1000) for target and performed reps
 * - Rest seconds validation (0-3600)
 *
 * ## SetScheme Entity
 *
 * Set schemes define the specific parameters for individual sets within an exercise:
 * - Unique identifier and set number within the exercise
 * - Reference to the parent programmed exercise
 * - Performance flags (AMRAP, EMOM, tempo usage)
 * - Target and performed weight, reps, and tempo
 * - Rest period and completion status
 *
 * ## Database Schema
 *
 * The set_scheme table contains:
 * - `id`: Primary key (auto-generated)
 * - `programmed_exercise_id`: Foreign key to programmed_exercise table
 * - `set_number`: Set number within the exercise (must be > 0)
 * - `was_set_performed`: Whether the set was completed
 * - `is_amrap`: As Many Reps As Possible flag
 * - `is_emom`: Every Minute On the Minute flag
 * - `use_tempo`: Whether to use tempo timing
 * - `eccentric_tempo`: Eccentric phase tempo (0-9)
 * - `isometric_tempo`: Isometric phase tempo (0-9)
 * - `concentric_tempo`: Concentric phase tempo (0-9)
 * - `target_weight`: Target weight for the set
 * - `performed_weight`: Actual weight used
 * - `target_rep_count`: Target number of reps
 * - `performed_rep_count`: Actual number of reps completed
 * - `rest_seconds`: Rest period after the set
 *
 * @property postgresClient Client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class SetSchemeDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(SetSchemeDAL::class.java)
    }

    /**
     * Retrieves a set scheme by its unique identifier.
     *
     * This method queries the database for a set scheme with the specified ID.
     * If no set scheme is found, a [NoResultsFoundException] is thrown.
     *
     * @param id The unique identifier of the set scheme to retrieve
     * @return Mono containing the set scheme if found
     * @throws NoResultsFoundException if no set scheme exists with the given ID
     */
    fun selectSetSchemeById(id: Long): Mono<SetScheme> {
        logger.debug("Selecting set scheme by id: {}", id)
        return postgresClient.selectIndividual(
            "SELECT * FROM set_scheme WHERE id=$1",
            id,
        )
    }

    /**
     * Retrieves all set schemes for a specific programmed exercise.
     *
     * This method queries the database for all set schemes that belong to a
     * specific programmed exercise, ordered by their set number within the exercise.
     *
     * @param programmedExerciseId The unique identifier of the programmed exercise
     * @return Mono containing a list of set schemes for the exercise
     */
    fun selectSetSchemesByProgrammedExerciseId(programmedExerciseId: Long): Mono<List<SetScheme>> {
        logger.debug("Selecting set schemes by programmed exercise id: {}", programmedExerciseId)
        return postgresClient.select(
            "SELECT * FROM set_scheme WHERE programmed_exercise_id=$1 ORDER BY set_number",
            programmedExerciseId,
        )
    }

    /**
     * Retrieves all set schemes from the database.
     *
     * This method queries the database for all set scheme records and returns
     * them as a list, ordered by programmed exercise ID and set number. If no
     * set schemes exist, an empty list is returned.
     *
     * @return Mono containing a list of all set schemes
     */
    fun selectSetSchemes(): Mono<List<SetScheme>> {
        logger.debug("Selecting all set schemes")
        return postgresClient.select("SELECT * FROM set_scheme ORDER BY programmed_exercise_id, set_number")
    }

    /**
     * Inserts a new set scheme into the database.
     *
     * This method validates the set scheme data and inserts a new set scheme
     * record. The set scheme ID is automatically generated by the database.
     * All set scheme properties are validated before insertion.
     *
     * @param setScheme The set scheme object to insert
     * @return Mono containing the inserted set scheme with generated ID
     * @throws ValidationException if set scheme data fails validation
     */
    fun insertSetScheme(setScheme: SetScheme): Mono<SetScheme> {
        logger.debug("Inserting set scheme for exercise: {}, set: {}", setScheme.programmedExerciseId, setScheme.setNumber)

        // Validate all CHECK constraints
        ValidationUtil.validateSetNumber(setScheme.setNumber)
        ValidationUtil.validateTempo(setScheme.eccentricTempo, "Eccentric")
        ValidationUtil.validateTempo(setScheme.isometricTempo, "Isometric")
        ValidationUtil.validateTempo(setScheme.concentricTempo, "Concentric")
        ValidationUtil.validateTargetWeight(setScheme.targetWeight)
        ValidationUtil.validatePerformedWeight(setScheme.performedWeight)
        ValidationUtil.validateTargetRepCount(setScheme.targetRepCount)
        ValidationUtil.validatePerformedRepCount(setScheme.performedRepCount)
        ValidationUtil.validateRestSeconds(setScheme.restSeconds)

        return postgresClient.update(
            """
            INSERT INTO set_scheme
                (programmed_exercise_id, set_number, was_set_performed, is_amrap, is_emom, use_tempo,
                 eccentric_tempo, isometric_tempo, concentric_tempo, target_weight, performed_weight,
                 target_rep_count, performed_rep_count, rest_seconds)
            VALUES
                ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
            """.trimIndent(),
            setScheme.programmedExerciseId,
            setScheme.setNumber,
            setScheme.wasSetPerformed,
            setScheme.isAmrap,
            setScheme.isEmom,
            setScheme.useTempo,
            setScheme.eccentricTempo,
            setScheme.isometricTempo,
            setScheme.concentricTempo,
            setScheme.targetWeight,
            setScheme.performedWeight,
            setScheme.targetRepCount,
            setScheme.performedRepCount,
            setScheme.restSeconds,
        )
    }

    /**
     * Updates an existing set scheme in the database.
     *
     * This method validates the set scheme data and updates the set scheme
     * record with the specified ID. All set scheme properties are validated
     * before the update operation.
     *
     * @param setScheme The set scheme object with updated data
     * @return Mono containing the updated set scheme
     * @throws ValidationException if set scheme data fails validation
     * @throws NoResultsFoundException if no set scheme exists with the given ID
     */
    fun updateSetScheme(setScheme: SetScheme): Mono<SetScheme> {
        logger.debug("Updating set scheme: {}", setScheme.id)

        // Validate all CHECK constraints
        ValidationUtil.validateSetNumber(setScheme.setNumber)
        ValidationUtil.validateTempo(setScheme.eccentricTempo, "Eccentric")
        ValidationUtil.validateTempo(setScheme.isometricTempo, "Isometric")
        ValidationUtil.validateTempo(setScheme.concentricTempo, "Concentric")
        ValidationUtil.validateTargetWeight(setScheme.targetWeight)
        ValidationUtil.validatePerformedWeight(setScheme.performedWeight)
        ValidationUtil.validateTargetRepCount(setScheme.targetRepCount)
        ValidationUtil.validatePerformedRepCount(setScheme.performedRepCount)
        ValidationUtil.validateRestSeconds(setScheme.restSeconds)

        return postgresClient.update(
            """
            UPDATE set_scheme
            SET programmed_exercise_id=$2, set_number=$3, was_set_performed=$4, is_amrap=$5, is_emom=$6, use_tempo=$7,
                eccentric_tempo=$8, isometric_tempo=$9, concentric_tempo=$10, target_weight=$11, performed_weight=$12,
                target_rep_count=$13, performed_rep_count=$14, rest_seconds=$15
            WHERE id=$1
            """.trimIndent(),
            setScheme.id,
            setScheme.programmedExerciseId,
            setScheme.setNumber,
            setScheme.wasSetPerformed,
            setScheme.isAmrap,
            setScheme.isEmom,
            setScheme.useTempo,
            setScheme.eccentricTempo,
            setScheme.isometricTempo,
            setScheme.concentricTempo,
            setScheme.targetWeight,
            setScheme.performedWeight,
            setScheme.targetRepCount,
            setScheme.performedRepCount,
            setScheme.restSeconds,
        )
    }

    /**
     * Deletes a set scheme from the database.
     *
     * This method removes the set scheme record with the specified ID from
     * the database. If no set scheme exists with the given ID, a
     * [NoResultsFoundException] is thrown. The method returns the deleted
     * set scheme data for confirmation.
     *
     * @param id The unique identifier of the set scheme to delete
     * @return Mono containing the deleted set scheme
     * @throws NoResultsFoundException if no set scheme exists with the given ID
     */
    fun deleteSetScheme(id: Long): Mono<SetScheme> {
        logger.debug("Deleting set scheme: {}", id)
        return postgresClient.update(
            """
            DELETE FROM set_scheme WHERE id=$1
            """.trimIndent(),
            id,
        )
    }
}
