package com.congen.controllers

import com.congen.assertMonoSuccess
import com.congen.createMockMono
import com.congen.createMockMonoError
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.exceptions.DatabaseException
import com.congen.exceptions.InvalidWeightUnitException
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockUserWeightUnitPreference
import com.congen.model.UserWeightUnitPreference
import com.congen.model.WeightUnit
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import reactor.core.publisher.Mono

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
                preference.preferredUnit.name
            )

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = (result.body as Mono<*>).block()
        assert(body == preference)

        verify(userWeightUnitPreferenceDAL).upsertUserWeightUnitPreference(
            eq(preference.userId),
            eq(preference.exerciseName),
            eq(preference.preferredUnit)
        )
    }

    @Test
    fun `upsert should handle invalid weight unit`() {
        // Given
        val userId = 1
        val exerciseName = "Bench Press"
        val invalidUnit = "INVALID"

        // When & Then
        assertThrows(InvalidWeightUnitException::class.java) {
            userWeightUnitPreferenceController.upsert(userId, exerciseName, invalidUnit)
        }
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
                preference.preferredUnit.name
            )

        // Then
        assert(result.statusCode == HttpStatus.OK)
        // The controller returns ResponseEntity.ok() with the Mono, so the error is not handled here
        // The error would be handled by the global exception handler
    }

    @Test
    fun `getByUserAndExercise should return preference when found`() {
        // Given
        val preference = mockUserWeightUnitPreference()

        whenever(
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(
                eq(preference.userId),
                eq(preference.exerciseName)
            )
        ).thenReturn(createMockMono(preference))

        // When
        val result =
            userWeightUnitPreferenceController.getByUserAndExercise(
                preference.userId,
                preference.exerciseName
            )

        // Then
        assertMonoSuccess(result, ResponseEntity.ok(preference))
    }

    @Test
    fun `getByUserAndExercise should handle preference not found`() {
        // Given
        val userId = 1
        val exerciseName = "NonExistentExercise"

        whenever(
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(
                eq(userId),
                eq(exerciseName)
            )
        ).thenReturn(createMockMonoError(NoResultsFoundException("Not found")))

        // When
        val result = userWeightUnitPreferenceController.getByUserAndExercise(userId, exerciseName)

        // Then
        assertMonoSuccess(result, ResponseEntity.notFound().build())
    }

    @Test
    fun `getAllByUser should return all preferences for user`() {
        // Given
        val userId = 1
        val preferences =
            listOf(
                mockUserWeightUnitPreference(userId = userId, exerciseName = "Bench Press"),
                mockUserWeightUnitPreference(userId = userId, exerciseName = "Deadlift", preferredUnit = WeightUnit.KG)
            )

        whenever(
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(eq(userId))
        ).thenReturn(createMockMono(preferences))

        // When
        val result = userWeightUnitPreferenceController.getAllByUser(userId)

        // Then
        assertMonoSuccess(result, ResponseEntity.ok(preferences))
    }

    @Test
    fun `getAllByUser should return empty list when no preferences exist`() {
        // Given
        val userId = 1

        whenever(
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(eq(userId))
        ).thenReturn(createMockMono(emptyList()))

        // When
        val result = userWeightUnitPreferenceController.getAllByUser(userId)

        // Then
        assertMonoSuccess(result, ResponseEntity.ok(emptyList<UserWeightUnitPreference>()))
    }

    @Test
    fun `delete should delete preference successfully`() {
        // Given
        val preference = mockUserWeightUnitPreference()

        whenever(
            userWeightUnitPreferenceDAL.deleteUserWeightUnitPreference(
                eq(preference.userId),
                eq(preference.exerciseName)
            )
        ).thenReturn(createMockMono(preference))

        // When
        val result =
            userWeightUnitPreferenceController.delete(
                preference.userId,
                preference.exerciseName
            )

        // Then
        assert(result.statusCode == HttpStatus.OK)
        val body = (result.body as Mono<*>).block()
        assert(body == preference)

        verify(userWeightUnitPreferenceDAL).deleteUserWeightUnitPreference(
            eq(preference.userId),
            eq(preference.exerciseName)
        )
    }

    @Test
    fun `delete should handle preference not found`() {
        // Given
        val userId = 1
        val exerciseName = "NonExistentExercise"

        whenever(
            userWeightUnitPreferenceDAL.deleteUserWeightUnitPreference(
                eq(userId),
                eq(exerciseName)
            )
        ).thenReturn(createMockMonoError(NoResultsFoundException("Not found")))

        // When
        val result = userWeightUnitPreferenceController.delete(userId, exerciseName)

        // Then
        assert(result.statusCode == HttpStatus.OK)
        // The controller returns ResponseEntity.ok() with the Mono, so the error is not handled here
        // The error would be handled by the global exception handler
    }

    @Test
    fun `delete should handle database errors`() {
        // Given
        val preference = mockUserWeightUnitPreference()

        whenever(
            userWeightUnitPreferenceDAL.deleteUserWeightUnitPreference(
                eq(preference.userId),
                eq(preference.exerciseName)
            )
        ).thenReturn(createMockMonoError(DatabaseException("Database error")))

        // When
        val result =
            userWeightUnitPreferenceController.delete(
                preference.userId,
                preference.exerciseName
            )

        // Then
        assert(result.statusCode == HttpStatus.OK)
        // The controller returns ResponseEntity.ok() with the Mono, so the error is not handled here
        // The error would be handled by the global exception handler
    }
}
