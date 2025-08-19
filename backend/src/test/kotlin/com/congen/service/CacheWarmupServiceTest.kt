package com.congen.service

import com.congen.config.CacheWarmupConfig
import com.congen.dal.*
import com.congen.model.*
import com.congen.model.MovementType
import com.congen.model.WorkoutStageTypeEnum
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.boot.ApplicationArguments
import reactor.core.publisher.Mono
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class CacheWarmupServiceTest {

    @Mock private lateinit var exerciseDAL: ExerciseDAL
    @Mock private lateinit var equipmentDAL: EquipmentDAL
    @Mock private lateinit var muscleDAL: MuscleDAL
    @Mock private lateinit var exerciseMuscleDAL: ExerciseMuscleDAL
    @Mock private lateinit var exerciseEquipmentDAL: ExerciseEquipmentDAL
    @Mock private lateinit var workoutStageTypeDAL: WorkoutStageTypeDAL
    @Mock private lateinit var exerciseWorkoutTypeDAL: ExerciseWorkoutTypeDAL
    @Mock private lateinit var programDAL: ProgramDAL
    @Mock private lateinit var userDAL: UserDAL
    @Mock private lateinit var userEquipmentDAL: UserEquipmentDAL
    @Mock private lateinit var userExercisePreferenceDAL: UserExercisePreferenceDAL
    @Mock private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL
    @Mock private lateinit var userWeakMuscleDAL: UserWeakMuscleDAL
    @Mock private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL
    @Mock private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL
    @Mock private lateinit var gdprComplianceDAL: GdprComplianceDAL
    @Mock private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL
    @Mock private lateinit var workoutStageDAL: WorkoutStageDAL
    @Mock private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL
    @Mock private lateinit var setSchemeDAL: SetSchemeDAL
    @Mock private lateinit var exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL
    @Mock private lateinit var cacheWarmupConfig: CacheWarmupConfig
    @Mock private lateinit var applicationArguments: ApplicationArguments

    private lateinit var cacheWarmupService: CacheWarmupService

    @BeforeEach
    fun setUp() {
        cacheWarmupService = CacheWarmupService(
            exerciseDAL, equipmentDAL, muscleDAL, exerciseMuscleDAL, exerciseEquipmentDAL,
            workoutStageTypeDAL, exerciseWorkoutTypeDAL, programDAL, userDAL, userEquipmentDAL,
            userExercisePreferenceDAL, userOneRepMaxDAL, userWeakMuscleDAL, userProgramPreferencesDAL,
            userWeightUnitPreferenceDAL, gdprComplianceDAL, programmedWorkoutDAL, workoutStageDAL,
            programmedExerciseDAL, setSchemeDAL, exerciseRotationHistoryDAL, cacheWarmupConfig
        )
    }

    @Test
    fun `should warm up reference data successfully`() {
        // Configure warmup settings
        whenever(cacheWarmupConfig.enabled).thenReturn(true)
        whenever(cacheWarmupConfig.warmupReferenceData).thenReturn(true)
        whenever(cacheWarmupConfig.warmupLists).thenReturn(false)
        whenever(cacheWarmupConfig.warmupRelationships).thenReturn(false)
        whenever(cacheWarmupConfig.popularExercises).thenReturn(listOf("Bench Press", "Squat", "Deadlift"))
        whenever(cacheWarmupConfig.popularEquipment).thenReturn(listOf("Barbell", "Dumbbell"))
        whenever(cacheWarmupConfig.popularMuscles).thenReturn(listOf("Chest", "Back"))

        listOf("Bench Press", "Squat", "Deadlift").forEach { exerciseName ->
            whenever(exerciseDAL.selectExerciseByName(exerciseName)).thenReturn(Mono.just(createMockExercise(exerciseName)))
        }
        listOf("Barbell", "Dumbbell").forEach { equipmentName ->
            whenever(equipmentDAL.selectEquipmentByName(equipmentName)).thenReturn(Mono.just(createMockEquipment(equipmentName)))
        }
        listOf("Chest", "Back").forEach { muscleName ->
            whenever(muscleDAL.selectMuscleByName(muscleName)).thenReturn(Mono.just(createMockMuscle(muscleName)))
        }

        cacheWarmupService.run(applicationArguments)

        listOf("Bench Press", "Squat", "Deadlift").forEach { exerciseName -> verify(exerciseDAL).selectExerciseByName(exerciseName) }
        listOf("Barbell", "Dumbbell").forEach { equipmentName -> verify(equipmentDAL).selectEquipmentByName(equipmentName) }
        listOf("Chest", "Back").forEach { muscleName -> verify(muscleDAL).selectMuscleByName(muscleName) }
    }

    @Test
    fun `should warm up frequently accessed lists successfully`() {
        // Configure warmup settings
        whenever(cacheWarmupConfig.enabled).thenReturn(true)
        whenever(cacheWarmupConfig.warmupReferenceData).thenReturn(false)
        whenever(cacheWarmupConfig.warmupLists).thenReturn(true)
        whenever(cacheWarmupConfig.warmupRelationships).thenReturn(false)

        val exercises = listOf(createMockExercise("Exercise 1"), createMockExercise("Exercise 2"))
        val equipment = listOf(createMockEquipment("Equipment 1"), createMockEquipment("Equipment 2"))
        val muscles = listOf(createMockMuscle("Muscle 1"), createMockMuscle("Muscle 2"))
        val workoutStageTypes = listOf(createMockWorkoutStageType(), createMockWorkoutStageType())

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(equipmentDAL.selectEquipment()).thenReturn(Mono.just(equipment))
        whenever(muscleDAL.selectMuscles()).thenReturn(Mono.just(muscles))
        whenever(workoutStageTypeDAL.selectWorkoutStageTypes()).thenReturn(Mono.just(workoutStageTypes))

        cacheWarmupService.run(applicationArguments)

        verify(exerciseDAL).selectExercises()
        verify(equipmentDAL).selectEquipment()
        verify(muscleDAL).selectMuscles()
        verify(workoutStageTypeDAL).selectWorkoutStageTypes()
    }

    @Test
    fun `should warm up core relationships successfully`() {
        // Configure warmup settings
        whenever(cacheWarmupConfig.enabled).thenReturn(true)
        whenever(cacheWarmupConfig.warmupReferenceData).thenReturn(false)
        whenever(cacheWarmupConfig.warmupLists).thenReturn(false)
        whenever(cacheWarmupConfig.warmupRelationships).thenReturn(true)
        whenever(cacheWarmupConfig.popularExercises).thenReturn(listOf("Bench Press", "Squat"))

        val exerciseMuscleRelationships = listOf(createMockExerciseMuscle("Bench Press", "Chest"))
        val exerciseEquipmentRelationships = listOf(createMockExerciseEquipment("Bench Press", "Barbell"))
        val exerciseWorkoutTypeRelationships = listOf(createMockExerciseWorkoutType("Bench Press", "Strength"))

        listOf("Bench Press", "Squat").forEach { exerciseName ->
            whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(exerciseName)).thenReturn(Mono.just(exerciseMuscleRelationships))
            whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName)).thenReturn(Mono.just(exerciseEquipmentRelationships))
            whenever(exerciseWorkoutTypeDAL.selectExerciseWorkoutTypesByExercise(exerciseName)).thenReturn(Mono.just(exerciseWorkoutTypeRelationships))
        }

        cacheWarmupService.run(applicationArguments)

        listOf("Bench Press", "Squat").forEach { exerciseName ->
            verify(exerciseMuscleDAL).selectExerciseMuscleByExercise(exerciseName)
            verify(exerciseEquipmentDAL).selectExerciseEquipmentByExercise(exerciseName)
            verify(exerciseWorkoutTypeDAL).selectExerciseWorkoutTypesByExercise(exerciseName)
        }
    }

    @Test
    fun `should handle errors gracefully and continue warmup`() {
        // Configure warmup settings
        whenever(cacheWarmupConfig.enabled).thenReturn(true)
        whenever(cacheWarmupConfig.warmupReferenceData).thenReturn(true)
        whenever(cacheWarmupConfig.warmupLists).thenReturn(true)
        whenever(cacheWarmupConfig.warmupRelationships).thenReturn(true)
        whenever(cacheWarmupConfig.popularExercises).thenReturn(listOf("Bench Press", "Squat"))
        whenever(cacheWarmupConfig.popularEquipment).thenReturn(listOf("Barbell"))
        whenever(cacheWarmupConfig.popularMuscles).thenReturn(listOf("Chest"))

        whenever(exerciseDAL.selectExerciseByName("Bench Press")).thenReturn(Mono.error(RuntimeException("Database error")))
        whenever(exerciseDAL.selectExerciseByName("Squat")).thenReturn(Mono.just(createMockExercise("Squat")))

        whenever(equipmentDAL.selectEquipmentByName(any())).thenReturn(Mono.just(createMockEquipment("Barbell")))
        whenever(muscleDAL.selectMuscleByName(any())).thenReturn(Mono.just(createMockMuscle("Chest")))
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(listOf(createMockExercise("Exercise"))))
        whenever(equipmentDAL.selectEquipment()).thenReturn(Mono.just(listOf(createMockEquipment("Equipment"))))
        whenever(muscleDAL.selectMuscles()).thenReturn(Mono.just(listOf(createMockMuscle("Muscle"))))
        whenever(workoutStageTypeDAL.selectWorkoutStageTypes()).thenReturn(Mono.just(listOf(createMockWorkoutStageType())))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(any())).thenReturn(Mono.just(listOf(createMockExerciseMuscle("Exercise", "Muscle"))))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(any())).thenReturn(Mono.just(listOf(createMockExerciseEquipment("Exercise", "Equipment"))))
        whenever(exerciseWorkoutTypeDAL.selectExerciseWorkoutTypesByExercise(any())).thenReturn(Mono.just(listOf(createMockExerciseWorkoutType("Exercise", "Type"))))

        cacheWarmupService.run(applicationArguments)

        verify(exerciseDAL).selectExerciseByName("Squat")
        verify(exerciseDAL).selectExerciseByName("Bench Press")
        verify(exerciseDAL).selectExercises()
        verify(equipmentDAL).selectEquipment()
        verify(muscleDAL).selectMuscles()
        verify(workoutStageTypeDAL).selectWorkoutStageTypes()
    }

    @Test
    fun `should complete warmup even when all operations fail`() {
        // Configure warmup settings
        whenever(cacheWarmupConfig.enabled).thenReturn(true)
        whenever(cacheWarmupConfig.warmupReferenceData).thenReturn(true)
        whenever(cacheWarmupConfig.warmupLists).thenReturn(true)
        whenever(cacheWarmupConfig.warmupRelationships).thenReturn(true)
        whenever(cacheWarmupConfig.popularExercises).thenReturn(listOf("Bench Press", "Squat", "Deadlift"))
        whenever(cacheWarmupConfig.popularEquipment).thenReturn(listOf("Barbell", "Dumbbell"))
        whenever(cacheWarmupConfig.popularMuscles).thenReturn(listOf("Chest", "Back"))

        whenever(exerciseDAL.selectExerciseByName(any())).thenReturn(Mono.error(RuntimeException("Error")))
        whenever(equipmentDAL.selectEquipmentByName(any())).thenReturn(Mono.error(RuntimeException("Error")))
        whenever(muscleDAL.selectMuscleByName(any())).thenReturn(Mono.error(RuntimeException("Error")))
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.error(RuntimeException("Error")))
        whenever(equipmentDAL.selectEquipment()).thenReturn(Mono.error(RuntimeException("Error")))
        whenever(muscleDAL.selectMuscles()).thenReturn(Mono.error(RuntimeException("Error")))
        whenever(workoutStageTypeDAL.selectWorkoutStageTypes()).thenReturn(Mono.error(RuntimeException("Error")))
        whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(any())).thenReturn(Mono.error(RuntimeException("Error")))
        whenever(exerciseEquipmentDAL.selectExerciseEquipmentByExercise(any())).thenReturn(Mono.error(RuntimeException("Error")))
        whenever(exerciseWorkoutTypeDAL.selectExerciseWorkoutTypesByExercise(any())).thenReturn(Mono.error(RuntimeException("Error")))

        // The run method is asynchronous, so we need to wait a bit for the operations to complete
        cacheWarmupService.run(applicationArguments)
        
        // Wait a bit for the reactive operations to complete
        Thread.sleep(100)

        verify(exerciseDAL, atLeastOnce()).selectExerciseByName(any())
        verify(equipmentDAL, atLeastOnce()).selectEquipmentByName(any())
        verify(muscleDAL, atLeastOnce()).selectMuscleByName(any())
        verify(exerciseDAL).selectExercises()
        verify(equipmentDAL).selectEquipment()
        verify(muscleDAL).selectMuscles()
        verify(workoutStageTypeDAL).selectWorkoutStageTypes()
    }

    @Test
    fun `should skip warmup when disabled in configuration`() {
        whenever(cacheWarmupConfig.enabled).thenReturn(false)
        
        cacheWarmupService.run(applicationArguments)

        // Verify that no DAL methods were called since warmup is disabled
        verifyNoInteractions(exerciseDAL, equipmentDAL, muscleDAL, exerciseMuscleDAL, 
                           exerciseEquipmentDAL, workoutStageTypeDAL, exerciseWorkoutTypeDAL, programDAL)
    }

    @Test
    fun `should skip specific warmup sections when disabled in configuration`() {
        whenever(cacheWarmupConfig.enabled).thenReturn(true)
        whenever(cacheWarmupConfig.warmupReferenceData).thenReturn(false)
        whenever(cacheWarmupConfig.warmupLists).thenReturn(true)
        whenever(cacheWarmupConfig.warmupRelationships).thenReturn(false)
        whenever(cacheWarmupConfig.warmupUserData).thenReturn(false)

        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(listOf(createMockExercise("Exercise"))))
        whenever(equipmentDAL.selectEquipment()).thenReturn(Mono.just(listOf(createMockEquipment("Equipment"))))
        whenever(muscleDAL.selectMuscles()).thenReturn(Mono.just(listOf(createMockMuscle("Muscle"))))
        whenever(workoutStageTypeDAL.selectWorkoutStageTypes()).thenReturn(Mono.just(listOf(createMockWorkoutStageType())))

        cacheWarmupService.run(applicationArguments)

        verify(exerciseDAL, never()).selectExerciseByName(any())
        verify(equipmentDAL, never()).selectEquipmentByName(any())
        verify(muscleDAL, never()).selectMuscleByName(any())
        verify(exerciseDAL).selectExercises()
        verify(equipmentDAL).selectEquipment()
        verify(muscleDAL).selectMuscles()
        verify(workoutStageTypeDAL).selectWorkoutStageTypes()
        verify(exerciseMuscleDAL, never()).selectExerciseMuscleByExercise(any())
        verify(exerciseEquipmentDAL, never()).selectExerciseEquipmentByExercise(any())
        verify(exerciseWorkoutTypeDAL, never()).selectExerciseWorkoutTypesByExercise(any())
        verify(userDAL, never()).selectRandomUserIds(any())
    }

    @Test
    fun `should warm up user data successfully`() {
        whenever(cacheWarmupConfig.enabled).thenReturn(true)
        whenever(cacheWarmupConfig.warmupReferenceData).thenReturn(false)
        whenever(cacheWarmupConfig.warmupLists).thenReturn(false)
        whenever(cacheWarmupConfig.warmupRelationships).thenReturn(false)

        whenever(cacheWarmupConfig.warmupUserData).thenReturn(true)
        whenever(cacheWarmupConfig.maxUsersToWarmup).thenReturn(2)

        val userIds = listOf("user1")
        whenever(userDAL.selectRandomUserIds(2)).thenReturn(Mono.just(userIds))
        
        // Mock all the user-related DAL calls
        val mockUser = createMockUser("user1")
        whenever(userDAL.selectUserByKeycloakId("user1")).thenReturn(Mono.just(mockUser))
        whenever(userEquipmentDAL.selectUserEquipmentByUser("user1")).thenReturn(Mono.just(listOf()))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser("user1")).thenReturn(Mono.just(listOf()))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser("user1")).thenReturn(Mono.just(listOf()))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser("user1")).thenReturn(Mono.just(listOf()))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences("user1")).thenReturn(Mono.just(createMockUserProgramPreferences("user1")))
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser("user1")).thenReturn(Mono.just(listOf()))
        whenever(gdprComplianceDAL.hasUserConsent("user1")).thenReturn(Mono.just(true))
        whenever(programDAL.selectProgramsByUserId("user1")).thenReturn(Mono.just(listOf()))
        whenever(exerciseRotationHistoryDAL.selectByUserId("user1")).thenReturn(Mono.just(listOf()))

        cacheWarmupService.run(applicationArguments)

        verify(userDAL).selectRandomUserIds(2)
    }

    private fun createMockExercise(name: String): Exercise {
        return Exercise(name = name, description = "Description for $name", movementType = MovementType.HORIZONTAL_PUSH, isUnilateral = false, isUpper = true, isAccessory = false)
    }

    private fun createMockEquipment(name: String): Equipment {
        return Equipment(name = name, description = "Description for $name")
    }

    private fun createMockMuscle(name: String): Muscle {
        return Muscle(name = name, description = "Description for $name")
    }

    private fun createMockWorkoutStageType(): WorkoutStageType {
        return WorkoutStageType(id = 1, name = WorkoutStageTypeEnum.WARMUP, createdAt = Instant.now())
    }

    private fun createMockExerciseMuscle(exerciseName: String, muscleName: String): ExerciseMuscle {
        return ExerciseMuscle(exerciseName = exerciseName, muscleName = muscleName)
    }

    private fun createMockExerciseEquipment(exerciseName: String, equipmentName: String): ExerciseEquipment {
        return ExerciseEquipment(exerciseName = exerciseName, equipmentName = equipmentName)
    }

    private fun createMockExerciseWorkoutType(exerciseName: String, workoutTypeName: String): ExerciseWorkoutType {
        return ExerciseWorkoutType(exerciseName = exerciseName, movementType = MovementType.HORIZONTAL_PUSH, workoutType = workoutTypeName)
    }

    private fun createMockProgram(name: String): Program {
        return Program(id = 1L, userId = "test-user-id", name = name, currentWeekNumber = 1, createdAt = Instant.now(), updatedAt = Instant.now(), isActive = true)
    }

    private fun createMockUser(keycloakId: String): User {
        return User(keycloakId = keycloakId, name = "Test User $keycloakId", createdAt = Instant.now(), updatedAt = Instant.now())
    }

    private fun createMockUserProgramPreferences(userId: String): UserProgramPreferences {
        return UserProgramPreferences(userId = userId, programDaysPerWeek = 3, sessionTimeLengthInMinutes = 60, createdAt = Instant.now(), updatedAt = Instant.now())
    }

    private fun createMockGdprCompliance(userId: String): UserConsent {
        return UserConsent(keycloakId = userId, dataProcessingConsent = true, consentTimestamp = Instant.now(), createdAt = Instant.now(), updatedAt = Instant.now())
    }
    }
