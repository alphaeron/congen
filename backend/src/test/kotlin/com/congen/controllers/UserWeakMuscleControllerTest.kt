package com.congen.controllers

import com.congen.dal.UserWeakMuscleDAL
import com.congen.model.UserWeakMuscle
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant

/**
 * Unit tests for UserWeakMuscleController.
 */
class UserWeakMuscleControllerTest {
    private lateinit var dal: UserWeakMuscleDAL
    private lateinit var controller: UserWeakMuscleController

    @BeforeEach
    fun setUp() {
        dal = mock()
        controller = UserWeakMuscleController(dal)
    }

    @Test
    fun `should add user weak muscle`() {
        val now = Instant.now()
        val userWeakMuscle = UserWeakMuscle(1, "hamstrings", now)
        whenever(dal.insertUserWeakMuscle(1, "hamstrings")).thenReturn(Mono.just(userWeakMuscle))
        val result = controller.add(1, "hamstrings")
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userWeakMuscle))
            .verifyComplete()
        verify(dal).insertUserWeakMuscle(1, "hamstrings")
    }

    @Test
    fun `should get user weak muscles`() {
        val now = Instant.now()
        val userWeakMuscle = UserWeakMuscle(2, "Hamstrings", now)
        whenever(dal.selectUserWeakMusclesByUser(2)).thenReturn(Mono.just(listOf(userWeakMuscle)))
        val result = controller.getByUser(2)
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(listOf(userWeakMuscle)))
            .verifyComplete()
        verify(dal).selectUserWeakMusclesByUser(2)
    }

    @Test
    fun `should delete user weak muscle`() {
        val now = Instant.now()
        val userWeakMuscle = UserWeakMuscle(3, "glutes", now)
        whenever(dal.deleteUserWeakMuscle(3, "glutes")).thenReturn(Mono.just(userWeakMuscle))
        val result = controller.delete(3, "glutes")
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userWeakMuscle))
            .verifyComplete()
        verify(dal).deleteUserWeakMuscle(3, "glutes")
    }
}
