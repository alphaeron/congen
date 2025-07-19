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
                DayTemplate("ME_Upper"),
                DayTemplate("DE_Lower")
            )

        private val THREE_DAY_TEMPLATE =
            listOf(
                DayTemplate("ME_Upper"),
                DayTemplate("DE_Lower"),
                DayTemplate("ME_Lower")
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
}
