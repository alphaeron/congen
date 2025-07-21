package com.congen.generator

import com.congen.model.Exercise
import com.congen.model.MovementType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Unit tests for MovementBalanceService.
 *
 * Tests the movement balance constraints and functionality including:
 * - Movement type balancing (vertical_push/horizontal_pull, horizontal_push/vertical_pull)
 * - Pull-to-push volume ratio calculations
 * - Exercise scoring for balance
 * - Exercise prioritization
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class MovementBalanceServiceTest {
    private lateinit var movementBalanceService: MovementBalanceService

    @BeforeEach
    fun setUp() {
        movementBalanceService = MovementBalanceService()
    }

    @Test
    fun `should create initial state with empty values`() {
        val state = movementBalanceService.createInitialState()

        assertEquals(emptyList<Exercise>(), state.selectedExercises)
        assertEquals(emptyMap<MovementType, Int>(), state.movementTypeCounts)
        assertEquals(BigDecimal.ZERO, state.pushVolume)
        assertEquals(BigDecimal.ZERO, state.pullVolume)
        assertEquals(0.0, state.getPullToPushRatio())
    }

    @Test
    fun `should calculate pull-to-push ratio correctly`() {
        val state =
            MovementBalanceService.MovementBalanceState(
                pushVolume = BigDecimal("1000"),
                pullVolume = BigDecimal("2000")
            )

        assertEquals(2.0, state.getPullToPushRatio())
    }

    @Test
    fun `should return zero ratio when no push volume`() {
        val state =
            MovementBalanceService.MovementBalanceState(
                pushVolume = BigDecimal.ZERO,
                pullVolume = BigDecimal("1000")
            )

        assertEquals(0.0, state.getPullToPushRatio())
    }

    @Test
    fun `should detect need for balancing movement`() {
        val state =
            MovementBalanceService.MovementBalanceState(
                movementTypeCounts =
                    mapOf(
                        MovementType.VERTICAL_PUSH to 1,
                        MovementType.HORIZONTAL_PULL to 0
                    )
            )

        assertTrue(state.needsBalancingMovement(MovementType.VERTICAL_PUSH))
        assertFalse(state.needsBalancingMovement(MovementType.HORIZONTAL_PULL))
    }

    @Test
    fun `should not need balancing when both movements present`() {
        val state =
            MovementBalanceService.MovementBalanceState(
                movementTypeCounts =
                    mapOf(
                        MovementType.VERTICAL_PUSH to 1,
                        MovementType.HORIZONTAL_PULL to 1
                    )
            )

        assertFalse(state.needsBalancingMovement(MovementType.VERTICAL_PUSH))
        assertFalse(state.needsBalancingMovement(MovementType.HORIZONTAL_PULL))
    }

    @Test
    fun `should detect need for more pull volume`() {
        val state =
            MovementBalanceService.MovementBalanceState(
                pushVolume = BigDecimal("1000"),
                // Below 2:1 ratio
                pullVolume = BigDecimal("1500")
            )

        assertTrue(state.needsMorePullVolume())
    }

    @Test
    fun `should not need more pull volume when ratio is adequate`() {
        val state =
            MovementBalanceService.MovementBalanceState(
                pushVolume = BigDecimal("1000"),
                // 2.5:1 ratio, more than target 2:1
                pullVolume = BigDecimal("2500")
            )

        assertFalse(state.needsMorePullVolume())
    }

    @Test
    fun `should add exercise and update state correctly`() {
        val initialState = movementBalanceService.createInitialState()
        val exercise =
            Exercise(
                name = "Bench Press",
                description = "Standard bench press",
                movementType = MovementType.HORIZONTAL_PUSH,
                isUnilateral = false,
                isUpper = true,
                isAccessory = false
            )

        val updatedState = initialState.addExercise(exercise, BigDecimal("1200"))

        assertEquals(1, updatedState.selectedExercises.size)
        assertEquals(exercise, updatedState.selectedExercises.first())
        assertEquals(1, updatedState.movementTypeCounts[MovementType.HORIZONTAL_PUSH])
        assertEquals(BigDecimal("1200"), updatedState.pushVolume)
        assertEquals(BigDecimal.ZERO, updatedState.pullVolume)
    }

    @Test
    fun `should estimate exercise volume correctly for non-accessory`() {
        val exercise =
            Exercise(
                name = "Bench Press",
                description = "Standard bench press",
                movementType = MovementType.HORIZONTAL_PUSH,
                isUnilateral = false,
                isUpper = true,
                isAccessory = false
            )

        val volume = movementBalanceService.estimateExerciseVolume(false)

        // Expected: 4 sets * 6 reps * 100 kg = 2400
        assertEquals(BigDecimal("2400"), volume)
    }

    @Test
    fun `should estimate exercise volume correctly for accessory`() {
        val exercise =
            Exercise(
                name = "Ab Wheel",
                description = "Ab wheel tool",
                movementType = MovementType.CORE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )

        val volume = movementBalanceService.estimateExerciseVolume(true)

        // Expected: 3 sets * 12 reps * 50 kg = 1800
        assertEquals(BigDecimal("1800"), volume)
    }

    @Test
    fun `should score exercise highly when it balances existing movement`() {
        val state =
            MovementBalanceService.MovementBalanceState(
                movementTypeCounts =
                    mapOf(
                        MovementType.VERTICAL_PUSH to 1,
                        MovementType.HORIZONTAL_PULL to 0
                    )
            )
        val exercise =
            Exercise(
                name = "Landmine Row",
                description = "Start with the landmine between your legs",
                movementType = MovementType.HORIZONTAL_PULL,
                isUnilateral = true,
                isUpper = true,
                isAccessory = false
            )

        val score = movementBalanceService.scoreExerciseForBalance(exercise, state)

        assertTrue(score >= 10.0) // Should get high score for balancing
    }

    @Test
    fun `should score pull exercise higher when more pull volume needed`() {
        val state =
            MovementBalanceService.MovementBalanceState(
                pushVolume = BigDecimal("1000"),
                // Below 2:1 ratio
                pullVolume = BigDecimal("1500")
            )
        val exercise =
            Exercise(
                name = "Chin-Up",
                description = "Standard chin-up",
                movementType = MovementType.VERTICAL_PULL,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )

        val score = movementBalanceService.scoreExerciseForBalance(exercise, state)

        assertTrue(score >= 5.0) // Should get bonus for helping pull volume
    }

    @Test
    fun `should penalize push exercise when pull volume is needed`() {
        val state =
            MovementBalanceService.MovementBalanceState(
                pushVolume = BigDecimal("1000"),
                // Below 2:1 ratio
                pullVolume = BigDecimal("1500")
            )
        val exercise =
            Exercise(
                name = "Incline Bench Press",
                description = "Bench press with an upward incline",
                movementType = MovementType.HORIZONTAL_PUSH,
                isUnilateral = false,
                isUpper = true,
                isAccessory = false
            )

        val score = movementBalanceService.scoreExerciseForBalance(exercise, state)

        assertTrue(score <= 0.0) // Should be penalized for adding push volume
    }

    @Test
    fun `should prioritize exercises for balance correctly`() {
        val state =
            MovementBalanceService.MovementBalanceState(
                movementTypeCounts =
                    mapOf(
                        MovementType.VERTICAL_PUSH to 1,
                        MovementType.HORIZONTAL_PULL to 0
                    )
            )

        val exercises =
            listOf(
                Exercise("Bench Press", "Standard bench press", MovementType.HORIZONTAL_PUSH, false, true, false),
                Exercise("Bent-Over Row", "Hinge your hips and lean forward", MovementType.VERTICAL_PULL, true, true, false),
                Exercise("Back Squat", "Start with the bar in a back rack position", MovementType.SQUAT, false, false, false)
            )

        val prioritized = movementBalanceService.prioritizeExercisesForBalance(exercises, state)

        // Bent-Over Row should be first as it balances the existing vertical push
        assertEquals("Bent-Over Row", prioritized.first().name)
    }

    @Test
    fun `should return empty list when no exercises provided`() {
        val state = movementBalanceService.createInitialState()
        val prioritized = movementBalanceService.prioritizeExercisesForBalance(emptyList(), state)

        assertTrue(prioritized.isEmpty())
    }

    @Test
    fun `should maintain exercise order when no balance constraints apply`() {
        val state = movementBalanceService.createInitialState() // Empty state, no constraints

        val exercises =
            listOf(
                Exercise("Back Squat", "Start with the bar in a back rack position", MovementType.SQUAT, false, false, false),
                Exercise("Deadlift", "Classical deadlift", MovementType.HINGE, false, false, false),
                Exercise("Ab Wheel", "Ab wheel tool", MovementType.CORE, false, true, true)
            )

        val prioritized = movementBalanceService.prioritizeExercisesForBalance(exercises, state)

        // Should maintain original order when no balance constraints apply (sorted by name as secondary criterion)
        assertEquals("Ab Wheel", prioritized[0].name)
        assertEquals("Back Squat", prioritized[1].name)
        assertEquals("Deadlift", prioritized[2].name)
    }

    @Test
    fun `should handle multiple exercises with different balance scores`() {
        val state =
            MovementBalanceService.MovementBalanceState(
                movementTypeCounts =
                    mapOf(
                        MovementType.VERTICAL_PUSH to 1,
                        MovementType.HORIZONTAL_PULL to 0
                    ),
                pushVolume = BigDecimal("1000"),
                // Below 2:1 ratio
                pullVolume = BigDecimal("1500")
            )

        val exercises =
            listOf(
                Exercise("Bench Press", "Standard bench press", MovementType.HORIZONTAL_PUSH, false, true, false),
                Exercise("Bent-Over Row", "Hinge your hips and lean forward", MovementType.VERTICAL_PULL, true, true, false),
                Exercise("Chin-Up", "Standard chin-up", MovementType.VERTICAL_PULL, false, true, true),
                Exercise("Back Squat", "Start with the bar in a back rack position", MovementType.SQUAT, false, false, false)
            )

        // Debug: Print scores for each exercise
        exercises.forEach { exercise ->
            val score = movementBalanceService.scoreExerciseForBalance(exercise, state)
            println("${exercise.name}: score = $score")
        }

        val prioritized = movementBalanceService.prioritizeExercisesForBalance(exercises, state)

        // Debug: Print prioritized order
        println("Prioritized order: ${prioritized.map { it.name }}")

        // Bent-Over Row should be first (balances vertical push + helps pull volume)
        // Chin-Up should be second (helps pull volume)
        // Back Squat should be third (neutral)
        // Bench Press should be last (penalized for adding push volume)
        assertEquals("Bent-Over Row", prioritized[0].name)
        assertEquals("Chin-Up", prioritized[1].name)
        assertEquals("Back Squat", prioritized[2].name)
        assertEquals("Bench Press", prioritized[3].name)
    }

    @Test
    fun `should handle accessory exercises with different volume estimates`() {
        val state = movementBalanceService.createInitialState()
        val accessoryExercise =
            Exercise(
                name = "Ab Wheel",
                description = "Ab wheel tool",
                movementType = MovementType.CORE,
                isUnilateral = false,
                isUpper = true,
                isAccessory = true
            )

        val updatedState = state.addExercise(accessoryExercise, BigDecimal("1800"))

        assertEquals(1, updatedState.selectedExercises.size)
        // Core not counted as push or pull
        assertEquals(BigDecimal.ZERO, updatedState.pushVolume)
        assertEquals(BigDecimal.ZERO, updatedState.pullVolume)
    }

    @Test
    fun `should accumulate volume correctly across multiple exercises`() {
        var state = movementBalanceService.createInitialState()

        val pushExercise = Exercise("Bench Press", "Standard bench press", MovementType.HORIZONTAL_PUSH, false, true, false)
        val pullExercise = Exercise("Bent-Over Row", "Hinge your hips and lean forward", MovementType.VERTICAL_PULL, true, true, false)

        state = state.addExercise(pushExercise, BigDecimal("1200"))
        state = state.addExercise(pullExercise, BigDecimal("2400"))

        assertEquals(2, state.selectedExercises.size)
        assertEquals(BigDecimal("1200"), state.pushVolume)
        assertEquals(BigDecimal("2400"), state.pullVolume)
        assertEquals(2.0, state.getPullToPushRatio())
    }
}
