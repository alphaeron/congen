package com.congen.generator

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

/**
 * Unit tests for the WorkoutStageGenerationServiceFactory.
 *
 * These tests verify that the factory correctly selects the appropriate
 * workout stage generation service based on the number of days per week.
 */
class WorkoutStageGenerationServiceFactoryTest {
    private lateinit var factory: WorkoutStageGenerationServiceFactory
    private lateinit var twoDayService: TwoDayWorkoutStageGenerationService
    private lateinit var threeDayService: ThreeDayWorkoutStageGenerationService
    private lateinit var fourDayService: FourDayWorkoutStageGenerationService

    @BeforeEach
    fun setUp() {
        twoDayService = mock()
        threeDayService = mock()
        fourDayService = mock()

        factory =
            WorkoutStageGenerationServiceFactory(
                twoDayWorkoutStageGenerationService = twoDayService,
                threeDayWorkoutStageGenerationService = threeDayService,
                fourDayWorkoutStageGenerationService = fourDayService
            )
    }

    @Test
    fun `getWorkoutStageGenerationService should return two day service for 2 days per week`() {
        // When
        val result = factory.getWorkoutStageGenerationService(2)

        // Then
        assertEquals(twoDayService, result)
    }

    @Test
    fun `getWorkoutStageGenerationService should return three day service for 3 days per week`() {
        // When
        val result = factory.getWorkoutStageGenerationService(3)

        // Then
        assertEquals(threeDayService, result)
    }

    @Test
    fun `getWorkoutStageGenerationService should return four day service for 4 days per week`() {
        // When
        val result = factory.getWorkoutStageGenerationService(4)

        // Then
        assertEquals(fourDayService, result)
    }

    @Test
    fun `getWorkoutStageGenerationService should throw exception for invalid number of days`() {
        // When & Then
        assertThrows(IllegalArgumentException::class.java) {
            factory.getWorkoutStageGenerationService(1)
        }

        assertThrows(IllegalArgumentException::class.java) {
            factory.getWorkoutStageGenerationService(5)
        }

        assertThrows(IllegalArgumentException::class.java) {
            factory.getWorkoutStageGenerationService(0)
        }

        assertThrows(IllegalArgumentException::class.java) {
            factory.getWorkoutStageGenerationService(-1)
        }
    }
}
