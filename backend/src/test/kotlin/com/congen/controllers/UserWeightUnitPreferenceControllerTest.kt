package com.congen.controllers

import com.congen.createMockMono
import com.congen.createMockMonoError
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.exceptions.DatabaseException
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockUserWeightUnitPreference
import com.congen.service.GdprComplianceService
import com.congen.util.KeycloakUtil
import com.congen.createGdprComplianceServiceSpy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

/**
 * Unit tests for UserWeightUnitPreferenceController.
 *
 * These tests verify the REST API endpoints for user weight unit preference operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@TestPropertySource(
    properties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"]
)
class UserWeightUnitPreferenceControllerTest {
    private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var gdprComplianceService: GdprComplianceService
    private lateinit var userWeightUnitPreferenceController: UserWeightUnitPreferenceController

    private val currentUserId = "b226d772-c063-4974-ae08-ab64134abbcf"

    @BeforeEach
    fun setUp() {
        userWeightUnitPreferenceDAL = mock()
        keycloakUtil = mock()
        gdprComplianceService = createGdprComplianceServiceSpy()
        userWeightUnitPreferenceController = UserWeightUnitPreferenceController(userWeightUnitPreferenceDAL, keycloakUtil, gdprComplianceService)

        // Mock KeycloakUtil methods for all tests
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(currentUserId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))

        // Mock GDPR compliance service for all tests
        doReturn(Mono.just(true)).whenever(gdprComplianceService).hasUserConsent(any<String>())
    }

    @Test
    fun `upsert should create preference successfully`() {
        // Given
        val preference = mockUserWeightUnitPreference().copy(userId = currentUserId)

        whenever(
            userWeightUnitPreferenceDAL.upsertUserWeightUnitPreference(
                eq(preference.userId),
                eq(preference.exerciseName),
                eq(preference.preferredUnit)
            )
        ).thenReturn(createMockMono(preference))

        // When
        val result =
            userWeightUnitPreferenceController.upsert(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit.name
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(preference))
            .verifyComplete()

        verify(userWeightUnitPreferenceDAL).upsertUserWeightUnitPreference(
            eq(preference.userId),
            eq(preference.exerciseName),
            eq(preference.preferredUnit)
        )
    }

    @Test
    fun `upsert should handle database errors`() {
        // Given
        val preference = mockUserWeightUnitPreference().copy(userId = currentUserId)

        whenever(
            userWeightUnitPreferenceDAL.upsertUserWeightUnitPreference(
                eq(preference.userId),
                eq(preference.exerciseName),
                eq(preference.preferredUnit)
            )
        ).thenReturn(createMockMonoError(DatabaseException("Database error")))

        // When
        val result =
            userWeightUnitPreferenceController.upsert(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit.name
            )

        // Then
        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
    }

    @Test
    fun `upsert should deny access for different user`() {
        // Given
        val differentUserId = "different-user-id"
        val preference = mockUserWeightUnitPreference().copy(userId = differentUserId)

        // When
        val result =
            userWeightUnitPreferenceController.upsert(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit.name
            )

        // Then
        StepVerifier.create(result)
            .expectError(AccessDeniedException::class.java)
            .verify()
    }

    @Test
    fun `getByUser should return preferences successfully`() {
        // Given
        val preferences = listOf(mockUserWeightUnitPreference().copy(userId = currentUserId))

        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(eq(currentUserId)))
            .thenReturn(createMockMono(preferences))

        // When
        val result = userWeightUnitPreferenceController.getAllByUser(currentUserId)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(preferences))
            .verifyComplete()

        verify(userWeightUnitPreferenceDAL).selectUserWeightUnitPreferencesByUser(eq(currentUserId))
    }

    @Test
    fun `getByUser should handle database errors`() {
        // Given
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(eq(currentUserId)))
            .thenReturn(createMockMonoError(DatabaseException("Database error")))

        // When
        val result = userWeightUnitPreferenceController.getAllByUser(currentUserId)

        // Then
        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
    }

    @Test
    fun `getByUser should deny access for different user`() {
        // Given
        val differentUserId = "different-user-id"

        // When
        val result = userWeightUnitPreferenceController.getAllByUser(differentUserId)

        // Then
        StepVerifier.create(result)
            .expectError(AccessDeniedException::class.java)
            .verify()
    }

    @Test
    fun `getByUserAndExercise should return preference successfully`() {
        // Given
        val exerciseName = "Bench Press"
        val preference = mockUserWeightUnitPreference().copy(userId = currentUserId)

        whenever(
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(
                eq(currentUserId),
                eq(exerciseName)
            )
        ).thenReturn(createMockMono(preference))

        // When
        val result = userWeightUnitPreferenceController.getByUserAndExercise(currentUserId, exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(preference))
            .verifyComplete()

        verify(userWeightUnitPreferenceDAL).selectUserWeightUnitPreference(eq(currentUserId), eq(exerciseName))
    }

    @Test
    fun `getByUserAndExercise should return not found when preference not found`() {
        // Given
        val exerciseName = "Bench Press"

        whenever(
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(
                eq(currentUserId),
                eq(exerciseName)
            )
        ).thenReturn(createMockMonoError(NoResultsFoundException("Preference not found")))

        // When
        val result = userWeightUnitPreferenceController.getByUserAndExercise(currentUserId, exerciseName)

        // Then
        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
    }

    @Test
    fun `getByUserAndExercise should deny access for different user`() {
        // Given
        val differentUserId = "different-user-id"
        val exerciseName = "Bench Press"

        // When
        val result = userWeightUnitPreferenceController.getByUserAndExercise(differentUserId, exerciseName)

        // Then
        StepVerifier.create(result)
            .expectError(AccessDeniedException::class.java)
            .verify()
    }

    @Test
    fun `delete should return preference successfully`() {
        // Given
        val exerciseName = "Bench Press"
        val preference = mockUserWeightUnitPreference().copy(userId = currentUserId)

        whenever(
            userWeightUnitPreferenceDAL.deleteUserWeightUnitPreference(
                eq(currentUserId),
                eq(exerciseName)
            )
        ).thenReturn(createMockMono(preference))

        // When
        val result = userWeightUnitPreferenceController.delete(currentUserId, exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(preference))
            .verifyComplete()

        verify(userWeightUnitPreferenceDAL).deleteUserWeightUnitPreference(eq(currentUserId), eq(exerciseName))
    }

    @Test
    fun `delete should deny access for different user`() {
        // Given
        val differentUserId = "different-user-id"
        val exerciseName = "Bench Press"

        // When
        val result = userWeightUnitPreferenceController.delete(differentUserId, exerciseName)

        // Then
        StepVerifier.create(result)
            .expectError(AccessDeniedException::class.java)
            .verify()
    }
}
