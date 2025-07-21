package com.congen.config

import com.congen.model.MovementType
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer

/**
 * Custom Jackson deserializer for MovementType enum.
 *
 * This deserializer handles both enum names (HORIZONTAL_PUSH) and display names (horizontal_push)
 * to provide flexible deserialization from JSON and database values.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class MovementTypeDeserializer : JsonDeserializer<MovementType>() {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): MovementType? {
        val value = p.valueAsString ?: return null

        // First try to find by enum name (for database values)
        return try {
            MovementType.valueOf(value)
        } catch (e: IllegalArgumentException) {
            // If not found by enum name, try by display name
            MovementType.fromString(value)
        }
    }
}
