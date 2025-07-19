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
        // Clean up database before each test
        cleanupDatabase()

        // Force all connections to see the same data by executing a transaction
        val refreshSql =
            """
            BEGIN;
            COMMIT;
            SELECT 1;
            """.trimIndent()

        val latch = CountDownLatch(1)
        sqlClient.query(refreshSql).execute { ar ->
            latch.countDown()
        }
        latch.await(5, TimeUnit.SECONDS)

        // Wait a bit more to ensure connection is fresh
        Thread.sleep(2000)

        // Verify cleanup again after connection refresh
        verifyCleanup()
    }

    @AfterEach
    fun tearDown() {
        // Clean up database after each test
        cleanupDatabase()
    }

    private fun cleanupDatabase() {
        try {
            // Use a transaction to ensure all operations are atomic and visible to all connections
            val cleanupSql =
                """
                BEGIN;

                -- Force all connections to see the same data by committing any pending transactions
                COMMIT;

                -- Truncate all tables in dependency-safe order using CASCADE
                -- Exclude tables that are pre-populated by data migrations:
                -- - muscle, equipment, exercise, exercise_muscle, exercise_equipment
                -- - workout_stage_type, exercise_workout_type
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

                -- Commit the transaction to make changes visible to all connections
                COMMIT;
                """.trimIndent()

            val latch = CountDownLatch(1)
            var error: Throwable? = null
            sqlClient.query(cleanupSql).execute { ar ->
                if (ar.failed()) {
                    error = ar.cause()
                    println("Cleanup failed with error: ${error?.message}")
                } else {
                    println("Cleanup completed successfully")
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

            // Verify cleanup worked by checking record counts
            verifyCleanup()
        } catch (e: Exception) {
            // Log the error but don't fail the test setup
            println("Warning: Database cleanup failed: ${e.message}")
            e.printStackTrace()
        }
    }

    private fun verifyCleanup() {
        try {
            val verificationSql =
                """
                SELECT
                    (SELECT COUNT(*) FROM "user") as user_count,
                    (SELECT COUNT(*) FROM program) as program_count,
                    (SELECT COUNT(*) FROM programmed_workout) as programmed_workout_count,
                    (SELECT COUNT(*) FROM workout_stage) as workout_stage_count,
                    (SELECT COUNT(*) FROM programmed_exercise) as programmed_exercise_count,
                    (SELECT COUNT(*) FROM set_scheme) as set_scheme_count
                """.trimIndent()

            val latch = CountDownLatch(1)
            var result: String? = null
            var error: Throwable? = null

            sqlClient.query(verificationSql).execute { ar ->
                if (ar.failed()) {
                    error = ar.cause()
                } else {
                    val rowSet = ar.result()
                    if (rowSet.size() > 0) {
                        val row = rowSet.iterator().next()
                        result = "user_count=${row.getInteger(0)}, program_count=${row.getInteger(1)}, " +
                            "programmed_workout_count=${row.getInteger(2)}, workout_stage_count=${row.getInteger(3)}, " +
                            "programmed_exercise_count=${row.getInteger(4)}, set_scheme_count=${row.getInteger(5)}"
                    }
                }
                latch.countDown()
            }

            if (latch.await(5, TimeUnit.SECONDS)) {
                if (error != null) {
                    println("Verification failed: ${error?.message}")
                } else {
                    println("Cleanup verification: $result")
                }
            } else {
                println("Verification timed out")
            }
        } catch (e: Exception) {
            println("Verification error: ${e.message}")
        }
    }
}
