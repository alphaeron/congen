package com.congen.generator

import com.congen.model.Band
import com.congen.model.WeightUnit
import com.congen.util.UnitConverter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.math.BigDecimal

/**
 * Service for computing band weights for Dynamic Effort exercises.
 *
 * This service handles the calculation of band weights and bar weights for DE exercises
 * based on undulating periodization guidelines. Bands are used to provide accommodated
 * resistance during the concentric phase of the movement.
 *
 * ## Band Weight Distribution by Week
 *
 * - **Week 1**: 25% band weight, 50% bar weight
 * - **Week 2**: 25% band weight, 55% bar weight
 * - **Week 3**: 25% band weight, 60% bar weight
 * - **Week 4**: 0% band weight, 50% bar weight (deload)
 *
 * ## Weight Computation Logic
 *
 * 1. Calculate total target weight based on 1RM and intensity
 * 2. Determine band weight percentage based on week in cycle
 * 3. Select appropriate band(s) to achieve target band weight
 * 4. Subtract total band weight from target weight to get bar weight
 * 5. Round bar weight to achievable plate weights
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class BandWeightService(
    private val unitConverter: UnitConverter,
) {
    /**
     * Computes band and bar weights for a Dynamic Effort exercise.
     *
     * @param totalTargetWeight The total target weight (bar + bands) in the specified unit
     * @param weightUnit The unit of the total target weight
     * @param weekInCycle The week in the 4-week cycle (1-4)
     * @return BandWeightResult containing band and bar weight information
     */
    fun computeBandAndBarWeights(
        totalTargetWeight: BigDecimal,
        weightUnit: WeightUnit,
        weekInCycle: Int
    ): BandWeightResult {
        val bandWeightPercentage = BAND_WEIGHT_PERCENTAGES[weekInCycle] ?: 0.0

        if (bandWeightPercentage <= 0.0) {
            // No bands for this week (deload week)
            return BandWeightResult(
                band = null,
                barWeight = totalTargetWeight,
            )
        }

        // Convert total weight to pounds for band calculations
        val totalWeightLbs =
            if (weightUnit == WeightUnit.KG) {
                unitConverter.fromKg(totalTargetWeight, WeightUnit.LBS)
            } else {
                totalTargetWeight
            }

        // Calculate target band weight
        val targetBandWeightLbs = totalWeightLbs * BigDecimal(bandWeightPercentage)

        // Select appropriate band(s)
        val selectedBand = selectBands(targetBandWeightLbs)

        // Calculate bar weight (total - band weight)
        val actualBandWeightLbs = selectedBand?.weightLbs?.multiply(BigDecimal(BANDS_PER_EXERCISE)) ?: BigDecimal.ZERO
        val barWeightLbs = totalWeightLbs - actualBandWeightLbs

        // Convert bar weight back to original unit and round to achievable weights
        val barWeight =
            if (weightUnit == WeightUnit.KG) {
                unitConverter.toKg(barWeightLbs, WeightUnit.LBS)
            } else {
                barWeightLbs
            }

        return BandWeightResult(
            band = selectedBand,
            barWeight = barWeight,
        )
    }

    /**
     * Selects the appropriate band to achieve the target band weight.
     *
     * @param targetBandWeightLbs The target band weight in pounds
     * @return Selected Band or null if no suitable band found
     */
    private fun selectBands(targetBandWeightLbs: BigDecimal): Band? {
        // Normalize the target weight to remove trailing zeros for comparison
        val normalizedTargetWeight = targetBandWeightLbs.stripTrailingZeros()

        // Find the allowed weight closest to the target
        val allowedWeights = Band.allowedWeights

        val selectedBandWeight =
            allowedWeights.minByOrNull { weight ->
                kotlin.math.abs(weight.toDouble() - normalizedTargetWeight.toDouble())
            }

        val result = selectedBandWeight?.let { Band.fromWeight(it) }

        return result
    }

    /**
     * Gets the band weight percentage for a given week.
     *
     * @param weekInCycle The week in the 4-week cycle (1-4)
     * @return The band weight percentage as a decimal (0.0 to 1.0)
     */
    fun getBandWeightPercentage(weekInCycle: Int): Double = BAND_WEIGHT_PERCENTAGES[weekInCycle] ?: 0.0

    companion object {
        private val logger = LoggerFactory.getLogger(BandWeightService::class.java)

        // Number of bands used per exercise
        private const val BANDS_PER_EXERCISE = 2

        // Band weight percentages by week (as percentage of total weight)
        private val BAND_WEIGHT_PERCENTAGES =
            mapOf(
                // Week 1: 25% band weight
                1 to 0.25,
                // Week 2: 25% band weight
                2 to 0.25,
                // Week 3: 25% band weight
                3 to 0.25,
                // Week 4: 0% band weight (deload)
                4 to 0.0
            )

        /**
         * Result of band weight computation for Dynamic Effort exercises.
         *
         * @property band The selected band, or null if no bands
         * @property barWeight The calculated bar weight in the original unit
         */
        data class BandWeightResult(
            val band: Band?,
            val barWeight: BigDecimal
        )
    }
}
