package com.congen.controllers

import com.congen.dal.UserExercisePreferenceDAL
import com.congen.model.UserExercisePreference
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

class UserExercisePreferenceControllerTest {
    private lateinit var userExercisePreferenceDAL: UserExercisePreferenceDAL
    private lateinit var userExercisePreferenceController: UserExercisePreferenceController

    @BeforeEach
    fun setUp() {
        userExercisePreferenceDAL = mock()
        userExercisePreferenceController = UserExercisePreferenceController(userExercisePreferenceDAL)
    }

    @Test
    fun `save should return saved user exercise preference`() {
        val pref = UserExercisePreference(userId = 1, exerciseName = "Bench Press", shouldAvoid = true)
        whenever(userExercisePreferenceDAL.insertUserExercisePreference(1, "Bench Press", true)).thenReturn(Mono.just(pref))
        val result = userExercisePreferenceController.save(1, "Bench Press", true)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserExercisePreference>).expectNext(pref).verifyComplete()
        verify(userExercisePreferenceDAL).insertUserExercisePreference(1, "Bench Press", true)
    }

    @Test
    fun `getByUser should return user exercise preferences when found`() {
        val prefs = listOf(UserExercisePreference(userId = 1, exerciseName = "Bench Press", shouldAvoid = true))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(1)).thenReturn(Mono.just(prefs))
        val result = userExercisePreferenceController.getByUser(1)
        StepVerifier.create(result).expectNext(ResponseEntity.ok(prefs)).verifyComplete()
        verify(userExercisePreferenceDAL).selectUserExercisePreferencesByUser(1)
    }

    @Test
    fun `update should return updated user exercise preference`() {
        val pref = UserExercisePreference(userId = 1, exerciseName = "Bench Press", shouldAvoid = false)
        whenever(userExercisePreferenceDAL.updateUserExercisePreference(1, "Bench Press", false)).thenReturn(Mono.just(pref))
        val result = userExercisePreferenceController.update(1, "Bench Press", false)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserExercisePreference>).expectNext(pref).verifyComplete()
        verify(userExercisePreferenceDAL).updateUserExercisePreference(1, "Bench Press", false)
    }

    @Test
    fun `delete should return deleted user exercise preference`() {
        val pref = UserExercisePreference(userId = 1, exerciseName = "Bench Press", shouldAvoid = true)
        whenever(userExercisePreferenceDAL.deleteUserExercisePreference(1, "Bench Press")).thenReturn(Mono.just(pref))
        val result = userExercisePreferenceController.delete(pref)
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<UserExercisePreference>).expectNext(pref).verifyComplete()
        verify(userExercisePreferenceDAL).deleteUserExercisePreference(1, "Bench Press")
    }
}
