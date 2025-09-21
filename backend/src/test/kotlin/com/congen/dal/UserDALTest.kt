package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockUser
import com.congen.model.User
import com.congen.service.AuditService
import com.congen.util.EncryptionUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

class UserDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var encryptionUtil: EncryptionUtil
    private lateinit var auditService: AuditService
    private lateinit var userDAL: UserDAL

    private val user = mockUser(name = "Test User")
    private val users = listOf(user)

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        encryptionUtil = mock()
        auditService = mock()

        // Mock PostgresClient.withTransaction to execute the block directly
        doAnswer { invocation ->
            val block = invocation.getArgument<() -> Mono<User>>(0)
            block.invoke()
        }.whenever(postgresClient).withTransaction(any<() -> Mono<User>>())

        userDAL = UserDAL(postgresClient, encryptionUtil, auditService)
    }

    @Test
    fun `selectUserByKeycloakId should return user`() {
        val mockRow =
            mapOf(
                "keycloak_id" to user.keycloakId,
                "name" to user.name,
                "age" to null,
                "weight" to null,
                "height" to null,
                "created_at" to Instant.parse("2024-01-01T00:00:00Z"),
                "updated_at" to Instant.parse("2024-01-01T00:00:00Z")
            )
        whenever(postgresClient.selectIndividual("SELECT * FROM \"user\" WHERE keycloak_id=$1", Map::class, user.keycloakId))
            .thenReturn(Mono.just(mockRow))
        whenever(encryptionUtil.decrypt(user.name)).thenReturn(user.name)
        whenever(auditService.logDataAccess(user.keycloakId, "USER_PROFILE", "SYSTEM"))
            .thenReturn(Mono.just(Unit))

        val result = userDAL.selectUserByKeycloakId(user.keycloakId)

        StepVerifier.create(result)
            .expectNext(user)
            .verifyComplete()
        verify(postgresClient).selectIndividual("SELECT * FROM \"user\" WHERE keycloak_id=$1", Map::class, user.keycloakId)
    }

    @Test
    fun `insertUser should return inserted user`() {
        val insertUser = mockUser(keycloakId = "0", name = "Test User")
        whenever(encryptionUtil.encrypt(insertUser.name)).thenReturn("encrypted_name")
        val mockInsertedRow = mapOf(
            "keycloak_id" to insertUser.keycloakId,
            "name" to "encrypted_name",
            "age" to null,
            "weight" to null,
            "height" to null,
            "created_at" to Instant.parse("2024-01-01T00:00:00Z"),
            "updated_at" to Instant.parse("2024-01-01T00:00:00Z")
        )
        whenever(
            postgresClient.update(
                """
                INSERT INTO "user"
                    (keycloak_id, name, age, weight, height)
                VALUES
                    ($1, $2, $3, $4, $5)
                """.trimIndent(),
                Map::class,
                insertUser.keycloakId,
                "encrypted_name",
                null,
                null,
                null
            )
        ).thenReturn(Mono.just(mockInsertedRow))
        whenever(
            auditService.logDataOperation(
                any(),
                any(),
                any(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(Mono.just(Unit))

        val result = userDAL.insertUser(insertUser.keycloakId, insertUser.name)

        StepVerifier.create(result)
            .assertNext { returnedUser ->
                assertEquals(insertUser.keycloakId, returnedUser.keycloakId)
                assertEquals(insertUser.name, returnedUser.name)
            }
            .verifyComplete()
    }

    @Test
    fun `deleteUserByKeycloakId should delete user and return deleted user data`() {
        val keycloakId = "test-keycloak-id"

        val mockUserRow = mapOf(
            "keycloak_id" to keycloakId,
            "name" to "encrypted_name",
            "age" to null,
            "weight" to null,
            "height" to null,
            "created_at" to Instant.parse("2024-01-01T00:00:00Z"),
            "updated_at" to Instant.parse("2024-01-01T00:00:00Z")
        )
        whenever(
            postgresClient.selectIndividual(
                "SELECT * FROM \"user\" WHERE keycloak_id=$1",
                Map::class,
                keycloakId
            )
        ).thenReturn(Mono.just(mockUserRow))
        whenever(
            postgresClient.update(
                "DELETE FROM \"user\" WHERE keycloak_id=$1",
                Map::class,
                keycloakId
            )
        ).thenReturn(Mono.just(mockUserRow))
        whenever(auditService.logDataOperation(any(), any(), any(), anyOrNull(), anyOrNull()))
            .thenReturn(Mono.just(Unit))
        whenever(encryptionUtil.decrypt("encrypted_name")).thenReturn("Test User")

        val result = userDAL.deleteUserByKeycloakId(keycloakId)

        StepVerifier.create(result)
            .expectNextMatches { user ->
                user.keycloakId == keycloakId && user.name.isNotEmpty()
            }
            .verifyComplete()

        verify(postgresClient).selectIndividual(
            "SELECT * FROM \"user\" WHERE keycloak_id=$1",
            Map::class,
            keycloakId
        )
        verify(postgresClient).update(
            "DELETE FROM \"user\" WHERE keycloak_id=$1",
            Map::class,
            keycloakId
        )
        verify(auditService).logDataOperation(
            eq(keycloakId),
            eq("DATA_DELETION"),
            eq("USER_PROFILE"),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `updateUser should update user successfully`() {
        val keycloakId = "test-keycloak-id"
        val newName = "Updated User"
        val updatedUser = mockUser(keycloakId = keycloakId, name = newName)

        whenever(encryptionUtil.encrypt(any())).thenReturn("encrypted-name")
        whenever(encryptionUtil.decrypt("encrypted-name")).thenReturn(newName)
        val mockUpdatedRow = mapOf(
            "keycloak_id" to keycloakId,
            "name" to "encrypted-name",
            "age" to null,
            "weight" to null,
            "height" to null,
            "created_at" to Instant.parse("2024-01-01T00:00:00Z"),
            "updated_at" to Instant.parse("2024-01-01T00:00:00Z")
        )
        whenever(
            postgresClient.update(
                """
                UPDATE "user"
                SET name=$2, age=$3, weight=$4, height=$5, updated_at=NOW()
                WHERE keycloak_id=$1
                """.trimIndent(),
                Map::class,
                keycloakId,
                "encrypted-name",
                null,
                null,
                null
            )
        ).thenReturn(Mono.just(mockUpdatedRow))
        whenever(auditService.logDataOperation(any(), any(), any(), anyOrNull(), anyOrNull()))
            .thenReturn(Mono.just(Unit))

        val result = userDAL.updateUser(keycloakId, newName)

        StepVerifier.create(result)
            .expectNextMatches { user ->
                user.keycloakId == keycloakId && user.name == newName
            }
            .verifyComplete()

        verify(postgresClient).update(
            """
            UPDATE "user"
            SET name=$2, age=$3, weight=$4, height=$5, updated_at=NOW()
            WHERE keycloak_id=$1
            """.trimIndent(),
            Map::class,
            keycloakId,
            "encrypted-name",
            null,
            null,
            null
        )
        verify(auditService).logDataOperation(
            eq(keycloakId),
            eq("DATA_UPDATE"),
            eq("USER_PROFILE"),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `insertUser should return inserted user with physical attributes`() {
        val insertUser = mockUser(keycloakId = "0", name = "Test User", age = 30, weight = 180, height = 72)
        whenever(encryptionUtil.encrypt(insertUser.name)).thenReturn("encrypted_name")
        whenever(encryptionUtil.encrypt("30")).thenReturn("encrypted_age")
        whenever(encryptionUtil.encrypt("180")).thenReturn("encrypted_weight")
        whenever(encryptionUtil.encrypt("72")).thenReturn("encrypted_height")
        val mockInsertedRowWithAttributes = mapOf(
            "keycloak_id" to insertUser.keycloakId,
            "name" to "encrypted_name",
            "age" to "encrypted_age",
            "weight" to "encrypted_weight",
            "height" to "encrypted_height",
            "created_at" to Instant.parse("2024-01-01T00:00:00Z"),
            "updated_at" to Instant.parse("2024-01-01T00:00:00Z")
        )
        whenever(
            postgresClient.update(
                """
                INSERT INTO "user"
                    (keycloak_id, name, age, weight, height)
                VALUES
                    ($1, $2, $3, $4, $5)
                """.trimIndent(),
                Map::class,
                insertUser.keycloakId,
                "encrypted_name",
                "encrypted_age",
                "encrypted_weight",
                "encrypted_height"
            )
        ).thenReturn(Mono.just(mockInsertedRowWithAttributes))
        whenever(
            auditService.logDataOperation(
                any(),
                any(),
                any(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(Mono.just(Unit))

        val result = userDAL.insertUser(insertUser.keycloakId, insertUser.name, insertUser.age, insertUser.weight, insertUser.height)

        StepVerifier.create(result)
            .assertNext { returnedUser ->
                assertEquals(insertUser.keycloakId, returnedUser.keycloakId)
                assertEquals(insertUser.name, returnedUser.name)
                assertEquals(insertUser.age, returnedUser.age)
                assertEquals(insertUser.weight, returnedUser.weight)
                assertEquals(insertUser.height, returnedUser.height)
            }
            .verifyComplete()
    }

    @Test
    fun `updateUser should update user with physical attributes successfully`() {
        val keycloakId = "test-keycloak-id"
        val newName = "Updated User"
        val age = 31
        val weight = 185
        val height = 73
        val updatedUser = mockUser(keycloakId = keycloakId, name = newName, age = age, weight = weight, height = height)

        whenever(encryptionUtil.encrypt(newName)).thenReturn("encrypted-value")
        whenever(encryptionUtil.encrypt(age.toString())).thenReturn("encrypted-value")
        whenever(encryptionUtil.encrypt(weight.toString())).thenReturn("encrypted-value")
        whenever(encryptionUtil.encrypt(height.toString())).thenReturn("encrypted-value")
        whenever(encryptionUtil.decrypt("encrypted-value")).thenReturn("decrypted-value")
        val mockUpdatedRowWithAttributes = mapOf(
            "keycloak_id" to keycloakId,
            "name" to "encrypted-value",
            "age" to "encrypted-value",
            "weight" to "encrypted-value",
            "height" to "encrypted-value",
            "created_at" to Instant.parse("2024-01-01T00:00:00Z"),
            "updated_at" to Instant.parse("2024-01-01T00:00:00Z")
        )
        whenever(
            postgresClient.update(
                """
                UPDATE "user"
                SET name=$2, age=$3, weight=$4, height=$5, updated_at=NOW()
                WHERE keycloak_id=$1
                """.trimIndent(),
                Map::class,
                keycloakId,
                "encrypted-value",
                "encrypted-value",
                "encrypted-value",
                "encrypted-value"
            )
        ).thenReturn(Mono.just(mockUpdatedRowWithAttributes))
        whenever(auditService.logDataOperation(any(), any(), any(), anyOrNull(), anyOrNull()))
            .thenReturn(Mono.just(Unit))

        val result = userDAL.updateUser(keycloakId, newName, age, weight, height)

        StepVerifier.create(result)
            .expectNextMatches { user ->
                user.keycloakId == keycloakId && user.name == newName && user.age == age && user.weight == weight && user.height == height
            }
            .verifyComplete()

        verify(postgresClient).update(
            """
            UPDATE "user"
            SET name=$2, age=$3, weight=$4, height=$5, updated_at=NOW()
            WHERE keycloak_id=$1
            """.trimIndent(),
            Map::class,
            keycloakId,
            "encrypted-value",
            "encrypted-value",
            "encrypted-value",
            "encrypted-value"
        )
        verify(auditService).logDataOperation(
            eq(keycloakId),
            eq("DATA_UPDATE"),
            eq("USER_PROFILE"),
            anyOrNull(),
            anyOrNull()
        )
    }
}
