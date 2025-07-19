package com.congen

import io.vertx.sqlclient.SqlClient
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@SpringBootTest(properties = ["spring.profiles.active=integration-test"])
@AutoConfigureWebTestClient
open class BaseIntegrationTest {
    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    @Qualifier("postgresDBWriter")
    protected lateinit var sqlClient: SqlClient

    @BeforeEach
    open fun setUp() {
        // Simplified cleanup - just truncate tables without complex verification
        cleanupDatabase()
    }

    @AfterEach
    fun tearDown() {
        // No cleanup needed after each test - let the next test handle it
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

            // Reduced timeout to 3 seconds
            if (!latch.await(3, TimeUnit.SECONDS)) {
                throw RuntimeException("Timed out waiting for database cleanup to complete")
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
