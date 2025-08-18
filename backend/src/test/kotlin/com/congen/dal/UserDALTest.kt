package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.mockUser
import com.congen.model.AuditLog
import com.congen.model.User
import com.congen.service.AuditService
import com.congen.util.EncryptionUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import org.mockito.kotlin.any as anyKotlin

class UserDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var encryptionUtil: EncryptionUtil
    private lateinit var auditService: AuditService
    private lateinit var userDAL: UserDAL

    private val user = mockUser()
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
                "created_at" to java.time.Instant.parse("2024-01-01T00:00:00Z"),
                "updated_at" to java.time.Instant.parse("2024-01-01T00:00:00Z")
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
        val insertUser = mockUser(keycloakId = "0")
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
                anyKotlin(),
                anyKotlin(),
                anyKotlin(),
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
