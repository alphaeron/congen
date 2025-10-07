package com.congen.service

import com.congen.client.KeycloakClient
import com.congen.client.PostgresClient
import com.congen.dal.GdprComplianceDAL
import com.congen.dal.ProgramDAL
import com.congen.dal.ProgramPreferencesDAL
import com.congen.dal.UserDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.model.User
import com.congen.model.UserConsent
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

/**
 * Test class for GdprComplianceService.
 *
 * Tests GDPR compliance functionality including:
 * - User consent management (recording and withdrawal)
 * - Data export functionality
 * - User data deletion ("right to be forgotten")
 * - Audit logging integration
 */
class GdprComplianceServiceTest {
    private lateinit var userDAL: UserDAL
    private lateinit var auditService: AuditService
    private lateinit var gdprComplianceDAL: GdprComplianceDAL
    private lateinit var userEquipmentDAL: UserEquipmentDAL
    private lateinit var userExercisePreferenceDAL: UserExercisePreferenceDAL
    private lateinit var programPreferencesDAL: ProgramPreferencesDAL
    private lateinit var userOneRepMaxService: UserOneRepMaxService
    private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL
    private lateinit var programDAL: ProgramDAL
    private lateinit var keycloakClient: KeycloakClient
    private lateinit var gdprComplianceService: GdprComplianceService

    @BeforeEach
    fun setUp() {
        userDAL = mock()
        auditService = mock()
        gdprComplianceDAL = mock()
        userEquipmentDAL = mock()
        userExercisePreferenceDAL = mock()
        programPreferencesDAL = mock()
        userOneRepMaxService = mock()
        userWeightUnitPreferenceDAL = mock()
        programDAL = mock()
        keycloakClient = mock()
        val postgresClient = mock<PostgresClient>()

        // Mock PostgresClient.withTransaction to execute the block directly
        doAnswer { invocation ->
            val block = invocation.getArgument<() -> Mono<kotlin.Unit>>(0)
            block.invoke()
        }.whenever(postgresClient).withTransaction(any<() -> Mono<kotlin.Unit>>())

        gdprComplianceService =
            GdprComplianceService(
                gdprComplianceDAL = gdprComplianceDAL,
                userDAL = userDAL,
                userEquipmentDAL = userEquipmentDAL,
                userExercisePreferenceDAL = userExercisePreferenceDAL,
                programPreferencesDAL = programPreferencesDAL,
                userOneRepMaxService = userOneRepMaxService,
                userWeightUnitPreferenceDAL = userWeightUnitPreferenceDAL,
                programDAL = programDAL,
                auditService = auditService,
                keycloakClient = keycloakClient,
                postgresClient = postgresClient
            )
    }

    private fun stubAuditService() {
        whenever(
            auditService.logDataOperation(
                any(),
                any(),
                any(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(Mono.just(Unit))

        whenever(
            auditService.logConsentChange(
                any(),
                any(),
                any()
            )
        ).thenReturn(Mono.just(Unit))
    }

    @Test
    fun `recordConsent should record consent and log audit successfully`() {
        stubAuditService()

        val keycloakId = "test-user-id"
        val consent = true
        val consentTimestamp = Instant.now()

        val userConsent =
            UserConsent(
                keycloakId = keycloakId,
                dataProcessingConsent = consent,
                consentTimestamp = consentTimestamp,
                createdAt = consentTimestamp,
                updatedAt = consentTimestamp
            )

        whenever(gdprComplianceDAL.updateUserConsent(keycloakId, consent))
            .thenReturn(Mono.just(userConsent))

        StepVerifier.create(
            gdprComplianceService.updateUserConsent(keycloakId, consent)
        )
            .expectNext(userConsent)
            .verifyComplete()

        verify(gdprComplianceDAL).updateUserConsent(keycloakId, consent)
        verify(auditService).logDataOperation(
            eq(keycloakId),
            eq("CONSENT_GIVEN"),
            eq("USER_CONSENT"),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `recordConsent should record consent withdrawal with CONSENT_WITHDRAWN operation`() {
        stubAuditService()

        val keycloakId = "test-user-id"
        val consent = false
        val consentTimestamp = Instant.now()

        val userConsent =
            UserConsent(
                keycloakId = keycloakId,
                dataProcessingConsent = consent,
                consentTimestamp = consentTimestamp,
                createdAt = consentTimestamp,
                updatedAt = consentTimestamp
            )

        whenever(gdprComplianceDAL.updateUserConsent(keycloakId, consent))
            .thenReturn(Mono.just(userConsent))

        StepVerifier.create(
            gdprComplianceService.updateUserConsent(keycloakId, consent)
        )
            .expectNext(userConsent)
            .verifyComplete()

        verify(gdprComplianceDAL).updateUserConsent(keycloakId, consent)
        verify(auditService).logDataOperation(
            eq(keycloakId),
            eq("CONSENT_WITHDRAWN"),
            eq("USER_CONSENT"),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `exportUserData should export user data and log audit successfully`() {
        stubAuditService()

        val keycloakId = "test-user-id"
        val user =
            User(
                keycloakId = keycloakId,
                name = "Test User",
                age = null,
                weight = null,
                height = null,
                gender = null,
                createdAt = Instant.now(),
                updatedAt = Instant.now()
            )

        val userConsent = UserConsent(keycloakId, false, null, Instant.now(), Instant.now())

        // Mock all the DAL calls to return simple, predictable values
        whenever(userDAL.selectUserByKeycloakId(keycloakId)).thenReturn(Mono.just(user))
        whenever(gdprComplianceDAL.getUserConsent(keycloakId)).thenReturn(Mono.just(userConsent))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(keycloakId)).thenReturn(Mono.just(emptyList()))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(keycloakId)).thenReturn(Mono.just(emptyList()))
        whenever(programPreferencesDAL.selectProgramPreferences(any())).thenReturn(Mono.empty())
        whenever(userOneRepMaxService.selectUserOneRepMaxByUser(keycloakId)).thenReturn(Mono.just(emptyList()))
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(keycloakId)).thenReturn(Mono.just(emptyList()))
        whenever(programDAL.selectProgramsByUserId(keycloakId)).thenReturn(Mono.just(emptyList()))
        whenever(programDAL.selectProgramsWithWorkoutHierarchyByUserId(keycloakId)).thenReturn(Mono.just(emptyList()))
        whenever(gdprComplianceDAL.getUserAuditLogs(keycloakId)).thenReturn(Mono.just(emptyList()))
        whenever(gdprComplianceDAL.getDataRetentionPolicies()).thenReturn(Mono.just(emptyList()))

        StepVerifier.create(
            gdprComplianceService.exportUserData(keycloakId)
        )
            .expectNextCount(1)
            .verifyComplete()

        verify(auditService).logDataOperation(
            eq(keycloakId),
            eq("DATA_EXPORT"),
            eq("ALL_USER_DATA"),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `deleteAllPersonalData should delete all user data and log audit successfully`() {
        stubAuditService()

        val keycloakId = "test-user-id"

        whenever(userDAL.deleteUserByKeycloakId(keycloakId)).thenReturn(Mono.empty())
        whenever(keycloakClient.deleteUser(keycloakId)).thenReturn(Mono.empty())

        StepVerifier.create(
            gdprComplianceService.deleteAllPersonalData(keycloakId)
        )
            .verifyComplete()

        verify(auditService).logDataOperation(
            eq(keycloakId),
            eq("DATA_DELETION_STARTED"),
            eq("ALL_USER_DATA"),
            anyOrNull(),
            anyOrNull()
        )
        verify(userDAL).deleteUserByKeycloakId(keycloakId)
        verify(keycloakClient).deleteUser(keycloakId)
    }

    @Test
    fun `deleteAllPersonalData should log failure when deletion fails`() {
        // Mock the audit service for this specific test - need to mock all three calls
        whenever(
            auditService.logDataOperation(
                any(),
                eq("DATA_DELETION_STARTED"),
                any(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(Mono.just(Unit))

        whenever(
            auditService.logDataOperation(
                any(),
                eq("DATA_DELETION_FAILED"),
                any(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(Mono.just(Unit))

        val keycloakId = "test-user-id"
        val error = RuntimeException("Database error")

        whenever(userDAL.deleteUserByKeycloakId(keycloakId)).thenReturn(Mono.error(error))
        // Mock KeycloakClient.deleteUser to return a successful Mono since the first operation fails
        whenever(keycloakClient.deleteUser(keycloakId)).thenReturn(Mono.empty())

        StepVerifier.create(gdprComplianceService.deleteAllPersonalData(keycloakId))
            .verifyError(RuntimeException::class.java)

        verify(userDAL).deleteUserByKeycloakId(keycloakId)

        // Verify audit logs were recorded for start and failure
        verify(auditService).logDataOperation(
            eq(keycloakId),
            eq("DATA_DELETION_STARTED"),
            eq("ALL_USER_DATA"),
            anyOrNull(),
            anyOrNull()
        )
        verify(auditService).logDataOperation(
            eq(keycloakId),
            eq("DATA_DELETION_FAILED"),
            eq("ALL_USER_DATA"),
            anyOrNull(),
            anyOrNull()
        )
    }

    @Test
    fun `getUserConsent should return true when user has consent`() {
        val keycloakId = "test-user-id"
        val userConsent = UserConsent(keycloakId, true, Instant.now(), Instant.now(), Instant.now())

        whenever(gdprComplianceDAL.getUserConsent(keycloakId)).thenReturn(Mono.just(userConsent))

        StepVerifier.create(gdprComplianceService.hasUserConsent(keycloakId))
            .expectNext(true)
            .verifyComplete()

        verify(gdprComplianceDAL).getUserConsent(keycloakId)
    }

    @Test
    fun `getUserConsent should return false when user has no consent`() {
        val keycloakId = "test-user-id"
        val userConsent = UserConsent(keycloakId, false, Instant.now(), Instant.now(), Instant.now())

        whenever(gdprComplianceDAL.getUserConsent(keycloakId)).thenReturn(Mono.just(userConsent))

        StepVerifier.create(gdprComplianceService.hasUserConsent(keycloakId))
            .expectNext(false)
            .verifyComplete()

        verify(gdprComplianceDAL).getUserConsent(keycloakId)
    }
}
