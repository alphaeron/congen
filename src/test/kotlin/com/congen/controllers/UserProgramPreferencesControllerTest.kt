package com.congen.controllers

import com.congen.dal.UserProgramPreferencesDAL
import com.congen.model.UserProgramPreferences
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
class UserProgramPreferencesControllerTest {
    private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL
    private lateinit var userProgramPreferencesController: UserProgramPreferencesController

    @BeforeEach
    fun setUp() {
        userProgramPreferencesDAL = mock()
        userProgramPreferencesController = UserProgramPreferencesController(userProgramPreferencesDAL)
    }

    @Test
    fun `save should return created user program preferences`() {
        val userId = 1
        val programDaysPerWeek = 4
        val sessionTimeLengthInMinutes = 60
        val now = Instant.now()
        val userProgramPreferences =
            UserProgramPreferences(
                userId = userId,
                programDaysPerWeek = programDaysPerWeek,
                sessionTimeLengthInMinutes = sessionTimeLengthInMinutes,
                createdAt = now,
                updatedAt = now
            )
        whenever(
            userProgramPreferencesDAL.insertUserProgramPreferences(userId, programDaysPerWeek, sessionTimeLengthInMinutes)
        ).thenReturn(Mono.just(userProgramPreferences))

        val result = userProgramPreferencesController.save(userId, programDaysPerWeek, sessionTimeLengthInMinutes)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserProgramPreferences>)
            .expectNext(userProgramPreferences)
            .verifyComplete()

        verify(userProgramPreferencesDAL).insertUserProgramPreferences(userId, programDaysPerWeek, sessionTimeLengthInMinutes)
    }

    @Test
    fun `get should return user program preferences when found`() {
        val userId = 1
        val programDaysPerWeek = 4
        val sessionTimeLengthInMinutes = 60
        val now = Instant.now()
        val userProgramPreferences =
            UserProgramPreferences(
                userId = userId,
                programDaysPerWeek = programDaysPerWeek,
                sessionTimeLengthInMinutes = sessionTimeLengthInMinutes,
                createdAt = now,
                updatedAt = now
            )
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.just(userProgramPreferences))

        val result = userProgramPreferencesController.get(userId)

        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userProgramPreferences))
            .verifyComplete()

        verify(userProgramPreferencesDAL).selectUserProgramPreferences(userId)
    }

    @Test
    fun `update should return updated user program preferences`() {
        val userId = 1
        val programDaysPerWeek = 5
        val sessionTimeLengthInMinutes = 75
        val now = Instant.now()
        val userProgramPreferences =
            UserProgramPreferences(
                userId = userId,
                programDaysPerWeek = programDaysPerWeek,
                sessionTimeLengthInMinutes = sessionTimeLengthInMinutes,
                createdAt = now,
                updatedAt = now
            )
        whenever(
            userProgramPreferencesDAL.updateUserProgramPreferences(userId, programDaysPerWeek, sessionTimeLengthInMinutes)
        ).thenReturn(Mono.just(userProgramPreferences))

        val result = userProgramPreferencesController.update(userId, programDaysPerWeek, sessionTimeLengthInMinutes)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserProgramPreferences>)
            .expectNext(userProgramPreferences)
            .verifyComplete()

        verify(userProgramPreferencesDAL).updateUserProgramPreferences(userId, programDaysPerWeek, sessionTimeLengthInMinutes)
    }

    @Test
    fun `delete should return deleted user program preferences`() {
        val userId = 1
        val programDaysPerWeek = 4
        val sessionTimeLengthInMinutes = 60
        val now = Instant.now()
        val userProgramPreferences =
            UserProgramPreferences(
                userId = userId,
                programDaysPerWeek = programDaysPerWeek,
                sessionTimeLengthInMinutes = sessionTimeLengthInMinutes,
                createdAt = now,
                updatedAt = now
            )
        whenever(userProgramPreferencesDAL.deleteUserProgramPreferences(userId)).thenReturn(Mono.just(userProgramPreferences))

        val result = userProgramPreferencesController.delete(userId)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserProgramPreferences>)
            .expectNext(userProgramPreferences)
            .verifyComplete()

        verify(userProgramPreferencesDAL).deleteUserProgramPreferences(userId)
    }

    @Test
    fun `should handle DAL error gracefully for save`() {
        val userId = 1
        val programDaysPerWeek = 4
        val sessionTimeLengthInMinutes = 60

        whenever(
            userProgramPreferencesDAL.insertUserProgramPreferences(userId, programDaysPerWeek, sessionTimeLengthInMinutes)
        ).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userProgramPreferencesController.save(userId, programDaysPerWeek, sessionTimeLengthInMinutes)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserProgramPreferences>)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for get`() {
        val userId = 1

        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(userId)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userProgramPreferencesController.get(userId)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for update`() {
        val userId = 1
        val programDaysPerWeek = 5
        val sessionTimeLengthInMinutes = 75

        whenever(
            userProgramPreferencesDAL.updateUserProgramPreferences(userId, programDaysPerWeek, sessionTimeLengthInMinutes)
        ).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userProgramPreferencesController.update(userId, programDaysPerWeek, sessionTimeLengthInMinutes)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserProgramPreferences>)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `should handle DAL error gracefully for delete`() {
        val userId = 1

        whenever(userProgramPreferencesDAL.deleteUserProgramPreferences(userId)).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = userProgramPreferencesController.delete(userId)

        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserProgramPreferences>)
            .expectError(RuntimeException::class.java)
            .verify()
    }
}
