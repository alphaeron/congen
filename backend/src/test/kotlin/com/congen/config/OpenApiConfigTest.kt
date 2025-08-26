package com.congen.config

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Unit tests for [OpenApiConfig].
 *
 * Tests cover all functionality including:
 * - OpenAPI bean creation
 * - API metadata configuration
 * - Contact information setup
 * - License configuration
 * - Server configurations for different environments
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
class OpenApiConfigTest {
    private lateinit var openApiConfig: OpenApiConfig

    @BeforeEach
    fun setUp() {
        val openApiProps =
            OpenApiProperties(
                serverPort = "8888",
                activeProfile = "test"
            )
        openApiConfig = OpenApiConfig(openApiProps)
    }

    @Test
    fun `should create OpenAPI bean`() {
        val openAPI = openApiConfig.openAPI()

        assertNotNull(openAPI)
    }

    @Test
    fun `should configure API info correctly`() {
        val openAPI = openApiConfig.openAPI()
        val info = openAPI.info

        assertNotNull(info)
        assertEquals("Congen API", info.title)
        assertEquals("1.0.0", info.version)
        assertNotNull(info.description)
        assert(info.description!!.contains("Conjugate Workout Generator API"))
        assert(info.description!!.contains("User Management"))
        assert(info.description!!.contains("Exercise Library"))
        assert(info.description!!.contains("Program Generation"))
        assert(info.description!!.contains("Preference Management"))
        assert(info.description!!.contains("Equipment Tracking"))
        assert(info.description!!.contains("Authentication"))
        assert(info.description!!.contains("Rate Limiting"))
        assert(info.description!!.contains("Error Handling"))
        assert(info.description!!.contains("Data Validation"))
    }

    @Test
    fun `should configure contact information correctly`() {
        val openAPI = openApiConfig.openAPI()
        val contact = openAPI.info.contact

        assertNotNull(contact)
        assertEquals("Congen Development Team", contact.name)
        assertEquals("support@congen.com", contact.email)
        assertEquals("https://github.com/congen/congen", contact.url)
    }

    @Test
    fun `should configure license information correctly`() {
        val openAPI = openApiConfig.openAPI()
        val license = openAPI.info.license

        assertNotNull(license)
        assertEquals("MIT License", license.name)
        assertEquals("https://opensource.org/licenses/MIT", license.url)
    }

    @Test
    fun `should configure servers correctly`() {
        val openAPI = openApiConfig.openAPI()
        val servers = openAPI.servers

        assertNotNull(servers)
        assertEquals(2, servers.size)

        val localServer = servers[0]
        assertEquals("http://localhost:8888/api/v1", localServer.url)
        assertEquals("Local Development Server", localServer.description)

        val productionServer = servers[1]
        assertEquals("https://api.congen.com/api/v1", productionServer.url)
        assertEquals("Production Server", productionServer.description)
    }

    @Test
    fun `should handle different server ports`() {
        val openApiProps =
            OpenApiProperties(
                serverPort = "9090",
                activeProfile = "test"
            )
        val configWithDifferentPort = OpenApiConfig(openApiProps)

        val openAPI = configWithDifferentPort.openAPI()
        val servers = openAPI.servers

        assertEquals("http://localhost:9090/api/v1", servers[0].url)
    }

    @Test
    fun `should include all required API documentation sections`() {
        val openAPI = openApiConfig.openAPI()
        val description = openAPI.info.description

        assertNotNull(description)
        assert(description!!.contains("## Key Features"))
        assert(description.contains("## Authentication"))
        assert(description.contains("## Rate Limiting"))
        assert(description.contains("## Error Handling"))
        assert(description.contains("## Data Validation"))
    }

    @Test
    fun `should include validation rules in description`() {
        val openAPI = openApiConfig.openAPI()
        val description = openAPI.info.description

        assertNotNull(description)
        assert(description!!.contains("Program days per week: 2, 3, or 4 days"))
    }

    @Test
    fun `should include feature descriptions in API info`() {
        val openAPI = openApiConfig.openAPI()
        val description = openAPI.info.description

        assertNotNull(description)
        assert(description!!.contains("User Management"))
        assert(description.contains("Exercise Library"))
        assert(description.contains("Program Generation"))
        assert(description.contains("Preference Management"))
        assert(description.contains("Equipment Tracking"))
    }
}
