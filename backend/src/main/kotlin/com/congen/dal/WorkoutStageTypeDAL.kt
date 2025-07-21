package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.WorkoutStageType
import com.congen.model.WorkoutStageTypeEnum
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for WorkoutStageType entity operations.
 *
 * This class provides database operations for the WorkoutStageType entity in the Congen application.
 * WorkoutStageType represents the types of stages that can be part of a workout, such as warm-up, main, or cool-down.
 *
 * ## WorkoutStageType Entity
 *
 * WorkoutStageType represents:
 * - A type/category of workout stage (e.g., warm-up, main, cool-down)
 * - Used for classifying and organizing workout stages
 *
 * ## Database Operations
 *
 * - **Select by ID**: Retrieve a workout stage type by its unique identifier
 * - **Select by name**: Retrieve a workout stage type by its name
 * - **Select all**: Retrieve all workout stage types
 *
 * ## Error Handling
 *
 * - **NoResultsFoundException**: When workout stage type doesn't exist
 * - **DatabaseException**: When database operations fail
 *
 * @property postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class WorkoutStageTypeDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(WorkoutStageTypeDAL::class.java)
    }

    /**
     * Retrieves a workout stage type by its unique identifier.
     *
     * This method queries the database to find a workout stage type with the specified ID.
     * If no workout stage type exists with the given ID, a NoResultsFoundException is thrown.
     *
     * @param id The unique identifier of the workout stage type to retrieve
     * @return Mono containing the workout stage type if found
     * @throws NoResultsFoundException when the workout stage type doesn't exist
     */
    fun selectWorkoutStageTypeById(id: Int): Mono<WorkoutStageType> {
        logger.debug("Selecting workout stage type by id: {}", id)
        return postgresClient.selectIndividual(
            "SELECT * FROM workout_stage_type WHERE id=$1",
            id,
        )
    }

    /**
     * Retrieves a workout stage type by its enum type.
     *
     * This method queries the database to find a workout stage type with the specified enum type.
     * If no workout stage type exists with the given type, a NoResultsFoundException is thrown.
     *
     * @param stageType The enum type of the workout stage type to retrieve
     * @return Mono containing the workout stage type if found
     * @throws NoResultsFoundException when the workout stage type doesn't exist
     */
    fun selectWorkoutStageTypeByEnum(stageType: WorkoutStageTypeEnum): Mono<WorkoutStageType> {
        logger.debug("Selecting workout stage type by enum: {}", stageType)
        return postgresClient.selectIndividual(
            "SELECT * FROM workout_stage_type WHERE name=$1",
            stageType.displayName,
        )
    }

    /**
     * Retrieves all workout stage types from the database.
     *
     * This method fetches all workout stage type records and returns them as a list, ordered by name.
     * If no workout stage types exist, an empty list is returned.
     *
     * @return Mono containing a list of all workout stage types
     */
    fun selectWorkoutStageTypes(): Mono<List<WorkoutStageType>> {
        logger.debug("Selecting all workout stage types")
        return postgresClient.select("SELECT * FROM workout_stage_type ORDER BY name")
    }
}
