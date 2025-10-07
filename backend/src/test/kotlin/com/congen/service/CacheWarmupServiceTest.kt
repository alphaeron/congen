package com.congen.service

import com.congen.config.CacheWarmupConfig
import com.congen.dal.EquipmentDAL
import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.ExerciseWorkoutTypeDAL
import com.congen.dal.GdprComplianceDAL
import com.congen.dal.MuscleDAL
import com.congen.dal.ProgramDAL
import com.congen.dal.ProgramPreferencesDAL
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserWeakMuscleDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.model.Equipment
import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.ExerciseWorkoutType
import com.congen.model.MovementType
import com.congen.model.Muscle
import com.congen.model.User
import com.congen.model.WorkoutStageType
import com.congen.model.WorkoutStageTypeEnum
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
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

    @Mock private lateinit var userOneRepMaxService: UserOneRepMaxService

    @Mock private lateinit var userWeakMuscleDAL: UserWeakMuscleDAL

    @Mock private lateinit var programPreferencesDAL: ProgramPreferencesDAL

    @Mock private lateinit var userWeightUnitPreferenceDAL: UserWeightUnitPreferenceDAL

    @Mock private lateinit var gdprComplianceDAL: GdprComplianceDAL

    @Mock private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL

    @Mock private lateinit var workoutStageDAL: WorkoutStageDAL

    @Mock private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL

    @Mock private lateinit var setSchemeDAL: SetSchemeDAL

    @Mock private lateinit var cacheWarmupConfig: CacheWarmupConfig


    private lateinit var cacheWarmupService: CacheWarmupService

    @BeforeEach
    fun setUp() {
        cacheWarmupService =
            CacheWarmupService(
                exerciseDAL,
                equipmentDAL,
                muscleDAL,
                exerciseMuscleDAL,
                exerciseEquipmentDAL,
                workoutStageTypeDAL,
                exerciseWorkoutTypeDAL,
                programDAL,
                userDAL,
                userEquipmentDAL,
                userExercisePreferenceDAL,
                userOneRepMaxService,
                userWeakMuscleDAL,
                programPreferencesDAL,
                userWeightUnitPreferenceDAL,
                gdprComplianceDAL,
                programmedWorkoutDAL,
                workoutStageDAL,
                programmedExerciseDAL,
                setSchemeDAL,
                cacheWarmupConfig
            )
    }

    @Test
    fun `should warm up reference data successfully`() {
        // Configure warmup settings
        whenever(cacheWarmupConfig.enabled).thenReturn(true)
        whenever(cacheWarmupConfig.warmupReferenceData).thenReturn(true)
        whenever(cacheWarmupConfig.warmupLists).thenReturn(false)
        whenever(cacheWarmupConfig.warmupRelationships).thenReturn(false)
        whenever(cacheWarmupConfig.popularExercises).thenReturn(
            listOf(
                "Bench Press",
                "Back Squat",
                "Deadlift",
                "Overhead Press",
                "Chin-Up",
                "TRX Push-Up",
                "Bent-Over Row",
                "Split Squat",
                "Front Squat",
                "Landmine Row"
            )
        )
        whenever(cacheWarmupConfig.popularEquipment).thenReturn(
            listOf(
                "power bar",
                "dumbbells",
                "pull-up bar",
                "bench",
                "power rack"
            )
        )
        whenever(cacheWarmupConfig.popularMuscles).thenReturn(
            listOf(
                "pec major",
                "lats",
                "quadriceps",
                "anterior deltoid",
                "biceps",
                "rectus abdominis"
            )
        )

        // Mock all popular exercises
        listOf(
            "Bench Press",
            "Back Squat",
            "Deadlift",
            "Overhead Press",
            "Chin-Up",
            "TRX Push-Up",
            "Bent-Over Row",
            "Split Squat",
            "Front Squat",
            "Landmine Row"
        ).forEach { exerciseName ->
            whenever(exerciseDAL.selectExerciseByName(exerciseName)).thenReturn(Mono.just(createMockExercise(exerciseName)))
        }

        // Mock all popular equipment
        listOf(
            "power bar",
            "dumbbells",
            "pull-up bar",
            "bench",
            "power rack"
        ).forEach { equipmentName ->
            whenever(equipmentDAL.selectEquipmentByName(equipmentName)).thenReturn(Mono.just(createMockEquipment(equipmentName)))
        }

        // Mock all popular muscles
        listOf(
            "pec major",
            "lats",
            "quadriceps",
            "anterior deltoid",
            "biceps",
            "rectus abdominis"
        ).forEach { muscleName ->
            whenever(muscleDAL.selectMuscleByName(muscleName)).thenReturn(Mono.just(createMockMuscle(muscleName)))
        }

        cacheWarmupService.onApplicationReady()

        // Verify all popular exercises were called
        listOf(
            "Bench Press",
            "Back Squat",
            "Deadlift",
            "Overhead Press",
            "Chin-Up",
            "TRX Push-Up",
            "Bent-Over Row",
            "Split Squat",
            "Front Squat",
            "Landmine Row"
        ).forEach { exerciseName -> verify(exerciseDAL).selectExerciseByName(exerciseName) }

        // Verify all popular equipment were called
        listOf(
            "power bar",
            "dumbbells",
            "pull-up bar",
            "bench",
            "power rack"
        ).forEach { equipmentName -> verify(equipmentDAL).selectEquipmentByName(equipmentName) }

        // Verify all popular muscles were called
        listOf(
            "pec major",
            "lats",
            "quadriceps",
            "anterior deltoid",
            "biceps",
            "rectus abdominis"
        ).forEach { muscleName -> verify(muscleDAL).selectMuscleByName(muscleName) }
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

        cacheWarmupService.onApplicationReady()

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
        whenever(cacheWarmupConfig.popularExercises).thenReturn(listOf("Bench Press", "Back Squat"))

        val exerciseMuscleRelationships = listOf(createMockExerciseMuscle("Bench Press", "pec major"))
        val exerciseEquipmentRelationships = listOf(createMockExerciseEquipment("Bench Press", "power bar"))
        val exerciseWorkoutTypeRelationships = listOf(createMockExerciseWorkoutType("Bench Press", "Strength"))

        listOf("Bench Press", "Back Squat").forEach { exerciseName ->
            whenever(exerciseMuscleDAL.selectExerciseMuscleByExercise(exerciseName)).thenReturn(Mono.just(exerciseMuscleRelationships))
            whenever(
                exerciseEquipmentDAL.selectExerciseEquipmentByExercise(exerciseName)
            ).thenReturn(Mono.just(exerciseEquipmentRelationships))
            whenever(
                exerciseWorkoutTypeDAL.selectExerciseWorkoutTypesByExercise(exerciseName)
            ).thenReturn(Mono.just(exerciseWorkoutTypeRelationships))
        }

        cacheWarmupService.onApplicationReady()

        listOf("Bench Press", "Back Squat").forEach { exerciseName ->
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
        whenever(cacheWarmupConfig.popularExercises).thenReturn(listOf("Bench Press", "Back Squat"))
        whenever(cacheWarmupConfig.popularEquipment).thenReturn(listOf("power bar"))
        whenever(cacheWarmupConfig.popularMuscles).thenReturn(listOf("pec major"))

        whenever(exerciseDAL.selectExerciseByName("Bench Press")).thenReturn(Mono.error(RuntimeException("Database error")))
        whenever(exerciseDAL.selectExerciseByName("Back Squat")).thenReturn(Mono.just(createMockExercise("Back Squat")))

        whenever(equipmentDAL.selectEquipmentByName(any())).thenReturn(Mono.just(createMockEquipment("power bar")))
        whenever(muscleDAL.selectMuscleByName(any())).thenReturn(Mono.just(createMockMuscle("pec major")))
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(listOf(createMockExercise("Exercise"))))
        whenever(equipmentDAL.selectEquipment()).thenReturn(Mono.just(listOf(createMockEquipment("Equipment"))))
        whenever(muscleDAL.selectMuscles()).thenReturn(Mono.just(listOf(createMockMuscle("Muscle"))))
        whenever(workoutStageTypeDAL.selectWorkoutStageTypes()).thenReturn(Mono.just(listOf(createMockWorkoutStageType())))
        whenever(
            exerciseMuscleDAL.selectExerciseMuscleByExercise(any())
        ).thenReturn(Mono.just(listOf(createMockExerciseMuscle("Exercise", "Muscle"))))
        whenever(
            exerciseEquipmentDAL.selectExerciseEquipmentByExercise(any())
        ).thenReturn(Mono.just(listOf(createMockExerciseEquipment("Exercise", "Equipment"))))
        whenever(
            exerciseWorkoutTypeDAL.selectExerciseWorkoutTypesByExercise(any())
        ).thenReturn(Mono.just(listOf(createMockExerciseWorkoutType("Exercise", "Type"))))

        cacheWarmupService.onApplicationReady()

        verify(exerciseDAL).selectExerciseByName("Back Squat")
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
        whenever(cacheWarmupConfig.popularExercises).thenReturn(listOf("Bench Press", "Back Squat", "Deadlift"))
        whenever(cacheWarmupConfig.popularEquipment).thenReturn(listOf("power bar", "dumbbells"))
        whenever(cacheWarmupConfig.popularMuscles).thenReturn(listOf("pec major", "lats"))

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
        cacheWarmupService.onApplicationReady()

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

        cacheWarmupService.onApplicationReady()

        // Verify that no DAL methods were called since warmup is disabled
        verifyNoInteractions(
            exerciseDAL,
            equipmentDAL,
            muscleDAL,
            exerciseMuscleDAL,
            exerciseEquipmentDAL,
            workoutStageTypeDAL,
            exerciseWorkoutTypeDAL,
            programDAL
        )
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

        cacheWarmupService.onApplicationReady()

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
        whenever(userOneRepMaxService.selectUserOneRepMaxByUser("user1")).thenReturn(Mono.just(listOf()))
        whenever(userWeakMuscleDAL.selectUserWeakMusclesByUser("user1")).thenReturn(Mono.just(listOf()))
        whenever(userWeightUnitPreferenceDAL.selectUserWeightUnitPreferencesByUser("user1")).thenReturn(Mono.just(listOf()))
        whenever(gdprComplianceDAL.hasUserConsent("user1")).thenReturn(Mono.just(true))
        whenever(programDAL.selectProgramsByUserId("user1")).thenReturn(Mono.just(listOf()))

        cacheWarmupService.onApplicationReady()

        verify(userDAL).selectRandomUserIds(2)
    }

    private fun createMockExercise(name: String): Exercise {
        return Exercise(
            name = name,
            description = "Description for $name",
            movementType = MovementType.HORIZONTAL_PUSH,
            isUnilateral = false,
            isUpper = true,
            isAccessory = false
        )
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

    private fun createMockExerciseMuscle(
        exerciseName: String,
        muscleName: String
    ): ExerciseMuscle {
        return ExerciseMuscle(exerciseName = exerciseName, muscleName = muscleName)
    }

    private fun createMockExerciseEquipment(
        exerciseName: String,
        equipmentName: String
    ): ExerciseEquipment {
        return ExerciseEquipment(exerciseName = exerciseName, equipmentName = equipmentName)
    }

    private fun createMockExerciseWorkoutType(
        exerciseName: String,
        workoutTypeName: String
    ): ExerciseWorkoutType {
        return ExerciseWorkoutType(exerciseName = exerciseName, movementType = MovementType.HORIZONTAL_PUSH, workoutType = workoutTypeName)
    }

    private fun createMockUser(keycloakId: String): User {
        return User(
            keycloakId = keycloakId,
            name = "Test User $keycloakId",
            age = null,
            weight = null,
            height = null,
            gender = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}
