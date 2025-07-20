# Congen API Documentation

Welcome to the Congen API documentation. This directory contains automatically generated documentation for the Conjugate Workout Generator API.

## Files

- **[API Documentation](API_DOCUMENTATION.md)** - Comprehensive API reference
- **[Weight Estimation Algorithms](WEIGHT_ESTIMATION_ALGORITHMS.md)** - 1RM calculator, exercise matching, and reference exercise detection
- **[OpenAPI JSON](openapi.json)** - OpenAPI specification in JSON format
- **[OpenAPI YAML](openapi.yaml)** - OpenAPI specification in YAML format

## Interactive Documentation

When the application is running, you can access the interactive Swagger UI at:
- http://localhost:8080/swagger-ui.html

## Regenerating Documentation

To regenerate this documentation, run:

```bash
./scripts/generate-api-docs.sh
```

This script will:
1. Start the application
2. Generate OpenAPI specifications
3. Create markdown documentation
4. Stop the application

## Manual Generation

If you prefer to generate documentation manually:

1. Start the application: `./gradlew bootRun`
2. Access the OpenAPI JSON: http://localhost:8080/api-docs
3. Access the Swagger UI: http://localhost:8080/swagger-ui.html

---

*Documentation generated on $(date)*
