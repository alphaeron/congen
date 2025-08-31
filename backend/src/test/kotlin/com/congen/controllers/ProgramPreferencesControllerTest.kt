package com.congen.controllers

import com.congen.createGdprComplianceServiceSpy
import com.congen.dal.ProgramPreferencesDAL
import com.congen.exceptions.DatabaseQueryException
import com.congen.mockProgram
import com.congen.mockProgramPreferences
import com.congen.model.ProgramPreferences
import com.congen.service.GdprComplianceService
import com.congen.service.ProgramService
import com.congen.util.KeycloakUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for ProgramPreferencesController.
 *
 * These tests verify the REST API endpoints for program preferences operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@TestPropertySource(
    properties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"]
)
class ProgramPreferencesControllerTest {
    private lateinit var programPreferencesDAL: ProgramPreferencesDAL
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var gdprComplianceService: GdprComplianceService
    private lateinit var programService: ProgramService
    private lateinit var programPreferencesController: ProgramPreferencesController

    companion object {
        private const val PROGRAM_ID = 1L
        private const val PROGRAM_DAYS_PER_WEEK_4 = 4
        private const val PROGRAM_DAYS_PER_WEEK_5 = 5
        private const val SESSION_TIME_60 = 60
        private const val SESSION_TIME_75 = 75
    }

    @BeforeEach
    fun setUp() {
        programPreferencesDAL = mock()
        keycloakUtil = mock()
        gdprComplianceService = createGdprComplianceServiceSpy()
        programService = mock()
        programPreferencesController =
            ProgramPreferencesController(
                programPreferencesDAL,
                keycloakUtil,
                gdprComplianceService,
                programService
            )

        // Mock KeycloakUtil methods for all tests
        doReturn(Mono.just("test-keycloak-user-id")).whenever(keycloakUtil).getCurrentUserId()
        doReturn(Mono.just(setOf("user"))).whenever(keycloakUtil).getCurrentUserRoles()

        // Mock GDPR compliance service for all tests
        doReturn(Mono.just(true)).whenever(gdprComplianceService).hasUserConsent(any<String>())

        // Mock ProgramService methods for all tests
        doReturn(Mono.just(mockProgram(userId = "test-keycloak-user-id"))).whenever(programService).selectProgramById(any())
    }

    @Test
    fun `get should return program preferences when found`() {
        val programPreferences =
            mockProgramPreferences(
                programId = PROGRAM_ID,
                programDaysPerWeek = PROGRAM_DAYS_PER_WEEK_4,
                sessionTimeLengthInMinutes = SESSION_TIME_60
            )
        whenever(programPreferencesDAL.selectProgramPreferences(PROGRAM_ID)).thenReturn(Mono.just(programPreferences))
        val result = programPreferencesController.get(PROGRAM_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programPreferences))
            .verifyComplete()
        verify(programPreferencesDAL).selectProgramPreferences(PROGRAM_ID)
    }

    @Test
    fun `update should return updated program preferences`() {
        val now = Instant.now()
        val programPreferences =
            ProgramPreferences(
                programId = PROGRAM_ID,
                programDaysPerWeek = PROGRAM_DAYS_PER_WEEK_4,
                sessionTimeLengthInMinutes = SESSION_TIME_75,
                createdAt = now,
                updatedAt = now
            )
        whenever(programPreferencesDAL.updateProgramPreferences(PROGRAM_ID, SESSION_TIME_75))
            .thenReturn(Mono.just(programPreferences))
        val result = programPreferencesController.update(PROGRAM_ID, SESSION_TIME_75)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programPreferences))
            .verifyComplete()
        verify(programPreferencesDAL).updateProgramPreferences(PROGRAM_ID, SESSION_TIME_75)
    }

    @Test
    fun `should handle DAL error gracefully for get`() {
        whenever(programPreferencesDAL.selectProgramPreferences(PROGRAM_ID))
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = programPreferencesController.get(PROGRAM_ID)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for update`() {
        whenever(
            programPreferencesDAL.updateProgramPreferences(
                PROGRAM_ID,
                SESSION_TIME_75
            )
        )
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = programPreferencesController.update(PROGRAM_ID, SESSION_TIME_75)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }
}
