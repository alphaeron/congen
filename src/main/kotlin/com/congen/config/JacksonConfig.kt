package com.congen.config

import com.congen.model.Band
import com.congen.model.MovementType
import com.congen.model.WorkoutStageTypeEnum
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
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.NoSuchElementException

/**
 * Jackson configuration for JSON serialization/deserialization.
 *
 * This configuration sets up custom serializers and deserializers for:
 * - Instant timestamps (handles UTC conversion)
 * - WorkoutStageTypeEnum (handles display names)
 * - Numeric types (handles PostgreSQL SMALLINT to Int conversion)
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
     * Custom WorkoutStageTypeEnum serializer that uses the enum name.
     *
     * This serializer ensures that WorkoutStageTypeEnum values are serialized as their
     * enum names (e.g., "WARMUP") instead of their display names (e.g., "Warmup").
     */
    class WorkoutStageTypeEnumSerializer : JsonSerializer<WorkoutStageTypeEnum>() {
        override fun serialize(
            value: WorkoutStageTypeEnum?,
            gen: JsonGenerator,
            serializers: SerializerProvider
        ) {
            if (value != null) {
                gen.writeString(value.displayName)
            }
        }
    }

    /**
     * Custom deserializer for WorkoutStageTypeEnum.
     */
    class WorkoutStageTypeEnumDeserializer : JsonDeserializer<WorkoutStageTypeEnum>() {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext
        ): WorkoutStageTypeEnum {
            val value = p.text
            return try {
                // Try to match ignoring case
                WorkoutStageTypeEnum.values().first { it.name.equals(value, ignoreCase = true) }
            } catch (e: NoSuchElementException) {
                throw JsonMappingException(p, "Invalid WorkoutStageTypeEnum value: $value")
            }
        }
    }

    /**
     * Custom Band serializer that serializes to the weight value.
     *
     * This serializer ensures that Band values are serialized as their weight value
     * for database storage and API responses.
     */
    class BandSerializer : JsonSerializer<Band>() {
        override fun serialize(
            value: Band?,
            gen: JsonGenerator,
            serializers: SerializerProvider
        ) {
            if (value != null) {
                gen.writeNumber(value.weightLbs)
            }
        }
    }

    /**
     * Custom deserializer for Band.
     */
    class BandDeserializer : JsonDeserializer<Band>() {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext
        ): Band {
            val weightLbs = p.decimalValue
            return Band.fromWeight(weightLbs) ?: throw JsonMappingException(p, "Invalid Band weight: $weightLbs")
        }
    }

    /**
     * Custom MovementType serializer that uses the display name.
     *
     * This serializer ensures that MovementType values are serialized as their
     * display names (e.g., "horizontal push") instead of their enum names (e.g., "HORIZONTAL_PUSH").
     */
    class MovementTypeSerializer : JsonSerializer<MovementType>() {
        override fun serialize(
            value: MovementType?,
            gen: JsonGenerator,
            serializers: SerializerProvider
        ) {
            if (value != null) {
                gen.writeString(value.displayName)
            }
        }
    }

    /**
     * Custom deserializer for MovementType.
     */
    class MovementTypeDeserializer : JsonDeserializer<MovementType>() {
        override fun deserialize(
            p: JsonParser,
            ctxt: DeserializationContext
        ): MovementType {
            val value = p.text
            // First try to find by enum name (case-insensitive)
            return try {
                MovementType.values().first { it.name.equals(value, ignoreCase = true) }
            } catch (e: NoSuchElementException) {
                // If not found by enum name, try by display name
                MovementType.fromString(value) ?: throw JsonMappingException(p, "Invalid MovementType value: $value")
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

            // Configure WorkoutStageTypeEnum serialization
            javaTimeModule.addSerializer(WorkoutStageTypeEnum::class.java, WorkoutStageTypeEnumSerializer())
            // Configure Band serialization
            javaTimeModule.addSerializer(Band::class.java, BandSerializer())
            // Configure MovementType serialization
            javaTimeModule.addSerializer(MovementType::class.java, MovementTypeSerializer())

            mapper.registerModule(javaTimeModule)

            // Register custom module for WorkoutStageTypeEnum, Band, and numeric types
            val customModule = SimpleModule()
            customModule.addDeserializer(WorkoutStageTypeEnum::class.java, WorkoutStageTypeEnumDeserializer())
            customModule.addSerializer(WorkoutStageTypeEnum::class.java, WorkoutStageTypeEnumSerializer())
            customModule.addDeserializer(Band::class.java, BandDeserializer())
            customModule.addSerializer(Band::class.java, BandSerializer())
            customModule.addDeserializer(MovementType::class.java, MovementTypeDeserializer())
            customModule.addSerializer(MovementType::class.java, MovementTypeSerializer())
            customModule.addDeserializer(Int::class.java, NumericIntDeserializer())
            customModule.addDeserializer(Int::class.javaObjectType, NumericIntDeserializer())
            mapper.registerModule(customModule)
        }
    }
}
