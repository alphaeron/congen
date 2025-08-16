package com.congen.controllers

import com.congen.dal.UserProgramPreferencesDAL
import com.congen.exceptions.DatabaseQueryException
import com.congen.mockUserProgramPreferences
import com.congen.model.UserProgramPreferences
import com.congen.service.GdprComplianceService
import com.congen.util.KeycloakUtil
import com.congen.createGdprComplianceServiceSpy
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
 * Unit tests for UserProgramPreferencesController.
 *
 * These tests verify the REST API endpoints for user program preferences operations,
 * including CRUD operations and error handling.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@TestPropertySource(
    properties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"]
)
class UserProgramPreferencesControllerTest {
    private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL
    private lateinit var keycloakUtil: KeycloakUtil
    private lateinit var gdprComplianceService: GdprComplianceService
    private lateinit var userProgramPreferencesController: UserProgramPreferencesController

    companion object {
        private const val USER_ID = "test-keycloak-user-id"
        private const val PROGRAM_DAYS_PER_WEEK_4 = 4
        private const val PROGRAM_DAYS_PER_WEEK_5 = 5
        private const val SESSION_TIME_60 = 60
        private const val SESSION_TIME_75 = 75
    }

    @BeforeEach
    fun setUp() {
        userProgramPreferencesDAL = mock()
        keycloakUtil = mock()
        gdprComplianceService = createGdprComplianceServiceSpy()
        userProgramPreferencesController = UserProgramPreferencesController(userProgramPreferencesDAL, keycloakUtil, gdprComplianceService)

        // Mock KeycloakUtil methods for all tests
        doReturn(Mono.just("test-keycloak-user-id")).whenever(keycloakUtil).getCurrentUserId()
        doReturn(Mono.just(setOf("user"))).whenever(keycloakUtil).getCurrentUserRoles()

        // Mock GDPR compliance service for all tests
        doReturn(Mono.just(true)).whenever(gdprComplianceService).hasUserConsent(any<String>())
    }

    @Test
    fun `save should return saved user program preferences`() {
        val userProgramPreferences =
            mockUserProgramPreferences(
                userId = USER_ID,
                programDaysPerWeek = PROGRAM_DAYS_PER_WEEK_4,
                sessionTimeLengthInMinutes = SESSION_TIME_60
            )
        whenever(
            userProgramPreferencesDAL.insertUserProgramPreferences(
                USER_ID,
                PROGRAM_DAYS_PER_WEEK_4,
                SESSION_TIME_60
            )
        )
            .thenReturn(Mono.just(userProgramPreferences))
        val result = userProgramPreferencesController.save(USER_ID, PROGRAM_DAYS_PER_WEEK_4, SESSION_TIME_60)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userProgramPreferences))
            .verifyComplete()
        verify(userProgramPreferencesDAL).insertUserProgramPreferences(USER_ID, PROGRAM_DAYS_PER_WEEK_4, SESSION_TIME_60)
    }

    @Test
    fun `getByUser should return user program preferences when found`() {
        val userProgramPreferences =
            mockUserProgramPreferences(
                userId = USER_ID,
                programDaysPerWeek = PROGRAM_DAYS_PER_WEEK_4,
                sessionTimeLengthInMinutes = SESSION_TIME_60
            )
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(USER_ID)).thenReturn(Mono.just(userProgramPreferences))
        val result = userProgramPreferencesController.get(USER_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userProgramPreferences))
            .verifyComplete()
        verify(userProgramPreferencesDAL).selectUserProgramPreferences(USER_ID)
    }

    @Test
    fun `update should return updated user program preferences`() {
        val now = Instant.now()
        val userProgramPreferences =
            UserProgramPreferences(
                userId = USER_ID,
                programDaysPerWeek = PROGRAM_DAYS_PER_WEEK_5,
                sessionTimeLengthInMinutes = SESSION_TIME_75,
                createdAt = now,
                updatedAt = now
            )
        whenever(userProgramPreferencesDAL.updateUserProgramPreferences(USER_ID, PROGRAM_DAYS_PER_WEEK_5, SESSION_TIME_75))
            .thenReturn(Mono.just(userProgramPreferences))
        val result = userProgramPreferencesController.update(USER_ID, PROGRAM_DAYS_PER_WEEK_5, SESSION_TIME_75)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userProgramPreferences))
            .verifyComplete()
        verify(userProgramPreferencesDAL).updateUserProgramPreferences(USER_ID, PROGRAM_DAYS_PER_WEEK_5, SESSION_TIME_75)
    }

    @Test
    fun `delete should return deleted user program preferences`() {
        val now = Instant.now()
        val userProgramPreferences =
            UserProgramPreferences(
                userId = USER_ID,
                programDaysPerWeek = PROGRAM_DAYS_PER_WEEK_4,
                sessionTimeLengthInMinutes = SESSION_TIME_60,
                createdAt = now,
                updatedAt = now
            )
        whenever(userProgramPreferencesDAL.deleteUserProgramPreferences(USER_ID)).thenReturn(Mono.just(userProgramPreferences))
        val result = userProgramPreferencesController.delete(USER_ID)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userProgramPreferences))
            .verifyComplete()
        verify(userProgramPreferencesDAL).deleteUserProgramPreferences(USER_ID)
    }

    @Test
    fun `should handle DAL error gracefully for save`() {
        whenever(
            userProgramPreferencesDAL.insertUserProgramPreferences(
                USER_ID,
                PROGRAM_DAYS_PER_WEEK_4,
                SESSION_TIME_60
            )
        )
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = userProgramPreferencesController.save(USER_ID, PROGRAM_DAYS_PER_WEEK_4, SESSION_TIME_60)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for getByUser`() {
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(USER_ID))
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = userProgramPreferencesController.get(USER_ID)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for update`() {
        whenever(
            userProgramPreferencesDAL.updateUserProgramPreferences(
                USER_ID,
                PROGRAM_DAYS_PER_WEEK_5,
                SESSION_TIME_75
            )
        )
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = userProgramPreferencesController.update(USER_ID, PROGRAM_DAYS_PER_WEEK_5, SESSION_TIME_75)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for delete`() {
        whenever(userProgramPreferencesDAL.deleteUserProgramPreferences(USER_ID))
            .thenReturn(Mono.error(DatabaseQueryException("Database error")))
        val result = userProgramPreferencesController.delete(USER_ID)
        StepVerifier.create(result)
            .expectError(DatabaseQueryException::class.java)
            .verify()
    }
}
