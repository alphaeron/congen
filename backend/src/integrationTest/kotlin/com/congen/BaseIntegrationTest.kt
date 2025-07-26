package com.congen

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.vertx.sqlclient.SqlClient
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.MountableFile
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Base class for all integration tests.
 *
 * This class provides:
 * - TestContainers PostgreSQL database setup
 * - Dynamic property configuration for database connection
 * - Database cleanup after each test
 * - Common test utilities and configuration
 *
 * IMPORTANT: The @DirtiesContext annotation is critical for test stability.
 *
 * PROBLEM SOLVED: Previously, tests were failing with "Connection refused" errors
 * when multiple tests ran simultaneously. This was caused by:
 * - Resource contention between parallel tests within the same JVM
 * - Database connection conflicts when multiple tests tried to use the same TestContainers instance
 * - Timing issues with database cleanup interfering with running tests
 * - Keycloak container connection becoming stale when Spring context is reused
 *
 * SOLUTION: @DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
 * ensures each test class gets a fresh Spring application context, providing:
 * - Complete test isolation between test classes with no shared state
 * - Fresh database setup for each test class (Liquibase migrations run cleanly)
 * - Elimination of resource contention and timing issues
 * - Reliable Keycloak container connections
 * - Better performance than AFTER_EACH_TEST_METHOD while maintaining reliability
 *
 * This approach balances performance (context recreation per class) with test reliability,
 * which is essential for integration tests that depend on database state and external services.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class BaseIntegrationTest {
    companion object {
        private val postgres =
            PostgreSQLContainer<Nothing>("postgres:15").apply {
                withDatabaseName("postgres")
                withUsername("postgres")
                withPassword("postgres")
            }

        private val keycloak =
            KeycloakContainer()
                .withRealmImportFile("/realm_configuration.json")
                .withCopyFileToContainer(
                    MountableFile.forClasspathResource("realm_configuration.json"),
                    "/realm_configuration.json"
                )

        @JvmStatic
        @BeforeAll
        fun startContainers() {
            postgres.start()
            keycloak.start()
        }

        @JvmStatic
        @AfterAll
        fun stopContainers() {
            keycloak.stop()
            postgres.stop()
        }

        @JvmStatic
        @DynamicPropertySource
        fun registerPgProperties(registry: DynamicPropertyRegistry) {
            registry.add("congen.postgres.writer.host") { postgres.host }
            registry.add("congen.postgres.reader.host") { postgres.host }
            registry.add("congen.postgres.port") { postgres.firstMappedPort }
            registry.add("congen.postgres.username") { postgres.username }
            registry.add("congen.postgres.password") { postgres.password }
            registry.add("congen.postgres.db-name") { postgres.databaseName }
            registry.add("congen.postgres.ssl-mode") { false }

            // Liquibase database properties
            registry.add(
                "spring.datasource.url"
            ) { "jdbc:postgresql://${postgres.host}:${postgres.firstMappedPort}/${postgres.databaseName}" }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }

            // Keycloak properties
            registry.add("congen.keycloak.url") { keycloak.authServerUrl }
            registry.add("congen.keycloak.realm") { "congen" }
            registry.add("congen.keycloak.client.id") { "congen-backend" }
            registry.add("congen.keycloak.client.secret") { "congen-backend-secret" }
            registry.add("congen.keycloak.service_account.username") { "service-account-congen-backend" }
        }
    }

    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    @Qualifier("postgresDBWriter")
    protected lateinit var sqlClient: SqlClient

    @BeforeEach
    open fun setUp() {
        // Database cleanup will be handled after Spring context is initialized
    }

    @AfterEach
    fun tearDown() {
        // Clean up database after each test
        cleanupDatabase()
    }

    private fun cleanupDatabase() {
        try {
            // Simplified cleanup using a single transaction
            val cleanupSql =
                """
                TRUNCATE TABLE
                    set_scheme,
                    programmed_exercise,
                    workout_stage,
                    programmed_workout,
                    program,
                    user_exercise_preference,
                    user_program_preferences,
                    user_equipment,
                    "user",
                    exercise_rotation_history,
                    user_one_rep_max
                CASCADE;
                """.trimIndent()

            val latch = CountDownLatch(1)
            var error: Throwable? = null
            sqlClient.query(cleanupSql).execute { ar ->
                if (ar.failed()) {
                    error = ar.cause()
                }
                latch.countDown()
            }

            // Reduced timeout to 2 seconds for faster cleanup
            if (!latch.await(2, TimeUnit.SECONDS)) {
                println("Warning: Database cleanup timed out")
                return
            }
            if (error != null) {
                throw RuntimeException("Database cleanup failed", error)
            }
        } catch (e: Exception) {
            // Log the error but don't fail the test setup
            println("Warning: Database cleanup failed: ${e.message}")
        }
    }
}
