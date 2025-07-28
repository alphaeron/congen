# Congen API Documentation

This directory contains automatically generated API documentation for the Conjugate Workout Generator.

## Contents

- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Complete API reference with endpoints, models, and examples
- **[openapi.json](openapi.json)** - OpenAPI 3.0 specification (JSON)
- **[openapi.yaml](openapi.yaml)** - OpenAPI 3.0 specification (YAML)
- **[swagger-ui/](swagger-ui/)** - Static Swagger UI files

## Quick Access

- **Interactive API Docs**: http://localhost:8888/api/v1/swagger-ui.html (when running)
- **OpenAPI JSON**: http://localhost:8888/api/v1/api-docs
- **OpenAPI YAML**: http://localhost:8888/api/v1/api-docs.yaml

## Regeneration

```bash
# Using Gradle (recommended)
./gradlew generateApiDocs

# Using script directly
./scripts/generate-api-docs.sh
```

---

*Generated: $(date)*
