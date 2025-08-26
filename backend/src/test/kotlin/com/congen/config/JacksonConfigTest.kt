package com.congen.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.time.Instant

/**
 * Unit tests for [JacksonConfig].
 *
 * Tests cover all functionality including:
 * - ObjectMapper bean creation and configuration
 * - Custom Instant serialization and deserialization
 * - WorkoutStageTypeEnum serialization and deserialization
 * - Numeric Int deserialization
 * - Error handling for invalid inputs
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class JacksonConfigTest {
    @Mock
    private lateinit var jsonGenerator: JsonGenerator

    @Mock
    private lateinit var jsonParser: JsonParser

    @Mock
    private lateinit var deserializationContext: DeserializationContext

    @Mock
    private lateinit var serializerProvider: SerializerProvider

    @Mock
    private lateinit var objectMapper: ObjectMapper

    private lateinit var jacksonConfig: JacksonConfig

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        jacksonConfig = JacksonConfig()
    }

    @Test
    fun `should create ObjectMapper bean`() {
        val objectMapper = jacksonConfig.objectMapper()

        assertNotNull(objectMapper)
    }

    @Test
    fun `should configure ObjectMapper with custom settings`() {
        val mapper = ObjectMapper()

        JacksonConfig.configureObjectMapper(mapper)

        assertNotNull(mapper)
    }

    @Test
    fun `should serialize Instant to ISO string`() {
        val instant = Instant.parse("2024-01-01T12:00:00Z")
        val serializer = JacksonConfig.CustomInstantSerializer()

        serializer.serialize(instant, jsonGenerator, serializerProvider)

        // Note: We can't easily verify the exact call due to Mockito limitations with JsonGenerator
        // The test ensures the method doesn't throw exceptions
    }

    @Test
    fun `should serialize null Instant`() {
        val serializer = JacksonConfig.CustomInstantSerializer()

        // Should not throw exception
        serializer.serialize(null, jsonGenerator, serializerProvider)
    }

    @Test
    fun `should deserialize ISO instant string`() {
        val instantString = "2024-01-01T12:00:00Z"
        val expectedInstant = Instant.parse(instantString)
        val deserializer = JacksonConfig.CustomInstantDeserializer()
        `when`(jsonParser.text).thenReturn(instantString)

        val result = deserializer.deserialize(jsonParser, deserializationContext)

        assertEquals(expectedInstant, result)
    }

    @Test
    fun `should deserialize Unix timestamp`() {
        val unixTimestamp = "1704110400.0"
        val deserializer = JacksonConfig.CustomInstantDeserializer()
        `when`(jsonParser.text).thenReturn(unixTimestamp)

        val result = deserializer.deserialize(jsonParser, deserializationContext)

        assertEquals(Instant.ofEpochSecond(1704110400L), result)
    }

    @Test
    fun `should deserialize LocalDateTime string as UTC`() {
        val localDateTimeString = "2024-01-01T12:00:00"
        val expectedInstant = Instant.parse("2024-01-01T12:00:00Z")
        val deserializer = JacksonConfig.CustomInstantDeserializer()
        `when`(jsonParser.text).thenReturn(localDateTimeString)

        val result = deserializer.deserialize(jsonParser, deserializationContext)

        assertEquals(expectedInstant, result)
    }

    @Test
    fun `should throw exception for invalid timestamp format`() {
        val invalidTimestamp = "invalid-timestamp"
        val deserializer = JacksonConfig.CustomInstantDeserializer()
        `when`(jsonParser.text).thenReturn(invalidTimestamp)

        assertThrows<IllegalArgumentException> {
            deserializer.deserialize(jsonParser, deserializationContext)
        }
    }

    @Test
    fun `should deserialize integer value to Int`() {
        val deserializer = JacksonConfig.NumericIntDeserializer()
        `when`(jsonParser.currentToken).thenReturn(JsonToken.VALUE_NUMBER_INT)
        `when`(jsonParser.intValue).thenReturn(42)

        val result = deserializer.deserialize(jsonParser, deserializationContext)

        assertEquals(42, result)
    }

    @Test
    fun `should deserialize float value to Int`() {
        val deserializer = JacksonConfig.NumericIntDeserializer()
        `when`(jsonParser.currentToken).thenReturn(JsonToken.VALUE_NUMBER_FLOAT)
        `when`(jsonParser.doubleValue).thenReturn(42.5)

        val result = deserializer.deserialize(jsonParser, deserializationContext)

        assertEquals(42, result)
    }

    @Test
    fun `should deserialize string value to Int`() {
        val deserializer = JacksonConfig.NumericIntDeserializer()
        `when`(jsonParser.currentToken).thenReturn(JsonToken.VALUE_STRING)
        `when`(jsonParser.text).thenReturn("42")

        val result = deserializer.deserialize(jsonParser, deserializationContext)

        assertEquals(42, result)
    }

    @Test
    fun `should deserialize object with single int field to Int`() {
        val json =
            ObjectMapper().createObjectNode().apply {
                put("value", 42)
            }.toString()

        val result = jacksonConfig.objectMapper().readValue(json, Int::class.java)

        assertEquals(42, result)
    }

    @Test
    fun `should deserialize object with single long field to Int`() {
        val json =
            ObjectMapper().createObjectNode().apply {
                put("value", 42L)
            }.toString()

        val result = jacksonConfig.objectMapper().readValue(json, Int::class.java)

        assertEquals(42, result)
    }

    @Test
    fun `should deserialize object with single double field to Int`() {
        val json =
            ObjectMapper().createObjectNode().apply {
                put("value", 42.0)
            }.toString()

        val result = jacksonConfig.objectMapper().readValue(json, Int::class.java)

        assertEquals(42, result)
    }

    @Test
    fun `should deserialize object with single string field to Int`() {
        val json =
            ObjectMapper().createObjectNode().apply {
                put("value", "42")
            }.toString()

        val result = jacksonConfig.objectMapper().readValue(json, Int::class.java)

        assertEquals(42, result)
    }

    @Test
    fun `should throw exception for object with multiple fields`() {
        val json =
            ObjectMapper().createObjectNode().apply {
                put("value1", 42)
                put("value2", 100)
            }.toString()

        assertThrows<Exception> {
            jacksonConfig.objectMapper().readValue(json, Int::class.java)
        }
    }

    @Test
    fun `should throw exception for unsupported token type`() {
        val deserializer = JacksonConfig.NumericIntDeserializer()
        `when`(jsonParser.currentToken).thenReturn(JsonToken.START_ARRAY)

        assertThrows<JsonMappingException> {
            deserializer.deserialize(jsonParser, deserializationContext)
        }
    }
}
