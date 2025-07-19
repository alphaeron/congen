package com.congen.service.conjugate

import com.congen.model.Band
import com.congen.model.WeightUnit
import com.congen.service.UnitConversionService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNull

class BandWeightServiceTest {
    @Mock
    private lateinit var unitConversionService: UnitConversionService

    private lateinit var bandWeightService: BandWeightService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        bandWeightService = BandWeightService(unitConversionService)
    }

    @Test
    fun `should compute band weights for week1`() {
        val totalWeight = BigDecimal(200)
        val weekInCycle = 1
        whenever(unitConversionService.fromKg(totalWeight, WeightUnit.LBS)).thenReturn(totalWeight)
        whenever(unitConversionService.toKg(any(), any())).thenReturn(BigDecimal("100"))

        val result =
            bandWeightService.computeBandAndBarWeights(
                "Bench Press",
                totalWeight,
                WeightUnit.LBS,
                weekInCycle
            )

        assertEquals(Band(BigDecimal("50")), result.band)
        assertEquals(BigDecimal("100"), result.barWeight)
    }

    @Test
    fun `should compute band weights for week 4 (deload)`() {
        val totalWeight = BigDecimal(200)
        val weekInCycle = 4

        val result =
            bandWeightService.computeBandAndBarWeights(
                "Bench Press",
                totalWeight,
                WeightUnit.LBS,
                weekInCycle
            )

        assertNull(result.band)
        assertEquals(totalWeight, result.barWeight)
    }

    @Test
    fun `should get correct band weight percentages`() {
        assertEquals(0.25, bandWeightService.getBandWeightPercentage(1))
        assertEquals(0.25, bandWeightService.getBandWeightPercentage(2))
        assertEquals(0.25, bandWeightService.getBandWeightPercentage(3))
        assertEquals(0.0, bandWeightService.getBandWeightPercentage(4))
        assertEquals(0.0, bandWeightService.getBandWeightPercentage(5))
    }
}
