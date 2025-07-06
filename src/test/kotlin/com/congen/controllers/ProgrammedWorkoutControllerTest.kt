package com.congen.controllers

import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.model.ProgrammedWorkout
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono
import reactor.test.StepVerifier

@ExtendWith(MockitoExtension::class)
class ProgrammedWorkoutControllerTest {
    @Mock
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL

    @InjectMocks
    private lateinit var programmedWorkoutController: ProgrammedWorkoutController

    private lateinit var testProgrammedWorkout: ProgrammedWorkout

    @BeforeEach
    fun setUp() {
        testProgrammedWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 5L,
                dayNumber = 1,
                name = "Upper Body Strength"
            )
    }

    @Test
    fun `save should create new programmed workout successfully`() {
        // Given
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(5L, 1, "Upper Body Strength"))
            .thenReturn(Mono.just(testProgrammedWorkout))

        // When
        val result =
            programmedWorkoutController.save(
                programId = 5L,
                dayNumber = 1,
                name = "Upper Body Strength"
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgrammedWorkout))
            .verifyComplete()

        verify(programmedWorkoutDAL).insertProgrammedWorkout(5L, 1, "Upper Body Strength")
    }

    @Test
    fun `save should handle service error`() {
        // Given
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(5L, 1, "Upper Body Strength"))
            .thenReturn(Mono.error(RuntimeException("Database error")))

        // When
        val result =
            programmedWorkoutController.save(
                programId = 5L,
                dayNumber = 1,
                name = "Upper Body Strength"
            )

        // Then
        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()

        verify(programmedWorkoutDAL).insertProgrammedWorkout(5L, 1, "Upper Body Strength")
    }

    @Test
    fun `get should return programmed workout by id`() {
        // Given
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(1L))
            .thenReturn(Mono.just(testProgrammedWorkout))

        // When
        val result = programmedWorkoutController.get(1L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgrammedWorkout))
            .verifyComplete()

        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(1L)
    }

    @Test
    fun `get should return 404 when programmed workout not found`() {
        // Given
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutById(1L))
            .thenReturn(Mono.error(RuntimeException("Not found")))

        // When
        val result = programmedWorkoutController.get(1L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(programmedWorkoutDAL).selectProgrammedWorkoutById(1L)
    }

    @Test
    fun `getAll should return all programmed workouts`() {
        // Given
        val workouts =
            listOf(
                testProgrammedWorkout,
                testProgrammedWorkout.copy(id = 2L, dayNumber = 2, name = "Lower Body Strength")
            )
        whenever(programmedWorkoutDAL.selectProgrammedWorkouts())
            .thenReturn(Mono.just(workouts))

        // When
        val result = programmedWorkoutController.getAll()

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedWorkout>>)
            .expectNext(workouts)
            .verifyComplete()

        verify(programmedWorkoutDAL).selectProgrammedWorkouts()
    }

    @Test
    fun `getByProgramId should return programmed workouts for program`() {
        // Given
        val workouts =
            listOf(
                testProgrammedWorkout,
                testProgrammedWorkout.copy(id = 2L, dayNumber = 2, name = "Lower Body Strength")
            )
        whenever(programmedWorkoutDAL.selectProgrammedWorkoutsByProgramId(5L))
            .thenReturn(Mono.just(workouts))

        // When
        val result = programmedWorkoutController.getByProgramId(5L)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = result.body as Mono<*>
        StepVerifier.create(body as Mono<List<ProgrammedWorkout>>)
            .expectNext(workouts)
            .verifyComplete()

        verify(programmedWorkoutDAL).selectProgrammedWorkoutsByProgramId(5L)
    }

    @Test
    fun `update should update programmed workout successfully`() {
        // Given
        val updatedWorkout =
            testProgrammedWorkout.copy(
                dayNumber = 2,
                name = "Modified Upper Body Strength"
            )
        whenever(programmedWorkoutDAL.updateProgrammedWorkout(1L, 5L, 2, "Modified Upper Body Strength"))
            .thenReturn(Mono.just(updatedWorkout))

        // When
        val result =
            programmedWorkoutController.update(
                id = 1L,
                programId = 5L,
                dayNumber = 2,
                name = "Modified Upper Body Strength"
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(updatedWorkout))
            .verifyComplete()

        verify(programmedWorkoutDAL).updateProgrammedWorkout(1L, 5L, 2, "Modified Upper Body Strength")
    }

    @Test
    fun `update should return 404 when programmed workout not found`() {
        // Given
        whenever(programmedWorkoutDAL.updateProgrammedWorkout(1L, 5L, 2, "Modified Upper Body Strength"))
            .thenReturn(Mono.error(RuntimeException("Not found")))

        // When
        val result =
            programmedWorkoutController.update(
                id = 1L,
                programId = 5L,
                dayNumber = 2,
                name = "Modified Upper Body Strength"
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(programmedWorkoutDAL).updateProgrammedWorkout(1L, 5L, 2, "Modified Upper Body Strength")
    }

    @Test
    fun `delete should delete programmed workout successfully`() {
        // Given
        whenever(programmedWorkoutDAL.deleteProgrammedWorkout(1L))
            .thenReturn(Mono.just(testProgrammedWorkout))

        // When
        val result = programmedWorkoutController.delete(1L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(testProgrammedWorkout))
            .verifyComplete()

        verify(programmedWorkoutDAL).deleteProgrammedWorkout(1L)
    }

    @Test
    fun `delete should return 404 when programmed workout not found`() {
        // Given
        whenever(programmedWorkoutDAL.deleteProgrammedWorkout(1L))
            .thenReturn(Mono.error(RuntimeException("Not found")))

        // When
        val result = programmedWorkoutController.delete(1L)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()

        verify(programmedWorkoutDAL).deleteProgrammedWorkout(1L)
    }
}
