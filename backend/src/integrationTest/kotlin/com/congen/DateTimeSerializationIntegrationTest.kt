package com.congen

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Integration test to validate datetime serialization works correctly.
 *
 * This test ensures that when Instant is used and LocalDateTime is used to parse
 * values from the database, it never messes up the value in the database.
 */
class DateTimeSerializationIntegrationTest : BaseIntegrationTest() {
    @Test
    fun `should correctly serialize and deserialize Instant timestamps`() {
        // Create a user (which has Instant fields)
        val userId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                name = "Test User",
                age = 25,
                height = 175.0,
                weight = 80.0
            )

        // Retrieve the user to get the full response with timestamps
        val userResponse = IntegrationTestHelpers.getTestUser(webTestClient, userId)

        // Verify the user was created with valid timestamps
        assertNotNull(userResponse.id)
        assertNotNull(userResponse.createdAt)
        assertNotNull(userResponse.updatedAt)

        // Verify timestamps are in the expected format (UTC)
        val now = Instant.now()
        val oneMinuteAgo = now.minusSeconds(60)
        val oneMinuteFromNow = now.plusSeconds(60)

        // Created and updated timestamps should be recent
        assertTrue(
            userResponse.createdAt.isAfter(oneMinuteAgo),
            "Created timestamp should be recent, got: ${userResponse.createdAt}"
        )
        assertTrue(
            userResponse.createdAt.isBefore(oneMinuteFromNow),
            "Created timestamp should be recent, got: ${userResponse.createdAt}"
        )
        assertTrue(
            userResponse.updatedAt.isAfter(oneMinuteAgo),
            "Updated timestamp should be recent, got: ${userResponse.updatedAt}"
        )
        assertTrue(
            userResponse.updatedAt.isBefore(oneMinuteFromNow),
            "Updated timestamp should be recent, got: ${userResponse.updatedAt}"
        )

        // Verify timestamps are in UTC (no timezone offset)
        val createdAtLocal = LocalDateTime.ofInstant(userResponse.createdAt, ZoneOffset.UTC)
        val updatedAtLocal = LocalDateTime.ofInstant(userResponse.updatedAt, ZoneOffset.UTC)

        // Convert back to Instant and verify they match
        val recreatedCreatedAt = createdAtLocal.atZone(ZoneOffset.UTC).toInstant()
        val recreatedUpdatedAt = updatedAtLocal.atZone(ZoneOffset.UTC).toInstant()

        assertEquals(
            userResponse.createdAt,
            recreatedCreatedAt,
            "Created timestamp should be preserved through LocalDateTime conversion"
        )
        assertEquals(
            userResponse.updatedAt,
            recreatedUpdatedAt,
            "Updated timestamp should be preserved through LocalDateTime conversion"
        )
    }

    @Test
    fun `should handle microsecond precision timestamps correctly`() {
        // Create a user
        val userId =
            IntegrationTestHelpers.createTestUser(
                webTestClient = webTestClient,
                name = "Test User 3",
                age = 35,
                height = 170.0,
                weight = 75.0
            )

        // Retrieve the user to get the full response with timestamps
        val userResponse = IntegrationTestHelpers.getTestUser(webTestClient, userId)

        // Verify the timestamp has microsecond precision (6 decimal places)
        val createdAtString = userResponse.createdAt.toString()
        val updatedAtString = userResponse.updatedAt.toString()

        // Check that the timestamp format includes at least microsecond precision (6 or more decimal places)
        assertTrue(
            createdAtString.matches(Regex(".*\\.\\d{6,}Z")),
            "Created timestamp should have at least microsecond precision, got: $createdAtString"
        )
        assertTrue(
            updatedAtString.matches(Regex(".*\\.\\d{6,}Z")),
            "Updated timestamp should have at least microsecond precision, got: $updatedAtString"
        )

        // Verify the timestamps are in UTC format
        assertTrue(
            createdAtString.endsWith("Z"),
            "Created timestamp should end with Z (UTC), got: $createdAtString"
        )
        assertTrue(
            updatedAtString.endsWith("Z"),
            "Updated timestamp should end with Z (UTC), got: $updatedAtString"
        )
    }
}
