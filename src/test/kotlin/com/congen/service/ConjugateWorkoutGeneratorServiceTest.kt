package com.congen.service

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.dal.ProgramDAL
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserProgramPreferencesDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.dal.WorkoutStageTypeDAL
import com.congen.exceptions.ValidationException
import com.congen.mockExercise
import com.congen.mockExerciseRotationHistory
import com.congen.mockUserEquipment
import com.congen.mockUserOneRepMax
import com.congen.mockUserProgramPreferences
import com.congen.model.Exercise
import com.congen.model.ExerciseRotationHistory
import com.congen.model.Program
import com.congen.model.ProgrammedWorkout
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.UserProgramPreferences
import com.congen.model.WorkoutStage
import com.congen.service.conjugate.ConjugateTemplates
import com.congen.service.conjugate.ExerciseSelectionService
import com.congen.service.conjugate.SessionTimeCalculator
import com.congen.service.conjugate.WorkoutStageGenerator
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

class ConjugateWorkoutGeneratorServiceTest {
    private companion object {
        private const val USER_ID = 1
        private const val CURRENT_WEEK = 1
        private const val PROGRAM_ID = 1L
        private const val PROGRAM_NAME = "Test Program"
        private const val WORKOUT_ID = 1L
        private const val WORKOUT_NAME = "ME_Upper Day"
        private const val STAGE_ID = 1L
        private const val STAGE_NAME = "Test Stage"
        private const val EXERCISE_NAME = "Bench Press"
        private const val EXERCISE_NAME_2 = "Incline Bench Press"
        private const val EXERCISE_NAME_3 = "Squat"
        private const val EXERCISE_NAME_4 = "Deadlift"
        private const val EXERCISE_NAME_5 = "Overhead Press"
        private const val EXERCISE_NAME_6 = "Pull-ups"
        private const val EXERCISE_NAME_7 = "Bicep Curls"
        private const val EXERCISE_NAME_8 = "Tricep Extensions"
        private const val EQUIPMENT_BARBELL = "Barbell"
        private const val EQUIPMENT_DUMBBELLS = "Dumbbells"
        private const val EQUIPMENT_BENCH = "Bench"
        private const val EQUIPMENT_PULLUP_BAR = "Pull-up Bar"
        private const val SESSION_TIME_MINUTES = 60
        private const val DAYS_PER_WEEK_2 = 2
        private const val DAYS_PER_WEEK_3 = 3
        private const val DAYS_PER_WEEK_4 = 4
        private const val DAYS_PER_WEEK_5 = 5
        private const val NUM_ACCESSORY_EXERCISES = 2
        private const val TARGET_REPS_PRIMARY = 5
        private const val TARGET_REPS_SECONDARY = 8
        private const val REST_SECONDS_PRIMARY = 180
        private const val REST_SECONDS_SECONDARY = 120
        private const val WEIGHT_PRIMARY = "100.0"
        private const val WEIGHT_SECONDARY = "80.0"
    }

    @Mock
    private lateinit var exerciseDAL: ExerciseDAL

    @Mock
    private lateinit var exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL

    @Mock
    private lateinit var programDAL: ProgramDAL

    @Mock
    private lateinit var programmedExerciseDAL: ProgrammedExerciseDAL

    @Mock
    private lateinit var programmedWorkoutDAL: ProgrammedWorkoutDAL

    @Mock
    private lateinit var setSchemeDAL: SetSchemeDAL

    @Mock
    private lateinit var userDAL: UserDAL

    @Mock
    private lateinit var userEquipmentDAL: UserEquipmentDAL

    @Mock
    private lateinit var userExercisePreferenceDAL: UserExercisePreferenceDAL

    @Mock
    private lateinit var userOneRepMaxDAL: UserOneRepMaxDAL

    @Mock
    private lateinit var userProgramPreferencesDAL: UserProgramPreferencesDAL

    @Mock
    private lateinit var workoutStageDAL: WorkoutStageDAL

    @Mock
    private lateinit var workoutStageTypeDAL: WorkoutStageTypeDAL

    @Mock
    private lateinit var conjugateTemplates: ConjugateTemplates

    @Mock
    private lateinit var exerciseSelectionService: ExerciseSelectionService

    @Mock
    private lateinit var workoutStageGenerator: WorkoutStageGenerator

    @Mock
    private lateinit var sessionTimeCalculator: SessionTimeCalculator

    private lateinit var conjugateWorkoutGeneratorService: ConjugateWorkoutGeneratorService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        conjugateWorkoutGeneratorService =
            ConjugateWorkoutGeneratorService(
                exerciseDAL,
                userExercisePreferenceDAL,
                userEquipmentDAL,
                userOneRepMaxDAL,
                userProgramPreferencesDAL,
                exerciseRotationHistoryDAL,
                programDAL,
                programmedWorkoutDAL,
                conjugateTemplates,
                exerciseSelectionService,
                workoutStageGenerator,
                sessionTimeCalculator
            )

        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        whenever(conjugateTemplates.selectTemplate(any())).thenReturn(
            listOf(
                com.congen.service.conjugate.DayTemplate("ME_Upper"),
                com.congen.service.conjugate.DayTemplate("DE_Lower"),
                com.congen.service.conjugate.DayTemplate("ME_Lower"),
                com.congen.service.conjugate.DayTemplate("DE_Upper")
            )
        )
        whenever(conjugateTemplates.hasSecondaryMovement(any())).thenReturn(true)
        whenever(conjugateTemplates.hasConditioning(any())).thenReturn(true)

        whenever(exerciseSelectionService.selectRotatingExercise(any(), any(), any(), any(), any(), any(), any())).thenReturn(
            mockExercise(name = EXERCISE_NAME)
        )
        whenever(exerciseSelectionService.filterExercisesByAccessoryStatus(any(), any())).thenReturn(
            listOf(mockExercise(name = EXERCISE_NAME))
        )
        whenever(exerciseSelectionService.filterExercisesExcluding(any(), any())).thenReturn(
            listOf(mockExercise(name = EXERCISE_NAME_2))
        )

        whenever(workoutStageGenerator.generatePrilepinBasedScheme(any(), any(), any(), any(), any(), any())).thenReturn(
            listOf(
                com.congen.service.conjugate.SetSchemeParams(
                    1,
                    false,
                    false,
                    false,
                    null,
                    null,
                    null,
                    BigDecimal(WEIGHT_PRIMARY),
                    null,
                    TARGET_REPS_PRIMARY,
                    null,
                    REST_SECONDS_PRIMARY
                )
            )
        )
        whenever(workoutStageGenerator.generateSecondaryExerciseScheme(any(), any(), any())).thenReturn(
            listOf(
                com.congen.service.conjugate.SetSchemeParams(
                    1,
                    false,
                    false,
                    false,
                    null,
                    null,
                    null,
                    BigDecimal(WEIGHT_SECONDARY),
                    null,
                    TARGET_REPS_SECONDARY,
                    null,
                    REST_SECONDS_SECONDARY
                )
            )
        )
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(
            Mono.just(mockWorkoutStage())
        )
        whenever(workoutStageGenerator.createProgrammedExercise(any(), any())).thenReturn(
            Mono.just(mockProgrammedExercise())
        )
        whenever(workoutStageGenerator.createSetSchemes(any(), any())).thenReturn(Mono.empty())

        whenever(
            sessionTimeCalculator.calculateNumAccessoryExercisesDynamic(any(), any(), any(), any())
        ).thenReturn(NUM_ACCESSORY_EXERCISES)
    }

    @Test
    fun `generateNextWeek should create program successfully with 3 days per week`() {
        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()
        val program = mockProgram()

        setupDALMocks(exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory, program)
        setupWorkoutMocks()

        val result = conjugateWorkoutGeneratorService.generateNextWeek(USER_ID, CURRENT_WEEK)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should handle user with no exercise history`() {
        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = emptyList<UserOneRepMax>()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()
        val program = mockProgram()

        setupDALMocks(exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory, program)
        setupWorkoutMocks()

        val result = conjugateWorkoutGeneratorService.generateNextWeek(USER_ID, CURRENT_WEEK)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should handle user with exercise preferences`() {
        val exercises = createSampleExercises()
        val preferences =
            listOf(
                UserExercisePreference(
                    userId = USER_ID,
                    exerciseName = EXERCISE_NAME_3,
                    shouldAvoid = true,
                    createdAt = Instant.now()
                )
            )
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()
        val program = mockProgram()

        setupDALMocks(exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory, program)
        setupWorkoutMocks()

        val result = conjugateWorkoutGeneratorService.generateNextWeek(USER_ID, CURRENT_WEEK)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should handle user with exercise rotation history`() {
        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory =
            listOf(
                mockExerciseRotationHistory(exerciseName = EXERCISE_NAME)
            )
        val program = mockProgram()

        setupDALMocks(exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory, program)
        setupWorkoutMocks()

        val result = conjugateWorkoutGeneratorService.generateNextWeek(USER_ID, CURRENT_WEEK)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should handle 2-day program`() {
        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = mockUserProgramPreferences(programDaysPerWeek = DAYS_PER_WEEK_2)
        val rotationHistory = emptyList<ExerciseRotationHistory>()
        val program = mockProgram()

        setupDALMocks(exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory, program)
        setupWorkoutMocks()

        val result = conjugateWorkoutGeneratorService.generateNextWeek(USER_ID, CURRENT_WEEK)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should handle 4-day program`() {
        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = mockUserProgramPreferences(programDaysPerWeek = DAYS_PER_WEEK_4)
        val rotationHistory = emptyList<ExerciseRotationHistory>()
        val program = mockProgram()

        setupDALMocks(exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory, program)
        setupWorkoutMocks()

        val result = conjugateWorkoutGeneratorService.generateNextWeek(USER_ID, CURRENT_WEEK)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should handle database errors gracefully`() {
        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        setupDALMocks(exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory, null)
        whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.error(RuntimeException("Database error")))

        val result = conjugateWorkoutGeneratorService.generateNextWeek(USER_ID, CURRENT_WEEK)

        StepVerifier.create(result)
            .expectError(RuntimeException::class.java)
            .verify()
    }

    @Test
    fun `generateNextWeek should handle missing user data`() {
        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = emptyList<UserEquipment>()
        val oneRepMaxes = emptyList<UserOneRepMax>()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()
        val program = mockProgram()

        setupDALMocks(exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory, program)
        setupWorkoutMocks()

        val result = conjugateWorkoutGeneratorService.generateNextWeek(USER_ID, CURRENT_WEEK)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `should calculate accessory exercises based on session time`() {
        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()
        val program = mockProgram()

        setupDALMocks(exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory, program)
        setupWorkoutMocks()

        val result = conjugateWorkoutGeneratorService.generateNextWeek(USER_ID, CURRENT_WEEK)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `should calculate fewer accessories for DE days with conditioning`() {
        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()
        val program = mockProgram()

        setupDALMocks(exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory, program)
        setupWorkoutMocks()

        val result = conjugateWorkoutGeneratorService.generateNextWeek(USER_ID, CURRENT_WEEK)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `should prioritize unused exercises in rotation history`() {
        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory =
            listOf(
                mockExerciseRotationHistory(exerciseName = EXERCISE_NAME),
                mockExerciseRotationHistory(exerciseName = EXERCISE_NAME),
                mockExerciseRotationHistory(exerciseName = EXERCISE_NAME_2)
            )
        val program = mockProgram()

        setupDALMocks(exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory, program)
        setupWorkoutMocks()

        val result = conjugateWorkoutGeneratorService.generateNextWeek(USER_ID, CURRENT_WEEK)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should apply undulating periodization correctly for different weeks`() {
        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()
        val program = mockProgram()

        setupDALMocks(exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory, program)
        setupWorkoutMocks()

        val result = conjugateWorkoutGeneratorService.generateNextWeek(USER_ID, CURRENT_WEEK)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should select different exercises for primary and secondary stages`() {
        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = createSampleProgramPreferences()
        val rotationHistory = emptyList<ExerciseRotationHistory>()
        val program = mockProgram()

        setupDALMocks(exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory, program)
        setupWorkoutMocks()

        val result = conjugateWorkoutGeneratorService.generateNextWeek(USER_ID, CURRENT_WEEK)

        StepVerifier.create(result)
            .expectNext(program)
            .verifyComplete()
    }

    @Test
    fun `generateNextWeek should throw exception for invalid programDaysPerWeek`() {
        val exercises = createSampleExercises()
        val preferences = emptyList<UserExercisePreference>()
        val userEquipment = createSampleUserEquipment()
        val oneRepMaxes = createSampleOneRepMaxes()
        val programPreferences = mockUserProgramPreferences(programDaysPerWeek = DAYS_PER_WEEK_5)
        val rotationHistory = emptyList<ExerciseRotationHistory>()

        setupDALMocks(exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory, null)

        val result = conjugateWorkoutGeneratorService.generateNextWeek(USER_ID, CURRENT_WEEK)

        StepVerifier.create(result)
            .expectError(ValidationException::class.java)
            .verify()
    }

    private fun setupDALMocks(
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: UserProgramPreferences,
        rotationHistory: List<ExerciseRotationHistory>,
        program: Program?
    ) {
        whenever(exerciseDAL.selectExercises()).thenReturn(Mono.just(exercises))
        whenever(userExercisePreferenceDAL.selectUserExercisePreferencesByUser(USER_ID)).thenReturn(Mono.just(preferences))
        whenever(userEquipmentDAL.selectUserEquipmentByUser(USER_ID)).thenReturn(Mono.just(userEquipment))
        whenever(userOneRepMaxDAL.selectUserOneRepMaxByUser(USER_ID)).thenReturn(Mono.just(oneRepMaxes))
        whenever(userProgramPreferencesDAL.selectUserProgramPreferences(USER_ID)).thenReturn(Mono.just(programPreferences))
        whenever(exerciseRotationHistoryDAL.selectAll()).thenReturn(Mono.just(rotationHistory))
        program?.let { whenever(programDAL.insertProgram(any(), any(), any())).thenReturn(Mono.just(it)) }
    }

    private fun setupWorkoutMocks() {
        val createdWorkout = mockProgrammedWorkout()
        whenever(programmedWorkoutDAL.insertProgrammedWorkout(any(), any(), any())).thenReturn(Mono.just(createdWorkout))
        whenever(workoutStageGenerator.createWorkoutStage(any(), any(), any())).thenReturn(Mono.just(mockWorkoutStage()))
    }

    private fun mockProgram(): Program {
        return Program(
            id = PROGRAM_ID,
            userId = USER_ID,
            name = PROGRAM_NAME,
            currentWeekNumber = CURRENT_WEEK,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    private fun mockProgrammedWorkout(): ProgrammedWorkout {
        return ProgrammedWorkout(
            id = WORKOUT_ID,
            programId = PROGRAM_ID,
            dayNumber = 1,
            name = WORKOUT_NAME,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    private fun mockWorkoutStage(): WorkoutStage {
        return WorkoutStage(
            id = STAGE_ID,
            programmedWorkoutId = WORKOUT_ID,
            stageTypeId = 1,
            name = STAGE_NAME,
            position = 1,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    private fun mockProgrammedExercise(): com.congen.model.ProgrammedExercise {
        return com.congen.model.ProgrammedExercise(
            id = 1L,
            workoutStageId = STAGE_ID,
            exerciseName = EXERCISE_NAME,
            position = 1,
            notes = null,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }

    private fun createSampleExercises(): List<Exercise> {
        return listOf(
            mockExercise(
                name = EXERCISE_NAME,
                description = "A compound upper body exercise",
                movementType = "horizontal_push",
                isUpper = true,
                isAccessory = false
            ),
            mockExercise(
                name = EXERCISE_NAME_2,
                description = "An incline compound upper body exercise",
                movementType = "horizontal_push",
                isUpper = true,
                isAccessory = false
            ),
            mockExercise(
                name = EXERCISE_NAME_3,
                description = "A compound lower body exercise",
                movementType = "squat",
                isUpper = false,
                isAccessory = false
            ),
            mockExercise(
                name = EXERCISE_NAME_4,
                description = "A compound hinge exercise",
                movementType = "hinge",
                isUpper = false,
                isAccessory = false
            ),
            mockExercise(
                name = EXERCISE_NAME_5,
                description = "A compound vertical push exercise",
                movementType = "vertical_push",
                isUpper = true,
                isAccessory = false
            ),
            mockExercise(
                name = EXERCISE_NAME_6,
                description = "A compound vertical pull exercise",
                movementType = "vertical_pull",
                isUpper = true,
                isAccessory = false
            ),
            mockExercise(
                name = EXERCISE_NAME_7,
                description = "An isolation exercise",
                movementType = "accessory",
                isUpper = true,
                isAccessory = true
            ),
            mockExercise(
                name = EXERCISE_NAME_8,
                description = "An isolation exercise",
                movementType = "accessory",
                isUpper = true,
                isAccessory = true
            )
        )
    }

    private fun createSampleUserEquipment(): List<UserEquipment> {
        return listOf(
            mockUserEquipment(equipmentName = EQUIPMENT_BARBELL),
            mockUserEquipment(equipmentName = EQUIPMENT_DUMBBELLS),
            mockUserEquipment(equipmentName = EQUIPMENT_BENCH),
            mockUserEquipment(equipmentName = EQUIPMENT_PULLUP_BAR)
        )
    }

    private fun createSampleOneRepMaxes(): List<UserOneRepMax> {
        return listOf(
            mockUserOneRepMax(exerciseName = EXERCISE_NAME, oneRepMax = BigDecimal("100.0")),
            mockUserOneRepMax(exerciseName = EXERCISE_NAME_3, oneRepMax = BigDecimal("150.0")),
            mockUserOneRepMax(exerciseName = EXERCISE_NAME_4, oneRepMax = BigDecimal("200.0"))
        )
    }

    private fun createSampleProgramPreferences(): UserProgramPreferences {
        return mockUserProgramPreferences(programDaysPerWeek = DAYS_PER_WEEK_3)
    }

    private fun mockUserProgramPreferences(programDaysPerWeek: Int): UserProgramPreferences {
        return UserProgramPreferences(
            userId = USER_ID,
            programDaysPerWeek = programDaysPerWeek,
            sessionTimeLengthInMinutes = SESSION_TIME_MINUTES,
            createdAt = Instant.now(),
            updatedAt = Instant.now()
        )
    }
}
