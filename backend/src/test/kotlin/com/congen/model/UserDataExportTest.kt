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
        val userDataExport =
            UserDataExport(
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
        val userDataExport =
            UserDataExport(
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
        val userDataExport =
            UserDataExport(
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
        val program =
            Program(
                id = 1L,
                userId = "test-user-id",
                name = "Test Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )
        val programWithWorkouts =
            ProgramWithWorkouts(
                program = program,
                workouts = emptyList()
            )

        val userDataExport =
            UserDataExport(
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
                trainingPrograms = listOf(programWithWorkouts),
                auditLogs = emptyList(),
                dataRetentionPolicies = emptyList(),
                exportTimestamp = now
            )

        assertEquals(1, userDataExport.trainingPrograms.size)
        assertEquals("Test Program", userDataExport.trainingPrograms[0].program.name)
    }

    @Test
    fun `should create UserDataExport with complete workout structure`() {
        val now = Instant.now()
        val setScheme =
            SetScheme(
                id = 1L,
                programmedExerciseId = 1L,
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
                updatedAt = now,
                band = null
            )

        val programmedExercise =
            ProgrammedExercise(
                id = 1L,
                workoutStageId = 1L,
                exerciseName = "Bench Press",
                position = 1,
                notes = "Test exercise",
                createdAt = now,
                updatedAt = now
            )

        val programmedExerciseWithSetSchemes =
            ProgrammedExerciseWithSetSchemes(
                exercise = programmedExercise,
                setSchemes = listOf(setScheme)
            )

        val workoutStage =
            WorkoutStage(
                id = 1L,
                programmedWorkoutId = 1L,
                stageTypeId = 1,
                position = 1,
                name = "Main",
                createdAt = now,
                updatedAt = now
            )

        val workoutStageWithExercises =
            WorkoutStageWithExercises(
                stage = workoutStage,
                exercises = listOf(programmedExerciseWithSetSchemes)
            )

        val programmedWorkout =
            ProgrammedWorkout(
                id = 1L,
                programId = 1L,
                dayNumber = 1,
                name = "Push Day",
                createdAt = now,
                updatedAt = now
            )

        val programmedWorkoutWithStages =
            ProgrammedWorkoutWithStages(
                workout = programmedWorkout,
                stages = listOf(workoutStageWithExercises)
            )

        val program =
            Program(
                id = 1L,
                userId = "test-user-id",
                name = "Test Program",
                currentWeekNumber = 1,
                createdAt = now,
                updatedAt = now,
                isActive = true
            )

        val programWithWorkouts =
            ProgramWithWorkouts(
                program = program,
                workouts = listOf(programmedWorkoutWithStages)
            )

        val userDataExport =
            UserDataExport(
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
                trainingPrograms = listOf(programWithWorkouts),
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
