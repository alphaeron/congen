package com.congen.service

import com.congen.dal.DataRetentionDAL
import com.congen.model.AuditLog
import com.congen.model.DataRetentionPolicy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Test class for DataRetentionService.
 *
 * Tests data retention functionality including:
 * - Cleanup operations and result reporting
 * - Retention policy management
 * - Impact estimation for data cleanup
 * - Audit logging integration
 */
class DataRetentionServiceTest {
    private lateinit var dataRetentionDAL: DataRetentionDAL
    private lateinit var auditService: AuditService
    private lateinit var dataRetentionService: DataRetentionService

    @BeforeEach
    fun setUp() {
        dataRetentionDAL = mock()
        auditService = mock()
        dataRetentionService = DataRetentionService(dataRetentionDAL, auditService, true)
    }

    private fun stubAuditService() {
        whenever(
            auditService.logDataOperation(
                anyOrNull(),
                any(),
                any(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(Mono.just(Unit))
    }

    @Test
    fun `cleanupExpiredData should execute cleanup and return results`() {
        stubAuditService()

        // Note: We'll test the actual return values from executeCleanupExpiredData based on the real implementation
        whenever(dataRetentionDAL.executeCleanupExpiredData())
            .thenReturn(Mono.just(emptyList()))

        StepVerifier.create(dataRetentionService.executeCleanupExpiredData())
            .expectNext(emptyList())
            .verifyComplete()

        verify(dataRetentionDAL).executeCleanupExpiredData()

        // Verify audit log was recorded with the correct parameters
        verify(auditService).logDataOperation(
            null,
            "DATA_RETENTION_CLEANUP",
            "ALL_DATA_TYPES",
            null,
            "Deleted 0 total records across 0 data types"
        )
    }

    @Test
    fun `getRetentionPolicies should return all policies`() {
        whenever(dataRetentionDAL.getAllRetentionPolicies()).thenReturn(Mono.just(emptyList()))

        StepVerifier.create(dataRetentionService.getAllRetentionPolicies())
            .expectNext(emptyList())
            .verifyComplete()

        verify(dataRetentionDAL).getAllRetentionPolicies()
    }

    @Test
    fun `estimateCleanupImpact should return count estimate for audit logs`() {
        val dataType = "AUDIT_LOGS"
        val expectedCount = 150

        whenever(dataRetentionDAL.estimateAuditLogCleanup()).thenReturn(Mono.just(expectedCount))

        StepVerifier.create(dataRetentionService.estimateCleanupImpact(dataType))
            .expectNext(expectedCount)
            .verifyComplete()

        verify(dataRetentionDAL).estimateAuditLogCleanup()
    }

    @Test
    fun `estimateCleanupImpact should return count estimate for consent records`() {
        val dataType = "CONSENT_RECORDS"
        val expectedCount = 25

        whenever(dataRetentionDAL.estimateConsentHistoryCleanup()).thenReturn(Mono.just(expectedCount))

        StepVerifier.create(dataRetentionService.estimateCleanupImpact(dataType))
            .expectNext(expectedCount)
            .verifyComplete()

        verify(dataRetentionDAL).estimateConsentHistoryCleanup()
    }

    @Test
    fun `estimateCleanupImpact should return 0 for unknown data type`() {
        val dataType = "UNKNOWN_TYPE"

        StepVerifier.create(dataRetentionService.estimateCleanupImpact(dataType))
            .expectNext(0)
            .verifyComplete()

        // No DAL method should be called for unknown data type
        verify(dataRetentionDAL, never()).estimateAuditLogCleanup()
        verify(dataRetentionDAL, never()).estimateConsentHistoryCleanup()
    }

    @Test
    fun `estimateCleanupImpact should handle database errors gracefully`() {
        val dataType = "AUDIT_LOGS"

        whenever(dataRetentionDAL.estimateAuditLogCleanup())
            .thenReturn(Mono.error(RuntimeException("Database connection failed")))

        StepVerifier.create(dataRetentionService.estimateCleanupImpact(dataType))
            .expectError(RuntimeException::class.java)
            .verify()

        verify(dataRetentionDAL).estimateAuditLogCleanup()
    }

    @Test
    fun `updateRetentionPolicy should update policy and log operation`() {
        // Mock the audit service for this specific test
        whenever(
            auditService.logDataOperation(
                anyOrNull(),
                any(),
                any(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(Mono.just(Unit))

        val dataType = "USER_CONSENT"
        val retentionPeriodDays = 2555 // 7 years
        val description = "GDPR compliance: 7 year retention for consent records"

        // Create a mock DataRetentionPolicy to return
        val mockPolicy =
            DataRetentionPolicy(
                dataType = dataType,
                retentionPeriodDays = retentionPeriodDays,
                description = description
            )

        whenever(dataRetentionDAL.upsertRetentionPolicy(dataType, retentionPeriodDays, description))
            .thenReturn(Mono.just(mockPolicy))

        StepVerifier.create(dataRetentionService.upsertRetentionPolicy(dataType, retentionPeriodDays, description))
            .expectNext(mockPolicy)
            .verifyComplete()

        verify(dataRetentionDAL).upsertRetentionPolicy(dataType, retentionPeriodDays, description)

        // Verify audit log was recorded with the correct parameters
        verify(auditService).logDataOperation(
            null,
            "RETENTION_POLICY_UPDATE",
            dataType,
            null,
            "Updated retention period to $retentionPeriodDays days"
        )
    }
}
