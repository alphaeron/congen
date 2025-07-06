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

class UserProgramPreferencesControllerTest {
    private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL
    private lateinit var userProgramPreferencesController: UserProgramPreferencesController

    @BeforeEach
    fun setUp() {
        userProgramPreferencesDAL = mock()
        userProgramPreferencesController = UserProgramPreferencesController(userProgramPreferencesDAL)
    }

    @Test
    fun `save should return saved user program preferences`() {
        val prefs = UserProgramPreferences(userId = 1, programDaysPerWeek = 4, sessionTimeLengthInMinutes = 60)
        whenever(userProgramPreferencesDAL.insertUserProgramPreferences(1, 4, 60)).thenReturn(Mono.just(prefs))
        val result = userProgramPreferencesController.save(1, 4, 60)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserProgramPreferences>).expectNext(prefs).verifyComplete()
        verify(userProgramPreferencesDAL).insertUserProgramPreferences(1, 4, 60)
    }

    @Test
    fun `get should return user program preferences when found`() {
        val prefs = UserProgramPreferences(userId = 1, programDaysPerWeek = 4, sessionTimeLengthInMinutes = 60)
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(1)).thenReturn(Mono.just(prefs))
        val result = userProgramPreferencesController.get(1)
        StepVerifier.create(result).expectNext(ResponseEntity.ok(prefs)).verifyComplete()
        verify(userProgramPreferencesDAL).selectUserProgramPreferences(1)
    }

    @Test
    fun `update should return updated user program preferences`() {
        val prefs = UserProgramPreferences(userId = 1, programDaysPerWeek = 3, sessionTimeLengthInMinutes = 75)
        whenever(userProgramPreferencesDAL.updateUserProgramPreferences(1, 3, 75)).thenReturn(Mono.just(prefs))
        val result = userProgramPreferencesController.update(1, 3, 75)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserProgramPreferences>).expectNext(prefs).verifyComplete()
        verify(userProgramPreferencesDAL).updateUserProgramPreferences(1, 3, 75)
    }

    @Test
    fun `delete should return deleted user program preferences`() {
        val prefs = UserProgramPreferences(userId = 1, programDaysPerWeek = 4, sessionTimeLengthInMinutes = 60)
        whenever(userProgramPreferencesDAL.deleteUserProgramPreferences(1)).thenReturn(Mono.just(prefs))
        val result = userProgramPreferencesController.delete(1)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserProgramPreferences>).expectNext(prefs).verifyComplete()
        verify(userProgramPreferencesDAL).deleteUserProgramPreferences(1)
    }
}
