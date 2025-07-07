package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.User
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDateTime

class UserDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var userDAL: UserDAL
    private val now = LocalDateTime.now()

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        userDAL = UserDAL(postgresClient)
    }

    @Test
    fun `selectUserById should return user`() {
        val user =
            User(
                id = 1,
                name = "John Doe",
                age = 30,
                height = BigDecimal("180.5"),
                weight = BigDecimal("75.0"),
                createdAt = now,
                updatedAt = now
            )
        whenever(postgresClient.selectIndividual<User>("SELECT * FROM \"user\" WHERE id=$1", 1)).thenReturn(Mono.just(user))
        val result = userDAL.selectUserById(1)
        StepVerifier.create(result).expectNext(user).verifyComplete()
        verify(postgresClient).selectIndividual<User>("SELECT * FROM \"user\" WHERE id=$1", 1)
    }

    @Test
    fun `selectUsers should return list of users`() {
        val users =
            listOf(
                User(
                    id = 1,
                    name = "John Doe",
                    age = 30,
                    height = BigDecimal("180.5"),
                    weight = BigDecimal("75.0"),
                    createdAt = now,
                    updatedAt = now
                )
            )
        whenever(postgresClient.select<User>("SELECT * FROM \"user\"")).thenReturn(Mono.just(users))
        val result = userDAL.selectUsers()
        StepVerifier.create(result).expectNext(users).verifyComplete()
        verify(postgresClient).select<User>("SELECT * FROM \"user\"")
    }

    @Test
    fun `insertUser should return inserted user`() {
        val user =
            User(
                id = 0,
                name = "John Doe",
                age = 30,
                height = BigDecimal("180.5"),
                weight = BigDecimal("75.0"),
                createdAt = now,
                updatedAt = now
            )
        whenever(
            postgresClient.update<User>(
                """
                INSERT INTO "user"
                    (name, age, height, weight)
                VALUES
                    ($1, $2, $3, $4)
                """.trimIndent(),
                user.name,
                user.age,
                user.height,
                user.weight,
            ),
        ).thenReturn(Mono.just(user))
        val result = userDAL.insertUser(user.name, user.age, user.height, user.weight)
        StepVerifier.create(result).expectNext(user).verifyComplete()
        verify(postgresClient).update<User>(
            """
            INSERT INTO "user"
                (name, age, height, weight)
            VALUES
                ($1, $2, $3, $4)
            """.trimIndent(),
            user.name,
            user.age,
            user.height,
            user.weight,
        )
    }

    @Test
    fun `updateUser should return updated user`() {
        val user =
            User(
                id = 1,
                name = "John Doe",
                age = 31,
                height = BigDecimal("180.5"),
                weight = BigDecimal("75.0"),
                createdAt = now,
                updatedAt = now
            )
        whenever(
            postgresClient.update<User>(
                """
                UPDATE "user"
                SET name=$2, age=$3, height=$4, weight=$5, updated_at=NOW()
                WHERE id=$1
                """.trimIndent(),
                user.id,
                user.name,
                user.age,
                user.height,
                user.weight,
            ),
        ).thenReturn(Mono.just(user))
        val result = userDAL.updateUser(user.id, user.name, user.age, user.height, user.weight)
        StepVerifier.create(result).expectNext(user).verifyComplete()
        verify(postgresClient).update<User>(
            """
            UPDATE "user"
            SET name=$2, age=$3, height=$4, weight=$5, updated_at=NOW()
            WHERE id=$1
            """.trimIndent(),
            user.id,
            user.name,
            user.age,
            user.height,
            user.weight,
        )
    }

    @Test
    fun `deleteUser should return deleted user`() {
        val user =
            User(
                id = 1,
                name = "John Doe",
                age = 30,
                height = BigDecimal("180.5"),
                weight = BigDecimal("75.0"),
                createdAt = now,
                updatedAt = now
            )
        whenever(
            postgresClient.update<User>("DELETE FROM \"user\" WHERE id=$1", 1),
        ).thenReturn(Mono.just(user))
        val result = userDAL.deleteUser(1)
        StepVerifier.create(result).expectNext(user).verifyComplete()
        verify(postgresClient).update<User>("DELETE FROM \"user\" WHERE id=$1", 1)
    }
}
