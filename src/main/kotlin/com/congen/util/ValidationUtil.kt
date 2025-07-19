package com.congen.util

import com.congen.exceptions.ValidationException
import com.congen.model.WeightUnit
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.math.BigDecimal

/**
 * Utility class for validating data across the Congen application.
 *
 * This object provides comprehensive validation methods for all data types
 * used in the workout generation system. Each validation method follows
 * a consistent pattern of checking constraints and throwing [ValidationException]
 * with descriptive error messages when validation fails.
 *
 * ## Validation Categories
 *
 * ### User Validations
 * - [validateUserAge] - Validates user age (1-150 years)
 * - [validateUserHeight] - Validates user height (0.01-300 cm)
 * - [validateUserWeight] - Validates user weight (0.01-1000 kg)
 * - [validateUserWeightWithUnit] - Validates user weight with unit conversion (0.01-1000 kg equivalent)
 *
 * ### Program Preferences Validations
 * - [validateProgramDaysPerWeek] - Validates program days per week (2, 3, or 4)
 * - [validateSessionTimeLength] - Validates session duration (15-300 minutes)
 *
 * ### Workout Validations
 * - [validateDayNumber] - Validates day number in program (1-365)
 * - [validatePosition] - Validates exercise position (> 0)
 *
 * ### Set Scheme Validations
 * - [validateSetNumber] - Validates set number (> 0)
 * - [validateTempo] - Validates tempo format (single digit 0-9)
 * - [validateTargetWeight] - Validates target weight (> 0)
 * - [validatePerformedWeight] - Validates performed weight (> 0)
 * - [validateTargetWeightWithUnit] - Validates target weight with unit conversion (> 0 kg equivalent)
 * - [validatePerformedWeightWithUnit] - Validates performed weight with unit conversion (> 0 kg equivalent)
 * - [validateTargetRepCount] - Validates target reps (1-1000)
 * - [validatePerformedRepCount] - Validates performed reps (1-1000)
 * - [validateRestSeconds] - Validates rest time (0-3600 seconds)
 *
 * ### One Rep Max Validations
 * - [validateOneRepMax] - Validates one rep max (0.01-1000 kg)
 * - [validateOneRepMaxWithUnit] - Validates one rep max with unit conversion (0.01-1000 kg equivalent)
 *
 * ## Usage
 *
 * ```kotlin
 * // Validate user data
 * ValidationUtil.validateUserAge(25)
 * ValidationUtil.validateUserHeight(BigDecimal("175.5"))
 * ValidationUtil.validateUserWeight(BigDecimal("80.0"))
 *
 * // Validate with unit conversion
 * val weightInKg = ValidationUtil.validateUserWeightWithUnit(BigDecimal("176.0"), WeightUnit.LBS, unitConverter)
 *
 * // Validate program preferences
 * ValidationUtil.validateProgramDaysPerWeek(3)
 * ValidationUtil.validateSessionTimeLength(60)
 * ```
 *
 * ## Error Handling
 *
 * All validation methods throw [ValidationException] with descriptive
 * error messages when validation fails. These exceptions are caught by
 * the global exception handler and returned as HTTP 422 responses.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Component
object ValidationUtil {
    private val logger = LoggerFactory.getLogger(ValidationUtil::class.java)

    /**
     * Validates user age to ensure it's within acceptable bounds.
     *
     * Age must be between 1 and 150 years. This range covers typical
     * human lifespans while allowing for edge cases and data entry errors.
     *
     * @param age The age to validate in years
     * @throws ValidationException if age is not within valid range (1-150)
     *
     * @example
     * ```kotlin
     * ValidationUtil.validateUserAge(25)  // Valid
     * ValidationUtil.validateUserAge(0)   // Throws ValidationException
     * ValidationUtil.validateUserAge(151) // Throws ValidationException
     * ```
     */
    fun validateUserAge(age: Int) {
        if (age <= 0 || age > 150) {
            val message = "User age must be between 1 and 150, got: $age"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates user height to ensure it matches DB constraints.
     * Height must be > 0 and <= 300 (cm).
     * @param height Height in centimeters
     * @throws ValidationException if height is not in (0, 300]
     */
    fun validateUserHeight(height: BigDecimal) {
        if (height <= BigDecimal.ZERO || height > BigDecimal("300")) {
            val message = "User height must be between 0.01 and 300 cm, got: $height"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates user weight to ensure it matches DB constraints.
     * Weight must be > 0 and <= 1000 (kg).
     * @param weight Weight in kilograms
     * @throws ValidationException if weight is not in (0, 1000]
     */
    fun validateUserWeight(weight: BigDecimal) {
        if (weight <= BigDecimal.ZERO || weight > BigDecimal("1000")) {
            val message = "User weight must be between 0.01 and 1000 kg, got: $weight"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates one rep max value for user_one_rep_max (DB: > 0 and <= 1000 kg).
     * @param oneRepMax One rep max value in kilograms
     * @throws ValidationException if not in (0, 1000]
     */
    fun validateOneRepMax(oneRepMax: BigDecimal) {
        if (oneRepMax <= BigDecimal.ZERO || oneRepMax > BigDecimal("1000")) {
            val message = "One rep max must be between 0.01 and 1000 kg, got: $oneRepMax"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates program days per week to ensure it matches conjugate method requirements.
     *
     * The conjugate method is designed to work with specific training frequencies.
     * Only 2, 3, or 4 days per week are supported as these provide optimal
     * training stimulus while allowing adequate recovery.
     *
     * @param daysPerWeek The number of training days per week
     * @throws ValidationException if days per week is not 2, 3, or 4
     *
     * @example
     * ```kotlin
     * ValidationUtil.validateProgramDaysPerWeek(3)  // Valid
     * ValidationUtil.validateProgramDaysPerWeek(1)  // Throws ValidationException
     * ValidationUtil.validateProgramDaysPerWeek(5)  // Throws ValidationException
     * ```
     */
    fun validateProgramDaysPerWeek(daysPerWeek: Int) {
        if (daysPerWeek !in listOf(2, 3, 4)) {
            val message = "Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: $daysPerWeek"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates session time length in minutes (DB: 15-300).
     * @param minutes Session length in minutes
     * @throws ValidationException if not in [15, 300]
     */
    fun validateSessionTimeLength(minutes: Int) {
        if (minutes < 15 || minutes > 300) {
            val message = "Session time length must be between 15 and 300 minutes, got: $minutes"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates day number for programmed workouts (DB: 1-365).
     * @param dayNumber Day number in program
     * @throws ValidationException if not in [1, 365]
     */
    fun validateDayNumber(dayNumber: Int) {
        if (dayNumber <= 0 || dayNumber > 365) {
            val message = "Day number must be between 1 and 365, got: $dayNumber"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates position for workout stages (DB: > 0).
     * @param position Position in stage
     * @throws ValidationException if not > 0
     */
    fun validatePosition(position: Int) {
        if (position <= 0) {
            val message = "Position must be greater than 0, got: $position"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates set number for set schemes (DB: > 0).
     * @param setNumber Set number
     * @throws ValidationException if not > 0
     */
    fun validateSetNumber(setNumber: Int) {
        if (setNumber <= 0) {
            val message = "Set number must be greater than 0, got: $setNumber"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates tempo value for set schemes (DB: single digit 0-9 or X/x for explosive).
     * @param tempo Tempo value as string
     * @param fieldName Name of the tempo field
     * @throws ValidationException if not a single digit 0-9 or X/x
     */
    fun validateTempo(
        tempo: String?,
        fieldName: String,
    ) {
        if (tempo != null && !tempo.matches(Regex("[0-9Xx]"))) {
            val message = "$fieldName tempo must be a single digit (0-9) or X/x for explosive, got: $tempo"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates target weight for set schemes (DB: > 0).
     * @param weight Target weight
     * @throws ValidationException if not > 0
     */
    fun validateTargetWeight(weight: BigDecimal?) {
        if (weight != null && weight <= BigDecimal.ZERO) {
            val message = "Target weight must be greater than 0, got: $weight"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates performed weight for set schemes (DB: > 0).
     * @param weight Performed weight
     * @throws ValidationException if not > 0
     */
    fun validatePerformedWeight(weight: BigDecimal?) {
        if (weight != null && weight <= BigDecimal.ZERO) {
            val message = "Performed weight must be greater than 0, got: $weight"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates target rep count for set schemes (DB: 1-1000).
     * @param repCount Target rep count
     * @throws ValidationException if not in [1, 1000]
     */
    fun validateTargetRepCount(repCount: Int?) {
        if (repCount != null && (repCount <= 0 || repCount > 1000)) {
            val message = "Target rep count must be between 1 and 1000, got: $repCount"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates performed rep count for set schemes (DB: 1-1000).
     * @param repCount Performed rep count
     * @throws ValidationException if not in [1, 1000]
     */
    fun validatePerformedRepCount(repCount: Int?) {
        if (repCount != null && (repCount <= 0 || repCount > 1000)) {
            val message = "Performed rep count must be between 1 and 1000, got: $repCount"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates rest seconds for set schemes (DB: 0-3600).
     * @param restSeconds Rest seconds
     * @throws ValidationException if not in [0, 3600]
     */
    fun validateRestSeconds(restSeconds: Int?) {
        if (restSeconds != null && (restSeconds < 0 || restSeconds > 3600)) {
            val message = "Rest seconds must be between 0 and 3600, got: $restSeconds"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates that program days per week can be changed for a user.
     *
     * This method checks if a user is trying to change program days per week
     * and prevents it, as this would cause day numbering conflicts and disrupt
     * the conjugate program structure.
     *
     * @param userId The ID of the user
     * @param newProgramDaysPerWeek The new number of days per week being set
     * @param currentProgramDaysPerWeek The current number of days per week
     * @throws ValidationException if the user is trying to change program days per week
     */
    fun validateProgramDaysPerWeekChange(
        userId: Int,
        newProgramDaysPerWeek: Int,
        currentProgramDaysPerWeek: Int
    ) {
        // If user is trying to change program days per week
        if (newProgramDaysPerWeek != currentProgramDaysPerWeek) {
            val message =
                "Cannot change program days per week from $currentProgramDaysPerWeek to $newProgramDaysPerWeek for user $userId " +
                    "because they have existing workouts. " +
                    "Program days per week becomes immutable once workouts are generated to prevent day numbering conflicts " +
                    "and maintain program consistency. " +
                    "To change program frequency, the user must start a new program."
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates exercise category for exercise rotation history.
     *
     * Exercise categories must be one of the predefined valid categories
     * used in the workout generation system. This ensures consistency
     * in exercise classification across the application.
     *
     * @param category The exercise category to validate
     * @throws ValidationException if category is not a valid exercise category
     *
     * @example
     * ```kotlin
     * ValidationUtil.validateExerciseCategory("primary")   // Valid
     * ValidationUtil.validateExerciseCategory("secondary") // Valid
     * ValidationUtil.validateExerciseCategory("accessory") // Valid
     * ValidationUtil.validateExerciseCategory("invalid")   // Throws ValidationException
     * ```
     */
    fun validateExerciseCategory(category: String) {
        val validCategories = listOf("primary", "secondary", "accessory")
        if (category !in validCategories) {
            val message = "Exercise category must be one of: ${validCategories.joinToString(", ")}, got: $category"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    /**
     * Validates user weight with unit conversion to ensure it matches DB constraints.
     * Weight must be > 0 and <= 1000 kg equivalent after conversion.
     *
     * @param weight Weight value in the specified unit
     * @param unit The unit of the weight value
     * @param unitConverter Utility for unit conversions
     * @return The weight converted to kg if validation passes
     * @throws ValidationException if weight is not in valid range after conversion to kg
     */
    fun validateUserWeightWithUnit(
        weight: BigDecimal,
        unit: WeightUnit,
        unitConverter: UnitConverter
    ): BigDecimal {
        val weightInKg = unitConverter.toKg(weight, unit)
        validateUserWeight(weightInKg)
        return weightInKg
    }

    /**
     * Validates one rep max value with unit conversion for user_one_rep_max (DB: > 0 and <= 1000 kg equivalent).
     *
     * @param oneRepMax One rep max value in the specified unit
     * @param unit The unit of the one rep max value
     * @param unitConverter Utility for unit conversions
     * @return The one rep max converted to kg if validation passes
     * @throws ValidationException if not in valid range after conversion to kg
     */
    fun validateOneRepMaxWithUnit(
        oneRepMax: BigDecimal,
        unit: WeightUnit,
        unitConverter: UnitConverter
    ): BigDecimal {
        val oneRepMaxInKg = unitConverter.toKg(oneRepMax, unit)
        validateOneRepMax(oneRepMaxInKg)
        return oneRepMaxInKg
    }

    /**
     * Validates target weight with unit conversion for set schemes (DB: > 0 kg equivalent).
     *
     * @param weight Target weight value in the specified unit, or null
     * @param unit The unit of the weight value
     * @param unitConverter Utility for unit conversions
     * @return The weight converted to kg if validation passes, or null if input was null
     * @throws ValidationException if not > 0 after conversion to kg
     */
    fun validateTargetWeightWithUnit(
        weight: BigDecimal?,
        unit: WeightUnit,
        unitConverter: UnitConverter
    ): BigDecimal? {
        if (weight == null) return null
        val weightInKg = unitConverter.toKg(weight, unit)
        validateTargetWeight(weightInKg)
        return weightInKg
    }

    /**
     * Validates performed weight with unit conversion for set schemes (DB: > 0 kg equivalent).
     *
     * @param weight Performed weight value in the specified unit, or null
     * @param unit The unit of the weight value
     * @param unitConverter Utility for unit conversions
     * @return The weight converted to kg if validation passes, or null if input was null
     * @throws ValidationException if not > 0 after conversion to kg
     */
    fun validatePerformedWeightWithUnit(
        weight: BigDecimal?,
        unit: WeightUnit,
        unitConverter: UnitConverter
    ): BigDecimal? {
        if (weight == null) return null
        val weightInKg = unitConverter.toKg(weight, unit)
        validatePerformedWeight(weightInKg)
        return weightInKg
    }
}
