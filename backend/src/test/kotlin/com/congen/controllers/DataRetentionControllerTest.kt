package com.congen.controllers

import com.congen.model.DataCleanupResult
import com.congen.model.DataRetentionPolicy
import com.congen.service.DataRetentionService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.springframework.http.HttpStatus
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Unit tests for DataRetentionController.
 *
 * Tests TTL management endpoints including policy management,
 * cleanup operations, and impact estimation.
 */
class DataRetentionControllerTest {
    private lateinit var dataRetentionService: DataRetentionService
    private lateinit var dataRetentionController: DataRetentionController

    @BeforeEach
    fun setUp() {
        dataRetentionService = mock(DataRetentionService::class.java)
        dataRetentionController = DataRetentionController(dataRetentionService)
    }

    @Test
    fun `getRetentionPolicies should return all policies`() {
        val policies =
            listOf(
                DataRetentionPolicy("AUDIT_LOGS", 2555, "Audit logs retained for 7 years"),
                DataRetentionPolicy("CONSENT_RECORDS", 2555, "Consent records retained for 7 years")
            )

        `when`(dataRetentionService.getAllRetentionPolicies()).thenReturn(Mono.just(policies))

        StepVerifier.create(dataRetentionController.getRetentionPolicies())
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.OK)
                val body = response.body!!
                assert(body.size == 2)
                assert(body[0].dataType == "AUDIT_LOGS")
                assert(body[0].retentionPeriodDays == 2555)
            }
            .verifyComplete()

        verify(dataRetentionService).getAllRetentionPolicies()
    }

    @Test
    fun `updateRetentionPolicy should update policy successfully`() {
        val dataType = "AUDIT_LOGS"
        val retentionPeriodDays = 1825
        val description = "Updated to 5 years"

        val mockPolicy = DataRetentionPolicy(dataType, retentionPeriodDays, description)
        `when`(dataRetentionService.upsertRetentionPolicy(dataType, retentionPeriodDays, description))
            .thenReturn(Mono.just(mockPolicy))

        StepVerifier.create(dataRetentionController.updateRetentionPolicy(dataType, retentionPeriodDays, description))
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.OK)
                val body = response.body!!
                assert(body.dataType == dataType)
                assert(body.retentionPeriodDays == retentionPeriodDays)
                assert(body.description == description)
            }
            .verifyComplete()

        verify(dataRetentionService).upsertRetentionPolicy(dataType, retentionPeriodDays, description)
    }

    @Test
    fun `updateRetentionPolicy should reject invalid retention period`() {
        val dataType = "AUDIT_LOGS"
        val invalidRetentionPeriod = 0

        StepVerifier.create(dataRetentionController.updateRetentionPolicy(dataType, invalidRetentionPeriod, null))
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.BAD_REQUEST)
                // Body should be null for bad request with no content
                assert(response.body == null)
            }
            .verifyComplete()

        // Verify service was not called with invalid input
        verify(dataRetentionService, never()).upsertRetentionPolicy(
            any(),
            any(),
            any()
        )
    }

    @Test
    fun `estimateCleanupImpact should return estimate for specific data type`() {
        val dataType = "AUDIT_LOGS"
        val estimatedCount = 150

        `when`(dataRetentionService.estimateCleanupImpact(dataType)).thenReturn(Mono.just(estimatedCount))

        StepVerifier.create(dataRetentionController.estimateCleanupImpact(dataType))
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.OK)
                val body = response.body!!
                assert(body.estimatedDeletions.size == 1)
                assert(body.estimatedDeletions[0].dataType == dataType)
                assert(body.estimatedDeletions[0].estimatedRecordsToDelete == estimatedCount)
            }
            .verifyComplete()

        verify(dataRetentionService).estimateCleanupImpact(dataType)
    }

    @Test
    fun `estimateCleanupImpact should return estimates for all data types when none specified`() {
        `when`(dataRetentionService.estimateCleanupImpact("AUDIT_LOGS")).thenReturn(Mono.just(100))
        `when`(dataRetentionService.estimateCleanupImpact("CONSENT_RECORDS")).thenReturn(Mono.just(50))

        StepVerifier.create(dataRetentionController.estimateCleanupImpact(null))
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.OK)
                val body = response.body!!
                assert(body.estimatedDeletions.size == 2)
                assert(body.estimatedDeletions[0].dataType == "AUDIT_LOGS")
                assert(body.estimatedDeletions[0].estimatedRecordsToDelete == 100)
                assert(body.estimatedDeletions[1].dataType == "CONSENT_RECORDS")
                assert(body.estimatedDeletions[1].estimatedRecordsToDelete == 50)
            }
            .verifyComplete()

        verify(dataRetentionService).estimateCleanupImpact("AUDIT_LOGS")
        verify(dataRetentionService).estimateCleanupImpact("CONSENT_RECORDS")
    }

    @Test
    fun `triggerManualCleanup should execute cleanup successfully`() {
        val cleanupResults =
            listOf(
                DataCleanupResult("AUDIT_LOGS", 75),
                DataCleanupResult("CONSENT_RECORDS", 25)
            )

        `when`(dataRetentionService.executeCleanupExpiredData()).thenReturn(Mono.just(cleanupResults))

        StepVerifier.create(dataRetentionController.triggerManualCleanup())
            .assertNext { response ->
                assert(response.statusCode == HttpStatus.OK)
                val body = response.body!!
                assert(body.cleanupResults.size == 2)
                assert(body.summary.totalDeleted == 100)
                assert(body.summary.dataTypesProcessed == 2)
                assert(body.cleanupResults[0].dataType == "AUDIT_LOGS")
                assert(body.cleanupResults[0].count == 75)
                assert(body.cleanupResults[1].dataType == "CONSENT_RECORDS")
                assert(body.cleanupResults[1].count == 25)
            }
            .verifyComplete()

        verify(dataRetentionService).executeCleanupExpiredData()
    }
}
