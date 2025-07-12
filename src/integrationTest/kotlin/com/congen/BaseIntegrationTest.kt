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

@SpringBootTest
@AutoConfigureWebTestClient
open class BaseIntegrationTest {
    @Autowired
    protected lateinit var webTestClient: WebTestClient

    @Autowired
    @Qualifier("postgresDBWriter")
    protected lateinit var sqlClient: SqlClient

    @BeforeEach
    open fun setUp() {
        // Clean up database before each test
        cleanupDatabase()
    }

    @AfterEach
    fun tearDown() {
        // Clean up database after each test
        cleanupDatabase()
    }

    private fun cleanupDatabase() {
        try {
            // Truncate all tables in dependency-safe order using CASCADE
            // Exclude tables that are pre-populated by data migrations:
            // - muscle, equipment, exercise, exercise_muscle, exercise_equipment
            // - workout_stage_type, exercise_workout_type
            val truncateSql =
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
            sqlClient.query(truncateSql).execute { ar ->
                if (ar.failed()) {
                    error = ar.cause()
                }
                latch.countDown()
            }
            // Wait up to 10 seconds for the operation to complete
            if (!latch.await(10, TimeUnit.SECONDS)) {
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
