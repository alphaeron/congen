package com.congen.generator

import org.springframework.stereotype.Component

/**
 * Service for managing conjugate workout templates.
 *
 * Provides templates for different program lengths and handles template selection
 * based on the number of training days per week.
 */
@Component
class ConjugateTemplates {
    companion object {
        // Conjugate workout templates - focus on workout types rather than specific movements
        private val TWO_DAY_TEMPLATE =
            listOf(
                // Day 1: ME Upper + DE Lower
                DayTemplate("ME_Upper_DE_Lower"),
                // Day 2: ME Lower + DE Upper
                DayTemplate("ME_Lower_DE_Upper")
            )

        private val THREE_DAY_TEMPLATE =
            listOf(
                // Day 1: ME Upper + DE Lower
                DayTemplate("ME_Upper_DE_Lower"),
                // Day 2: ME Lower + DE Upper
                DayTemplate("ME_Lower_DE_Upper"),
                // Day 3: Full Body Dynamic Effort
                DayTemplate("DE_Full_Body")
            )

        private val FOUR_DAY_TEMPLATE =
            listOf(
                DayTemplate("ME_Upper"),
                DayTemplate("DE_Lower"),
                DayTemplate("ME_Lower"),
                DayTemplate("DE_Upper")
            )
    }

    /**
     * Selects the appropriate template based on the number of days per week.
     *
     * @param numDaysPerWeek The number of training days per week (2, 3, or 4)
     * @return List of day templates for the week
     * @throws IllegalArgumentException if numDaysPerWeek is not 2, 3, or 4
     */
    fun selectTemplate(numDaysPerWeek: Int): List<DayTemplate> {
        return when (numDaysPerWeek) {
            2 -> TWO_DAY_TEMPLATE
            3 -> THREE_DAY_TEMPLATE
            4 -> FOUR_DAY_TEMPLATE
            else -> throw IllegalArgumentException("Number of days per week must be 2, 3, or 4")
        }
    }

    /**
     * Determines if a day template includes a secondary movement.
     *
     * @param dayType The type of workout day
     * @return true if the day includes a secondary movement, false otherwise
     */
    fun hasSecondaryMovement(dayType: String): Boolean {
        // 2 and 3 day programs don't have secondary movements
        // Only 4-day programs have secondary movements for ME_Upper and DE_Upper
        return dayType in listOf("ME_Upper", "DE_Upper")
    }

    /**
     * Determines if a day template includes conditioning.
     *
     * @param dayType The type of workout day
     * @return true if the day includes conditioning, false otherwise
     */
    fun hasConditioning(dayType: String): Boolean {
        return dayType.contains("DE")
    }

    /**
     * Determines if a day template is a combined ME+DE day.
     *
     * @param dayType The type of workout day
     * @return true if the day combines ME and DE movements, false otherwise
     */
    fun isCombinedMEDay(dayType: String): Boolean {
        return dayType in listOf("ME_Upper_DE_Lower", "ME_Lower_DE_Upper")
    }

    /**
     * Determines if a day template is a full body dynamic effort day.
     *
     * @param dayType The type of workout day
     * @return true if the day is a full body DE day, false otherwise
     */
    fun isFullBodyDE(dayType: String): Boolean {
        return dayType == "DE_Full_Body"
    }

    /**
     * Gets the primary movement type for a given day template.
     *
     * @param dayType The type of workout day
     * @return The primary movement type (ME_Upper, ME_Lower, DE_Upper, DE_Lower, or DE_Full_Body)
     */
    fun getPrimaryMovementType(dayType: String): String {
        return when (dayType) {
            "ME_Upper_DE_Lower" -> "ME_Upper"
            "ME_Lower_DE_Upper" -> "ME_Lower"
            "DE_Full_Body" -> "DE_Full_Body"
            else -> dayType // For 4-day programs, return as-is
        }
    }

    /**
     * Gets the secondary movement type for a given day template.
     *
     * @param dayType The type of workout day
     * @return The secondary movement type, or null if no secondary movement
     */
    fun getSecondaryMovementType(dayType: String): String? {
        return when (dayType) {
            "ME_Upper_DE_Lower" -> "DE_Lower"
            "ME_Lower_DE_Upper" -> "DE_Upper"
            else -> null // No secondary movement for other day types
        }
    }
}
