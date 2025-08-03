package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockUser
import com.congen.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class UserDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var userDAL: UserDAL

    private val user = mockUser()
    private val users = listOf(user)

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        userDAL = UserDAL(postgresClient)
    }

    @Test
    fun `selectUserByKeycloakId should return user`() {
        whenever(postgresClient.selectIndividual<User>("SELECT * FROM \"user\" WHERE keycloak_id=$1", user.keycloakId))
            .thenReturn(Mono.just(user))

        val result = userDAL.selectUserByKeycloakId(user.keycloakId)

        StepVerifier.create(result)
            .expectNext(user)
            .verifyComplete()
        verify(postgresClient).selectIndividual<User>("SELECT * FROM \"user\" WHERE keycloak_id=$1", user.keycloakId)
    }

    @Test
    fun `selectUsers should return list of users`() {
        whenever(postgresClient.select<User>("SELECT * FROM \"user\""))
            .thenReturn(Mono.just(users))

        val result = userDAL.selectUsers()

        StepVerifier.create(result)
            .expectNext(users)
            .verifyComplete()
        verify(postgresClient).select<User>("SELECT * FROM \"user\"")
    }

    @Test
    fun `insertUser should return inserted user`() {
        val insertUser = mockUser(keycloakId = "0")
        whenever(
            postgresClient.update<User>(
                """
                INSERT INTO "user"
                    (keycloak_id, name, age, height, weight)
                VALUES
                    ($1, $2, $3, $4, $5)
                """.trimIndent(),
                insertUser.keycloakId,
                insertUser.name,
                insertUser.age,
                insertUser.height,
                insertUser.weight,
            ),
        ).thenReturn(Mono.just(insertUser))

        val result = userDAL.insertUser(insertUser.keycloakId, insertUser.name, insertUser.age, insertUser.height, insertUser.weight)

        StepVerifier.create(result)
            .expectNext(insertUser)
            .verifyComplete()
        verify(postgresClient).update<User>(
            """
            INSERT INTO "user"
                (keycloak_id, name, age, height, weight)
            VALUES
                ($1, $2, $3, $4, $5)
            """.trimIndent(),
            insertUser.keycloakId,
            insertUser.name,
            insertUser.age,
            insertUser.height,
            insertUser.weight,
        )
    }

    @Test
    fun `updateUser should return updated user`() {
        val updatedUser = mockUser(age = 31)
        whenever(
            postgresClient.update<User>(
                """
                UPDATE "user"
                SET name=$2, age=$3, height=$4, weight=$5, updated_at=NOW()
                WHERE keycloak_id=$1
                """.trimIndent(),
                updatedUser.keycloakId,
                updatedUser.name,
                updatedUser.age,
                updatedUser.height,
                updatedUser.weight,
            ),
        ).thenReturn(Mono.just(updatedUser))

        val result = userDAL.updateUser(updatedUser.keycloakId, updatedUser.name, updatedUser.age, updatedUser.height, updatedUser.weight)

        StepVerifier.create(result)
            .expectNext(updatedUser)
            .verifyComplete()
        verify(postgresClient).update<User>(
            """
            UPDATE "user"
            SET name=$2, age=$3, height=$4, weight=$5, updated_at=NOW()
            WHERE keycloak_id=$1
            """.trimIndent(),
            updatedUser.keycloakId,
            updatedUser.name,
            updatedUser.age,
            updatedUser.height,
            updatedUser.weight,
        )
    }

    @Test
    fun `deleteUser should return deleted user`() {
        whenever(postgresClient.update<User>("DELETE FROM \"user\" WHERE keycloak_id=$1", user.keycloakId))
            .thenReturn(Mono.just(user))

        val result = userDAL.deleteUser(user.keycloakId)

        StepVerifier.create(result)
            .expectNext(user)
            .verifyComplete()
        verify(postgresClient).update<User>("DELETE FROM \"user\" WHERE keycloak_id=$1", user.keycloakId)
    }
}
