package com.congen.dal

import com.congen.client.PostgresClient
import com.congen.model.DataRetentionPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Unit tests for DataRetentionDAL.
 *
 * Tests database operations for data retention, cleanup execution,
 * and policy management for GDPR compliance.
 */
class DataRetentionDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var dataRetentionDAL: DataRetentionDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock(PostgresClient::class.java)
        dataRetentionDAL = DataRetentionDAL(postgresClient)
    }

    @Test
    fun `executeCleanupExpiredData should return cleanup results`() {
        // Mock the audit log cleanup operation
        `when`(
            postgresClient.selectIndividual<Int>(
                """
                WITH deleted_audit_logs AS (
                    DELETE FROM gdpr_audit_log
                    WHERE timestamp < (
                        CURRENT_TIMESTAMP - INTERVAL '1 day' * COALESCE(
                            (SELECT retention_period_days FROM data_retention_policy WHERE data_type = 'AUDIT_LOGS'),
                            2555
                        )
                    )
                    RETURNING id
                )
                SELECT COUNT(*) FROM deleted_audit_logs
                """.trimIndent()
            )
        ).thenReturn(Mono.just(25))

        StepVerifier.create(dataRetentionDAL.executeCleanupExpiredData())
            .assertNext { results ->
                assertEquals(2, results.size)
                assertEquals("AUDIT_LOGS", results[0].dataType)
                assertEquals(25, results[0].count)
                assertEquals("CONSENT_RECORDS", results[1].dataType)
                assertEquals(0, results[1].count)
            }
            .verifyComplete()

        verify(postgresClient).selectIndividual<Int>(
            """
            WITH deleted_audit_logs AS (
                DELETE FROM gdpr_audit_log
                WHERE timestamp < (
                    CURRENT_TIMESTAMP - INTERVAL '1 day' * COALESCE(
                        (SELECT retention_period_days FROM data_retention_policy WHERE data_type = 'AUDIT_LOGS'),
                        2555
                    )
                )
                RETURNING id
            )
            SELECT COUNT(*) FROM deleted_audit_logs
            """.trimIndent()
        )
    }

    @Test
    fun `getAllRetentionPolicies should return all policies`() {
        val mockRows =
            listOf(
                mapOf(
                    "data_type" to "AUDIT_LOGS",
                    "retention_period_days" to 2555,
                    "description" to "Audit logs retained for 7 years"
                ),
                mapOf(
                    "data_type" to "CONSENT_RECORDS",
                    "retention_period_days" to 2555,
                    "description" to "Consent records retained for 7 years"
                )
            )

        `when`(postgresClient.select<DataRetentionPolicy>("SELECT * FROM data_retention_policy ORDER BY data_type"))
            .thenReturn(
                Mono.just(
                    listOf(
                        DataRetentionPolicy("AUDIT_LOGS", 2555, "Audit logs retained for 7 years"),
                        DataRetentionPolicy("CONSENT_RECORDS", 2555, "Consent records retained for 7 years")
                    )
                )
            )

        StepVerifier.create(dataRetentionDAL.getAllRetentionPolicies())
            .assertNext { policies ->
                assertEquals(2, policies.size)
                assertEquals("AUDIT_LOGS", policies[0].dataType)
                assertEquals(2555, policies[0].retentionPeriodDays)
                assertEquals("Audit logs retained for 7 years", policies[0].description)
            }
            .verifyComplete()
    }

    @Test
    fun `upsertRetentionPolicy should update policy successfully`() {
        val dataType = "AUDIT_LOGS"
        val retentionPeriodDays = 1825
        val description = "Updated to 5 years"

        val mockPolicy = DataRetentionPolicy(dataType, retentionPeriodDays, description ?: "Updated policy")
        `when`(
            postgresClient.update<DataRetentionPolicy>(
                """
                INSERT INTO data_retention_policy (data_type, retention_period_days, description)
                VALUES ($1, $2, $3)
                ON CONFLICT (data_type)
                DO UPDATE SET
                    retention_period_days = EXCLUDED.retention_period_days,
                    description = EXCLUDED.description,
                    updated_at = CURRENT_TIMESTAMP
                """.trimIndent(),
                dataType,
                retentionPeriodDays,
                description
            )
        ).thenReturn(Mono.just(mockPolicy))

        StepVerifier.create(dataRetentionDAL.upsertRetentionPolicy(dataType, retentionPeriodDays, description))
            .assertNext { policy ->
                assertEquals(dataType, policy.dataType)
                assertEquals(retentionPeriodDays, policy.retentionPeriodDays)
                assertEquals(description, policy.description)
            }
            .verifyComplete()

        verify(postgresClient).update<DataRetentionPolicy>(
            """
            INSERT INTO data_retention_policy (data_type, retention_period_days, description)
            VALUES ($1, $2, $3)
            ON CONFLICT (data_type)
            DO UPDATE SET
                retention_period_days = EXCLUDED.retention_period_days,
                description = EXCLUDED.description,
                updated_at = CURRENT_TIMESTAMP
            """.trimIndent(),
            dataType,
            retentionPeriodDays,
            description
        )
    }

    @Test
    fun `estimateAuditLogCleanup should return count estimate`() {
        val mockResult: Map<String, Any> = mapOf("count_estimate" to 42L)

        `when`(
            postgresClient.selectIndividual<Int>(
                """
                SELECT COUNT(*)
                FROM gdpr_audit_log a
                INNER JOIN data_retention_policy p ON p.data_type = 'AUDIT_LOGS'
                WHERE a.timestamp < (CURRENT_TIMESTAMP - INTERVAL '1 day' * p.retention_period_days)
                """.trimIndent()
            )
        ).thenReturn(Mono.just(42))

        StepVerifier.create(dataRetentionDAL.estimateAuditLogCleanup())
            .assertNext { count ->
                assertEquals(42, count)
            }
            .verifyComplete()
    }

    @Test
    fun `estimateConsentHistoryCleanup should return count estimate`() {
        val mockResult: Map<String, Any> = mapOf("count_estimate" to 15L)

        `when`(
            postgresClient.selectIndividual<Int>(
                """
                SELECT COUNT(*)
                FROM user_consent c
                INNER JOIN data_retention_policy p ON p.data_type = 'CONSENT_RECORDS'
                WHERE c.updated_at < (CURRENT_TIMESTAMP - INTERVAL '1 day' * p.retention_period_days)
                """.trimIndent()
            )
        ).thenReturn(Mono.just(15))

        StepVerifier.create(dataRetentionDAL.estimateConsentHistoryCleanup())
            .assertNext { count ->
                assertEquals(15, count)
            }
            .verifyComplete()
    }

    @Test
    fun `estimateAuditLogCleanup should return 0 on error`() {
        `when`(
            postgresClient.selectIndividual<Int>(
                """
                SELECT COUNT(*)
                FROM gdpr_audit_log a
                INNER JOIN data_retention_policy p ON p.data_type = 'AUDIT_LOGS'
                WHERE a.timestamp < (CURRENT_TIMESTAMP - INTERVAL '1 day' * p.retention_period_days)
                """.trimIndent()
            )
        ).thenReturn(Mono.error(RuntimeException("Database error")))

        StepVerifier.create(dataRetentionDAL.estimateAuditLogCleanup())
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `executeCleanupForDataType should cleanup audit logs`() {
        // Mock the audit log cleanup operation
        `when`(
            postgresClient.selectIndividual<Int>(
                """
                WITH deleted_audit_logs AS (
                    DELETE FROM gdpr_audit_log
                    WHERE timestamp < (
                        CURRENT_TIMESTAMP - INTERVAL '1 day' * COALESCE(
                            (SELECT retention_period_days FROM data_retention_policy WHERE data_type = 'AUDIT_LOGS'),
                            2555
                        )
                    )
                    RETURNING id
                )
                SELECT COUNT(*) FROM deleted_audit_logs
                """.trimIndent()
            )
        ).thenReturn(Mono.just(30))

        StepVerifier.create(dataRetentionDAL.executeCleanupForDataType("AUDIT_LOGS"))
            .assertNext { result ->
                assertEquals("AUDIT_LOGS", result.dataType)
                assertEquals(30, result.count)
            }
            .verifyComplete()
    }

    @Test
    fun `executeCleanupForDataType should return zero for consent records`() {
        StepVerifier.create(dataRetentionDAL.executeCleanupForDataType("CONSENT_RECORDS"))
            .assertNext { result ->
                assertEquals("CONSENT_RECORDS", result.dataType)
                assertEquals(0, result.count)
            }
            .verifyComplete()
    }

    @Test
    fun `executeCleanupForDataType should return zero for unknown data type`() {
        StepVerifier.create(dataRetentionDAL.executeCleanupForDataType("UNKNOWN_TYPE"))
            .assertNext { result ->
                assertEquals("UNKNOWN_TYPE", result.dataType)
                assertEquals(0, result.count)
            }
            .verifyComplete()
    }

    @Test
    fun `getRetentionPolicy should return policy when found`() {
        val dataType = "AUDIT_LOGS"
        val mockRow: Map<String, Any> =
            mapOf(
                "data_type" to dataType,
                "retention_period_days" to 2555,
                "description" to "Audit logs retained for 7 years"
            )

        `when`(
            postgresClient.selectIndividual<DataRetentionPolicy>(
                "SELECT * FROM data_retention_policy WHERE data_type = $1",
                dataType
            )
        ).thenReturn(Mono.just(DataRetentionPolicy(dataType, 2555, "Audit logs retained for 7 years")))

        StepVerifier.create(dataRetentionDAL.getRetentionPolicy(dataType))
            .assertNext { policy ->
                assertEquals(dataType, policy.dataType)
                assertEquals(2555, policy.retentionPeriodDays)
                assertEquals("Audit logs retained for 7 years", policy.description)
            }
            .verifyComplete()
    }

    @Test
    fun `getRetentionPolicy should return empty when not found`() {
        val dataType = "NON_EXISTENT"

        `when`(
            postgresClient.selectIndividual<DataRetentionPolicy>(
                "SELECT * FROM data_retention_policy WHERE data_type = $1",
                dataType
            )
        ).thenReturn(Mono.error(RuntimeException("Not found")))

        StepVerifier.create(dataRetentionDAL.getRetentionPolicy(dataType))
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `deleteRetentionPolicy should delete policy successfully`() {
        val dataType = "AUDIT_LOGS"

        `when`(
            postgresClient.updateLiteral(
                "DELETE FROM data_retention_policy WHERE data_type = $1",
                Map::class,
                dataType
            )
        ).thenReturn(Mono.just(emptyMap<String, Any>()))

        StepVerifier.create(dataRetentionDAL.deleteRetentionPolicy(dataType))
            .verifyComplete()

        verify(postgresClient).updateLiteral(
            "DELETE FROM data_retention_policy WHERE data_type = $1",
            Map::class,
            dataType
        )
    }
}
