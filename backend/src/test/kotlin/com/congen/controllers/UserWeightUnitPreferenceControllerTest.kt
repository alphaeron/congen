package com.congen.controllers

import com.congen.createMockMono
import com.congen.createMockMonoError
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.exceptions.DatabaseException
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockUserWeightUnitPreference
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.ResponseEntity
import org.springframework.test.context.TestPropertySource
import reactor.test.StepVerifier

@TestPropertySource(
    properties = [
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.reactive.ReactiveSecurityAutoConfiguration"
    ]
)
class UserWeightUnitPreferenceControllerTest {
    private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL
    private lateinit var userWeightUnitPreferenceController: UserWeightUnitPreferenceController

    @BeforeEach
    fun setUp() {
        userWeightUnitPreferenceDAL = mock()
        userWeightUnitPreferenceController = UserWeightUnitPreferenceController(userWeightUnitPreferenceDAL)
    }

    @Test
    fun `upsert should create preference successfully`() {
        // Given
        val preference = mockUserWeightUnitPreference()

        whenever(
            userWeightUnitPreferenceDAL.upsertUserWeightUnitPreference(
                eq(preference.userId),
                eq(preference.exerciseName),
                eq(preference.preferredUnit)
            )
        ).thenReturn(createMockMono(preference))

        // When
        val result =
            userWeightUnitPreferenceController.upsert(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit
            )

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(preference))
            .verifyComplete()

        verify(userWeightUnitPreferenceDAL).upsertUserWeightUnitPreference(
            eq(preference.userId),
            eq(preference.exerciseName),
            eq(preference.preferredUnit)
        )
    }

    @Test
    fun `upsert should handle database errors`() {
        // Given
        val preference = mockUserWeightUnitPreference()

        whenever(
            userWeightUnitPreferenceDAL.upsertUserWeightUnitPreference(
                eq(preference.userId),
                eq(preference.exerciseName),
                eq(preference.preferredUnit)
            )
        ).thenReturn(createMockMonoError(DatabaseException("Database error")))

        // When
        val result =
            userWeightUnitPreferenceController.upsert(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit
            )

        // Then
        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
    }

    @Test
    fun `getByUser should return preferences successfully`() {
        // Given
        val userId = 1
        val preferences = listOf(mockUserWeightUnitPreference())

        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(eq(userId)))
            .thenReturn(createMockMono(preferences))

        // When
        val result = userWeightUnitPreferenceController.getAllByUser(userId)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(preferences))
            .verifyComplete()

        verify(userWeightUnitPreferenceDAL).selectUserWeightUnitPreferencesByUser(eq(userId))
    }

    @Test
    fun `getByUser should handle database errors`() {
        // Given
        val userId = 1

        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(eq(userId)))
            .thenReturn(createMockMonoError(DatabaseException("Database error")))

        // When
        val result = userWeightUnitPreferenceController.getAllByUser(userId)

        // Then
        StepVerifier.create(result)
            .expectError(DatabaseException::class.java)
            .verify()
    }

    @Test
    fun `getByUserAndExercise should return preference successfully`() {
        // Given
        val userId = 1
        val exerciseName = "Bench Press"
        val preference = mockUserWeightUnitPreference()

        whenever(
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(
                eq(userId),
                eq(exerciseName)
            )
        ).thenReturn(createMockMono(preference))

        // When
        val result = userWeightUnitPreferenceController.getByUserAndExercise(userId, exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(preference))
            .verifyComplete()

        verify(userWeightUnitPreferenceDAL).selectUserWeightUnitPreference(eq(userId), eq(exerciseName))
    }

    @Test
    fun `getByUserAndExercise should return not found when preference not found`() {
        // Given
        val userId = 1
        val exerciseName = "Bench Press"

        whenever(
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(
                eq(userId),
                eq(exerciseName)
            )
        ).thenReturn(createMockMonoError(NoResultsFoundException("Preference not found")))

        // When
        val result = userWeightUnitPreferenceController.getByUserAndExercise(userId, exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.notFound().build())
            .verifyComplete()
    }

    @Test
    fun `delete should return preference successfully`() {
        // Given
        val userId = 1
        val exerciseName = "Bench Press"
        val preference = mockUserWeightUnitPreference()

        whenever(
            userWeightUnitPreferenceDAL.deleteUserWeightUnitPreference(
                eq(userId),
                eq(exerciseName)
            )
        ).thenReturn(createMockMono(preference))

        // When
        val result = userWeightUnitPreferenceController.delete(userId, exerciseName)

        // Then
        StepVerifier.create(result)
            .expectNext(ResponseEntity.ok(preference))
            .verifyComplete()

        verify(userWeightUnitPreferenceDAL).deleteUserWeightUnitPreference(eq(userId), eq(exerciseName))
    }
}
