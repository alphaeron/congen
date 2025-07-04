# Scripts Documentation

This directory contains scripts and utilities for documentation and development automation.

## Scripts

### generate-api-docs.sh

Generates API documentation from the running application, including:
- OpenAPI JSON and YAML specs
- Markdown API reference
- Swagger UI assets

**Usage:**
```bash
./scripts/generate-api-docs.sh
```

**Output:**
- `docs/openapi.json` — OpenAPI JSON spec
- `docs/openapi.yaml` — OpenAPI YAML spec
- `docs/api-documentation.md` — Markdown API reference
- `docs/swagger-ui/` — Swagger UI assets

**Note:** All files in `docs/` are ignored by git (see .gitignore).

## Gradle Documentation Tasks

### API Documentation
- `./gradlew generateApiDocs` — Runs the script above and generates all API docs
- `./gradlew generateOpenApiJson` — Only OpenAPI JSON
- `./gradlew generateOpenApiYaml` — Only OpenAPI YAML
- `./gradlew serveDocs` — Serve docs/ locally at http://localhost:8000
- `./gradlew cleanDocs` — Remove generated docs

### KDoc (Dokka) Documentation
- `./gradlew dokkaHtml` — Generates KDoc HTML documentation for all Kotlin code
- Output is in `build/dokka/` (also ignored by git)

## Adding New Scripts
- Place new scripts in this directory
- Add a section here describing their purpose and usage

---

For more details, see the main project README and docs/DEVELOPMENT.md. 