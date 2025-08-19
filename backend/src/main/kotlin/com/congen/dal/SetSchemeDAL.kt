package com.congen.dal

import com.congen.cache.CacheInvalidationStrategy
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.model.Band
import com.congen.model.SetScheme
import com.congen.util.ValidationUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import java.math.BigDecimal

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
 * - `updated_at`: Timestamp of the last update
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
    @Cacheable(
        ttl = CacheTTL.SHORT_TERM,
        keyStrategy = CacheKeyStrategy.STANDARD,
        entityName = "set_scheme"
    )
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
    @Cacheable(
        ttl = CacheTTL.SHORT_TERM,
        keyStrategy = CacheKeyStrategy.STANDARD,
        entityName = "set_scheme"
    )
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
    @Cacheable(
        ttl = CacheTTL.SHORT_TERM,
        keyStrategy = CacheKeyStrategy.LIST_QUERY,
        entityName = "set_scheme"
    )
    fun selectSetSchemes(): Mono<List<SetScheme>> {
        logger.debug("Selecting all set schemes")
        return postgresClient.select("SELECT * FROM set_scheme ORDER BY programmed_exercise_id, set_number")
    }

    /**
     * Retrieves all set schemes owned by a specific user.
     *
     * This method efficiently fetches all set schemes that belong to programmed exercises
     * owned by the specified user by joining through the relationship chain:
     * SetScheme → ProgrammedExercise → WorkoutStage → ProgrammedWorkout → Program → User
     * If no set schemes exist for the user, an empty list is returned.
     *
     * @param userId The Keycloak identifier of the user
     * @return Mono containing a list of set schemes owned by the user
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "set_scheme"
    )
    fun selectSetSchemesByUserId(userId: String): Mono<List<SetScheme>> {
        logger.debug("Selecting set schemes by user id: {}", userId)
        return postgresClient.select(
            """
            SELECT ss.*
            FROM set_scheme ss
            JOIN programmed_exercise pe ON ss.programmed_exercise_id = pe.id
            JOIN workout_stage ws ON pe.workout_stage_id = ws.id
            JOIN programmed_workout pw ON ws.programmed_workout_id = pw.id
            JOIN program p ON pw.program_id = p.id
            WHERE p.user_id = $1
            ORDER BY ss.programmed_exercise_id, ss.set_number
            """.trimIndent(),
            userId
        )
    }

    /**
     * Inserts a new set scheme into the database.
     *
     * This method validates the set scheme data and inserts a new set scheme
     * record. The set scheme ID is automatically generated by the database.
     * All set scheme properties are validated before insertion.
     *
     * @param programmedExerciseId ID of the programmed exercise this set belongs to
     * @param setNumber Order of this set within the exercise (1-based)
     * @param isAmrap As Many Reps As Possible flag
     * @param isEmom Every Minute On the Minute flag
     * @param useTempo Whether to use tempo timing
     * @param eccentricTempo Eccentric phase tempo (0-9 seconds)
     * @param isometricTempo Isometric phase tempo (0-9 seconds)
     * @param concentricTempo Concentric phase tempo (0-9 seconds)
     * @param targetWeight Target weight for the set in kg
     * @param performedWeight Actual weight used in kg
     * @param targetRepCount Target number of repetitions
     * @param performedRepCount Actual number of repetitions completed
     * @param restSeconds Rest period after the set in seconds
     * @param band The band information for Dynamic Effort exercises
     * @return Mono containing the inserted set scheme with generated ID
     * @throws ValidationException if set scheme data fails validation
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.STANDARD,
        entityName = "set_scheme"
    )
    fun insertSetScheme(
        programmedExerciseId: Long,
        setNumber: Int,
        isAmrap: Boolean,
        isEmom: Boolean,
        useTempo: Boolean,
        eccentricTempo: String?,
        isometricTempo: String?,
        concentricTempo: String?,
        targetWeight: BigDecimal?,
        performedWeight: BigDecimal?,
        targetRepCount: Int?,
        performedRepCount: Int?,
        restSeconds: Int?,
        band: Band? = null,
    ): Mono<SetScheme> {
        logger.debug("Inserting set scheme for exercise: {}, set: {}", programmedExerciseId, setNumber)

        // Validate all CHECK constraints
        ValidationUtil.validateSetNumber(setNumber)
        ValidationUtil.validateTempo(eccentricTempo, "Eccentric")
        ValidationUtil.validateTempo(isometricTempo, "Isometric")
        ValidationUtil.validateTempo(concentricTempo, "Concentric")
        ValidationUtil.validateTargetWeight(targetWeight)
        ValidationUtil.validatePerformedWeight(performedWeight)
        ValidationUtil.validateTargetRepCount(targetRepCount)
        ValidationUtil.validatePerformedRepCount(performedRepCount)
        ValidationUtil.validateRestSeconds(restSeconds)

        return postgresClient.update(
            """
            INSERT INTO set_scheme
                (programmed_exercise_id, set_number, is_amrap, is_emom, use_tempo,
                 eccentric_tempo, isometric_tempo, concentric_tempo, target_weight, performed_weight,
                 target_rep_count, performed_rep_count, rest_seconds, band_weight_lbs)
            VALUES
                ($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11, $12, $13, $14)
            """.trimIndent(),
            programmedExerciseId,
            setNumber,
            isAmrap,
            isEmom,
            useTempo,
            eccentricTempo,
            isometricTempo,
            concentricTempo,
            targetWeight,
            performedWeight,
            targetRepCount,
            performedRepCount,
            restSeconds,
            band?.weightLbs,
        )
    }

    /**
     * Updates an existing set scheme in the database.
     *
     * This method validates the set scheme data and updates the set scheme
     * record with the specified ID. All set scheme properties are validated
     * before the update operation.
     *
     * @param id The unique identifier of the set scheme to update
     * @param programmedExerciseId ID of the programmed exercise this set belongs to
     * @param setNumber Order of this set within the exercise (1-based)
     * @param isAmrap As Many Reps As Possible flag
     * @param isEmom Every Minute On the Minute flag
     * @param useTempo Whether to use tempo timing
     * @param eccentricTempo Eccentric phase tempo (0-9 seconds)
     * @param isometricTempo Isometric phase tempo (0-9 seconds)
     * @param concentricTempo Concentric phase tempo (0-9 seconds)
     * @param targetWeight Target weight for the set in kg
     * @param performedWeight Actual weight used in kg
     * @param targetRepCount Target number of repetitions
     * @param performedRepCount Actual number of repetitions completed
     * @param restSeconds Rest period after the set in seconds
     * @param band The band information for Dynamic Effort exercises
     * @return Mono containing the updated set scheme
     * @throws ValidationException if set scheme data fails validation
     * @throws NoResultsFoundException if no set scheme exists with the given ID
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.STANDARD,
        entityName = "set_scheme"
    )
    fun updateSetScheme(
        id: Long,
        programmedExerciseId: Long,
        setNumber: Int,
        isAmrap: Boolean,
        isEmom: Boolean,
        useTempo: Boolean,
        eccentricTempo: String?,
        isometricTempo: String?,
        concentricTempo: String?,
        targetWeight: BigDecimal?,
        performedWeight: BigDecimal?,
        targetRepCount: Int?,
        performedRepCount: Int?,
        restSeconds: Int?,
        band: Band? = null,
    ): Mono<SetScheme> {
        logger.debug("Updating set scheme: {}", id)

        // Validate all CHECK constraints
        ValidationUtil.validateSetNumber(setNumber)
        ValidationUtil.validateTempo(eccentricTempo, "Eccentric")
        ValidationUtil.validateTempo(isometricTempo, "Isometric")
        ValidationUtil.validateTempo(concentricTempo, "Concentric")
        ValidationUtil.validateTargetWeight(targetWeight)
        ValidationUtil.validatePerformedWeight(performedWeight)
        ValidationUtil.validateTargetRepCount(targetRepCount)
        ValidationUtil.validatePerformedRepCount(performedRepCount)
        ValidationUtil.validateRestSeconds(restSeconds)

        // First perform the update without returning data
        return postgresClient.updateLiteral(
            """
            UPDATE set_scheme
            SET programmed_exercise_id=$2, set_number=$3, is_amrap=$4, is_emom=$5, use_tempo=$6,
                eccentric_tempo=$7, isometric_tempo=$8, concentric_tempo=$9, target_weight=$10, performed_weight=$11,
                target_rep_count=$12, performed_rep_count=$13, rest_seconds=$14, band_weight_lbs=$15, updated_at=NOW()
            WHERE id=$1
            RETURNING id, programmed_exercise_id, set_number, is_amrap, is_emom, use_tempo,
                      eccentric_tempo, isometric_tempo, concentric_tempo, target_weight, performed_weight,
                      target_rep_count, performed_rep_count, rest_seconds, band_weight_lbs, created_at, updated_at
            """.trimIndent(),
            SetScheme::class,
            id,
            programmedExerciseId,
            setNumber,
            isAmrap,
            isEmom,
            useTempo,
            eccentricTempo,
            isometricTempo,
            concentricTempo,
            targetWeight,
            performedWeight,
            targetRepCount,
            performedRepCount,
            restSeconds,
            band?.weightLbs,
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
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.STANDARD,
        entityName = "set_scheme"
    )
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
