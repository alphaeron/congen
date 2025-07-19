package com.congen.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class BandTest {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    @Test
    fun `should create band with correct weight`() {
        val blackBand = Band(BigDecimal("100"))
        val greenBand = Band(BigDecimal("65"))
        val blueBand = Band(BigDecimal("50"))
        val redBand = Band(BigDecimal("30"))
        val orangeBand = Band(BigDecimal("15"))

        assertEquals(BigDecimal("100"), blackBand.weightLbs)
        assertEquals(BigDecimal("65"), greenBand.weightLbs)
        assertEquals(BigDecimal("50"), blueBand.weightLbs)
        assertEquals(BigDecimal("30"), redBand.weightLbs)
        assertEquals(BigDecimal("15"), orangeBand.weightLbs)
    }

    @Test
    fun `should create band with correct color`() {
        assertEquals("Black", Band(BigDecimal("100")).color)
        assertEquals("Green", Band(BigDecimal("65")).color)
        assertEquals("Blue", Band(BigDecimal("50")).color)
        assertEquals("Red", Band(BigDecimal("30")).color)
        assertEquals("Orange", Band(BigDecimal("15")).color)
    }

    @Test
    fun `should create bands from weight using companion object`() {
        val blackBand = Band.fromWeight(BigDecimal("100"))
        val greenBand = Band.fromWeight(BigDecimal("65"))
        val blueBand = Band.fromWeight(BigDecimal("50"))
        val redBand = Band.fromWeight(BigDecimal("30"))
        val orangeBand = Band.fromWeight(BigDecimal("15"))

        assertNotNull(blackBand)
        assertNotNull(greenBand)
        assertNotNull(blueBand)
        assertNotNull(redBand)
        assertNotNull(orangeBand)
    }

    @Test
    fun `should return null for invalid weights`() {
        assertNull(Band.fromWeight(BigDecimal("25")))
        assertNull(Band.fromWeight(BigDecimal("75")))
        assertNull(Band.fromWeight(BigDecimal("0")))
        assertNull(Band.fromWeight(BigDecimal("-10")))
    }

    @Test
    fun `should create bands from weight string`() {
        assertNotNull(Band.fromWeight("100"))
        assertNotNull(Band.fromWeight("65"))
        assertNotNull(Band.fromWeight("50"))
        assertNotNull(Band.fromWeight("30"))
        assertNotNull(Band.fromWeight("15"))
    }

    @Test
    fun `should return null for invalid weight strings`() {
        assertNull(Band.fromWeight("25"))
        assertNull(Band.fromWeight("invalid"))
        assertNull(Band.fromWeight(""))
    }

    @Test
    fun `should serialize to JSON with weight`() {
        val band = Band(BigDecimal("100"))
        val json = objectMapper.writeValueAsString(band)
        assertEquals("{\"weightLbs\":100,\"color\":\"Black\"}", json)
    }

    @Test
    fun `should deserialize from JSON with weight`() {
        val band = objectMapper.readValue("{\"weightLbs\":100}", Band::class.java)
        assertEquals(BigDecimal("100"), band.weightLbs)
    }
}
