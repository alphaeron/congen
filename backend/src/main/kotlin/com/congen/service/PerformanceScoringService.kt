package com.congen.service

import com.congen.model.UserPerformanceMetrics
import com.congen.model.UserPerformanceScores
import com.congen.model.UserWeeklyTest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.min
import kotlin.math.round
import kotlin.math.tanh

/**
 * Service for calculating performance scores and gamified metrics.
 *
 * This service implements the core algorithms for converting raw performance
 * metrics into normalized scores, HP/MP/Fatigue values, and athleticism levels.
 * It uses non-linear scaling functions to provide realistic difficulty curves
 * and diminishing returns at elite performance levels.
 *
 * ## Scoring Functions
 *
 * Each metric is normalized to a 0-100 scale using appropriate scaling functions:
 * - **Logarithmic**: For metrics with exponential growth (strength, jump)
 * - **Linear**: For metrics with linear relationships (HR recovery)
 * - **Inverse Linear**: For metrics where lower is better (reaction time)
 *
 * ## HP/MP/Fatigue Calculation
 *
 * - **HP (Health Points)**: Physical resilience based on VO₂ max, muscular endurance, and HR recovery
 * - **MP (Magic Points)**: Neural readiness based on HR recovery, reaction time, and explosiveness
 * - **Fatigue**: Session-level depletion based on strain, sleep, and subjective tiredness
 *
 * ## Level Progression
 *
 * Uses tanh scaling to create diminishing returns at higher levels, making
 * progression from level 15 to 16 harder than level 3 to 4.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Service
class PerformanceScoringService {
    companion object {
        private val logger = LoggerFactory.getLogger(PerformanceScoringService::class.java)
    }

    /**
     * Calculates all performance scores from daily metrics and weekly test data.
     *
     * @param dailyMetrics Daily wearable and subjective metrics
     * @param weeklyTest Latest weekly test results (optional)
     * @return Complete performance scores with HP/MP/Fatigue and athleticism level
     */
    fun calculatePerformanceScores(dailyMetrics: UserPerformanceMetrics, weeklyTest: UserWeeklyTest? = null): UserPerformanceScores {
        logger.debug("Calculating performance scores for user: ${dailyMetrics.keycloakId}")

        // Calculate individual metric scores from weekly test data and daily metrics
        val explosivenessScore = calculateExplosivenessScore(weeklyTest?.verticalJumpResult)
        val aerobicCapacityScore = calculateAerobicCapacityScore(dailyMetrics.vo2Max)
        val recoveryScore = calculateRecoveryScore(weeklyTest?.hrRecoveryResult?.toDouble())
        val reactionTimeScore = calculateReactionTimeScore(weeklyTest?.reflexResult?.toDouble())

        // Calculate average score
        val scores = listOfNotNull(
            explosivenessScore,
            aerobicCapacityScore,
            recoveryScore,
            reactionTimeScore
        )
        val averageScore = if (scores.isNotEmpty()) scores.average() else 0.0

        // Level is the tanh-scaled athleticism score (1-100)
        val level = max(1, applyTanhScaling(averageScore).toInt())

        // Calculate HP/MP/Fatigue from daily metrics and weekly test data
        val hp = calculateHpScore(dailyMetrics, weeklyTest)
        val hpLoss = calculateHpLoss(dailyMetrics)
        val mp = calculateMpScore(dailyMetrics, weeklyTest)
        val mpLoss = calculateMpLoss(dailyMetrics)
        val fatigue = calculateFatigueScore(dailyMetrics)
        val fatigueLoss = calculateFatigueLoss(dailyMetrics)

        // Generate skills based on metric thresholds
        val skills = generateSkills(
            explosivenessScore,
            aerobicCapacityScore,
            recoveryScore,
            reactionTimeScore
        )

        return UserPerformanceScores(
            keycloakId = dailyMetrics.keycloakId,
            explosivenessScore = explosivenessScore,
            aerobicCapacityScore = aerobicCapacityScore,
            recoveryScore = recoveryScore,
            reactionTimeScore = reactionTimeScore,
            level = level,
            hp = hp,
            hpLoss = hpLoss,
            mp = mp,
            mpLoss = mpLoss,
            fatigue = fatigue,
            fatigueLoss = fatigueLoss,
            skills = skills,
            createdAt = dailyMetrics.createdAt,
            updatedAt = dailyMetrics.updatedAt
        )
    }


    /**
     * Calculates explosiveness score using logarithmic scaling.
     * Typical range: 30cm (low) to 70cm (elite) vertical jump.
     */
    private fun calculateExplosivenessScore(jumpCm: Double?): Double? {
        if (jumpCm == null || jumpCm <= 0) return null
        return max(0.0, min(100.0, 25.0 * log10(jumpCm / 30.0)))
    }

    /**
     * Calculates aerobic capacity score using logarithmic scaling.
     * Typical range: 35 (average) to 70+ (elite) VO₂ max.
     */
    private fun calculateAerobicCapacityScore(vo2Max: Double?): Double? {
        if (vo2Max == null || vo2Max <= 0) return null
        return max(0.0, min(100.0, 25.0 * log10(vo2Max / 35.0)))
    }

    /**
     * Calculates recovery score using linear scaling.
     * Typical range: 20 (poor) to 60+ (excellent) bpm drop.
     */
    private fun calculateRecoveryScore(hrRecovery: Double?): Double? {
        if (hrRecovery == null || hrRecovery < 0) return null
        return min(100.0, 2.0 * (hrRecovery - 20.0))
    }

    /**
     * Calculates reaction time score using inverse linear scaling.
     * Typical range: 300ms (fast) to 600ms (slow).
     */
    private fun calculateReactionTimeScore(reactionTimeMs: Double?): Double? {
        if (reactionTimeMs == null || reactionTimeMs <= 0) return null
        return max(0.0, min(100.0, 100.0 - ((reactionTimeMs - 300.0) * 0.33)))
    }

    /**
     * Applies tanh scaling to create diminishing returns at higher levels.
     * Centers at 50, scales by 15 for curve steepness.
     */
    private fun applyTanhScaling(avgScore: Double): Double {
        val scaled = (tanh((avgScore - 50.0) / 15.0) + 1.0) * 50.0
        return round(scaled * 10.0) / 10.0
    }

    /**
     * Calculates HP (Health Points) - static physical resilience value.
     * Represents physical durability based on VO₂ Max, muscular endurance, and recovery ability.
     * This is the "max HP" - HP loss is tracked separately based on daily stress factors.
     */
    private fun calculateHpScore(metrics: UserPerformanceMetrics, weeklyTest: UserWeeklyTest?): Double {
        // HP is based on physical resilience metrics from weekly tests and daily metrics
        val vo2MaxScore = calculateAerobicCapacityScore(metrics.vo2Max)
        val recoveryScore = calculateRecoveryScore(weeklyTest?.hrRecoveryResult?.toDouble())
        
        // Average the physical resilience scores
        val scores = listOfNotNull(vo2MaxScore, recoveryScore)
        val avgHp = if (scores.isNotEmpty()) scores.average() else 50.0 // Default to 50 if no data
        
        return round(avgHp * 10.0) / 10.0
    }

    /**
     * Calculates HP Loss - current HP reduction from daily stress factors.
     * Based on strain, recovery, sleep quality, and subjective tiredness.
     */
    private fun calculateHpLoss(metrics: UserPerformanceMetrics): Double {
        val strain = metrics.strain
        val recoveryScore = metrics.recovery ?: 75.0
        val sleepScore = metrics.sleepScore ?: 75.0
        val subjectiveTiredness = metrics.subjectiveTiredness?.toDouble() ?: 3.0
        
        var hpLoss = 0.0
        
        // HP loss from high strain (structural stress) - only if strain is available
        if (strain != null && strain > 12) {
            hpLoss += (strain - 12) * 2.0 // More significant HP loss for high strain
        }
        
        // HP loss from poor recovery
        if (recoveryScore < 70) {
            hpLoss += (70 - recoveryScore) * 0.5
        }
        
        // HP loss from poor sleep quality
        if (sleepScore < 70) {
            hpLoss += (70 - sleepScore) * 0.3
        }
        
        // HP loss from subjective tiredness (increased weight when strain is missing)
        if (subjectiveTiredness > 3) {
            val tirednessMultiplier = if (strain == null) 8.0 else 5.0 // Higher impact when strain is missing
            hpLoss += (subjectiveTiredness - 3) * tirednessMultiplier
        }
        
        return max(0.0, min(100.0, round(hpLoss * 10.0) / 10.0))
    }

    /**
     * Calculates MP Loss - current MP reduction from daily stress factors.
     * Based on sleep quality, HRV, strain, recovery, and mental fatigue.
     */
    private fun calculateMpLoss(metrics: UserPerformanceMetrics): Double {
        val strain = metrics.strain
        val recoveryScore = metrics.recovery ?: 75.0
        val sleepScore = metrics.sleepScore ?: 75.0
        val hrv = metrics.hrv ?: 50.0
        val subjectiveTiredness = metrics.subjectiveTiredness?.toDouble() ?: 3.0

        var mpLoss = 0.0

        // MP loss from poor sleep quality (affects neural recovery)
        if (sleepScore < 70) {
            mpLoss += (70 - sleepScore) * 0.4 // Higher impact on MP than HP
        }

        // MP loss from low HRV (CNS fatigue)
        if (hrv < 40) {
            mpLoss += (40 - hrv) * 0.5 // Significant MP loss for low HRV
        }

        // MP loss from high strain with poor recovery (CNS overload)
        if (strain != null && recoveryScore < 70) {
            val strainRecoveryRatio = strain / recoveryScore
            if (strainRecoveryRatio > 0.3) { // High strain relative to recovery
                mpLoss += (strainRecoveryRatio - 0.3) * 20.0
            }
        }

        // MP loss from poor recovery score
        if (recoveryScore < 70) {
            mpLoss += (70 - recoveryScore) * 0.3
        }

        // MP loss from subjective tiredness (mental fatigue)
        if (subjectiveTiredness > 3) {
            val tirednessMultiplier = if (strain == null) 6.0 else 4.0 // Higher impact when strain is missing
            mpLoss += (subjectiveTiredness - 3) * tirednessMultiplier
        }

        return max(0.0, min(100.0, round(mpLoss * 10.0) / 10.0))
    }

    /**
     * Calculates Fatigue Loss - current fatigue increase from daily stress factors.
     * Based on acute strain, sleep deprivation, and immediate energy depletion.
     * Represents session-to-session exhaustion that recovers quickly.
     */
    private fun calculateFatigueLoss(metrics: UserPerformanceMetrics): Double {
        val strain = metrics.strain
        val sleepScore = metrics.sleepScore ?: 75.0
        val subjectiveTiredness = metrics.subjectiveTiredness?.toDouble() ?: 3.0
        val remSleepMinutes = metrics.remSleepMinutes ?: 80.0
        val deepSleepMinutes = metrics.deepSleepMinutes ?: 120.0

        var fatigueLoss = 0.0

        // Fatigue from acute strain (immediate performance inhibition)
        if (strain != null) {
            fatigueLoss += strain * 4.0 // High impact on immediate fatigue
        }

        // Fatigue from poor sleep quality (immediate energy depletion)
        if (sleepScore < 70) {
            fatigueLoss += (70 - sleepScore) * 0.5
        }

        // Fatigue from insufficient REM sleep (affects immediate alertness)
        if (remSleepMinutes < 60) {
            fatigueLoss += (60 - remSleepMinutes) * 0.3
        }

        // Fatigue from insufficient deep sleep (affects physical recovery)
        if (deepSleepMinutes < 90) {
            fatigueLoss += (90 - deepSleepMinutes) * 0.2
        }

        // Fatigue from subjective tiredness (immediate perception)
        if (subjectiveTiredness > 3) {
            val tirednessMultiplier = if (strain == null) 12.0 else 8.0 // Higher impact when strain is missing
            fatigueLoss += (subjectiveTiredness - 3) * tirednessMultiplier
        }

        return max(0.0, min(100.0, round(fatigueLoss * 10.0) / 10.0))
    }

    /**
     * Calculates MP (Magic Points) - neurological/cognitive readiness.
     * Represents CNS efficiency, focus, and mental energy.
     * Based on HRV, reaction time, and explosiveness (fast-twitch/CNS power).
     */
    private fun calculateMpScore(metrics: UserPerformanceMetrics, weeklyTest: UserWeeklyTest?): Double {
        // MP is based on neurological readiness metrics
        val hrvScore = calculateHrvScore(metrics.hrv)
        val reactionTimeScore = calculateReactionTimeScore(weeklyTest?.reflexResult?.toDouble())
        val explosivenessScore = calculateExplosivenessScore(weeklyTest?.verticalJumpResult)
        
        // Average the neurological readiness scores
        val scores = listOfNotNull(hrvScore, reactionTimeScore, explosivenessScore)
        val avgMp = if (scores.isNotEmpty()) scores.average() else 50.0 // Default to 50 if no data
        
        return round(avgMp * 10.0) / 10.0
    }
    
    /**
     * Calculates HRV score for MP calculation.
     * Higher HRV indicates better neurological readiness.
     */
    private fun calculateHrvScore(hrv: Double?): Double? {
        if (hrv == null || hrv <= 0) return null
        // HRV scoring: 30ms (low) to 60+ms (high neurological readiness)
        return min(100.0, max(0.0, (hrv - 30) * 3.33))
    }

    /**
     * Calculates fatigue score - immediate performance inhibition.
     * Represents session-to-session exhaustion.
     * Recovery timeframe: hours to 1-2 days.
     */
    private fun calculateFatigueScore(metrics: UserPerformanceMetrics): Double {
        val strain = metrics.strain
        val subjectiveTiredness = metrics.subjectiveTiredness?.toDouble() ?: 3.0
        
        // Calculate fatigue based on available data
        val fatigue = when {
            strain != null -> {
                // Use both strain and subjective tiredness
                min(100.0, strain * 5.0 + subjectiveTiredness * 10.0)
            }
            else -> {
                // Use only subjective tiredness with higher weight
                min(100.0, subjectiveTiredness * 15.0)
            }
        }
        
        return round(fatigue * 10.0) / 10.0
    }

    /**
     * Generates skills based on metric thresholds.
     * Creates motivational skill labels when users exceed certain performance levels.
     */
    private fun generateSkills(
        explosivenessScore: Double?,
        aerobicScore: Double?,
        recoveryScore: Double?,
        reactionScore: Double?
    ): List<String> {
        val skills = mutableListOf<String>()

        // Explosiveness-based skills
        if (explosivenessScore != null && explosivenessScore >= 80) {
            skills.add("Explosive Power")
        } else if (explosivenessScore != null && explosivenessScore >= 60) {
            skills.add("Quick Burst")
        }

        // Aerobic-based skills
        if (aerobicScore != null && aerobicScore >= 80) {
            skills.add("Iron Lungs")
        } else if (aerobicScore != null && aerobicScore >= 60) {
            skills.add("Endurance Runner")
        }

        // Recovery-based skills
        if (recoveryScore != null && recoveryScore >= 80) {
            skills.add("Rapid Recovery")
        } else if (recoveryScore != null && recoveryScore >= 60) {
            skills.add("Quick Healer")
        }

        // Reaction-based skills
        if (reactionScore != null && reactionScore >= 80) {
            skills.add("Lightning Reflexes")
        } else if (reactionScore != null && reactionScore >= 60) {
            skills.add("Quick Response")
        }

        return skills
    }
}
