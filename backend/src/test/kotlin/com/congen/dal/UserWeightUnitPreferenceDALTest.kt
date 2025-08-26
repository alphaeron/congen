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

        val result =
            userWeightUnitPreferenceDAL.upsertUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit
            )

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

        val result =
            userWeightUnitPreferenceDAL.upsertUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit
            )

        assertMonoSuccess(result, preference)
    }

    @Test
    fun `upsertUserWeightUnitPreference should handle database errors`() {
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
        val preference = mockUserWeightUnitPreference()

        whenever(
            postgresClient.selectIndividual<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(preference.userId),
                eq(preference.exerciseName)
            )
        ).thenReturn(createMockMono(preference))

        val result =
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName
            )

        assertMonoSuccess(result, preference)
    }

    @Test
    fun `selectUserWeightUnitPreference should throw NoResultsFoundException when not found`() {
        val userId = "test-keycloak-user-id"
        val exerciseName = "NonExistentExercise"

        whenever(
            postgresClient.selectIndividual<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(userId),
                eq(exerciseName)
            )
        ).thenReturn(createMockMonoError(NoResultsFoundException("No results returned from query")))

        assertMonoError(
            userWeightUnitPreferenceDAL.selectUserWeightUnitPreference(userId, exerciseName),
            NoResultsFoundException::class.java
        )
    }

    @Test
    fun `selectUserWeightUnitPreferencesByUser should return all preferences for user`() {
        val userId = "test-keycloak-user-id"
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

        val result = userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(userId)

        assertMonoSuccess(result, preferences)
    }

    @Test
    fun `selectUserWeightUnitPreferencesByUser should return empty list when no preferences exist`() {
        val userId = "test-keycloak-user-id"

        whenever(
            postgresClient.select<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(userId)
            )
        ).thenReturn(createMockMono(emptyList()))

        val result = userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser(userId)

        assertMonoSuccess(result, emptyList())
    }

    @Test
    fun `deleteUserWeightUnitPreference should delete preference successfully`() {
        val preference = mockUserWeightUnitPreference()

        whenever(
            postgresClient.update<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(preference.userId),
                eq(preference.exerciseName)
            )
        ).thenReturn(createMockMono(preference))

        val result =
            userWeightUnitPreferenceDAL.deleteUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName
            )

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
        val preference = mockUserWeightUnitPreference()

        whenever(
            postgresClient.update<UserWeightUnitPreference>(
                any<String>(),
                any(),
                eq(preference.userId),
                eq(preference.exerciseName)
            )
        ).thenReturn(createMockMonoError(DatabaseException("Database error")))

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

        val result =
            userWeightUnitPreferenceDAL.insertUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit
            )

        assertMonoSuccess(result, preference)
    }

    @Test
    fun `updateUserWeightUnitPreference should update existing preference successfully`() {
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

        val result =
            userWeightUnitPreferenceDAL.updateUserWeightUnitPreference(
                preference.userId,
                preference.exerciseName,
                preference.preferredUnit
            )

        assertMonoSuccess(result, preference)
    }

    @Test
    fun `updateUserWeightUnitPreference should throw NoResultsFoundException when preference does not exist`() {
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
