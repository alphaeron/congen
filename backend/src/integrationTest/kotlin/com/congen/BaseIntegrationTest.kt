package com.congen

import com.buralotech.oss.testcontainers.memcached.MemcachedContainer
import com.congen.components.RateLimitFilter
import com.fasterxml.jackson.databind.ObjectMapper
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
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.Base64
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
                .withEnv("KC_HEALTH_ENABLED", "true")
                .withEnv("KC_HTTP_ENABLED", "true")
                .withEnv("KC_HTTP_RELATIVE_PATH", "/")

        private val memcached = MemcachedContainer("1.6.39-alpine")

        /**
         * Gets a default test token for integration tests.
         * This is a static method that can be called from IntegrationTestHelpers.
         */
        @JvmStatic
        fun getDefaultTestToken(): String {
            val realm = "congen"
            val clientId = "congen-backend"
            val clientSecret = "congen-backend-secret"

            val tokenUrl = "${keycloak.authServerUrl}/realms/$realm/protocol/openid-connect/token"

            // Use password grant type for user operations by default
            val username = "testuser"
            val password = "testpassword"
            val requestBody = "grant_type=password&client_id=$clientId&client_secret=$clientSecret&username=$username&password=$password"

            val client = HttpClient.newHttpClient()
            val request =
                HttpRequest.newBuilder()
                    .uri(URI.create(tokenUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build()

            return try {
                val response = client.send(request, HttpResponse.BodyHandlers.ofString())
                if (response.statusCode() == 200) {
                    val jsonResponse = ObjectMapper().readTree(response.body())
                    jsonResponse.get("access_token").asText()
                } else {
                    throw RuntimeException("Failed to get token from Keycloak: ${response.statusCode()} - ${response.body()}")
                }
            } catch (e: Exception) {
                throw RuntimeException("Error getting token from Keycloak", e)
            }
        }

        @JvmStatic
        @BeforeAll
        fun startContainers() {
            postgres.start()
            keycloak.start()
            memcached.start()
        }

        @JvmStatic
        @AfterAll
        fun stopContainers() {
            memcached.stop()
            keycloak.stop()
            postgres.stop()
        }

        /**
         * Extracts the Keycloak user ID from a JWT token.
         * The user ID is in the 'sub' field of the JWT payload.
         */
        @JvmStatic
        fun getKeycloakUserIdFromToken(token: String): String {
            return try {
                val parts = token.split(".")
                if (parts.size != 3) {
                    throw RuntimeException("Invalid JWT token format")
                }

                val payload = parts[1]
                // Add padding if needed
                val paddedPayload = payload + "=".repeat((4 - payload.length % 4) % 4)
                val decodedBytes = Base64.getUrlDecoder().decode(paddedPayload)
                val payloadJson = String(decodedBytes, Charsets.UTF_8)

                val jsonNode = ObjectMapper().readTree(payloadJson)
                jsonNode.get("sub").asText()
            } catch (e: Exception) {
                throw RuntimeException("Failed to extract Keycloak user ID from token", e)
            }
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
            registry.add("congen.keycloak.management.url") { "http://localhost:${keycloak.getMappedPort(9000)}" }
            registry.add("congen.keycloak.realm") { "congen" }
            registry.add("congen.keycloak.client.id") { "congen-backend" }
            registry.add("congen.keycloak.client.secret") { "congen-backend-secret" }
            registry.add("congen.keycloak.service_account.username") { "service-account-congen-backend" }

            // JWT configuration properties
            registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri") { "${keycloak.authServerUrl}/realms/congen" }
            registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri") {
                "${keycloak.authServerUrl}/realms/congen/protocol/openid-connect/certs"
            }
            // Configure audiences to match the service account token audiences
            registry.add("spring.security.oauth2.resourceserver.jwt.audiences") { "realm-management,account" }

            // Environment variables for JWT configuration
            registry.add("KEYCLOAK_URL") { keycloak.authServerUrl }
            registry.add("KEYCLOAK_REALM") { "congen" }
            registry.add("KEYCLOAK_CLIENT_ID") { "congen-backend" }
            registry.add("KEYCLOAK_CLIENT_SECRET") { "congen-backend-secret" }
            registry.add("KEYCLOAK_SERVICE_ACCOUNT_USERNAME") { "service-account-congen-backend" }

            registry.add("congen.encryption.key") { "dGVzdC1lbmNyeXB0aW9uLWtleS1mb3ItaW50ZWdyYXQ=" }
            registry.add("congen.gdpr.audit-enabled") { "true" }
            registry.add("congen.gdpr.data-retention-check-enabled") { "true" }

            // Memcached properties
            registry.add("memcached.host") { memcached.host }
            registry.add("memcached.port") { memcached.getMappedPort(11211) }
            registry.add("memcached.connection-pool-size") { 1 }
            registry.add("memcached.op-timeout") { 1000L }
            registry.add("memcached.max-queued-noreply") { 1000 }
            registry.add("memcached.scheduler-thread-pool-size") { 2 }
            registry.add("memcached.scheduler-queue-capacity") { 100 }
            registry.add("memcached.scheduler-thread-name-prefix") { "test-memcached-scheduler" }
            registry.add("memcached.use-elasticache") { false }
            registry.add("memcached.poll-config-interval-ms") { 30000L }
        }
    }

    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    @Qualifier("postgresDBWriter")
    protected lateinit var sqlClient: SqlClient

    @Autowired
    protected lateinit var rateLimitFilter: RateLimitFilter

    @BeforeEach
    open fun setUp() {
        // Reset rate limiting state before each test to ensure isolation
        rateLimitFilter.resetRateLimitState()
        // Database cleanup will be handled after Spring context is initialized
    }

    /**
     * Creates a test user in Keycloak for integration tests.
     * This ensures the user has no required actions and is fully set up.
     */
    protected fun createTestUserInKeycloak(
        username: String,
        password: String
    ): String {
        val realm = "congen"
        val clientId = "congen-backend"
        val clientSecret = "congen-backend-secret"

        val tokenUrl = "${keycloak.authServerUrl}/realms/$realm/protocol/openid-connect/token"
        val adminUrl = "${keycloak.authServerUrl}/admin/realms/$realm/users"

        // First get service account token
        val client = HttpClient.newHttpClient()
        val tokenRequestBody = "grant_type=client_credentials&client_id=$clientId&client_secret=$clientSecret"

        val tokenRequest =
            HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(tokenRequestBody))
                .build()

        val tokenResponse = client.send(tokenRequest, HttpResponse.BodyHandlers.ofString())
        if (tokenResponse.statusCode() != 200) {
            throw RuntimeException("Failed to get service account token: ${tokenResponse.statusCode()} - ${tokenResponse.body()}")
        }

        val tokenJson = ObjectMapper().readTree(tokenResponse.body())
        val accessToken = tokenJson.get("access_token").asText()

        // Create user with no required actions and no roles (users have no roles by default)
        val userRequest =
            """
            {
                "username": "$username",
                "email": "$username@test.com",
                "firstName": "Test",
                "lastName": "User",
                "enabled": true,
                "emailVerified": true,
                "requiredActions": [],
                "realmRoles": [
                    "default-roles-congen"
                ],
                "credentials": [
                    {
                        "type": "password",
                        "value": "$password",
                        "temporary": false
                    }
                ]
            }
            """.trimIndent()

        val userRequestHttp =
            HttpRequest.newBuilder()
                .uri(URI.create(adminUrl))
                .header("Authorization", "Bearer $accessToken")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(userRequest))
                .build()

        val userResponse = client.send(userRequestHttp, HttpResponse.BodyHandlers.ofString())
        if (userResponse.statusCode() != 201) {
            throw RuntimeException("Failed to create test user: ${userResponse.statusCode()} - ${userResponse.body()}")
        }

        // Extract user ID from Location header
        val location = userResponse.headers().firstValue("Location").orElse(null)
        if (location == null) {
            throw RuntimeException("No Location header returned when creating user")
        }

        return location.substringAfterLast("/")
    }

    @AfterEach
    fun tearDown() {
        // Clean up database after each test
        cleanupDatabase()
    }

    /**
     * Gets a valid JWT token for testing authentication using client credentials.
     * This method uses the service account to get a token with the specified role.
     *
     * NOTE: Client credentials grant is used ONLY FOR TESTING purposes. In production,
     * this would be for machine-to-machine communication, not user authentication.
     * The proper OAuth2 flow for production would be the authorization code flow
     * with PKCE (Proof Key for Code Exchange) for public clients.
     */
    protected fun getValidToken(role: String = "user"): String {
        val realm = "congen"
        val clientId = "congen-backend"
        val clientSecret = "congen-backend-secret"

        val tokenUrl = "${keycloak.authServerUrl}/realms/$realm/protocol/openid-connect/token"

        val client = HttpClient.newHttpClient()
        val requestBody: String

        when (role) {
            "user" -> {
                // Use password grant type for user operations
                val username = "testuser-${System.nanoTime()}"
                val password = "testpassword"

                // Create a fresh test user to avoid required actions issues
                val finalUsername =
                    try {
                        createTestUserInKeycloak(username, password)
                        username
                    } catch (e: Exception) {
                        // If user creation fails, fall back to the pre-configured user
                        "testuser"
                    }

                requestBody = "grant_type=password&client_id=$clientId&client_secret=$clientSecret" +
                    "&username=$finalUsername&password=$password"
            }
            "admin" -> {
                // Use password grant type for admin operations
                val username = "admin"
                val password = "admin"
                requestBody = "grant_type=password&client_id=$clientId&client_secret=$clientSecret&username=$username&password=$password"
            }
            else -> {
                // Use client credentials grant type for service operations
                requestBody = "grant_type=client_credentials&client_id=$clientId&client_secret=$clientSecret"
            }
        }

        val request =
            HttpRequest.newBuilder()
                .uri(URI.create(tokenUrl))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build()

        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                val jsonResponse = ObjectMapper().readTree(response.body())
                jsonResponse.get("access_token").asText()
            } else {
                throw RuntimeException("Failed to get token from Keycloak: ${response.statusCode()} - ${response.body()}")
            }
        } catch (e: Exception) {
            throw RuntimeException("Error getting token from Keycloak", e)
        }
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
