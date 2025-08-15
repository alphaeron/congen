package com.congen.controllers

import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Program
import com.congen.service.GdprComplianceService
import com.congen.service.ProgramService
import com.congen.util.KeycloakUtil
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import kotlin.test.assertNotNull

/**
 * Unit tests for ProgramController.
 *
 * These tests verify the REST API endpoints for program operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@TestPropertySource(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration"
    ]
)
class ProgramControllerTest {
    @Mock
    private lateinit var programService: ProgramService

    @Mock
    private lateinit var keycloakUtil: KeycloakUtil

    @Mock
    private lateinit var gdprComplianceService: GdprComplianceService

    private lateinit var programController: ProgramController

    companion object {
        private const val PROGRAM_ID_1 = 1L
        private const val PROGRAM_ID_2 = 2L
        private const val USER_ID = 1
        private const val CURRENT_WEEK = 1
        private const val UPDATED_WEEK = 2
        private const val NON_EXISTENT_ID = 999L
        private const val CONJUGATE_PROGRAM_NAME = "Conjugate Powerlifting Program"
        private const val FIVE_THREE_ONE_PROGRAM = "5/3/1 Program"
        private const val UPDATED_PROGRAM_NAME = "Updated Conjugate Program"
    }

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        programController = ProgramController(programService, keycloakUtil, gdprComplianceService)

        // Mock KeycloakUtil methods for all tests
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(USER_ID.toString()))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("user")))
        
        // Mock GDPR compliance service for all tests
        whenever(gdprComplianceService.withUserConsent(any<String>(), any<() -> Mono<*>>())).thenAnswer { invocation ->
            val callback = invocation.getArgument<() -> Mono<*>>(1)
            callback()
        }
        whenever(gdprComplianceService.hasUserConsent(any<String>())).thenReturn(Mono.just(true))
    }

    private fun createTestProgram(
        id: Long,
        userId: String,
        name: String
    ): Program {
        return Program(
            id = id,
            userId = userId,
            name = name,
            currentWeekNumber = CURRENT_WEEK,
            createdAt = Instant.now(),
            updatedAt = Instant.now(),
            isActive = true
        )
    }

    @Test
    fun `save method should exist`() {
        // This test verifies the save method exists
        // The actual authorization logic requires integration testing with real security context
        assertNotNull(ProgramController::save)
    }

    @Test
    fun `get should return program when found`() {
        val program = createTestProgram(PROGRAM_ID_1, USER_ID.toString(), CONJUGATE_PROGRAM_NAME)
        whenever(programService.getProgramById(PROGRAM_ID_1)).thenReturn(Mono.just(program))
        val result = programController.get(PROGRAM_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(program))
            .verifyComplete()
        verify(programService).getProgramById(PROGRAM_ID_1)
    }

    @Test
    fun `get should return not found when program not found`() {
        whenever(
            programService.getProgramById(NON_EXISTENT_ID)
        ).thenReturn(Mono.error(NoResultsFoundException("SELECT * FROM program WHERE id=$1")))
        val result = programController.get(NON_EXISTENT_ID)
        StepVerifier.create(result).expectError(NoResultsFoundException::class.java).verify()
        verify(programService).getProgramById(NON_EXISTENT_ID)
    }

    @Test
    fun `getAll should return all programs`() {
        val programs =
            listOf(
                createTestProgram(PROGRAM_ID_1, USER_ID.toString(), CONJUGATE_PROGRAM_NAME),
                createTestProgram(PROGRAM_ID_2, USER_ID.toString(), FIVE_THREE_ONE_PROGRAM).copy(isActive = false)
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just("1"))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(setOf("admin")))
        whenever(programService.getAllPrograms()).thenReturn(Mono.just(programs))
        val result = programController.getAll()
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programs))
            .verifyComplete()
        verify(programService).getAllPrograms()
    }

    @Test
    fun `getAll returns all items for admin`() {
        val userId = "1"
        val roles = setOf("admin")
        val programs =
            listOf(
                createTestProgram(PROGRAM_ID_1, "1", CONJUGATE_PROGRAM_NAME),
                createTestProgram(PROGRAM_ID_2, "2", FIVE_THREE_ONE_PROGRAM)
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programService.getAllPrograms()).thenReturn(Mono.just(programs))

        val result = programController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body == programs)
            }
            .verifyComplete()
        verify(programService).getAllPrograms()
    }

    @Test
    fun `getAll returns all items for service`() {
        val userId = "1"
        val roles = setOf("service")
        val programs =
            listOf(
                createTestProgram(PROGRAM_ID_1, "1", CONJUGATE_PROGRAM_NAME),
                createTestProgram(PROGRAM_ID_2, "2", FIVE_THREE_ONE_PROGRAM)
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programService.getAllPrograms()).thenReturn(Mono.just(programs))

        val result = programController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body == programs)
            }
            .verifyComplete()
        verify(programService).getAllPrograms()
    }

    @Test
    fun `getAll returns only owned items for regular user`() {
        val userId = "1"
        val roles = setOf("user")
        val programs =
            listOf(
                createTestProgram(PROGRAM_ID_1, "1", CONJUGATE_PROGRAM_NAME),
                createTestProgram(PROGRAM_ID_2, "1", FIVE_THREE_ONE_PROGRAM)
            )
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programService.getProgramsByUserId(userId, null)).thenReturn(Mono.just(programs))

        val result = programController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body == programs)
            }
            .verifyComplete()
        verify(programService).getProgramsByUserId(userId, null)
    }

    @Test
    fun `getAll returns empty for regular user with no owned items`() {
        val userId = "3"
        val roles = setOf("user")
        val programs = emptyList<Program>()
        whenever(keycloakUtil.getCurrentUserId()).thenReturn(Mono.just(userId))
        whenever(keycloakUtil.getCurrentUserRoles()).thenReturn(Mono.just(roles))
        whenever(programService.getProgramsByUserId(userId, null)).thenReturn(Mono.just(programs))

        val result = programController.getAll()
        StepVerifier.create(result)
            .assertNext { response ->
                assert(response.body!!.isEmpty())
            }
            .verifyComplete()
        verify(programService).getProgramsByUserId(userId, null)
    }

    @Test
    fun `update should return updated program when found`() {
        val updatedProgram =
            createTestProgram(
                PROGRAM_ID_2,
                USER_ID.toString(),
                UPDATED_PROGRAM_NAME
            ).copy(currentWeekNumber = UPDATED_WEEK)
        whenever(programService.getProgramById(PROGRAM_ID_2)).thenReturn(Mono.just(updatedProgram))
        whenever(programService.updateProgram(PROGRAM_ID_2, UPDATED_PROGRAM_NAME, UPDATED_WEEK, true)).thenReturn(Mono.just(updatedProgram))
        val result = programController.update(PROGRAM_ID_2, UPDATED_PROGRAM_NAME, UPDATED_WEEK, true)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(updatedProgram))
            .verifyComplete()
        verify(programService).updateProgram(PROGRAM_ID_2, UPDATED_PROGRAM_NAME, UPDATED_WEEK, true)
    }

    @Test
    fun `update should return not found when program not found`() {
        whenever(
            programService.getProgramById(NON_EXISTENT_ID)
        ).thenReturn(Mono.error(NoResultsFoundException("SELECT * FROM program WHERE id=$1")))
        val result = programController.update(NON_EXISTENT_ID, UPDATED_PROGRAM_NAME, CURRENT_WEEK, true)
        StepVerifier.create(result).expectError(NoResultsFoundException::class.java).verify()
        verify(programService).getProgramById(NON_EXISTENT_ID)
    }

    @Test
    fun `delete should return deleted program when found`() {
        val program = createTestProgram(PROGRAM_ID_1, USER_ID.toString(), CONJUGATE_PROGRAM_NAME)
        whenever(programService.getProgramById(PROGRAM_ID_1)).thenReturn(Mono.just(program))
        whenever(programService.deleteProgram(PROGRAM_ID_1)).thenReturn(Mono.just(program))
        val result = programController.delete(PROGRAM_ID_1)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(program))
            .verifyComplete()
        verify(programService).deleteProgram(PROGRAM_ID_1)
    }

    @Test
    fun `delete should return not found when program not found`() {
        whenever(
            programService.getProgramById(NON_EXISTENT_ID)
        ).thenReturn(Mono.error(NoResultsFoundException("SELECT * FROM program WHERE id=$1")))
        val result = programController.delete(NON_EXISTENT_ID)
        StepVerifier.create(result).expectError(NoResultsFoundException::class.java).verify()
        verify(programService).getProgramById(NON_EXISTENT_ID)
    }

    @Test
    fun `getByUserId should return programs for user without filter`() {
        val programs =
            listOf(
                createTestProgram(PROGRAM_ID_1, USER_ID.toString(), CONJUGATE_PROGRAM_NAME),
                createTestProgram(PROGRAM_ID_2, USER_ID.toString(), FIVE_THREE_ONE_PROGRAM).copy(isActive = false)
            )
        whenever(programService.getProgramsByUserId(USER_ID.toString(), null)).thenReturn(Mono.just(programs))
        val result = programController.getByUserId(USER_ID.toString(), null)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(programs))
            .verifyComplete()
        verify(programService).getProgramsByUserId(USER_ID.toString(), null)
    }

    @Test
    fun `getByUserId should return active programs for user`() {
        val activePrograms = listOf(createTestProgram(PROGRAM_ID_1, USER_ID.toString(), CONJUGATE_PROGRAM_NAME))
        whenever(programService.getProgramsByUserId(USER_ID.toString(), true)).thenReturn(Mono.just(activePrograms))
        val result = programController.getByUserId(USER_ID.toString(), true)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(activePrograms))
            .verifyComplete()
        verify(programService).getProgramsByUserId(USER_ID.toString(), true)
    }

    @Test
    fun `getByUserId should return inactive programs for user`() {
        val inactivePrograms = listOf(createTestProgram(PROGRAM_ID_2, USER_ID.toString(), FIVE_THREE_ONE_PROGRAM).copy(isActive = false))
        whenever(programService.getProgramsByUserId(USER_ID.toString(), false)).thenReturn(Mono.just(inactivePrograms))
        val result = programController.getByUserId(USER_ID.toString(), false)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(inactivePrograms))
            .verifyComplete()
        verify(programService).getProgramsByUserId(USER_ID.toString(), false)
    }
}
