package com.congen.dal

import com.congen.cache.CacheKeyStrategy
import com.congen.cache.CacheTTL
import com.congen.cache.annotation.Cacheable
import com.congen.client.PostgresClient
import com.congen.model.TestProtocol
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * Data Access Layer for test protocol configuration operations.
 *
 * This DAL handles all database operations related to test protocol configurations,
 * including retrieving protocol definitions and managing test metadata.
 *
 * @param postgresClient PostgreSQL client for database operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
class TestProtocolConfigDAL(
    private val postgresClient: PostgresClient
) {
    companion object {
        private val logger = LoggerFactory.getLogger(TestProtocolConfigDAL::class.java)
    }

    /**
     * Retrieves all test protocol configurations.
     *
     * @return Mono containing list of test protocols
     */
    @Cacheable(
        ttl = CacheTTL.LONG_TERM,
        keyStrategy = CacheKeyStrategy.LIST_QUERY,
        entityName = "test_protocol_config"
    )
    fun getAllTestProtocols(): Mono<List<TestProtocol>> {
        logger.debug("Retrieving all test protocol configurations")
        
        return postgresClient.select(
            "SELECT test_name, display_name, description, unit, icon_name, is_required, " +
            "display_order, radar_chart_color, radar_chart_enabled " +
            "FROM test_protocol_config " +
            "ORDER BY display_order, test_name"
        )
    }

    /**
     * Retrieves a specific test protocol configuration by test name.
     *
     * @param testName The test name to retrieve
     * @return Mono containing the test protocol
     * @throws NoResultsFoundException when test protocol with the specified name doesn't exist
     */
    @Cacheable(
        ttl = CacheTTL.LONG_TERM,
        keyStrategy = CacheKeyStrategy.ENTITY_BY_NAME,
        entityName = "test_protocol_config"
    )
    fun getTestProtocol(testName: String): Mono<TestProtocol> {
        logger.debug("Retrieving test protocol configuration for: $testName")
        
        return postgresClient.selectIndividual(
            "SELECT test_name, display_name, description, unit, icon_name, is_required, " +
            "display_order, radar_chart_color, radar_chart_enabled " +
            "FROM test_protocol_config " +
            "WHERE test_name = $1",
            testName
        )
    }
}
