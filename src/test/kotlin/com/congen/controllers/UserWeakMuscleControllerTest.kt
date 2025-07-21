package com.congen.controllers

import com.congen.dal.UserWeakMuscleDAL
import com.congen.model.UserWeakMuscle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import java.time.Instant

/**
 * Unit tests for UserWeakMuscleController.
 */
class UserWeakMuscleControllerTest {
    private lateinit var dal: UserWeakMuscleDAL
    private lateinit var controller: UserWeakMuscleController

    @BeforeEach
    fun setUp() {
        dal = mock(UserWeakMuscleDAL::class.java)
        controller = UserWeakMuscleController(dal)
    }

    @Test
    fun `should add user weak muscle`() {
        val now = Instant.now()
        val userWeakMuscle = UserWeakMuscle(1, "hamstrings", now)
        `when`(dal.insertUserWeakMuscle(1, "hamstrings")).thenReturn(Mono.just(userWeakMuscle))
        val response = controller.add(1, "hamstrings")
        val actual = (response.body as Mono<*>).block()
        assertEquals(userWeakMuscle, actual)
    }

    @Test
    fun `should get user weak muscles`() {
        val now = Instant.now()
        val userWeakMuscle = UserWeakMuscle(2, "Hamstrings", now)
        `when`(dal.selectUserWeakMusclesByUser(2)).thenReturn(Mono.just(listOf(userWeakMuscle)))
        val result = controller.getByUser(2).block()
        assertEquals(ResponseEntity.ok(listOf(userWeakMuscle)), result)
    }

    @Test
    fun `should delete user weak muscle`() {
        val now = Instant.now()
        val userWeakMuscle = UserWeakMuscle(3, "glutes", now)
        `when`(dal.deleteUserWeakMuscle(3, "glutes")).thenReturn(Mono.just(userWeakMuscle))
        val response = controller.delete(3, "glutes")
        val actual = (response.body as Mono<*>).block()
        assertEquals(userWeakMuscle, actual)
    }
}
