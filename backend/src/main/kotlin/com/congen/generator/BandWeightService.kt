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
 * Bar weight percentage comes from [PrilepinGuidelinesService] (same intensity used for total).
 * Caller passes that value as [barWeightPercentage].
 *
 * 1. Total target weight = 1RM × intensity (from Prilepin)
 * 2. Bar weight = total × barWeightPercentage (same as intensity for band weeks)
 * 3. Band weight = total × band weight percentage (25% weeks 1–3); select band pair to match
 * 4. Round bar weight to achievable plate weights
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
     * @param barWeightPercentage The bar weight as fraction of total (from Prilepin intensity for this week)
     * @return BandWeightResult containing band and bar weight information
     */
    fun computeBandAndBarWeights(
        totalTargetWeight: BigDecimal,
        weightUnit: WeightUnit,
        weekInCycle: Int,
        barWeightPercentage: Double
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

        val barWeightLbs = totalWeightLbs * BigDecimal(barWeightPercentage)
        val targetBandWeightLbs = totalWeightLbs * BigDecimal(bandWeightPercentage)
        val singleBandWeight = selectBands(targetBandWeightLbs).weightLbs
        val totalBandWeightLbs = singleBandWeight.multiply(BigDecimal(BANDS_PER_EXERCISE))

        val barWeight =
            if (weightUnit == WeightUnit.KG) {
                unitConverter.toKg(barWeightLbs, WeightUnit.LBS)
            } else {
                barWeightLbs
            }

        return BandWeightResult(
            band = Band(totalBandWeightLbs),
            barWeight = barWeight,
        )
    }

    /**
     * Selects the band such that the pair (2 bands) has total resistance closest to the target band weight, rounding up.
     * Always returns a band (smallest pair when target exceeds all pair totals).
     *
     * @param targetBandWeightLbs The target total band weight in pounds (25% of DE exercise weight)
     * @return The band to use (2 of these); never null
     */
    private fun selectBands(targetBandWeightLbs: BigDecimal): Band {
        val allowedWeights = Band.allowedWeights
        val pairTotals = allowedWeights.map { it to it.multiply(BigDecimal(BANDS_PER_EXERCISE)) }
        val roundedUp =
            pairTotals.filter { (_, pairTotal) -> pairTotal >= targetBandWeightLbs }
                .minByOrNull { (_, pairTotal) -> pairTotal }
        val selectedBandWeight =
            roundedUp?.first ?: pairTotals.minByOrNull { (_, pairTotal) -> pairTotal }?.first
                ?: Band.allowedWeights.minOrNull()!!

        return Band.fromWeight(selectedBandWeight) ?: Band(Band.allowedWeights.minOrNull()!!)
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
