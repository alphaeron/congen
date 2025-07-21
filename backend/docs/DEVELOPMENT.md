# Development Documentation Guide

This guide explains the documentation standards and practices for the Congen project.

## Overview

The Congen project uses automatic documentation generation to ensure that API documentation is always up-to-date with the source code. This eliminates the need for manual documentation maintenance and reduces the risk of documentation becoming stale.

## Documentation Architecture

### Automatic Documentation Generation

The project uses **SpringDoc OpenAPI** to automatically generate API documentation from:

1. **Controller Annotations** - OpenAPI annotations on REST endpoints
2. **Model Annotations** - Schema annotations on data classes
3. **Configuration** - OpenAPI configuration class

### Documentation Types

1. **Interactive Documentation** - Swagger UI for testing APIs
2. **OpenAPI Specifications** - JSON and YAML formats for tooling
3. **Markdown Documentation** - Human-readable API reference
4. **KDoc Documentation** - Kotlin source code documentation

## Documentation Standards

### KDoc Documentation

All public classes, functions, and properties should be documented using KDoc comments.

#### Class Documentation

```kotlin
/**
 * Brief description of the class.
 *
 * Detailed description explaining the purpose, usage, and important details
 * about the class. This can span multiple paragraphs and include examples.
 *
 * ## Usage
 *
 * ```kotlin
 * val instance = MyClass()
 * instance.doSomething()
 * ```
 *
 * ## Thread Safety
 *
 * This class is thread-safe and can be used concurrently.
 *
 * @property propertyName Description of the property
 * @param paramName Description of the parameter
 * @return Description of the return value
 * @throws ExceptionType Description of when this exception is thrown
 *
 * @author Developer Name
 * @since 1.0.0
 */
```

#### Function Documentation

```kotlin
/**
 * Brief description of what the function does.
 *
 * Detailed explanation of the function's behavior, parameters, return values,
 * and any side effects. Include examples for complex functions.
 *
 * @param paramName Description of the parameter
 * @return Description of the return value
 * @throws ExceptionType Description of when this exception is thrown
 *
 * @example
 * ```kotlin
 * val result = myFunction("input")
 * println(result) // Output: processed input
 * ```
 */
```

### OpenAPI Annotations

#### Controller Documentation

```kotlin
@RestController
@RequestMapping("/api")
@Tag(
    name = "Resource Name",
    description = "Operations for managing resource"
)
class MyController {

    @PostMapping("/")
    @Operation(
        summary = "Create resource",
        description = "Creates a new resource with the provided data"
    )
    @ApiResponses(
        value = [
            ApiResponse(
                responseCode = "200",
                description = "Resource created successfully",
                content = [
                    Content(
                        mediaType = "application/json",
                        schema = Schema(implementation = MyModel::class)
                    )
                ]
            ),
            ApiResponse(
                responseCode = "422",
                description = "Validation error",
                content = [
                    Content(
                        mediaType = "application/json",
                        examples = [
                            ExampleObject(
                                name = "Validation Error",
                                value = """
                                {
                                  "error": "Validation failed",
                                  "message": "Invalid input"
                                }
                                """.trimIndent()
                            )
                        ]
                    )
                ]
            )
        ]
    )
    fun createResource(
        @Parameter(
            description = "Resource data to create",
            required = true
        )
        @RequestBody resource: MyModel
    ): ResponseEntity<*> {
        // Implementation
    }
}
```

#### Model Documentation

```kotlin
@Schema(
    description = "Description of the model",
    example = """
    {
      "id": 1,
      "name": "Example",
      "created_at": "2024-01-01T00:00:00Z"
    }
    """.trimIndent()
)
data class MyModel(
    @Schema(
        description = "Unique identifier",
        example = "1",
        readOnly = true
    )
    val id: Int? = null,

    @Schema(
        description = "Resource name",
        example = "Example Name",
        minLength = 1,
        maxLength = 255
    )
    val name: String,

    @Schema(
        description = "Creation timestamp",
        example = "2024-01-01T00:00:00Z",
        readOnly = true
    )
    val createdAt: LocalDateTime? = null
)
```

## Documentation Generation

### Local Development

To generate documentation locally:

```bash
# Generate all documentation
./gradlew generateApiDocs

# Generate only OpenAPI JSON
./gradlew generateOpenApiJson

# Generate only OpenAPI YAML
./gradlew generateOpenApiYaml

# Serve documentation locally
./gradlew serveDocs
```

### Using the Script

```bash
# Generate complete documentation
./scripts/generate-api-docs.sh
```

The script will:
1. Build the application
2. Start the application
3. Generate OpenAPI specifications
4. Create markdown documentation
5. Stop the application

### Manual Generation

If you prefer manual generation:

1. Start the application: `./gradlew bootRun`
2. Access OpenAPI JSON: http://localhost:8080/api-docs
3. Access Swagger UI: http://localhost:8080/swagger-ui.html

## Documentation Structure

```
docs/
├── README.md                 # Documentation index
├── API_DOCUMENTATION.md      # Generated API reference
├── openapi.json             # OpenAPI specification (JSON)
├── openapi.yaml             # OpenAPI specification (YAML)
├── swagger-ui/              # Static Swagger UI files
└── DEVELOPMENT.md           # This file
```

## Best Practices

### 1. Keep Documentation Close to Code

Documentation should be written alongside the code it describes. This ensures that documentation stays in sync with implementation.

### 2. Use Descriptive Names

Choose clear, descriptive names for classes, functions, and parameters. Good naming reduces the need for extensive documentation.

### 3. Include Examples

Provide examples in documentation, especially for complex APIs or validation rules.

### 4. Document Error Cases

Always document what happens when things go wrong, including validation errors and exceptions.

### 5. Keep Documentation Updated

When changing code, update the corresponding documentation immediately.

### 6. Use Consistent Formatting

Follow consistent formatting patterns for all documentation to improve readability.

## Validation Rules Documentation

When adding new validation rules, document them in both the validation function and the API documentation:

```kotlin
/**
 * Validates user input according to business rules.
 *
 * ## Validation Rules
 *
 * - **Name**: Required, non-empty string, max 255 characters
 * - **Age**: Required, integer between 1 and 150
 * - **Email**: Required, valid email format
 *
 * @param user The user data to validate
 * @throws ValidationException if validation fails
 */
fun validateUser(user: User) {
    // Implementation
}
```

## API Versioning

When making breaking changes to the API:

1. **Version the API** using URL paths (e.g., `/api/v2/`)
2. **Document the changes** in the changelog
3. **Maintain backward compatibility** when possible
4. **Update examples** to reflect the new API

## Testing Documentation

Documentation should be tested to ensure accuracy:

1. **Test examples** in documentation
2. **Validate OpenAPI specifications** using tools
3. **Check generated documentation** for completeness
4. **Verify links and references** work correctly

## Testing Best Practices

The Congen project uses a layered testing strategy:

### 1. Unit Tests
- **Location:** `src/test/kotlin/`
- **Purpose:** Test individual components (controllers, services, utilities) in isolation.
- **Frameworks:** JUnit 5, Mockito, Kotlin Test
- **Guidelines:**
  - Mock all external dependencies (database, network, etc.)
  - Cover all validation and business logic branches
  - Use descriptive test names and group related tests in classes

### 2. Integration Tests
- **Location:** `src/integrationTest/kotlin/`
- **Purpose:** Test the full application stack with a real PostgreSQL database.
- **Frameworks:** JUnit 5, Testcontainers
- **Guidelines:**
  - Use Testcontainers to spin up a PostgreSQL instance for tests
  - Test real HTTP endpoints and database interactions
  - Clean up test data between runs to avoid state leakage
  - Use unique data per test to avoid conflicts

### 3. Running Tests
- **Unit tests:**
  ```bash
  ./gradlew test
  ```
- **Integration tests:**
  ```bash
  ./gradlew integrationTest
  ```
- **All tests:**
  ```bash
  ./gradlew check
  ```

### 4. Best Practices
- Write tests for all new features and bug fixes
- Keep tests fast and isolated
- Use clear, descriptive assertions
- Prefer integration tests for end-to-end flows
- Use unit tests for validation and logic
- Review test coverage regularly

---

For more, see the test source directories and the main README.

## Tools and Resources

### Documentation Tools

- **SpringDoc OpenAPI**: Automatic API documentation
- **KDoc**: Kotlin documentation format
- **Swagger UI**: Interactive API testing
- **Gradle**: Build and documentation tasks

### Validation Tools

- **jq**: JSON processing and validation
- **yq**: YAML processing and validation
- **swagger-cli**: OpenAPI specification validation

### IDE Support

- **IntelliJ IDEA**: KDoc rendering and validation
- **VS Code**: Markdown preview and validation
- **Swagger Editor**: OpenAPI specification editing

## Troubleshooting

### Common Issues

1. **Documentation not generating**: Check if application starts successfully
2. **Missing endpoints**: Verify OpenAPI annotations are correct
3. **Invalid JSON**: Check for syntax errors in annotations
4. **Missing examples**: Ensure example values are valid JSON

### Debugging

1. Check application logs for errors
2. Verify OpenAPI endpoints are accessible
3. Validate generated JSON/YAML manually
4. Test Swagger UI functionality

## Contributing

When contributing to documentation:

1. Follow the established patterns
2. Test your changes locally
3. Update related documentation
4. Include examples where helpful
5. Validate generated documentation

## Resources

- [KDoc Documentation](https://kotlinlang.org/docs/kotlin-doc.html)
- [SpringDoc OpenAPI](https://springdoc.org/)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Swagger UI](https://swagger.io/tools/swagger-ui/)
