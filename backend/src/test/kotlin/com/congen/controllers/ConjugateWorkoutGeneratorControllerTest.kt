package com.congen.controllers

import com.congen.exceptions.DatabaseException
import com.congen.exceptions.NoResultsFoundException
import com.congen.exceptions.ValidationException
import com.congen.generator.ConjugateWorkoutGeneratorService
import com.congen.model.Program
import com.congen.service.GdprComplianceService
import com.congen.service.ProgramService
import com.congen.util.KeycloakUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

@ExtendWith(MockitoExtension::class)
@TestPropertySource(
    properties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"]
)
class ConjugateWorkoutGeneratorControllerTest {
    private lateinit var conjugateWorkoutGeneratorService: ConjugateWorkoutGeneratorService
    private lateinit var programService: ProgramService
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var gdprComplianceService: GdprComplianceService
    private lateinit var conjugateWorkoutGeneratorController: ConjugateWorkoutGeneratorController

    private lateinit var testProgram: Program

    companion object {
        private const val USER_ID = "b226d772-c063-4974-ae08-ab64134abbcf"
        private const val CURRENT_WEEK = 1
        private const val PROGRAM_ID = 1L
        private const val PROGRAM_NAME = "Conjugate Powerlifting - Week 1"
    }

    @BeforeEach
    fun setUp() {
        conjugateWorkoutGeneratorService = mock()
        programService = mock()
        keycloakUtil = mock()
        conjugateWorkoutGeneratorController =
            ConjugateWorkoutGeneratorController(
                conjugateWorkoutGeneratorService,
                programService,
                keycloakUtil,
                gdprComplianceService
            )

        // Mock KeycloakUtil methods for all tests
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just("test-keycloak-user-id"))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        
        // Mock GDPR compliance service for all tests
        whenever(gdprComplianceService.withUserConsent(any<String>(), any<() -> Mono<*>>())).thenAnswer { invocation ->
            val callback = invocation.getArgument<() -> Mono<*>>(1)
            callback()
        }
        whenever(gdprComplianceService.hasUserConsent(any<String>())).thenReturn(Mono.just(true))

        testProgram =
            Program(
                id = PROGRAM_ID,
                userId = USER_ID,
                name = PROGRAM_NAME,
                currentWeekNumber = CURRENT_WEEK,
                createdAt = Instant.now(),
                updatedAt = Instant.now(),
                isActive = true
            )
    }

    @Test
    fun `generateNextWeek should generate workout program successfully`() {
        whenever(programService.isOwner(PROGRAM_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any()))
            .thenReturn(Mono.just(testProgram))
        val result = conjugateWorkoutGeneratorController.generateNextWeek(PROGRAM_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgram))
            .verifyComplete()
        verify(conjugateWorkoutGeneratorService).generateNextWeek(PROGRAM_ID)
    }

    @Test
    fun `generateNextWeek should return 404 when program not found`() {
        whenever(programService.isOwner(PROGRAM_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any()))
            .thenReturn(Mono.error(NoResultsFoundException("Program not found")))
        val result = conjugateWorkoutGeneratorController.generateNextWeek(PROGRAM_ID)
        StepVerifier.create(result)
            .expectError(NoResultsFoundException::class.java)
            .verify()
        verify(conjugateWorkoutGeneratorService).generateNextWeek(PROGRAM_ID)
    }

    @Test
    fun `generateNextWeek should return 422 for validation error`() {
        whenever(programService.isOwner(PROGRAM_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any()))
            .thenReturn(Mono.error(ValidationException("Invalid program parameters")))
        val result = conjugateWorkoutGeneratorController.generateNextWeek(PROGRAM_ID)
        StepVerifier.create(result)
            .expectError(ValidationException::class.java)
            .verify()
        verify(conjugateWorkoutGeneratorService).generateNextWeek(PROGRAM_ID)
    }

    @Test
    fun `generateNextWeek should return 500 for unexpected error`() {
        whenever(programService.isOwner(PROGRAM_ID, "test-keycloak-user-id")).thenReturn(Mono.just(true))
        whenever(conjugateWorkoutGeneratorService.generateNextWeek(any()))
            .thenReturn(Mono.error(DatabaseException("Unexpected error")))
        val result = conjugateWorkoutGeneratorController.generateNextWeek(PROGRAM_ID)
        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
        verify(conjugateWorkoutGeneratorService).generateNextWeek(PROGRAM_ID)
    }
}
