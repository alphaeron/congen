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
        val userWeakMuscle = UserWeakMuscle("test-keycloak-user-id", "hamstrings", now)
        whenever(dal.insertUserWeakMuscle("test-keycloak-user-id", "hamstrings")).thenReturn(Mono.just(userWeakMuscle))
        val result = controller.add("test-keycloak-user-id", "hamstrings")
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userWeakMuscle))
            .verifyComplete()
        verify(dal).insertUserWeakMuscle("test-keycloak-user-id", "hamstrings")
    }

    @Test
    fun `should get user weak muscles`() {
        val now = Instant.now()
        val userWeakMuscle = UserWeakMuscle("test-keycloak-user-id-2", "Hamstrings", now)
        whenever(dal.selectUserWeakMusclesByUser("test-keycloak-user-id-2")).thenReturn(Mono.just(listOf(userWeakMuscle)))
        val result = controller.getByUser("test-keycloak-user-id-2")
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(listOf(userWeakMuscle)))
            .verifyComplete()
        verify(dal).selectUserWeakMusclesByUser("test-keycloak-user-id-2")
    }

    @Test
    fun `should delete user weak muscle`() {
        val now = Instant.now()
        val userWeakMuscle = UserWeakMuscle("test-keycloak-user-id-3", "glutes", now)
        whenever(dal.deleteUserWeakMuscle("test-keycloak-user-id-3", "glutes")).thenReturn(Mono.just(userWeakMuscle))
        val result = controller.delete("test-keycloak-user-id-3", "glutes")
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(userWeakMuscle))
            .verifyComplete()
        verify(dal).deleteUserWeakMuscle("test-keycloak-user-id-3", "glutes")
    }
}
