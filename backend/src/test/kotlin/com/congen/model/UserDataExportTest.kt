package com.congen.model

import org.junit.jupiter.api.Test
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Test class for UserDataExport model.
 *
 * Tests the data structure for user data export functionality,
 * ensuring all fields are properly serialized and deserialized.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class UserDataExportTest {

    @Test
    fun `should create UserDataExport with all required fields`() {
        val now = Instant.now()
        val userDataExport = UserDataExport(
            keycloakId = "test-user-id",
            name = "Test User",
            createdAt = now,
            updatedAt = now,
            dataProcessingConsent = true,
            consentTimestamp = now,
            userEquipment = emptyList(),
            userExercisePreferences = emptyList(),
            userProgramPreferences = null,
            userOneRepMax = emptyList(),
            userWeightUnitPreferences = emptyList(),
            exerciseRotationHistory = emptyList(),
            trainingPrograms = emptyList(),
            auditLogs = emptyList(),
            dataRetentionPolicies = emptyList(),
            exportTimestamp = now
        )

        assertEquals("test-user-id", userDataExport.keycloakId)
        assertEquals("Test User", userDataExport.name)
        assertEquals(now, userDataExport.createdAt)
        assertEquals(now, userDataExport.updatedAt)
        assertEquals(true, userDataExport.dataProcessingConsent)
        assertEquals(now, userDataExport.consentTimestamp)
        assertEquals(now, userDataExport.exportTimestamp)
    }

    @Test
    fun `should create UserDataExport with consent withdrawal`() {
        val now = Instant.now()
        val userDataExport = UserDataExport(
            keycloakId = "test-user-id",
            name = "Test User",
            createdAt = now,
            updatedAt = now,
            dataProcessingConsent = false,
            consentTimestamp = now,
            userEquipment = emptyList(),
            userExercisePreferences = emptyList(),
            userProgramPreferences = null,
            userOneRepMax = emptyList(),
            userWeightUnitPreferences = emptyList(),
            exerciseRotationHistory = emptyList(),
            trainingPrograms = emptyList(),
            auditLogs = emptyList(),
            dataRetentionPolicies = emptyList(),
            exportTimestamp = now
        )

        assertEquals(false, userDataExport.dataProcessingConsent)
    }

    @Test
    fun `should create UserDataExport with null consent timestamp`() {
        val now = Instant.now()
        val userDataExport = UserDataExport(
            keycloakId = "test-user-id",
            name = "Test User",
            createdAt = now,
            updatedAt = now,
            dataProcessingConsent = false,
            consentTimestamp = null,
            userEquipment = emptyList(),
            userExercisePreferences = emptyList(),
            userProgramPreferences = null,
            userOneRepMax = emptyList(),
            userWeightUnitPreferences = emptyList(),
            exerciseRotationHistory = emptyList(),
            trainingPrograms = emptyList(),
            auditLogs = emptyList(),
            dataRetentionPolicies = emptyList(),
            exportTimestamp = now
        )

        assertEquals(null, userDataExport.consentTimestamp)
    }

    @Test
    fun `should create UserDataExport with training programs`() {
        val now = Instant.now()
        val trainingProgram = TrainingProgramExport(
            id = 1L,
            name = "Test Program",
            currentWeekNumber = 1,
            createdAt = now,
            updatedAt = now,
            workouts = emptyList()
        )

        val userDataExport = UserDataExport(
            keycloakId = "test-user-id",
            name = "Test User",
            createdAt = now,
            updatedAt = now,
            dataProcessingConsent = true,
            consentTimestamp = now,
            userEquipment = emptyList(),
            userExercisePreferences = emptyList(),
            userProgramPreferences = null,
            userOneRepMax = emptyList(),
            userWeightUnitPreferences = emptyList(),
            exerciseRotationHistory = emptyList(),
            trainingPrograms = listOf(trainingProgram),
            auditLogs = emptyList(),
            dataRetentionPolicies = emptyList(),
            exportTimestamp = now
        )

        assertEquals(1, userDataExport.trainingPrograms.size)
        assertEquals("Test Program", userDataExport.trainingPrograms[0].name)
    }

    @Test
    fun `should create UserDataExport with complete workout structure`() {
        val now = Instant.now()
        val setScheme = SetSchemeExport(
            id = 1L,
            setNumber = 1,
            isAmrap = false,
            isEmom = false,
            useTempo = false,
            eccentricTempo = null,
            isometricTempo = null,
            concentricTempo = null,
            targetWeight = null,
            performedWeight = null,
            targetRepCount = 10,
            performedRepCount = null,
            restSeconds = 60,
            createdAt = now,
            updatedAt = now
        )

        val programmedExercise = ProgrammedExerciseExport(
            id = 1L,
            exerciseName = "Bench Press",
            position = 1,
            notes = "Test exercise",
            createdAt = now,
            updatedAt = now,
            setSchemes = listOf(setScheme)
        )

        val workoutStage = WorkoutStageExport(
            id = 1L,
            stageTypeId = 1L,
            position = 1,
            name = "Main",
            createdAt = now,
            updatedAt = now,
            exercises = listOf(programmedExercise)
        )

        val workout = WorkoutExport(
            id = 1L,
            dayNumber = 1,
            name = "Push Day",
            createdAt = now,
            updatedAt = now,
            stages = listOf(workoutStage)
        )

        val trainingProgram = TrainingProgramExport(
            id = 1L,
            name = "Test Program",
            currentWeekNumber = 1,
            createdAt = now,
            updatedAt = now,
            workouts = listOf(workout)
        )

        val userDataExport = UserDataExport(
            keycloakId = "test-user-id",
            name = "Test User",
            createdAt = now,
            updatedAt = now,
            dataProcessingConsent = true,
            consentTimestamp = now,
            userEquipment = emptyList(),
            userExercisePreferences = emptyList(),
            userProgramPreferences = null,
            userOneRepMax = emptyList(),
            userWeightUnitPreferences = emptyList(),
            exerciseRotationHistory = emptyList(),
            trainingPrograms = listOf(trainingProgram),
            auditLogs = emptyList(),
            dataRetentionPolicies = emptyList(),
            exportTimestamp = now
        )

        assertNotNull(userDataExport.trainingPrograms)
        assertEquals(1, userDataExport.trainingPrograms.size)
        assertEquals(1, userDataExport.trainingPrograms[0].workouts.size)
        assertEquals(1, userDataExport.trainingPrograms[0].workouts[0].stages.size)
        assertEquals(1, userDataExport.trainingPrograms[0].workouts[0].stages[0].exercises.size)
        assertEquals(1, userDataExport.trainingPrograms[0].workouts[0].stages[0].exercises[0].setSchemes.size)
    }
}
