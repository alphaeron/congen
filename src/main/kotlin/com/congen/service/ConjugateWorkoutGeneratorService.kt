package com.congen.service

import com.congen.dal.ExerciseDAL
import com.congen.dal.ExerciseEquipmentDAL
import com.congen.dal.ExerciseMuscleDAL
import com.congen.dal.ExerciseRotationHistoryDAL
import com.congen.dal.ProgramDAL
import com.congen.dal.ProgrammedExerciseDAL
import com.congen.dal.ProgrammedWorkoutDAL
import com.congen.dal.SetSchemeDAL
import com.congen.dal.UserEquipmentDAL
import com.congen.dal.UserExercisePreferenceDAL
import com.congen.dal.UserOneRepMaxDAL
import com.congen.dal.UserProgramPreferencesDAL
import com.congen.dal.WorkoutStageDAL
import com.congen.exceptions.NoResultsFoundException
import com.congen.model.Exercise
import com.congen.model.ExerciseEquipment
import com.congen.model.ExerciseMuscle
import com.congen.model.ExerciseRotationHistory
import com.congen.model.Program
import com.congen.model.ProgrammedExercise
import com.congen.model.ProgrammedWorkout
import com.congen.model.SetScheme
import com.congen.model.UserEquipment
import com.congen.model.UserExercisePreference
import com.congen.model.UserOneRepMax
import com.congen.model.UserProgramPreferences
import com.congen.model.WorkoutStage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import kotlin.random.Random

/**
 * Service for generating conjugate powerlifting workout programs.
 *
 * This service implements the Westside Barbell conjugate method for powerlifting,
 * incorporating undulating periodization, exercise rotation, and personalized
 * programming based on user preferences and available equipment.
 *
 * ## Conjugate Method Principles
 *
 * - **Max Effort (ME)**: Heavy singles, doubles, or triples at 85-92% 1RM
 * - **Dynamic Effort (DE)**: Speed work at 60-70% 1RM with explosive intent
 * - **Accessory Work**: Targeted muscle development and weak point training
 * - **Exercise Rotation**: Prevent accommodation by rotating exercises every 1-3 weeks
 *
 * ## Program Structure
 *
 * - **2-day programs**: Condensed conjugate approach (Phil Daru method)
 * - **3-day programs**: Traditional conjugate with ME/DE/accessory split
 * - **4-day programs**: Extended conjugate with additional volume
 *
 * ## Exercise Categories
 *
 * - **Primary**: Main compound movements (squat, bench, deadlift variations)
 * - **Secondary**: Supporting compound movements
 * - **Accessory**: Isolation and weak point training
 * - **Conditioning**: Cardio and recovery work
 *
 * @property exerciseDAL Data access layer for exercise operations
 * @property exerciseMuscleDAL Data access layer for exercise-muscle relationships
 * @property exerciseEquipmentDAL Data access layer for exercise-equipment relationships
 * @property userExercisePreferenceDAL Data access layer for user exercise preferences
 * @property userEquipmentDAL Data access layer for user equipment
 * @property userOneRepMaxDAL Data access layer for user one rep max values
 * @property userProgramPreferencesDAL Data access layer for user program preferences
 * @property exerciseRotationHistoryDAL Data access layer for exercise rotation history
 * @property programDAL Data access layer for program operations
 * @property programmedWorkoutDAL Data access layer for programmed workout operations
 * @property workoutStageDAL Data access layer for workout stage operations
 * @property programmedExerciseDAL Data access layer for programmed exercise operations
 * @property setSchemeDAL Data access layer for set scheme operations
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class ConjugateWorkoutGeneratorService(
    private val exerciseDAL: ExerciseDAL,
    private val exerciseMuscleDAL: ExerciseMuscleDAL,
    private val exerciseEquipmentDAL: ExerciseEquipmentDAL,
    private val userExercisePreferenceDAL: UserExercisePreferenceDAL,
    private val userEquipmentDAL: UserEquipmentDAL,
    private val userOneRepMaxDAL: UserOneRepMaxDAL,
    private val userProgramPreferencesDAL: UserProgramPreferencesDAL,
    private val exerciseRotationHistoryDAL: ExerciseRotationHistoryDAL,
    private val programDAL: ProgramDAL,
    private val programmedWorkoutDAL: ProgrammedWorkoutDAL,
    private val workoutStageDAL: WorkoutStageDAL,
    private val programmedExerciseDAL: ProgrammedExerciseDAL,
    private val setSchemeDAL: SetSchemeDAL,
) {
    companion object {
        /** Logger instance for this class. */
        private val logger = LoggerFactory.getLogger(ConjugateWorkoutGeneratorService::class.java)

        // Conjugate workout templates - focus on workout types rather than specific movements
        private val TWO_DAY_TEMPLATE = listOf(
            DayTemplate("ME_Upper"),
            DayTemplate("DE_Lower")
        )

        private val THREE_DAY_TEMPLATE = listOf(
            DayTemplate("ME_Upper"),
            DayTemplate("DE_Lower"),
            DayTemplate("ME_Lower")
        )

        private val FOUR_DAY_TEMPLATE = listOf(
            DayTemplate("ME_Upper"),
            DayTemplate("DE_Lower"),
            DayTemplate("ME_Lower"),
            DayTemplate("DE_Upper")
        )

        // Updated Prilepin's Chart guidelines for different intensity ranges
        private val PRILEPIN_GUIDELINES = mapOf(
            "0.55-0.65" to PrilepinGuidelines(
                intensityRange = 0.55..0.65,
                repsPerSetRange = 3..6,
                totalReps = 24,
                restSeconds = 60..90
            ),
            "0.7-0.8" to PrilepinGuidelines(
                intensityRange = 0.7..0.8,
                repsPerSetRange = 3..6,
                totalReps = 18,
                restSeconds = 90..120
            ),
            "0.8-0.9" to PrilepinGuidelines(
                intensityRange = 0.8..0.9,
                repsPerSetRange = 2..4,
                totalReps = 15,
                restSeconds = 180..300
            ),
            "0.9-1.0" to PrilepinGuidelines(
                intensityRange = 0.9..1.0,
                repsPerSetRange = 1..2,
                totalReps = 4,
                restSeconds = 180..300
            )
        )

        // Default weak muscles for new users
        private val DEFAULT_WEAK_MUSCLES = listOf("hamstrings", "glutes", "upper_back", "core")
    }

    /**
     * Generates the next week of workouts for a user's conjugate powerlifting program.
     *
     * This method creates a complete week of workouts based on the conjugate method,
     * incorporating user preferences, available equipment, and exercise rotation history.
     *
     * @param userId The ID of the user
     * @param currentWeekNumber The current week number in the program
     * @param numDaysPerWeek The number of training days per week (2, 3, or 4)
     * @return Mono containing the generated program with workouts
     */
    fun generateNextWeek(
        userId: Int,
        currentWeekNumber: Int,
        numDaysPerWeek: Int
    ): Mono<Program> {
        logger.info("Generating week {} for user {} with {} days per week", currentWeekNumber, userId, numDaysPerWeek)

        return Mono.zip(
            exerciseDAL.selectExercises(),
            userExercisePreferenceDAL.selectUserExercisePreferencesByUser(userId),
            userEquipmentDAL.selectUserEquipmentByUser(userId),
            userOneRepMaxDAL.selectUserOneRepMaxesByUser(userId),
            userProgramPreferencesDAL.selectUserProgramPreferencesByUser(userId),
            exerciseRotationHistoryDAL.selectAll()
        ).flatMap { (exercises, preferences, userEquipment, oneRepMaxes, programPreferences, rotationHistory) ->
            val template = selectTemplate(numDaysPerWeek)
            val weakMuscles = determineWeakMuscles(oneRepMaxes, rotationHistory)
            
            createProgram(userId, currentWeekNumber, numDaysPerWeek)
                .flatMap { program ->
                    generateWorkoutsForWeek(
                        program = program,
                        exercises = exercises,
                        preferences = preferences,
                        userEquipment = userEquipment,
                        oneRepMaxes = oneRepMaxes,
                        programPreferences = programPreferences,
                        rotationHistory = rotationHistory,
                        template = template,
                        weakMuscles = weakMuscles,
                        currentWeekNumber = currentWeekNumber
                    ).thenReturn(program)
                }
        }
    }

    /**
     * Creates a new program for the user.
     */
    private fun createProgram(userId: Int, currentWeekNumber: Int, numDaysPerWeek: Int): Mono<Program> {
        val programName = "Conjugate Powerlifting - Week $currentWeekNumber"
        val description = "Conjugate powerlifting program with $numDaysPerWeek days per week"
        
        val program = Program(
            id = 0, // Will be generated by database
            userId = userId,
            name = programName,
            description = description
        )
        
        return programDAL.insertProgram(program)
    }

    /**
     * Generates workouts for the specified week.
     */
    private fun generateWorkoutsForWeek(
        program: Program,
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: List<UserProgramPreferences>,
        rotationHistory: List<ExerciseRotationHistory>,
        template: List<DayTemplate>,
        weakMuscles: List<String>,
        currentWeekNumber: Int
    ): Mono<Void> {
        return template.foldIndexed(Mono.empty<Void>()) { dayIndex, mono, dayTemplate ->
            mono.flatMap {
                val dayNumber = (currentWeekNumber - 1) * template.size + dayIndex + 1
                
                // Create programmed workout
                val workout = ProgrammedWorkout(
                    id = 0,
                    programId = program.id,
                    dayNumber = dayNumber,
                    name = "${dayTemplate.type} Day"
                )
                
                programmedWorkoutDAL.insertProgrammedWorkout(workout)
                    .flatMap { createdWorkout ->
                        generateWorkoutStages(
                            workout = createdWorkout,
                            dayTemplate = dayTemplate,
                            exercises = exercises,
                            preferences = preferences,
                            userEquipment = userEquipment,
                            oneRepMaxes = oneRepMaxes,
                            programPreferences = programPreferences,
                            rotationHistory = rotationHistory,
                            weakMuscles = weakMuscles,
                            currentWeekNumber = currentWeekNumber
                        )
                    }
            }
        }
    }

    /**
     * Generates workout stages for a specific workout.
     */
    private fun generateWorkoutStages(
        workout: ProgrammedWorkout,
        dayTemplate: DayTemplate,
        exercises: List<Exercise>,
        preferences: List<UserExercisePreference>,
        userEquipment: List<UserEquipment>,
        oneRepMaxes: List<UserOneRepMax>,
        programPreferences: List<UserProgramPreferences>,
        rotationHistory: List<ExerciseRotationHistory>,
        weakMuscles: List<String>,
        currentWeekNumber: Int
    ): Mono<Void> {
        var stagePosition = 1

        // Calculate number of accessory exercises based on session time
        val sessionTimeMinutes = programPreferences.firstOrNull()?.sessionTimeLengthInMinutes ?: 60
        val numAccessoryExercises = calculateNumAccessoryExercises(sessionTimeMinutes, dayTemplate.type)

        // Primary movement stage (ME or DE exercise)
        val primaryExercise = selectRotatingExercise(
            userId = workout.programId.toInt(), // Using program ID as user ID for now
            targetMuscles = weakMuscles, // Use weak muscles instead of hardcoded muscles
            userEquipment = userEquipment,
            preferences = preferences,
            exercises = exercises.filter { !it.isAccessory }, // Only primary exercises (is_accessory = false)
            isAccessory = false, // Primary movements are not accessories
            rotationHistory = rotationHistory
        )

        var primaryMono = if (primaryExercise != null) {
            createWorkoutStage(workout.id, "primary", stagePosition++)
                .flatMap { primaryStage ->
                    createProgrammedExercise(primaryStage.id, primaryExercise.name)
                        .flatMap { primaryProgrammedExercise ->
                            val primaryScheme = generatePrilepinBasedScheme(
                                userId = workout.programId.toInt(),
                                exercise = primaryExercise,
                                movementRole = "primary",
                                dayType = dayTemplate.type,
                                oneRepMaxes = oneRepMaxes,
                                currentWeekNumber = currentWeekNumber
                            )
                            createSetSchemes(primaryProgrammedExercise.id, primaryScheme)
                        }
                }
        } else {
            Mono.empty<Void>()
        }

        // Secondary movement stage (if exists)
        var secondaryMono = if (dayTemplate.type in listOf("ME_Upper", "DE_Upper")) {
            val secondaryExercise = selectRotatingExercise(
                userId = workout.programId.toInt(),
                targetMuscles = weakMuscles, // Use weak muscles instead of hardcoded muscles
                userEquipment = userEquipment,
                preferences = preferences,
                exercises = exercises.filter { !it.isAccessory && it.name != primaryExercise?.name }, // Only primary exercises (is_accessory = false) that are different from primary
                isAccessory = false, // Secondary movements are not accessories
                rotationHistory = rotationHistory
            )

            if (secondaryExercise != null) {
                createWorkoutStage(workout.id, "secondary", stagePosition++)
                    .flatMap { secondaryStage ->
                        createProgrammedExercise(secondaryStage.id, secondaryExercise.name)
                            .flatMap { secondaryProgrammedExercise ->
                                val secondaryScheme = generateSecondaryExerciseScheme(
                                    userId = workout.programId.toInt(),
                                    exercise = secondaryExercise,
                                    oneRepMaxes = oneRepMaxes
                                )
                                createSetSchemes(secondaryProgrammedExercise.id, secondaryScheme)
                            }
                    }
            } else {
                Mono.empty<Void>()
            }
        } else {
            Mono.empty<Void>()
        }

        // Accessory movements (calculated based on session time)
        val accessoryMonos = (0 until numAccessoryExercises).map { accessoryIndex ->
            val accessoryExercise = selectRotatingExercise(
                userId = workout.programId.toInt(),
                targetMuscles = weakMuscles,
                userEquipment = userEquipment,
                preferences = preferences,
                exercises = exercises.filter { !it.isAccessory }, // Only accessory exercises (is_accessory = false)
                isAccessory = true, // Accessory movements are accessories
                rotationHistory = rotationHistory
            )

            if (accessoryExercise != null) {
                createWorkoutStage(workout.id, "accessory", stagePosition++)
                    .flatMap { accessoryStage ->
                        createProgrammedExercise(accessoryStage.id, accessoryExercise.name)
                            .flatMap { accessoryProgrammedExercise ->
                                val accessoryScheme = generatePrilepinBasedScheme(
                                    userId = workout.programId.toInt(),
                                    exercise = accessoryExercise,
                                    movementRole = "accessory",
                                    dayType = dayTemplate.type,
                                    oneRepMaxes = oneRepMaxes,
                                    currentWeekNumber = currentWeekNumber
                                )
                                createSetSchemes(accessoryProgrammedExercise.id, accessoryScheme)
                            }
                    }
            } else {
                Mono.empty<Void>()
            }
        }

        // Conditioning stage (optional)
        val conditioningMono = if (dayTemplate.type.contains("DE")) {
            val conditioningExercise = selectRotatingExercise(
                userId = workout.programId.toInt(),
                targetMuscles = listOf("full_body"),
                userEquipment = userEquipment,
                preferences = preferences,
                exercises = exercises.filter { !it.isAccessory }, // Use accessory exercises for conditioning (is_accessory = false)
                isAccessory = true, // Conditioning uses accessory exercises
                rotationHistory = rotationHistory
            )

            if (conditioningExercise != null) {
                createWorkoutStage(workout.id, "conditioning", stagePosition)
                    .flatMap { conditioningStage ->
                        createProgrammedExercise(conditioningStage.id, conditioningExercise.name)
                            .flatMap { conditioningProgrammedExercise ->
                                val conditioningScheme = generateAmrapOrEmomScheme(
                                    userId = workout.programId.toInt(),
                                    exercise = conditioningExercise,
                                    oneRepMaxes = oneRepMaxes
                                )
                                createSetSchemes(conditioningProgrammedExercise.id, conditioningScheme)
                            }
                    }
            } else {
                Mono.empty<Void>()
            }
        } else {
            Mono.empty<Void>()
        }

        // Combine all monos
        return Mono.concat(
            listOf(primaryMono, secondaryMono, conditioningMono) + accessoryMonos
        ).then()
    }

    /**
     * Selects the appropriate template based on the number of days per week.
     */
    private fun selectTemplate(numDaysPerWeek: Int): List<DayTemplate> {
        return when (numDaysPerWeek) {
            2 -> TWO_DAY_TEMPLATE
            3 -> THREE_DAY_TEMPLATE
            4 -> FOUR_DAY_TEMPLATE
            else -> throw IllegalArgumentException("Number of days per week must be 2, 3, or 4")
        }
    }

    /**
     * Determines weak muscles based on user's 1RM data and exercise history.
     */
    private fun determineWeakMuscles(
        oneRepMaxes: List<UserOneRepMax>,
        rotationHistory: List<ExerciseRotationHistory>
    ): List<String> {
        // For now, return default weak muscles
        // In a real implementation, this would analyze 1RM data and exercise history
        // to identify areas that need more attention
        return DEFAULT_WEAK_MUSCLES
    }

    /**
     * Selects a rotating exercise based on various criteria.
     */
    private fun selectRotatingExercise(
        userId: Int,
        targetMuscles: List<String>,
        userEquipment: List<UserEquipment>,
        preferences: List<UserExercisePreference>,
        exercises: List<Exercise>,
        isAccessory: Boolean,
        rotationHistory: List<ExerciseRotationHistory>
    ): Exercise? {
        // Filter exercises based on preferences (exercises are already filtered by is_accessory)
        val availableExercises = exercises.filter { exercise ->
            !preferences.any { pref -> pref.exerciseName == exercise.name && pref.shouldAvoid }
        }

        if (availableExercises.isEmpty()) {
            logger.warn("No available exercises found for isAccessory: {}", isAccessory)
            return null
        }

        // Filter by equipment availability
        val equipmentFilteredExercises = availableExercises.filter { exercise ->
            // Check if user has any equipment for this exercise
            userEquipment.any { userEq ->
                // This would need to be implemented with actual equipment checking
                true // For now, assume all equipment is available
            }
        }

        if (equipmentFilteredExercises.isEmpty()) {
            logger.warn("No exercises available with user's equipment for isAccessory: {}", isAccessory)
            return availableExercises.firstOrNull() // Fallback to any available exercise
        }

        // Get exercise rotation history for this category
        val categoryHistory = rotationHistory.filter { it.isAccessory == isAccessory }
        
        // Get all exercises that have been used in this category
        val usedExercises = categoryHistory.map { it.exerciseName }.toSet()
        
        // Get exercises that haven't been used yet in this category
        val unusedExercises = equipmentFilteredExercises.filter { exercise ->
            !usedExercises.contains(exercise.name)
        }

        // If we have unused exercises, use them first
        val exercisesToChooseFrom = if (unusedExercises.isNotEmpty()) {
            unusedExercises
        } else {
            // If all exercises have been used, find the least recently used one
            val exerciseUsageCount = equipmentFilteredExercises.associateWith { exercise ->
                categoryHistory.count { it.exerciseName == exercise.name }
            }
            
            val minUsageCount = exerciseUsageCount.values.minOrNull() ?: 0
            equipmentFilteredExercises.filter { exercise ->
                exerciseUsageCount[exercise] == minUsageCount
            }
        }

        // Sort by number of equipment options (desc), targeted muscles (desc), exercise name
        val sortedExercises = exercisesToChooseFrom.sortedWith(
            compareByDescending<Exercise> { exercise ->
                // Count equipment options (would need actual implementation)
                1
            }.thenByDescending { exercise ->
                // Count targeted muscles (would need actual implementation)
                targetMuscles.size
            }.thenBy { exercise ->
                exercise.name
            }
        )

        return sortedExercises.firstOrNull()
    }

    /**
     * Generates a Prilepin-based set scheme for an exercise with undulating periodization.
     */
    private fun generatePrilepinBasedScheme(
        userId: Int,
        exercise: Exercise,
        movementRole: String,
        dayType: String,
        oneRepMaxes: List<UserOneRepMax>,
        currentWeekNumber: Int
    ): List<SetScheme> {
        val (guidelines, intensity) = getUndulatingPeriodizationGuidelines(
            dayType = dayType,
            movementRole = movementRole,
            currentWeekNumber = currentWeekNumber,
            exercise = exercise
        )

        val repsPerSet = guidelines.repsPerSetRange.random()
        val numSets = (guidelines.totalReps / repsPerSet).toInt()
        val restSeconds = guidelines.restSeconds.random()

        // Determine target weight
        val targetWeight = getTargetWeight(userId, exercise.name, intensity, oneRepMaxes)

        // Tempo: vary if accessory, else default
        val useTempo = movementRole != "primary" && Random.nextBoolean()
        val eccentric = if (useTempo) Random.nextInt(1, 4).toString() else "0"
        val isometric = if (useTempo) Random.nextInt(0, 3).toString() else "0"
        val concentric = if (useTempo) if (Random.nextBoolean()) "1" else "X" else "0"

        return (1..numSets).map { setNumber ->
            SetScheme(
                id = 0,
                programmedExerciseId = 0, // Will be set when creating the set schemes
                setNumber = setNumber,
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = useTempo,
                eccentricTempo = eccentric,
                isometricTempo = isometric,
                concentricTempo = concentric,
                targetWeight = targetWeight,
                performedWeight = null,
                targetRepCount = repsPerSet,
                performedRepCount = null,
                restSeconds = restSeconds
            )
        }
    }

    /**
     * Gets undulating periodization guidelines based on week number and movement type.
     * 
     * Undulating periodization works on a 4-week cycle:
     * - Week 1-2: Build intensity
     * - Week 3: Peak intensity
     * - Week 4: Deload
     */
    private fun getUndulatingPeriodizationGuidelines(
        dayType: String,
        movementRole: String,
        currentWeekNumber: Int,
        exercise: Exercise
    ): Pair<PrilepinGuidelines, Double> {
        val weekInCycle = ((currentWeekNumber - 1) % 4) + 1
        val isUpperBody = dayType.contains("Upper")
        val isLowerBody = dayType.contains("Lower")

        return when {
            // Max Effort movements
            dayType.contains("ME") -> getMaxEffortGuidelines(weekInCycle, isUpperBody)
            
            // Dynamic Effort movements
            dayType.contains("DE") -> getDynamicEffortGuidelines(weekInCycle, isUpperBody, isLowerBody)
            
            // Accessory movements
            else -> getAccessoryGuidelines(weekInCycle)
        }
    }

    /**
     * Gets Max Effort guidelines based on week in cycle.
     * 
     * Max Effort undulating periodization:
     * - Week 1-2: 80-90% intensity
     * - Week 3: 90-100% intensity (95% max for upper body, 100% for lower body)
     * - Week 4: Deload (55-65% intensity)
     */
    private fun getMaxEffortGuidelines(weekInCycle: Int, isUpperBody: Boolean): Pair<PrilepinGuidelines, Double> {
        return when (weekInCycle) {
            1, 2 -> {
                val guidelines = PRILEPIN_GUIDELINES["0.8-0.9"]!!
                val intensity = guidelines.intensityRange.random()
                Pair(guidelines, intensity)
            }
            3 -> {
                val guidelines = PRILEPIN_GUIDELINES["0.9-1.0"]!!
                val maxIntensity = if (isUpperBody) 0.95 else 1.0
                val intensity = (0.9..maxIntensity).random()
                Pair(guidelines, intensity)
            }
            4 -> {
                val guidelines = PRILEPIN_GUIDELINES["0.55-0.65"]!!
                val intensity = guidelines.intensityRange.random()
                Pair(guidelines, intensity)
            }
            else -> {
                val guidelines = PRILEPIN_GUIDELINES["0.8-0.9"]!!
                val intensity = guidelines.intensityRange.random()
                Pair(guidelines, intensity)
            }
        }
    }

    /**
     * Gets Dynamic Effort guidelines based on week in cycle.
     * 
     * Dynamic Effort undulating periodization:
     * Lower Body:
     * - Week 1: 12 sets of 2 reps or 5 sets of 5 reps, 75% intensity
     * - Week 2: 10 sets of 2 reps or 5 sets of 5 reps, 80% intensity
     * - Week 3: 8 sets of 2 reps or 5 sets of 5 reps, 85% intensity
     * - Week 4: 12 sets of 2 reps or 5 sets of 5 reps, 50% intensity (deload)
     * 
     * Upper Body:
     * - Week 1: 9 sets of 3 reps, 50% intensity + bands
     * - Week 2: 9 sets of 3 reps, 55% intensity + bands
     * - Week 3: 9 sets of 3 reps, 60% intensity + bands
     * - Week 4: 9 sets of 3 reps, 50% intensity, no bands (deload)
     */
    private fun getDynamicEffortGuidelines(weekInCycle: Int, isUpperBody: Boolean, isLowerBody: Boolean): Pair<PrilepinGuidelines, Double> {
        return when {
            isLowerBody -> getLowerBodyDynamicEffortGuidelines(weekInCycle)
            isUpperBody -> getUpperBodyDynamicEffortGuidelines(weekInCycle)
            else -> {
                // Default to lower body guidelines
                getLowerBodyDynamicEffortGuidelines(weekInCycle)
            }
        }
    }

    /**
     * Gets Lower Body Dynamic Effort guidelines.
     */
    private fun getLowerBodyDynamicEffortGuidelines(weekInCycle: Int): Pair<PrilepinGuidelines, Double> {
        return when (weekInCycle) {
            1 -> {
                // 12 sets of 2 reps or 5 sets of 5 reps, 75% intensity
                val useHighSets = Random.nextBoolean()
                val totalReps = if (useHighSets) 24 else 25 // 12*2 or 5*5
                val repsPerSet = if (useHighSets) 2 else 5
                val guidelines = PrilepinGuidelines(
                    intensityRange = 0.75..0.75,
                    repsPerSetRange = repsPerSet..repsPerSet,
                    totalReps = totalReps,
                    restSeconds = 60..90
                )
                Pair(guidelines, 0.75)
            }
            2 -> {
                // 10 sets of 2 reps or 5 sets of 5 reps, 80% intensity
                val useHighSets = Random.nextBoolean()
                val totalReps = if (useHighSets) 20 else 25 // 10*2 or 5*5
                val repsPerSet = if (useHighSets) 2 else 5
                val guidelines = PrilepinGuidelines(
                    intensityRange = 0.8..0.8,
                    repsPerSetRange = repsPerSet..repsPerSet,
                    totalReps = totalReps,
                    restSeconds = 60..90
                )
                Pair(guidelines, 0.8)
            }
            3 -> {
                // 8 sets of 2 reps or 5 sets of 5 reps, 85% intensity
                val useHighSets = Random.nextBoolean()
                val totalReps = if (useHighSets) 16 else 25 // 8*2 or 5*5
                val repsPerSet = if (useHighSets) 2 else 5
                val guidelines = PrilepinGuidelines(
                    intensityRange = 0.85..0.85,
                    repsPerSetRange = repsPerSet..repsPerSet,
                    totalReps = totalReps,
                    restSeconds = 60..90
                )
                Pair(guidelines, 0.85)
            }
            4 -> {
                // 12 sets of 2 reps or 5 sets of 5 reps, 50% intensity (deload)
                val useHighSets = Random.nextBoolean()
                val totalReps = if (useHighSets) 24 else 25 // 12*2 or 5*5
                val repsPerSet = if (useHighSets) 2 else 5
                val guidelines = PrilepinGuidelines(
                    intensityRange = 0.5..0.5,
                    repsPerSetRange = repsPerSet..repsPerSet,
                    totalReps = totalReps,
                    restSeconds = 60..90
                )
                Pair(guidelines, 0.5)
            }
            else -> {
                val guidelines = PRILEPIN_GUIDELINES["0.7-0.8"]!!
                val intensity = guidelines.intensityRange.random()
                Pair(guidelines, intensity)
            }
        }
    }

    /**
     * Gets Upper Body Dynamic Effort guidelines.
     */
    private fun getUpperBodyDynamicEffortGuidelines(weekInCycle: Int): Pair<PrilepinGuidelines, Double> {
        return when (weekInCycle) {
            1 -> {
                // 9 sets of 3 reps, 50% intensity + bands
                val guidelines = PrilepinGuidelines(
                    intensityRange = 0.5..0.5,
                    repsPerSetRange = 3..3,
                    totalReps = 27, // 9*3
                    restSeconds = 60..90
                )
                Pair(guidelines, 0.5)
            }
            2 -> {
                // 9 sets of 3 reps, 55% intensity + bands
                val guidelines = PrilepinGuidelines(
                    intensityRange = 0.55..0.55,
                    repsPerSetRange = 3..3,
                    totalReps = 27, // 9*3
                    restSeconds = 60..90
                )
                Pair(guidelines, 0.55)
            }
            3 -> {
                // 9 sets of 3 reps, 60% intensity + bands
                val guidelines = PrilepinGuidelines(
                    intensityRange = 0.6..0.6,
                    repsPerSetRange = 3..3,
                    totalReps = 27, // 9*3
                    restSeconds = 60..90
                )
                Pair(guidelines, 0.6)
            }
            4 -> {
                // 9 sets of 3 reps, 50% intensity, no bands (deload)
                val guidelines = PrilepinGuidelines(
                    intensityRange = 0.5..0.5,
                    repsPerSetRange = 3..3,
                    totalReps = 27, // 9*3
                    restSeconds = 60..90
                )
                Pair(guidelines, 0.5)
            }
            else -> {
                val guidelines = PRILEPIN_GUIDELINES["0.7-0.8"]!!
                val intensity = guidelines.intensityRange.random()
                Pair(guidelines, intensity)
            }
        }
    }

    /**
     * Gets Accessory guidelines based on week in cycle.
     * 
     * Accessory undulating periodization:
     * - Week 1: 55-65% intensity
     * - Week 2-3: 70-80% intensity
     * - Week 4: 55-65% intensity (deload)
     */
    private fun getAccessoryGuidelines(weekInCycle: Int): Pair<PrilepinGuidelines, Double> {
        return when (weekInCycle) {
            1, 4 -> {
                val guidelines = PRILEPIN_GUIDELINES["0.55-0.65"]!!
                val intensity = guidelines.intensityRange.random()
                Pair(guidelines, intensity)
            }
            2, 3 -> {
                val guidelines = PRILEPIN_GUIDELINES["0.7-0.8"]!!
                val intensity = guidelines.intensityRange.random()
                Pair(guidelines, intensity)
            }
            else -> {
                val guidelines = PRILEPIN_GUIDELINES["0.7-0.8"]!!
                val intensity = guidelines.intensityRange.random()
                Pair(guidelines, intensity)
            }
        }
    }

    /**
     * Generates a set scheme for secondary exercises with specific guidelines.
     * 
     * Secondary exercises use:
     * - 80-90% intensity (0.8-0.9)
     * - 3-4 sets
     * - 5-8 reps per set
     * - 180-300 second rest periods
     */
    private fun generateSecondaryExerciseScheme(
        userId: Int,
        exercise: Exercise,
        oneRepMaxes: List<UserOneRepMax>
    ): List<SetScheme> {
        // Secondary exercise guidelines: 80-90% intensity, 3-4 sets of 5-8 reps
        val intensity = (0.8..0.9).random()
        val repsPerSet = (5..8).random()
        val numSets = (3..4).random()
        val restSeconds = (180..300).random()

        // Determine target weight
        val targetWeight = getTargetWeight(userId, exercise.name, intensity, oneRepMaxes)

        // Tempo: vary for secondary exercises
        val useTempo = Random.nextBoolean()
        val eccentric = if (useTempo) Random.nextInt(1, 4).toString() else "0"
        val isometric = if (useTempo) Random.nextInt(0, 3).toString() else "0"
        val concentric = if (useTempo) if (Random.nextBoolean()) "1" else "X" else "0"

        return (1..numSets).map { setNumber ->
            SetScheme(
                id = 0,
                programmedExerciseId = 0, // Will be set when creating the set schemes
                setNumber = setNumber,
                wasSetPerformed = false,
                isAmrap = false,
                isEmom = false,
                useTempo = useTempo,
                eccentricTempo = eccentric,
                isometricTempo = isometric,
                concentricTempo = concentric,
                targetWeight = targetWeight,
                performedWeight = null,
                targetRepCount = repsPerSet,
                performedRepCount = null,
                restSeconds = restSeconds
            )
        }
    }

    /**
     * Generates an AMRAP or EMOM set scheme for conditioning exercises.
     */
    private fun generateAmrapOrEmomScheme(
        userId: Int,
        exercise: Exercise,
        oneRepMaxes: List<UserOneRepMax>
    ): List<SetScheme> {
        val isAmrap = Random.nextBoolean()
        val targetWeight = getTargetWeight(userId, exercise.name, 0.5, oneRepMaxes)
        
        val useTempo = Random.nextBoolean()
        val eccentric = if (useTempo) Random.nextInt(2, 4).toString() else "0"
        val isometric = if (useTempo) Random.nextInt(1, 3).toString() else "0"
        val concentric = if (useTempo) if (Random.nextBoolean()) "1" else "X" else "0"

        return listOf(
            SetScheme(
                id = 0,
                programmedExerciseId = 0, // Will be set when creating the set schemes
                setNumber = 1,
                wasSetPerformed = false,
                isAmrap = isAmrap,
                isEmom = !isAmrap,
                useTempo = useTempo,
                eccentricTempo = eccentric,
                isometricTempo = isometric,
                concentricTempo = concentric,
                targetWeight = targetWeight,
                performedWeight = null,
                targetRepCount = null, // Varies per person for AMRAP/EMOM
                performedRepCount = null,
                restSeconds = if (isAmrap) 0 else 60
            )
        )
    }

    /**
     * Gets the target weight for an exercise based on user's 1RM.
     */
    private fun getTargetWeight(
        userId: Int,
        exerciseName: String,
        intensity: Double,
        oneRepMaxes: List<UserOneRepMax>
    ): BigDecimal {
        val oneRepMax = oneRepMaxes.find { it.exerciseName == exerciseName }?.oneRepMax
        return if (oneRepMax != null) {
            (oneRepMax * BigDecimal(intensity)).setScale(2, RoundingMode.HALF_UP)
        } else {
            // Default weight for new users (would need to be configured)
            BigDecimal("50.0")
        }
    }

    /**
     * Creates a workout stage.
     */
    private fun createWorkoutStage(workoutId: Long, stageType: String, position: Int): Mono<WorkoutStage> {
        val stageTypeId = when (stageType) {
            "primary" -> 1
            "secondary" -> 2
            "accessory" -> 3
            "conditioning" -> 4
            else -> 1
        }

        val stage = WorkoutStage(
            id = 0,
            programmedWorkoutId = workoutId,
            stageTypeId = stageTypeId,
            position = position
        )

        return workoutStageDAL.insertWorkoutStage(stage)
    }

    /**
     * Creates a programmed exercise.
     */
    private fun createProgrammedExercise(workoutStageId: Long, exerciseName: String): Mono<ProgrammedExercise> {
        val programmedExercise = ProgrammedExercise(
            id = 0,
            workoutStageId = workoutStageId,
            exerciseName = exerciseName,
            notes = null
        )

        return programmedExerciseDAL.insertProgrammedExercise(programmedExercise)
    }

    /**
     * Creates set schemes for a programmed exercise.
     */
    private fun createSetSchemes(programmedExerciseId: Long, setSchemes: List<SetScheme>): Mono<Void> {
        val setSchemesWithId = setSchemes.map { setScheme ->
            setScheme.copy(programmedExerciseId = programmedExerciseId)
        }

        return setSchemesWithId.fold(Mono.empty<Void>()) { mono, setScheme ->
            mono.flatMap { setSchemeDAL.insertSetScheme(setScheme).then() }
        }
    }

    /**
     * Calculates the number of accessory exercises based on session time and workout type.
     * 
     * Time allocation:
     * - Primary movement: 10 minutes
     * - Secondary movement: 8 minutes (if applicable)
     * - Each accessory exercise: 5 minutes
     * - Conditioning: 10 minutes (for DE days)
     * 
     * @param sessionTimeMinutes The desired session time in minutes
     * @param dayType The type of workout day (ME_Upper, DE_Lower, etc.)
     * @return The number of accessory exercises to include
     */
    private fun calculateNumAccessoryExercises(sessionTimeMinutes: Int, dayType: String): Int {
        // Base time allocation
        var timeAllocated = 10 // Primary movement (10 minutes)
        
        // Add secondary movement time if applicable
        if (dayType in listOf("ME_Upper", "DE_Upper")) {
            timeAllocated += 8 // Secondary movement (8 minutes)
        }
        
        // Add conditioning time for DE days
        if (dayType.contains("DE")) {
            timeAllocated += 10 // Conditioning (10 minutes)
        }
        
        // Calculate remaining time for accessories
        val remainingTime = sessionTimeMinutes - timeAllocated
        
        // Each accessory exercise takes 5 minutes
        val numAccessories = (remainingTime / 5).coerceAtLeast(0)
        
        logger.info("Session time: {} minutes, allocated: {} minutes, remaining: {} minutes, accessories: {}", 
            sessionTimeMinutes, timeAllocated, remainingTime, numAccessories)
        
        return numAccessories
    }

    /**
     * Data class for day templates.
     */
    private data class DayTemplate(
        val type: String
    )

    /**
     * Data class for Prilepin guidelines.
     */
    private data class PrilepinGuidelines(
        val intensityRange: ClosedFloatingPointRange<Double>,
        val repsPerSetRange: IntRange,
        val totalReps: Int,
        val restSeconds: IntRange
    )
} 