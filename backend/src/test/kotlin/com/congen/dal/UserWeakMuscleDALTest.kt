package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.UserWeakMuscle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.sql.SQLException
import java.time.Instant

/**
 * Unit tests for UserWeakMuscleDAL.
 */
class UserWeakMuscleDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var dal: UserWeakMuscleDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock(PostgresClient::class.java)
        dal = UserWeakMuscleDAL(postgresClient)
    }

    @Test
    fun `should insert and select user weak muscle`() {
        val now = Instant.now()
        val userWeakMuscle = UserWeakMuscle("test-keycloak-user-id", "hamstrings", now)
        val insertQuery =
            """
            INSERT INTO user_weak_muscle (user_id, muscle_name)
            VALUES ($1, $2)
            """.trimIndent()
        val selectQuery = "SELECT * FROM user_weak_muscle WHERE user_id=$1"
        // Mock with exact arguments, no matchers
        whenever(
            postgresClient.update<UserWeakMuscle>(insertQuery, "test-keycloak-user-id", "hamstrings")
        ).thenReturn(Mono.just(userWeakMuscle))
        whenever(postgresClient.select<UserWeakMuscle>(selectQuery, "test-keycloak-user-id")).thenReturn(Mono.just(listOf(userWeakMuscle)))

        StepVerifier.create(dal.insertUserWeakMuscle("test-keycloak-user-id", "hamstrings"))
            .expectNext(userWeakMuscle)
            .verifyComplete()

        StepVerifier.create(dal.selectUserWeakMusclesByUser("test-keycloak-user-id"))
            .assertNext { list ->
                assertEquals(1, list.size)
                assertEquals(userWeakMuscle, list[0])
            }
            .verifyComplete()
    }

    @Test
    fun `should delete user weak muscle`() {
        val now = Instant.now()
        val userWeakMuscle = UserWeakMuscle("test-keycloak-user-id-2", "glutes", now)
        val deleteQuery = "DELETE FROM user_weak_muscle WHERE user_id=$1 AND muscle_name=$2"
        whenever(
            postgresClient.update<UserWeakMuscle>(deleteQuery, "test-keycloak-user-id-2", "glutes")
        ).thenReturn(Mono.just(userWeakMuscle))

        StepVerifier.create(dal.deleteUserWeakMuscle("test-keycloak-user-id-2", "glutes"))
            .expectNext(userWeakMuscle)
            .verifyComplete()
    }

    @Test
    fun `should handle error on insert`() {
        val insertQuery =
            """
            INSERT INTO user_weak_muscle (user_id, muscle_name)
            VALUES ($1, $2)
            """.trimIndent()
        whenever(
            postgresClient.update<UserWeakMuscle>(insertQuery, "test-keycloak-user-id-3", "lats")
        ).thenReturn(Mono.error(SQLException("fail")))

        StepVerifier.create(dal.insertUserWeakMuscle("test-keycloak-user-id-3", "lats"))
            .expectError(SQLException::class.java)
            .verify()
    }
}
