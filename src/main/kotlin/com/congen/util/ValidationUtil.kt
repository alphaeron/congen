package com.congen.util

import com.congen.exceptions.ValidationException
import org.slf4j.LoggerFactory
import java.math.BigDecimal

object ValidationUtil {
    private val logger = LoggerFactory.getLogger(ValidationUtil::class.java)

    // User validations
    fun validateUserAge(age: Int) {
        if (age <= 0 || age > 150) {
            val message = "User age must be between 1 and 150, got: $age"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    fun validateUserHeight(height: BigDecimal) {
        if (height <= BigDecimal.ZERO || height > BigDecimal("300")) {
            val message = "User height must be between 0.01 and 300 cm, got: $height"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    fun validateUserWeight(weight: BigDecimal) {
        if (weight <= BigDecimal.ZERO || weight > BigDecimal("1000")) {
            val message = "User weight must be between 0.01 and 1000 kg, got: $weight"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    // User Program Preferences validations
    fun validateProgramDaysPerWeek(daysPerWeek: Int) {
        if (daysPerWeek !in listOf(2, 3, 4)) {
            val message = "Program days per week must be 2, 3, or 4 days. Only valid program lengths are 2, 3, or 4 days, got: $daysPerWeek"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    fun validateSessionTimeLength(minutes: Int) {
        if (minutes < 15 || minutes > 300) {
            val message = "Session time length must be between 15 and 300 minutes, got: $minutes"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    // Programmed Workout validations
    fun validateDayNumber(dayNumber: Int) {
        if (dayNumber <= 0 || dayNumber > 365) {
            val message = "Day number must be between 1 and 365, got: $dayNumber"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    // Workout Stage validations
    fun validatePosition(position: Int) {
        if (position <= 0) {
            val message = "Position must be greater than 0, got: $position"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    // Set Scheme validations
    fun validateSetNumber(setNumber: Int) {
        if (setNumber <= 0) {
            val message = "Set number must be greater than 0, got: $setNumber"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    fun validateTempo(
        tempo: String?,
        fieldName: String,
    ) {
        if (tempo != null && !tempo.matches(Regex("[0-9]"))) {
            val message = "$fieldName tempo must be a single digit (0-9), got: $tempo"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    fun validateTargetWeight(weight: BigDecimal?) {
        if (weight != null && weight <= BigDecimal.ZERO) {
            val message = "Target weight must be greater than 0, got: $weight"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    fun validatePerformedWeight(weight: BigDecimal?) {
        if (weight != null && weight <= BigDecimal.ZERO) {
            val message = "Performed weight must be greater than 0, got: $weight"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    fun validateTargetRepCount(repCount: Int?) {
        if (repCount != null && (repCount <= 0 || repCount > 1000)) {
            val message = "Target rep count must be between 1 and 1000, got: $repCount"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    fun validatePerformedRepCount(repCount: Int?) {
        if (repCount != null && (repCount <= 0 || repCount > 1000)) {
            val message = "Performed rep count must be between 1 and 1000, got: $repCount"
            logger.error(message)
            throw ValidationException(message)
        }
    }

    fun validateRestSeconds(restSeconds: Int?) {
        if (restSeconds != null && (restSeconds < 0 || restSeconds > 3600)) {
            val message = "Rest seconds must be between 0 and 3600, got: $restSeconds"
            logger.error(message)
            throw ValidationException(message)
        }
    }
}
