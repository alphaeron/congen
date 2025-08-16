package com.congen.service

import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.dal.GdprComplianceDAL
import com.congen.dal.ProgramDAL
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserProgramPreferencesDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.model.AuditLog
import com.congen.model.User
import com.congen.model.UserConsent
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
    private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL
    private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL
    private lateinit var exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL
    private lateinit var programDAL: ProgramDAL
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    private lateinit var workoutStageDAL: WorkoutStageDAL
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL
    private lateinit var setSchemeDAL: SetSchemeDAL
    private lateinit var gdprComplianceService: GdprComplianceService

    @BeforeEach
    fun setUp() {
        userDAL = mock()
        auditService = mock()
        gdprComplianceDAL = mock()
        userEquipmentDAL = mock()
        userExercisePreferenceDAL = mock()
        userProgramPreferencesDAL = mock()
        userOneRepMaxDAL = mock()
        userWeightUnitPreferenceDAL = mock()
        exerciseRotationHistoryDAL = mock()
        programDAL = mock()
        programmedWorkoutDAL = mock()
        workoutStageDAL = mock()
        programmedExerciseDAL = mock()
        setSchemeDAL = mock()

        gdprComplianceService = GdprComplianceService(
            gdprComplianceDAL = gdprComplianceDAL,
            userDAL = userDAL,
            userEquipmentDAL = userEquipmentDAL,
            userExercisePreferenceDAL = userExercisePreferenceDAL,
            userProgramPreferencesDAL = userProgramPreferencesDAL,
            userOneRepMaxDAL = userOneRepMaxDAL,
            userWeightUnitPreferenceDAL = userWeightUnitPreferenceDAL,
            exerciseRotationHistoryDAL = exerciseRotationHistoryDAL,
            programDAL = programDAL,
            programmedWorkoutDAL = programmedWorkoutDAL,
            workoutStageDAL = workoutStageDAL,
            programmedExerciseDAL = programmedExerciseDAL,
            setSchemeDAL = setSchemeDAL,
            auditService = auditService
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
        ).thenReturn(Mono.just(AuditLog(1L, "test-user-id", "DATA_EXPORT", "ALL_USER_DATA", null, Instant.now(), null)))

        whenever(
            auditService.logConsentChange(
                any(),
                any(),
                any()
            )
        ).thenReturn(
            Mono.just(AuditLog(1L, "test-user-id", "CONSENT_GIVEN", "data_processing", null, Instant.now(), "Consent: true"))
        )
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
            gdprComplianceService.recordConsent(keycloakId, consent)
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
            gdprComplianceService.recordConsent(keycloakId, consent)
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
        val user = User(
            keycloakId = keycloakId,
            name = "Test User",
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )

        val userConsent = UserConsent(keycloakId, false, null, Instant.now(), Instant.now())

        // Mock all the DAL calls to return simple, predictable values
        whenever(userDAL.selectUserByKeycloakId(keycloakId)).thenReturn(Mono.just(user))
        whenever(gdprComplianceDAL.getUserConsent(keycloakId)).thenReturn(Mono.just(userConsent))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(keycloakId)).thenReturn(Mono.just(emptyList()))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(keycloakId)).thenReturn(Mono.just(emptyList()))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(keycloakId)).thenReturn(Mono.empty())
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(keycloakId)).thenReturn(Mono.just(emptyList()))
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(keycloakId)).thenReturn(Mono.just(emptyList()))
        whenever(exerciseRotationHistoryDAL.selectByUserId(keycloakId)).thenReturn(Mono.just(emptyList()))
        whenever(programDAL.selectProgramsByUserId(keycloakId)).thenReturn(Mono.just(emptyList()))
        whenever(gdprComplianceDAL.getUserAuditLogs(keycloakId)).thenReturn(Mono.just(emptyList()))
        whenever(gdprComplianceDAL.getDataRetentionPolicies()).thenReturn(Mono.just(emptyList()))
        
        // Mock the nested calls for programs with workouts - these are called when programs exist
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutsByProgramId(any())).thenReturn(Mono.just(emptyList()))
        whenever(workoutStageDAL.selectWorkoutStagesByProgrammedWorkoutId(any())).thenReturn(Mono.just(emptyList()))
        whenever(programmedExerciseDAL.selectProgrammedExercisesByWorkoutStageId(any())).thenReturn(Mono.just(emptyList()))
        whenever(setSchemeDAL.selectSetSchemesByProgrammedExerciseId(any())).thenReturn(Mono.just(emptyList()))

        StepVerifier.create(
            gdprComplianceService.exportUserData(keycloakId)
        )
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
        ).thenReturn(Mono.just(AuditLog(1L, "test-user-id", "DATA_DELETION_STARTED", "ALL_USER_DATA", null, Instant.now(), null)))

        whenever(
            auditService.logDataOperation(
                any(),
                eq("DATA_DELETION_FAILED"),
                any(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(Mono.just(AuditLog(3L, "test-user-id", "DATA_DELETION_FAILED", "ALL_USER_DATA", null, Instant.now(), null)))

        val keycloakId = "test-user-id"
        val error = RuntimeException("Database error")

        whenever(userDAL.deleteUserByKeycloakId(keycloakId)).thenReturn(Mono.error(error))

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
