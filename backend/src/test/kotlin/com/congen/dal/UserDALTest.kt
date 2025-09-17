package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockUser
import com.congen.service.AuditService
import com.congen.util.EncryptionUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
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
        userDAL = UserDAL(postgresClient, encryptionUtil, auditService)
    }

    @Test
    fun `selectUserByKeycloakId should return user`() {
        val mockRow =
            mapOf(
                "keycloak_id" to user.keycloakId,
                "name" to user.name,
                "created_at" to Instant.parse("2024-01-01T00:00:00Z"),
                "updated_at" to Instant.parse("2024-01-01T00:00:00Z")
            )
        whenever(postgresClient.selectIndividual<Map<String, Any>>("SELECT * FROM \"user\" WHERE keycloak_id=$1", user.keycloakId))
            .thenReturn(Mono.just(mockRow))
        whenever(encryptionUtil.decrypt(user.name)).thenReturn(user.name)
        whenever(auditService.logDataAccess(user.keycloakId, "USER_PROFILE", "SYSTEM"))
            .thenReturn(Mono.just(Unit))

        val result = userDAL.selectUserByKeycloakId(user.keycloakId)

        StepVerifier.create(result)
            .expectNext(user)
            .verifyComplete()
        verify(postgresClient).selectIndividual<Map<String, Any>>("SELECT * FROM \"user\" WHERE keycloak_id=$1", user.keycloakId)
    }

    @Test
    fun `insertUser should return inserted user`() {
        val insertUser = mockUser(keycloakId = "0", name = "Test User")
        val mockRow = mapOf("id" to 1)
        whenever(encryptionUtil.encrypt(insertUser.name)).thenReturn("encrypted_name")
        whenever(
            postgresClient.update<Map<String, Any>>(
                """
                INSERT INTO "user"
                    (keycloak_id, name)
                VALUES
                    ($1, $2)
                """.trimIndent(),
                insertUser.keycloakId,
                "encrypted_name"
            ),
        ).thenReturn(Mono.just(mockRow))
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
        val userData =
            mapOf(
                "keycloak_id" to keycloakId,
                "name" to "encrypted-name",
                "created_at" to "2024-01-01T00:00:00Z",
                "updated_at" to "2024-01-01T00:00:00Z"
            )

        whenever(
            postgresClient.selectIndividual<Map<String, Any>>(
                "SELECT * FROM \"user\" WHERE keycloak_id=$1",
                keycloakId
            )
        ).thenReturn(Mono.just(userData))
        whenever(
            postgresClient.update<Map<String, Any>>(
                "DELETE FROM \"user\" WHERE keycloak_id=$1",
                keycloakId
            )
        ).thenReturn(Mono.just(mapOf("deleted" to true)))
        whenever(auditService.logDataOperation(any(), any(), any(), anyOrNull(), anyOrNull()))
            .thenReturn(Mono.just(Unit))
        whenever(encryptionUtil.decrypt(any())).thenReturn("decrypted-name")

        val result = userDAL.deleteUserByKeycloakId(keycloakId)

        StepVerifier.create(result)
            .expectNextMatches { user ->
                user.keycloakId == keycloakId && user.name.isNotEmpty()
            }
            .verifyComplete()

        verify(postgresClient).selectIndividual<Map<String, Any>>(
            "SELECT * FROM \"user\" WHERE keycloak_id=$1",
            keycloakId
        )
        verify(postgresClient).update<Map<String, Any>>(
            "DELETE FROM \"user\" WHERE keycloak_id=$1",
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

        whenever(encryptionUtil.encrypt(any())).thenReturn("encrypted-name")
        whenever(encryptionUtil.decrypt("encrypted-name")).thenReturn(newName)
        val mockRow =
            mapOf(
                "keycloak_id" to keycloakId,
                "name" to "encrypted-name",
                "created_at" to "2025-01-01T00:00:00Z",
                "updated_at" to "2025-01-01T00:00:00Z"
            )
        whenever(
            postgresClient.update<Map<String, Any>>(
                """
                UPDATE "user"
                SET name=$2, updated_at=NOW()
                WHERE keycloak_id=$1
                """.trimIndent(),
                keycloakId,
                "encrypted-name"
            )
        ).thenReturn(Mono.just(mockRow))
        whenever(auditService.logDataOperation(any(), any(), any(), anyOrNull(), anyOrNull()))
            .thenReturn(Mono.just(Unit))

        val result = userDAL.updateUser(keycloakId, newName)

        StepVerifier.create(result)
            .expectNextMatches { user ->
                user.keycloakId == keycloakId && user.name == newName
            }
            .verifyComplete()

        verify(postgresClient).update<Map<String, Any>>(
            """
            UPDATE "user"
            SET name=$2, updated_at=NOW()
            WHERE keycloak_id=$1
            """.trimIndent(),
            keycloakId,
            "encrypted-name"
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
