package com.congen

import com.congen.client.KeycloakClient
import com.congen.dal.GdprComplianceDAL
import com.congen.dal.ProgramDAL
import com.congen.dal.ProgramPreferencesDAL
import com.congen.dal.UserDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserPerformanceMetricsDAL
import com.congen.dal.UserPerformanceScoresDAL
import com.congen.dal.UserTestResultDAL
import com.congen.dal.UserWeakMuscleDAL
import com.congen.dal.UserWeightUnitPreferenceDAL
import com.congen.generator.DayTemplate
import com.congen.generator.PrilepinGuidelines
import com.congen.generator.SetSchemeParams
import com.congen.generator.WeightSelectionService
import com.congen.model.Band
import com.congen.model.Equipment
import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.ExerciseWorkoutType
import com.congen.model.HealthCheck
import com.congen.model.HealthCheckResponse
import com.congen.model.HealthStatus
import com.congen.model.MovementType
import com.congen.model.Muscle
import com.congen.model.Program
import com.congen.model.ProgramPreferences
import com.congen.model.ProgrammedExercise
import com.congen.model.ProgrammedWorkout
import com.congen.model.SetScheme
import com.congen.model.User
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.UserWeightUnitPreference
import com.congen.model.WeightUnit
import com.congen.model.WorkoutStage
import com.congen.model.WorkoutStageType
import com.congen.model.WorkoutStageTypeEnum
import com.congen.service.AuditService
import com.congen.service.GdprComplianceService
import com.congen.service.UserOneRepMaxService
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant

fun sampleInstant(): Instant = Instant.parse("2024-01-01T00:00:00Z")

fun mockUser(
    keycloakId: String = "b226d772-c063-4974-ae08-ab64134abbcf",
    name: String = "John Doe",
    age: Int? = null,
    weight: Int? = null,
    height: Int? = null,
    gender: String? = null,
    createdAt: Instant = sampleInstant(),
    updatedAt: Instant = sampleInstant()
): User =
    User(
        keycloakId = keycloakId,
        name = name,
        age = age,
        weight = weight,
        height = height,
        gender = gender,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun mockSetScheme(
    id: Long = 1L,
    programmedExerciseId: Long = 1L,
    setNumber: Int = 1,
    isAmrap: Boolean = false,
    isEmom: Boolean = false,
    useTempo: Boolean = false,
    eccentricTempo: String? = null,
    isometricTempo: String? = null,
    concentricTempo: String? = null,
    targetWeight: BigDecimal? = BigDecimal("100.0"),
    performedWeight: BigDecimal? = null,
    targetRepCount: Int? = null,
    performedRepCount: Int? = null,
    restSeconds: Int? = null,
    createdAt: Instant = sampleInstant(),
    updatedAt: Instant = sampleInstant(),
    band: Band? = null
): SetScheme =
    SetScheme(
        id = id,
        programmedExerciseId = programmedExerciseId,
        setNumber = setNumber,
        isAmrap = isAmrap,
        isEmom = isEmom,
        useTempo = useTempo,
        eccentricTempo = eccentricTempo,
        isometricTempo = isometricTempo,
        concentricTempo = concentricTempo,
        targetWeight = targetWeight,
        performedWeight = performedWeight,
        targetRepCount = targetRepCount,
        performedRepCount = performedRepCount,
        restSeconds = restSeconds,
        createdAt = createdAt,
        updatedAt = updatedAt,
        band = band
    )

fun mockProgrammedExercise(
    id: Long = 1L,
    workoutStageId: Long = 1L,
    exerciseName: String = "Bench Press",
    position: Int = 1,
    notes: String? = null,
    createdAt: Instant = sampleInstant(),
    updatedAt: Instant = sampleInstant()
): ProgrammedExercise =
    ProgrammedExercise(
        id = id,
        workoutStageId = workoutStageId,
        exerciseName = exerciseName,
        position = position,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun mockUserOneRepMax(
    userId: String = "b226d772-c063-4974-ae08-ab64134abbcf",
    exerciseName: String = "Bench Press",
    oneRepMax: BigDecimal = BigDecimal("100.0"),
    updatedAt: Instant = sampleInstant()
): UserOneRepMax =
    UserOneRepMax(
        userId = userId,
        exerciseName = exerciseName,
        oneRepMax = oneRepMax,
        updatedAt = updatedAt
    )

fun mockUserEquipment(
    userId: String = "b226d772-c063-4974-ae08-ab64134abbcf",
    equipmentName: String = "Barbell",
    createdAt: Instant = sampleInstant()
): UserEquipment =
    UserEquipment(
        userId = userId,
        equipmentName = equipmentName,
        createdAt = createdAt
    )

fun mockUserExercisePreference(
    userId: String = "b226d772-c063-4974-ae08-ab64134abbcf",
    exerciseName: String = "Bench Press",
    shouldAvoid: Boolean = false,
    createdAt: Instant = sampleInstant()
): UserExercisePreference =
    UserExercisePreference(
        userId = userId,
        exerciseName = exerciseName,
        shouldAvoid = shouldAvoid,
        createdAt = createdAt
    )

fun mockProgramPreferences(
    programId: Long = 1L,
    programDaysPerWeek: Int = 3,
    sessionTimeLengthInMinutes: Int = 60,
    createdAt: Instant = sampleInstant(),
    updatedAt: Instant = sampleInstant()
): ProgramPreferences =
    ProgramPreferences(
        programId = programId,
        programDaysPerWeek = programDaysPerWeek,
        sessionTimeLengthInMinutes = sessionTimeLengthInMinutes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

fun mockExercise(
    name: String = "Bench Press",
    description: String = "A compound upper body exercise",
    movementType: MovementType = MovementType.HORIZONTAL_PUSH,
    isUnilateral: Boolean = false,
    isUpper: Boolean = true,
    isAccessory: Boolean = false
): Exercise =
    Exercise(
        name = name,
        description = description,
        movementType = movementType,
        isUnilateral = isUnilateral,
        isUpper = isUpper,
        isAccessory = isAccessory
    )

fun mockProgram(
    id: Long = 1L,
    userId: String = "b226d772-c063-4974-ae08-ab64134abbcf",
    name: String = "Test Program",
    currentWeekNumber: Int = 1,
    createdAt: Instant = sampleInstant(),
    updatedAt: Instant = sampleInstant(),
    isActive: Boolean = true
): Program =
    Program(
        id = id,
        userId = userId,
        name = name,
        currentWeekNumber = currentWeekNumber,
        createdAt = createdAt,
        updatedAt = updatedAt,
        isActive = isActive
    )

fun mockProgrammedWorkout(
    id: Long = 1L,
    programId: Long = 1L,
    dayNumber: Int = 1,
    name: String = "ME_Upper Day",
    createdAt: Instant = sampleInstant(),
    updatedAt: Instant = sampleInstant()
): ProgrammedWorkout =
    ProgrammedWorkout(
        id = id,
        programId = programId,
        dayNumber = dayNumber,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

// Health check helpers
fun mockHealthCheck(
    componentId: String = "postgres",
    componentType: String = "database",
    status: HealthStatus = HealthStatus.PASS,
    output: String = "Database connection successful"
): HealthCheck =
    HealthCheck(
        componentId = componentId,
        componentType = componentType,
        status = status,
        output = output
    )

fun mockHealthCheckResponse(
    status: HealthStatus = HealthStatus.PASS,
    version: String = "1.2.3",
    releaseId: String = "abc123",
    databaseStatus: HealthStatus = HealthStatus.PASS,
    keycloakStatus: HealthStatus = HealthStatus.PASS,
    applicationStatus: HealthStatus = HealthStatus.PASS
): HealthCheckResponse =
    HealthCheckResponse(
        status = status,
        version = version,
        releaseId = releaseId,
        checks =
            mapOf(
                "database" to
                    listOf(
                        mockHealthCheck(
                            componentId = "postgres",
                            componentType = "database",
                            status = databaseStatus,
                            output =
                                when (databaseStatus) {
                                    HealthStatus.PASS -> "Database connection successful"
                                    HealthStatus.WARN -> "Database connection slow"
                                    HealthStatus.FAIL -> "Database connection failed"
                                }
                        )
                    ),
                "keycloak" to
                    listOf(
                        mockHealthCheck(
                            componentId = "keycloak",
                            componentType = "auth",
                            status = keycloakStatus,
                            output =
                                when (keycloakStatus) {
                                    HealthStatus.PASS -> "Keycloak connection successful"
                                    HealthStatus.WARN -> "Keycloak connection slow"
                                    HealthStatus.FAIL -> "Keycloak connection failed"
                                }
                        )
                    ),
                "application" to
                    listOf(
                        mockHealthCheck(
                            componentId = "congen-api",
                            componentType = "service",
                            status = applicationStatus,
                            output = "Application is running"
                        )
                    )
            )
    )

// Add more helpers as needed for other models and test data

// Equipment helpers
fun mockEquipment(
    name: String = "Barbell",
    description: String = "A barbell for weightlifting"
): Equipment =
    Equipment(
        name = name,
        description = description
    )

fun mockExerciseEquipment(
    exerciseName: String = "Bench Press",
    equipmentName: String = "Barbell"
): ExerciseEquipment =
    ExerciseEquipment(
        exerciseName = exerciseName,
        equipmentName = equipmentName
    )

// Muscle helpers
fun mockMuscle(
    name: String = "Chest",
    description: String = "Chest muscles"
): Muscle =
    Muscle(
        name = name,
        description = description
    )

fun mockExerciseMuscle(
    exerciseName: String = "Bench Press",
    muscleName: String = "Chest"
): ExerciseMuscle =
    ExerciseMuscle(
        exerciseName = exerciseName,
        muscleName = muscleName
    )

// ExerciseWorkoutType helpers
fun mockExerciseWorkoutType(
    exerciseName: String = "Bench Press",
    movementType: MovementType = MovementType.HORIZONTAL_PUSH,
    workoutType: String = "dynamic_effort"
): ExerciseWorkoutType =
    ExerciseWorkoutType(
        exerciseName = exerciseName,
        movementType = movementType,
        workoutType = workoutType
    )

// WorkoutStage helpers
fun mockWorkoutStage(
    id: Long = 1L,
    programmedWorkoutId: Long = 5L,
    stageTypeId: Int = 1,
    position: Int = 1,
    name: String = "Main Lift",
    createdAt: Instant = sampleInstant(),
    updatedAt: Instant = sampleInstant()
): WorkoutStage =
    WorkoutStage(
        id = id,
        programmedWorkoutId = programmedWorkoutId,
        stageTypeId = stageTypeId,
        position = position,
        name = name,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

// WorkoutStageType helpers
fun mockWorkoutStageType(
    id: Int = 1,
    name: WorkoutStageTypeEnum = WorkoutStageTypeEnum.PRIMARY,
    createdAt: Instant = sampleInstant()
): WorkoutStageType =
    WorkoutStageType(
        id = id,
        name = name,
        createdAt = createdAt
    )

// Conjugate service helpers
fun mockSetSchemeParams(
    setNumber: Int = 1,
    isAmrap: Boolean = false,
    isEmom: Boolean = false,
    useTempo: Boolean = false,
    eccentricTempo: String? = null,
    isometricTempo: String? = null,
    concentricTempo: String? = null,
    targetWeight: BigDecimal? = BigDecimal("100.0"),
    performedWeight: BigDecimal? = null,
    targetRepCount: Int? = 5,
    performedRepCount: Int? = null,
    restSeconds: Int? = 180,
    band: Band? = null
): SetSchemeParams =
    SetSchemeParams(
        setNumber = setNumber,
        isAmrap = isAmrap,
        isEmom = isEmom,
        useTempo = useTempo,
        eccentricTempo = eccentricTempo,
        isometricTempo = isometricTempo,
        concentricTempo = concentricTempo,
        targetWeight = targetWeight,
        performedWeight = performedWeight,
        targetRepCount = targetRepCount,
        performedRepCount = performedRepCount,
        restSeconds = restSeconds,
        band = band
    )

fun mockDayTemplate(type: String = "ME_Upper"): DayTemplate = DayTemplate(type = type)

fun mockPrilepinGuidelines(
    intensityRange: ClosedFloatingPointRange<Double> = 0.8..0.9,
    repsPerSetRange: IntRange = 2..4,
    totalReps: Int = 15,
    totalRepsRange: IntRange = 12..18,
    restSeconds: IntRange = 180..300
): PrilepinGuidelines =
    PrilepinGuidelines(
        intensityRange = intensityRange,
        repsPerSetRange = repsPerSetRange,
        totalReps = totalReps,
        totalRepsRange = totalRepsRange,
        restSeconds = restSeconds
    )

fun mockWeightSelectionResult(
    targetWeight: BigDecimal = BigDecimal("100.0"),
    band: Band? = null
): WeightSelectionService.TargetWeightResult =
    WeightSelectionService.TargetWeightResult(
        targetWeight = targetWeight,
        band = band
    )

// Weight unit preference helpers
fun mockUserWeightUnitPreference(
    userId: String = "b226d772-c063-4974-ae08-ab64134abbcf",
    exerciseName: String = "Bench Press",
    preferredUnit: WeightUnit = WeightUnit.LBS,
    createdAt: Instant = sampleInstant(),
    updatedAt: Instant = sampleInstant()
): UserWeightUnitPreference =
    UserWeightUnitPreference(
        userId = userId,
        exerciseName = exerciseName,
        preferredUnit = preferredUnit,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

// Mock setup helpers for reactive testing
fun <T : Any> createMockMono(value: T): Mono<T> = Mono.just(value)

fun <T : Any> createMockMonoError(exception: Exception): Mono<T> = Mono.error(exception)

fun <T : Any> createMockFlux(values: List<T>): Flux<T> = Flux.fromIterable(values)

fun <T : Any> createMockFluxEmpty(): Flux<T> = Flux.empty()

// Common test assertions for reactive streams
fun <T : Any> assertMonoSuccess(
    mono: Mono<T>,
    expectedValue: T
) {
    StepVerifier.create(mono)
        .expectNext(expectedValue)
        .verifyComplete()
}

fun <T : Any> assertMonoError(
    mono: Mono<T>,
    expectedException: Class<out Exception>
) {
    StepVerifier.create(mono)
        .expectError(expectedException)
        .verify()
}

fun <T : Any> assertFluxSuccess(
    flux: Flux<T>,
    expectedValues: List<T>
) {
    StepVerifier.create(flux)
        .expectNextSequence(expectedValues)
        .verifyComplete()
}

fun <T : Any> assertFluxEmpty(flux: Flux<T>) {
    StepVerifier.create(flux)
        .verifyComplete()
}

/**
 * Creates a spy of GdprComplianceService with all dependencies mocked.
 * This helper function constructs all the required mocks for the GdprComplianceService
 * and returns a spy of the service for testing purposes.
 *
 * @return A spy of GdprComplianceService with all dependencies mocked
 */
fun createGdprComplianceServiceSpy(): GdprComplianceService {
    val gdprComplianceDAL = mock<GdprComplianceDAL>()
    val userDAL = mock<UserDAL>()
    val userEquipmentDAL = mock<UserEquipmentDAL>()
    val userExercisePreferenceDAL = mock<UserExercisePreferenceDAL>()
    val programPreferencesDAL = mock<ProgramPreferencesDAL>()
    val userOneRepMaxService = mock<UserOneRepMaxService>()
    val userWeightUnitPreferenceDAL = mock<UserWeightUnitPreferenceDAL>()
    val userPerformanceMetricsDAL = mock<UserPerformanceMetricsDAL>()
    val userPerformanceScoresDAL = mock<UserPerformanceScoresDAL>()
    val userTestResultDAL = mock<UserTestResultDAL>()
    val userWeakMuscleDAL = mock<UserWeakMuscleDAL>()
    val programDAL = mock<ProgramDAL>()
    val auditService = mock<AuditService>()
    val keycloakClient = mock<KeycloakClient>()

    val gdprComplianceService =
        GdprComplianceService(
            gdprComplianceDAL = gdprComplianceDAL,
            userDAL = userDAL,
            userEquipmentDAL = userEquipmentDAL,
            userExercisePreferenceDAL = userExercisePreferenceDAL,
            programPreferencesDAL = programPreferencesDAL,
            userOneRepMaxService = userOneRepMaxService,
            userWeightUnitPreferenceDAL = userWeightUnitPreferenceDAL,
            userPerformanceMetricsDAL = userPerformanceMetricsDAL,
            userPerformanceScoresDAL = userPerformanceScoresDAL,
            userTestResultDAL = userTestResultDAL,
            userWeakMuscleDAL = userWeakMuscleDAL,
            programDAL = programDAL,
            auditService = auditService,
            keycloakClient = keycloakClient,
            postgresClient = mock()
        )

    return spy(gdprComplianceService)
}
