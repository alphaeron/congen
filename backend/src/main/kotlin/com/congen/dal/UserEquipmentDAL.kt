package com.congen.dal

import com.congen.cache.annotation.Cacheable
import com.congen.cache.annotation.CacheEvict
import com.congen.cache.CacheTTL
import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheInvalidationStrategy
import com.congen.client.PostgresClient
import com.congen.model.UserEquipment
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for UserEquipment entity operations.
 *
 * This class provides database operations for the UserEquipment entity in the Congen application.
 * UserEquipment represents the relationship between users and the equipment they have available for workouts.
 *
 * ## UserEquipment Entity
 *
 * UserEquipment represents:
 * - Association between a user and a piece of equipment
 * - Used for filtering exercises and generating personalized workouts
 *
 * ## Database Operations
 *
 * - **Select by user and equipment**: Retrieve a specific user-equipment relationship
 * - **Select by user**: Retrieve all equipment for a specific user
 * - **Insert**: Create new user-equipment relationships
 * - **Delete**: Remove user-equipment relationships
 *
 * ## Error Handling
 *
 * - **NoResultsFoundException**: When user-equipment relationship doesn't exist
 * - **DatabaseException**: When database operations fail
 *
 * @property postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class UserEquipmentDAL(
    private val postgresClient: PostgresClient,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(UserEquipmentDAL::class.java)
    }

    /**
     * Retrieves a specific user-equipment relationship.
     *
     * This method queries the database to find the relationship between the specified user and equipment.
     * If no relationship exists, a NoResultsFoundException is thrown.
     *
     * @param userId The Keycloak identifier of the user
     * @param equipmentName The name of the equipment
     * @return Mono containing the user-equipment relationship if found
     * @throws NoResultsFoundException when the relationship doesn't exist
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.RELATIONSHIP,
        entityName = "user_equipment"
    )
    fun selectUserEquipment(
        userId: String,
        equipmentName: String,
    ): Mono<UserEquipment> {
        logger.debug("Selecting user equipment: {} - {}", userId, equipmentName)
        return postgresClient.selectIndividual(
            "SELECT * FROM user_equipment WHERE user_id=$1 AND equipment_name=$2",
            userId,
            equipmentName,
        )
    }

    /**
     * Retrieves all equipment for a specific user.
     *
     * This method fetches all equipment that is associated with the specified user.
     * If no equipment exists for the user, an empty list is returned.
     *
     * @param userId The Keycloak identifier of the user
     * @return Mono containing a list of user-equipment relationships
     */
    @Cacheable(
        ttl = CacheTTL.USER_DATA,
        keyStrategy = CacheKeyStrategy.USER_SPECIFIC,
        entityName = "user_equipment"
    )
    fun selectUserEquipmentByUser(userId: String): Mono<List<UserEquipment>> {
        logger.debug("Selecting equipment for user: {}", userId)
        return postgresClient.select(
            "SELECT * FROM user_equipment WHERE user_id=$1",
            userId,
        )
    }

    /**
     * Creates a new user-equipment relationship in the database.
     *
     * This method inserts a new relationship between the specified user and equipment.
     * The combination of user ID and equipment name must be unique.
     *
     * @param userId The Keycloak identifier of the user
     * @param equipmentName The name of the equipment
     * @return Mono containing the created user-equipment relationship
     * @throws DatabaseException when the relationship already exists or database operation fails
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.RELATIONSHIP,
        entityName = "user_equipment"
    )
    fun insertUserEquipment(
        userId: String,
        equipmentName: String,
    ): Mono<UserEquipment> {
        logger.debug("Inserting user equipment: {} - {}", userId, equipmentName)
        return postgresClient.update(
            """
            INSERT INTO user_equipment
                (user_id, equipment_name)
            VALUES
                ($1, $2)
            """.trimIndent(),
            userId,
            equipmentName,
        )
    }

    /**
     * Deletes a user-equipment relationship from the database.
     *
     * This method removes the relationship between the specified user and equipment.
     * If no relationship exists, a NoResultsFoundException is thrown.
     *
     * @param userId The Keycloak identifier of the user
     * @param equipmentName The name of the equipment
     * @return Mono containing the deleted user-equipment relationship
     * @throws NoResultsFoundException when the relationship doesn't exist
     */
    @CacheEvict(
        invalidationStrategy = CacheInvalidationStrategy.RELATIONSHIP,
        entityName = "user_equipment"
    )
    fun deleteUserEquipment(
        userId: String,
        equipmentName: String,
    ): Mono<UserEquipment> {
        logger.debug("Deleting user equipment: {} - {}", userId, equipmentName)
        return postgresClient.update(
            "DELETE FROM user_equipment WHERE user_id=$1 AND equipment_name=$2",
            userId,
            equipmentName,
        )
    }
}
