package com.congen

import com.congen.model.User
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
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
        val userResponse = webTestClient.post()
            .uri("/user/?name=Test User&age=25&height=175.0&weight=80.0")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User::class.java)
            .returnResult()
            .responseBody!!

        // Verify the user was created with valid timestamps
        assertNotNull(userResponse.id)
        assertNotNull(userResponse.createdAt)
        assertNotNull(userResponse.updatedAt)
        
        // Verify timestamps are in the expected format (UTC)
        val now = Instant.now()
        val oneMinuteAgo = now.minusSeconds(60)
        val oneMinuteFromNow = now.plusSeconds(60)
        
        // Created and updated timestamps should be recent
        assertTrue(userResponse.createdAt.isAfter(oneMinuteAgo), 
            "Created timestamp should be recent, got: ${userResponse.createdAt}")
        assertTrue(userResponse.createdAt.isBefore(oneMinuteFromNow), 
            "Created timestamp should be recent, got: ${userResponse.createdAt}")
        assertTrue(userResponse.updatedAt.isAfter(oneMinuteAgo), 
            "Updated timestamp should be recent, got: ${userResponse.updatedAt}")
        assertTrue(userResponse.updatedAt.isBefore(oneMinuteFromNow), 
            "Updated timestamp should be recent, got: ${userResponse.updatedAt}")
        
        // Verify timestamps are in UTC (no timezone offset)
        val createdAtLocal = LocalDateTime.ofInstant(userResponse.createdAt, ZoneOffset.UTC)
        val updatedAtLocal = LocalDateTime.ofInstant(userResponse.updatedAt, ZoneOffset.UTC)
        
        // Convert back to Instant and verify they match
        val recreatedCreatedAt = createdAtLocal.atZone(ZoneOffset.UTC).toInstant()
        val recreatedUpdatedAt = updatedAtLocal.atZone(ZoneOffset.UTC).toInstant()
        
        assertEquals(userResponse.createdAt, recreatedCreatedAt, 
            "Created timestamp should be preserved through LocalDateTime conversion")
        assertEquals(userResponse.updatedAt, recreatedUpdatedAt, 
            "Updated timestamp should be preserved through LocalDateTime conversion")
    }

    @Test
    fun `should handle timestamps without timezone information correctly`() {
        // Create a user and immediately retrieve it to test round-trip serialization
        val userResponse = webTestClient.post()
            .uri("/user/?name=Test User 2&age=30&height=180.0&weight=85.0")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User::class.java)
            .returnResult()
            .responseBody!!

        // Retrieve the same user by ID to test deserialization
        val retrievedUser = webTestClient.get()
            .uri("/user/${userResponse.id}")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User::class.java)
            .returnResult()
            .responseBody!!

        // Verify the timestamps are preserved exactly
        assertEquals(userResponse.createdAt, retrievedUser.createdAt, 
            "Created timestamp should be preserved through database round-trip")
        assertEquals(userResponse.updatedAt, retrievedUser.updatedAt, 
            "Updated timestamp should be preserved through database round-trip")
        
        // Verify the timestamps are valid Instant objects
        assertNotNull(retrievedUser.createdAt)
        assertNotNull(retrievedUser.updatedAt)
        
        // Verify they can be converted to LocalDateTime and back without loss
        val createdLocal = LocalDateTime.ofInstant(retrievedUser.createdAt, ZoneOffset.UTC)
        val updatedLocal = LocalDateTime.ofInstant(retrievedUser.updatedAt, ZoneOffset.UTC)
        
        val recreatedCreated = createdLocal.atZone(ZoneOffset.UTC).toInstant()
        val recreatedUpdated = updatedLocal.atZone(ZoneOffset.UTC).toInstant()
        
        assertEquals(retrievedUser.createdAt, recreatedCreated, 
            "Created timestamp should be preserved through LocalDateTime conversion")
        assertEquals(retrievedUser.updatedAt, recreatedUpdated, 
            "Updated timestamp should be preserved through LocalDateTime conversion")
    }

    @Test
    fun `should handle microsecond precision timestamps correctly`() {
        // Create a user
        val userResponse = webTestClient.post()
            .uri("/user/?name=Test User 3&age=35&height=170.0&weight=75.0")
            .exchange()
            .expectStatus().isOk()
            .expectBody(User::class.java)
            .returnResult()
            .responseBody!!

        // Verify the timestamp has microsecond precision (6 decimal places)
        val createdAtString = userResponse.createdAt.toString()
        val updatedAtString = userResponse.updatedAt.toString()
        
        // Check that the timestamp format includes at least microsecond precision (6 or more decimal places)
        assertTrue(createdAtString.matches(Regex(".*\\.\\d{6,}Z")), 
            "Created timestamp should have at least microsecond precision, got: $createdAtString")
        assertTrue(updatedAtString.matches(Regex(".*\\.\\d{6,}Z")), 
            "Updated timestamp should have at least microsecond precision, got: $updatedAtString")
        
        // Verify the timestamps are in UTC format
        assertTrue(createdAtString.endsWith("Z"), 
            "Created timestamp should end with Z (UTC), got: $createdAtString")
        assertTrue(updatedAtString.endsWith("Z"), 
            "Updated timestamp should end with Z (UTC), got: $updatedAtString")
    }
} 