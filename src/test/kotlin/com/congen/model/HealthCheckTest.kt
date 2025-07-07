package com.congen.model

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HealthCheckTest {
    @Test
    fun `HealthStatus enum should have correct values`() {
        assertEquals("pass", HealthStatus.PASS.value)
        assertEquals("warn", HealthStatus.WARN.value)
        assertEquals("fail", HealthStatus.FAIL.value)
    }

    @Test
    fun `HealthStatus toValue should return correct string`() {
        assertEquals("pass", HealthStatus.PASS.toValue())
        assertEquals("warn", HealthStatus.WARN.toValue())
        assertEquals("fail", HealthStatus.FAIL.toValue())
    }

    @Test
    fun `HealthCheck should be created with required fields`() {
        val healthCheck =
            HealthCheck(
                componentId = "database",
                componentType = "datastore",
                status = HealthStatus.PASS
            )

        assertEquals("database", healthCheck.componentId)
        assertEquals("datastore", healthCheck.componentType)
        assertEquals(HealthStatus.PASS, healthCheck.status)
        assertNull(healthCheck.observedValue)
        assertNull(healthCheck.observedUnit)
        assertTrue(healthCheck.affectedEndpoints.isEmpty())
        assertNotNull(healthCheck.time)
        assertNull(healthCheck.output)
        assertTrue(healthCheck.links.isEmpty())
    }

    @Test
    fun `HealthCheck should be created with all fields`() {
        val time = Instant.parse("2024-01-01T00:00:00Z")
        val healthCheck =
            HealthCheck(
                componentId = "database",
                componentType = "datastore",
                observedValue = 150L,
                observedUnit = "ms",
                status = HealthStatus.PASS,
                affectedEndpoints = listOf("/health", "/api/v1/users"),
                time = time,
                output = "Database connection successful",
                links = mapOf("self" to "/health/database")
            )

        assertEquals("database", healthCheck.componentId)
        assertEquals("datastore", healthCheck.componentType)
        assertEquals(150L, healthCheck.observedValue)
        assertEquals("ms", healthCheck.observedUnit)
        assertEquals(HealthStatus.PASS, healthCheck.status)
        assertEquals(2, healthCheck.affectedEndpoints.size)
        assertEquals("/health", healthCheck.affectedEndpoints[0])
        assertEquals("/api/v1/users", healthCheck.affectedEndpoints[1])
        assertEquals(time, healthCheck.time)
        assertEquals("Database connection successful", healthCheck.output)
        assertEquals(1, healthCheck.links.size)
        assertEquals("/health/database", healthCheck.links["self"])
    }

    @Test
    fun `HealthCheck should use default values when optional fields not provided`() {
        val healthCheck = HealthCheck(status = HealthStatus.FAIL)

        assertNull(healthCheck.componentId)
        assertNull(healthCheck.componentType)
        assertNull(healthCheck.observedValue)
        assertNull(healthCheck.observedUnit)
        assertEquals(HealthStatus.FAIL, healthCheck.status)
        assertTrue(healthCheck.affectedEndpoints.isEmpty())
        assertNotNull(healthCheck.time)
        assertNull(healthCheck.output)
        assertTrue(healthCheck.links.isEmpty())
    }

    @Test
    fun `DatabaseHealthCheck should be created with required fields`() {
        val dbHealthCheck = DatabaseHealthCheck(status = HealthStatus.PASS)

        assertEquals(HealthStatus.PASS, dbHealthCheck.status)
        assertNull(dbHealthCheck.responseTime)
        assertNull(dbHealthCheck.error)
        assertTrue(dbHealthCheck.details.isEmpty())
    }

    @Test
    fun `DatabaseHealthCheck should be created with all fields`() {
        val dbHealthCheck =
            DatabaseHealthCheck(
                status = HealthStatus.WARN,
                responseTime = 200L,
                error = "Connection timeout",
                details = mapOf("connections" to 5, "maxConnections" to 10)
            )

        assertEquals(HealthStatus.WARN, dbHealthCheck.status)
        assertEquals(200L, dbHealthCheck.responseTime)
        assertEquals("Connection timeout", dbHealthCheck.error)
        assertEquals(2, dbHealthCheck.details.size)
        assertEquals(5, dbHealthCheck.details["connections"])
        assertEquals(10, dbHealthCheck.details["maxConnections"])
    }

    @Test
    fun `HealthCheckResponse should be created with required fields`() {
        val response =
            HealthCheckResponse(
                status = HealthStatus.PASS,
                version = "1.0.0",
                releaseId = "v1.0.0"
            )

        assertEquals(HealthStatus.PASS, response.status)
        assertEquals("1.0.0", response.version)
        assertEquals("v1.0.0", response.releaseId)
        assertTrue(response.notes.isEmpty())
        assertNull(response.output)
        assertTrue(response.checks.isEmpty())
        assertTrue(response.links.isEmpty())
        assertEquals("congen", response.serviceId)
        assertEquals("Congen Exercise API Health Check", response.description)
    }

    @Test
    fun `HealthCheckResponse should be created with all fields`() {
        val checks =
            mapOf(
                "database" to
                    listOf(
                        HealthCheck(
                            componentId = "database",
                            componentType = "datastore",
                            status = HealthStatus.PASS
                        )
                    )
            )

        val response =
            HealthCheckResponse(
                status = HealthStatus.PASS,
                version = "1.0.0",
                releaseId = "v1.0.0",
                notes = listOf("All systems operational"),
                output = "Health check completed successfully",
                checks = checks,
                links = mapOf("self" to "/health"),
                serviceId = "custom-service",
                description = "Custom health check description"
            )

        assertEquals(HealthStatus.PASS, response.status)
        assertEquals("1.0.0", response.version)
        assertEquals("v1.0.0", response.releaseId)
        assertEquals(1, response.notes.size)
        assertEquals("All systems operational", response.notes[0])
        assertEquals("Health check completed successfully", response.output)
        assertEquals(1, response.checks.size)
        assertEquals(1, response.checks["database"]?.size)
        assertEquals("database", response.checks["database"]?.get(0)?.componentId)
        assertEquals(1, response.links.size)
        assertEquals("/health", response.links["self"])
        assertEquals("custom-service", response.serviceId)
        assertEquals("Custom health check description", response.description)
    }

    @Test
    fun `HealthCheckResponse should use default values when optional fields not provided`() {
        val response =
            HealthCheckResponse(
                status = HealthStatus.FAIL,
                version = "1.0.0",
                releaseId = "v1.0.0"
            )

        assertEquals(HealthStatus.FAIL, response.status)
        assertEquals("1.0.0", response.version)
        assertEquals("v1.0.0", response.releaseId)
        assertTrue(response.notes.isEmpty())
        assertNull(response.output)
        assertTrue(response.checks.isEmpty())
        assertTrue(response.links.isEmpty())
        assertEquals("congen", response.serviceId)
        assertEquals("Congen Exercise API Health Check", response.description)
    }

    @Test
    fun `HealthCheck should handle different observed value types`() {
        val healthCheckLong = HealthCheck(status = HealthStatus.PASS, observedValue = 123L)
        val healthCheckDouble = HealthCheck(status = HealthStatus.PASS, observedValue = 123.45)
        val healthCheckString = HealthCheck(status = HealthStatus.PASS, observedValue = "OK")
        val healthCheckNull = HealthCheck(status = HealthStatus.PASS)

        assertEquals(123L, healthCheckLong.observedValue)
        assertEquals(123.45, healthCheckDouble.observedValue)
        assertEquals("OK", healthCheckString.observedValue)
        assertNull(healthCheckNull.observedValue)
    }

    @Test
    fun `DatabaseHealthCheck should handle different detail value types`() {
        val details =
            mapOf(
                "string" to "value",
                "number" to 42,
                "decimal" to BigDecimal("123.45"),
                "boolean" to true
            )

        val dbHealthCheck =
            DatabaseHealthCheck(
                status = HealthStatus.PASS,
                details = details
            )

        assertEquals(4, dbHealthCheck.details.size)
        assertEquals("value", dbHealthCheck.details["string"])
        assertEquals(42, dbHealthCheck.details["number"])
        assertEquals(BigDecimal("123.45"), dbHealthCheck.details["decimal"])
        assertEquals(true, dbHealthCheck.details["boolean"])
    }
}
