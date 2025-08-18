package com.congen.dal

import com.congen.cache.annotation.Cacheable
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.CacheTTL
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheInvalidationStrategy
import com.congen.client.PostgresClient
import com.congen.model.Muscle
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for Muscle entity operations.
 *
 * This class provides database operations for the Muscle entity in the Congen application.
 * Muscles represent anatomical muscle groups that can be targeted by exercises,
 * such as chest, back, legs, shoulders, arms, and core muscles.
 *
 * ## Muscle Entity
 *
 * Muscle represents:
 * - Anatomical muscle groups in the human body
 * - Name and description of the muscle group
 * - Used for exercise targeting and muscle group analysis
 *
 * ## Database Operations
 *
 * - **Select by name**: Retrieve muscle by its unique name
 * - **Select all**: Retrieve all muscle records
 * - **Insert**: Create new muscle records
 * - **Update**: Modify existing muscle descriptions
 * - **Delete**: Remove muscle records
 *
 * ## Error Handling
 *
 * - **NoResultsFoundException**: When muscle with specified name doesn't exist
 * - **DatabaseException**: When database operations fail
 *
 * @property postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class MuscleDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(MuscleDAL::class.java)
    }

    /**
     * Retrieves muscle by its unique name.
     *
     * This method queries the database to find muscle with the specified name.
     * If no muscle exists with the given name, a NoResultsFoundException is thrown.
     *
     * @param muscleName The unique name of the muscle to retrieve
     * @return Mono containing the muscle if found
     * @throws NoResultsFoundException when muscle with the specified name doesn't exist
     */
    @Cacheable(
        ttl = CacheTTL.LONG_TERM,
        keyStrategy = CacheKeyStrategy.ENTITY_BY_NAME,
        entityName = "muscle"
    )
    fun selectMuscleByName(muscleName: String): Mono<Muscle> {
        logger.debug("Selecting muscle by name: {}", muscleName)
        return postgresClient.selectIndividual(
            "SELECT * FROM muscle WHERE name=$1",
            muscleName,
        )
    }

    /**
     * Retrieves all muscle records from the database.
     *
     * This method fetches all muscle records and returns them as a list.
     * If no muscles exist, an empty list is returned.
     *
     * @return Mono containing a list of all muscles
     */
    @Cacheable(
        ttl = CacheTTL.LONG_TERM,
        keyStrategy = CacheKeyStrategy.LIST_QUERY,
        entityName = "muscle"
    )
    fun selectMuscles(): Mono<List<Muscle>> {
        logger.debug("Selecting all muscles")
        return postgresClient.select("SELECT * FROM muscle")
    }

    /**
     * Creates a new muscle record in the database.
     *
     * This method inserts a new muscle record with the provided name and description.
     * The muscle name must be unique in the database.
     *
     * @param name The name of the muscle to create
     * @param description The description of the muscle
     * @return Mono containing the created muscle
     * @throws DatabaseException when the muscle name already exists or database operation fails
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.ENTITY_BY_NAME,
        entityName = "muscle"
    )
    fun insertMuscle(
        name: String,
        description: String
    ): Mono<Muscle> {
        logger.debug("Inserting muscle: {}", name)
        return postgresClient.update(
            """
            INSERT INTO muscle
                (name, description)
            VALUES
                ($1, $2)
            """.trimIndent(),
            name,
            description,
        )
    }

    /**
     * Updates an existing muscle record in the database.
     *
     * This method modifies the description of muscle with the specified name.
     * If no muscle exists with the given name, a NoResultsFoundException is thrown.
     *
     * @param name The name of the muscle to update
     * @param description The updated description of the muscle
     * @return Mono containing the updated muscle
     * @throws NoResultsFoundException when muscle with the specified name doesn't exist
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.ENTITY_BY_NAME,
        entityName = "muscle"
    )
    fun updateMuscle(
        name: String,
        description: String
    ): Mono<Muscle> {
        logger.debug("Updating muscle: {}", name)
        return postgresClient.update(
            """
            UPDATE muscle
            SET description=$2
            WHERE name=$1
            """.trimIndent(),
            name,
            description,
        )
    }

    /**
     * Deletes a muscle record from the database.
     *
     * This method removes the muscle record with the specified name.
     * If no muscle exists with the given name, a NoResultsFoundException is thrown.
     *
     * @param muscleName The unique name of the muscle to delete
     * @return Mono containing the deleted muscle
     * @throws NoResultsFoundException when muscle with the specified name doesn't exist
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.ENTITY_BY_NAME,
        entityName = "muscle"
    )
    fun deleteMuscle(muscleName: String): Mono<Muscle> {
        logger.debug("Deleting muscle: {}", muscleName)
        return postgresClient.update(
            "DELETE FROM muscle WHERE name=$1",
            muscleName,
        )
    }
}
