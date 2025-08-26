package com.congen.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * OpenAPI configuration for automatic API documentation generation.
 *
 * This configuration sets up Swagger/OpenAPI documentation that is automatically
 * generated from the Spring Boot controllers and models. The documentation
 * includes:
 *
 * - API endpoints with request/response schemas
 * - Model definitions with validation rules
 * - Interactive API testing interface
 * - Server information and contact details
 *
 * @param openApiProps The OpenAPI properties loaded from configuration.
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
/**
 * Configuration properties for OpenAPI documentation.
 *
 * This class contains configuration properties for OpenAPI documentation generation.
 *
 * @param serverPort The port on which the server is running
 * @param activeProfile The active Spring profile (e.g., local, staging, production)
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "openapi")
data class OpenApiProperties(
    /**
     * The port on which the server is running.
     */
    var serverPort: String = "8888",
    /**
     * The active Spring profile (e.g., local, staging, production).
     */
    var activeProfile: String = "default"
)

/**
 * OpenAPI configuration for automatic API documentation generation.
 *
 * This configuration sets up Swagger/OpenAPI documentation that is automatically
 * generated from the Spring Boot controllers and models. The documentation
 * includes:
 *
 * - API endpoints with request/response schemas
 * - Model definitions with validation rules
 * - Interactive API testing interface
 * - Server information and contact details
 *
 * @param openApiProps The OpenAPI properties loaded from configuration
 *
 * @author Congen Development Team
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(OpenApiProperties::class)
class OpenApiConfig(
    private val openApiProps: OpenApiProperties
) {
    /**
     * Configures the OpenAPI specification for the application.
     *
     * This bean defines the overall API documentation including:
     * - API metadata (title, description, version)
     * - Contact information
     * - License details
     * - Server configurations for different environments
     *
     * @return Configured OpenAPI specification
     */
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("Congen API")
                    .description(
                        """
                        # Conjugate Workout Generator API

                        A comprehensive REST API for managing workout programs, exercises, users, and preferences
                        using the conjugate method. This API provides endpoints for creating personalized
                        workout programs based on user preferences and available equipment.

                        ## Key Features

                        - **User Management**: Create and manage user profiles with fitness preferences
                        - **Exercise Library**: Comprehensive database of exercises with equipment and muscle targeting
                        - **Program Generation**: Generate personalized workout programs using the conjugate method
                        - **Preference Management**: Store and retrieve user preferences for programs and exercises
                        - **Equipment Tracking**: Manage available equipment for users

                        ## API Versioning

                        This API uses versioned endpoints with the base path `/api/v1/`. All endpoints
                        are automatically prefixed with this version path to ensure backward compatibility
                        and clear API versioning.

                        ## Authentication

                        Currently, the API does not require authentication. All endpoints are publicly accessible.

                        ## Rate Limiting

                        API requests are rate-limited to prevent DDoS attacks and ensure fair usage:
                        - **IP-based limits**: 100 requests per minute per IP address
                        - **User-based limits**: 50 requests per minute per authenticated user
                        - **Payload limits**: Maximum 1MB request size
                        - **Request timeouts**: 10-second request timeout

                        Please respect the rate limits and implement appropriate retry logic with exponential backoff.

                        ## Error Handling

                        The API uses standard HTTP status codes and returns detailed error messages
                        in JSON format for validation and processing errors.

                        ## Data Validation

                        All input data is validated according to business rules:
                        - Program days per week: 2, 3, or 4 days
                        - Exercise parameters: Valid ranges for reps, sets, weights, etc.

                        For detailed validation rules, see the individual endpoint documentation.
                        """.trimIndent(),
                    )
                    .version("1.0.0")
                    .contact(
                        Contact()
                            .name("Congen Development Team")
                            .email("support@congen.com")
                            .url("https://github.com/congen/congen"),
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT"),
                    ),
            )
            .servers(
                listOf(
                    Server()
                        .url("http://localhost:${openApiProps.serverPort}/api/v1")
                        .description("Local Development Server"),
                    Server()
                        .url("https://api.congen.com/api/v1")
                        .description("Production Server"),
                ),
            )
    }
}
