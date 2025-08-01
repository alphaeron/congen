package com.congen.config

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Jackson configuration for JSON serialization/deserialization.
 *
 * This configuration sets up custom serializers and deserializers for:
 * - Instant timestamps (handles UTC conversion)
 * - Numeric types (handles PostgreSQL SMALLINT to Int conversion)
 *
 * Note: Enums like WorkoutStageTypeEnum, MovementType, and Band use @JsonValue annotations
 * for serialization instead of custom serializers for simplicity.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
class JacksonConfig {
    /**
     * Custom Instant serializer that outputs ISO string format.
     *
     * This serializer ensures that Instant values are serialized as ISO-8601 strings
     * (e.g., "2024-01-01T12:00:00Z") instead of Unix timestamps.
     */
    class CustomInstantSerializer : JsonSerializer<Instant>() {
        override fun serialize(
            value: Instant?,
            gen: JsonGenerator,
            serializers: SerializerProvider
        ) {
            if (value != null) {
                gen.writeString(value.toString())
            }
        }
    }

    /**
     * Custom Instant deserializer that handles various timestamp formats.
     *
     * This deserializer handles:
     * - ISO instant strings (e.g., "2024-01-01T12:00:00Z")
     * - Unix timestamps (e.g., "1751933126.629009000")
     * - LocalDateTime strings (e.g., "2024-01-01T12:00:00")
     *
     * All timestamps are treated as UTC, which aligns with the project's UTC-only datetime policy.
     */
    class CustomInstantDeserializer : JsonDeserializer<Instant>() {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext
        ): Instant {
            val text = p.text
            return try {
                // Try to parse as ISO instant first
                Instant.parse(text)
            } catch (e: Exception) {
                try {
                    // Try to parse as Unix timestamp (seconds since epoch)
                    val seconds = text.toDouble()
                    Instant.ofEpochSecond(seconds.toLong(), ((seconds % 1) * 1_000_000_000).toLong())
                } catch (e2: Exception) {
                    try {
                        // If that fails, try parsing as LocalDateTime and treat as UTC
                        val localDateTime = LocalDateTime.parse(text, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        localDateTime.atZone(ZoneOffset.UTC).toInstant()
                    } catch (e3: Exception) {
                        throw IllegalArgumentException("Unable to parse timestamp: $text", e3)
                    }
                }
            }
        }
    }

    /**
     * Custom serializer for BigDecimal to output as numbers instead of strings.
     *
     * This serializer ensures that BigDecimal values are serialized as numbers
     * instead of strings, which is the expected format for the frontend.
     */
    class BigDecimalSerializer : JsonSerializer<BigDecimal>() {
        override fun serialize(
            value: BigDecimal?,
            gen: JsonGenerator,
            serializers: SerializerProvider
        ) {
            if (value != null) {
                gen.writeNumber(value)
            }
        }
    }

    /**
     * Custom deserializer for BigDecimal to handle various numeric input formats.
     *
     * This deserializer handles cases where BigDecimal values might be sent as
     * numbers, strings, or other numeric types from the frontend.
     */
    class BigDecimalDeserializer : JsonDeserializer<BigDecimal>() {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext
        ): BigDecimal {
            return when (p.currentToken) {
                JsonToken.VALUE_NUMBER_INT -> BigDecimal.valueOf(p.longValue)
                JsonToken.VALUE_NUMBER_FLOAT -> BigDecimal.valueOf(p.doubleValue)
                JsonToken.VALUE_STRING -> BigDecimal(p.text)
                else -> throw JsonMappingException(p, "Cannot deserialize ${p.currentToken} to BigDecimal")
            }
        }
    }

    /**
     * Custom deserializer for numeric types to handle PostgreSQL SMALLINT to Int conversion.
     *
     * This deserializer handles cases where PostgreSQL returns SMALLINT values that need to be
     * converted to Int for Kotlin models. It handles various numeric types including Short, Long,
     * and other numeric objects that might be returned from the database.
     */
    class NumericIntDeserializer : JsonDeserializer<Int>() {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext
        ): Int {
            return when (p.currentToken) {
                JsonToken.VALUE_NUMBER_INT -> p.intValue
                JsonToken.VALUE_NUMBER_FLOAT -> p.doubleValue.toInt()
                JsonToken.VALUE_STRING -> p.text.toInt()
                JsonToken.START_OBJECT -> {
                    val node = p.codec.readTree<com.fasterxml.jackson.databind.JsonNode>(p)
                    if (node.size() == 1) {
                        // Extract the first (and only) field value, regardless of field name
                        val fieldName = node.fieldNames().next()
                        val fieldValue = node.get(fieldName)
                        return when {
                            fieldValue.isInt -> fieldValue.asInt()
                            fieldValue.isLong -> fieldValue.asLong().toInt()
                            fieldValue.isDouble -> fieldValue.asDouble().toInt()
                            fieldValue.isTextual -> fieldValue.asText().toInt()
                            else -> throw JsonMappingException(p, "Cannot deserialize object with field '$fieldName' to Int: $node")
                        }
                    } else {
                        throw JsonMappingException(p, "Cannot deserialize object with multiple or unknown fields to Int: $node")
                    }
                }
                else -> throw JsonMappingException(p, "Cannot deserialize ${p.currentToken} to Int")
            }
        }
    }

    /**
     * Creates and configures the primary ObjectMapper bean.
     *
     * @return Configured ObjectMapper with custom serializers and deserializers
     */
    @Bean
    @Primary
    fun objectMapper(): ObjectMapper {
        val mapper = ObjectMapper()
        configureObjectMapper(mapper)
        return mapper
    }

    companion object {
        /**
         * Configures an ObjectMapper with the standard project configuration.
         *
         * @param mapper The ObjectMapper to configure
         */
        fun configureObjectMapper(mapper: ObjectMapper) {
            // Set property naming strategy to SNAKE_CASE globally
            mapper.propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE

            // Register Kotlin module for proper Kotlin property handling
            mapper.registerKotlinModule()

            // Register JavaTimeModule for Java 8 time support
            val javaTimeModule = JavaTimeModule()
            // Configure Instant serialization/deserialization
            javaTimeModule.addSerializer(Instant::class.java, CustomInstantSerializer())
            javaTimeModule.addDeserializer(Instant::class.java, CustomInstantDeserializer())

            mapper.registerModule(javaTimeModule)

            // Register custom module for numeric types
            val customModule = SimpleModule()
            customModule.addDeserializer(Int::class.java, NumericIntDeserializer())
            customModule.addDeserializer(Int::class.javaObjectType, NumericIntDeserializer())
            customModule.addSerializer(BigDecimal::class.java, BigDecimalSerializer())
            customModule.addDeserializer(BigDecimal::class.java, BigDecimalDeserializer())
            mapper.registerModule(customModule)
        }
    }
}
