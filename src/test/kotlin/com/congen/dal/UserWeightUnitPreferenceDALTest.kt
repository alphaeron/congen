package com.congen.dal

import com.congen.assertMonoError
import com.congen.assertMonoSuccess
import com.congen.client.PostgresClient
import com.congen.createMockMono
import com.congen.createMockMonoError
import com.congen.exceptions.DatabaseException
import com.congen.exceptions.NoResultsFoundException
import com.congen.mockUserWeightUnitPreference
import com.congen.model.UserWeightUnitPreference
import com.congen.model.WeightUnit
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class UserWeightUnitPreferenceDALTest {
    private lateinit var postgresClient: PostgresClient
    private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL

    @BeforeEach
    fun setUp() {
        postgresClient = mock()
        userWeightUnitPreferenceDAL = UserWeightUnitPreferenceDAL(postgresClient)
    }

    @Test
    fun `upsertUserWeightUnitPreference should insert new preference when it does not exist`() {
        // Given
        val preference = mockUserWeightUnitPreference()

        whenever(
            postgresClient.update<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(preference.userId),
                eq(preference.exerciseName),
                eq(preference.preferredUnit.name)
            )
        ).thenReturn(createMockMono(preference))

        // When
        val result =
            userWeightUnitPreferenceDAL.upsertUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit
            )

        // Then
        assertMonoSuccess(result, preference)
        verify(postgresClient).update<UserWeightUnitPreference>(
            any<String>(),
            any(),
            eq(preference.userId),
            eq(preference.exerciseName),
            eq(preference.preferredUnit.name)
        )
    }

    @Test
    fun `upsertUserWeightUnitPreference should update existing preference when it exists`() {
        // Given
        val preference = mockUserWeightUnitPreference(preferredUnit = WeightUnit.KG)

        whenever(
            postgresClient.update<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(preference.userId),
                eq(preference.exerciseName),
                eq(preference.preferredUnit.name)
            )
        ).thenReturn(createMockMono(preference))

        // When
        val result =
            userWeightUnitPreferenceDAL.upsertUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit
            )

        // Then
        assertMonoSuccess(result, preference)
    }

    @Test
    fun `upsertUserWeightUnitPreference should handle database errors`() {
        // Given
        val preference = mockUserWeightUnitPreference()

        whenever(
            postgresClient.update<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(preference.userId),
                eq(preference.exerciseName),
                eq(preference.preferredUnit.name)
            )
        ).thenReturn(createMockMonoError(DatabaseException("Database error")))

        // When & Then
        assertMonoError(
            userWeightUnitPreferenceDAL.upsertUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit
            ),
            DatabaseException::class.java
        )
    }

    @Test
    fun `selectUserWeightUnitPreference should return preference when found`() {
        // Given
        val preference = mockUserWeightUnitPreference()

        whenever(
            postgresClient.selectIndividual<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(preference.userId),
                eq(preference.exerciseName)
            )
        ).thenReturn(createMockMono(preference))

        // When
        val result =
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName
            )

        // Then
        assertMonoSuccess(result, preference)
    }

    @Test
    fun `selectUserWeightUnitPreference should throw NoResultsFoundException when not found`() {
        // Given
        val userId = 1
        val exerciseName = "NonExistentExercise"

        whenever(
            postgresClient.selectIndividual<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(userId),
                eq(exerciseName)
            )
        ).thenReturn(createMockMonoError(NoResultsFoundException("No results returned from query")))

        // When & Then
        assertMonoError(
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, exerciseName),
            NoResultsFoundException::class.java
        )
    }

    @Test
    fun `selectUserWeightUnitPreferencesByUser should return all preferences for user`() {
        // Given
        val userId = 1
        val preferences =
            listOf(
                mockUserWeightUnitPreference(userId = userId, exerciseName = "Bench Press"),
                mockUserWeightUnitPreference(userId = userId, exerciseName = "Deadlift", preferredUnit = WeightUnit.KG)
            )

        whenever(
            postgresClient.select<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(userId)
            )
        ).thenReturn(createMockMono(preferences))

        // When
        val result = userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(userId)

        // Then
        assertMonoSuccess(result, preferences)
    }

    @Test
    fun `selectUserWeightUnitPreferencesByUser should return empty list when no preferences exist`() {
        // Given
        val userId = 1

        whenever(
            postgresClient.select<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(userId)
            )
        ).thenReturn(createMockMono(emptyList()))

        // When
        val result = userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(userId)

        // Then
        assertMonoSuccess(result, emptyList())
    }

    @Test
    fun `deleteUserWeightUnitPreference should delete preference successfully`() {
        // Given
        val preference = mockUserWeightUnitPreference()

        whenever(
            postgresClient.update<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(preference.userId),
                eq(preference.exerciseName)
            )
        ).thenReturn(createMockMono(preference))

        // When
        val result =
            userWeightUnitPreferenceDAL.deleteUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName
            )

        // Then
        assertMonoSuccess(result, preference)
        verify(postgresClient).update<UserWeightUnitPreference>(
            any<String>(),
            any(),
            eq(preference.userId),
            eq(preference.exerciseName)
        )
    }

    @Test
    fun `deleteUserWeightUnitPreference should handle database errors`() {
        // Given
        val preference = mockUserWeightUnitPreference()

        whenever(
            postgresClient.update<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(preference.userId),
                eq(preference.exerciseName)
            )
        ).thenReturn(createMockMonoError(DatabaseException("Database error")))

        // When & Then
        assertMonoError(
            userWeightUnitPreferenceDAL.deleteUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName
            ),
            DatabaseException::class.java
        )
    }

    @Test
    fun `insertUserWeightUnitPreference should insert new preference successfully`() {
        // Given
        val preference = mockUserWeightUnitPreference()

        whenever(
            postgresClient.update<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(preference.userId),
                eq(preference.exerciseName),
                eq(preference.preferredUnit.name)
            )
        ).thenReturn(createMockMono(preference))

        // When
        val result =
            userWeightUnitPreferenceDAL.insertUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit
            )

        // Then
        assertMonoSuccess(result, preference)
    }

    @Test
    fun `updateUserWeightUnitPreference should update existing preference successfully`() {
        // Given
        val preference = mockUserWeightUnitPreference(preferredUnit = WeightUnit.KG)

        whenever(
            postgresClient.update<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(preference.userId),
                eq(preference.exerciseName),
                eq(preference.preferredUnit.name)
            )
        ).thenReturn(createMockMono(preference))

        // When
        val result =
            userWeightUnitPreferenceDAL.updateUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit
            )

        // Then
        assertMonoSuccess(result, preference)
    }

    @Test
    fun `updateUserWeightUnitPreference should throw NoResultsFoundException when preference does not exist`() {
        // Given
        val preference = mockUserWeightUnitPreference(exerciseName = "NonExistentExercise", preferredUnit = WeightUnit.KG)

        whenever(
            postgresClient.update<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(preference.userId),
                eq(preference.exerciseName),
                eq(preference.preferredUnit.name)
            )
        ).thenReturn(createMockMonoError(NoResultsFoundException("No results returned from query")))

        // When & Then
        assertMonoError(
            userWeightUnitPreferenceDAL.updateUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit
            ),
            NoResultsFoundException::class.java
        )
    }
}
