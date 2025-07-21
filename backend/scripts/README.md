# Scripts Documentation

This directory contains scripts and utilities for documentation and development automation.

## Scripts

### Kubernetes Deployment Scripts

#### start-local-access.sh / stop-local-access.sh
Set up or tear down port forwarding to access the application locally.

**Usage:**
```bash
./scripts/start-local-access.sh
./scripts/stop-local-access.sh
```

### Documentation Scripts

#### generate-api-docs.sh

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
- `docs/API_DOCUMENTATION.md` — Markdown API reference
- `docs/swagger-ui/` — Swagger UI assets

**Note:** Auto-generated files in `docs/` are ignored by git (see .gitignore). Manual documentation files are preserved and version controlled.

### generate-schema-dot.sh

Automatically generates a Graphviz DOT file from the database schema by:
1. Setting up port-forwarding to Kubernetes PostgreSQL
2. Using SchemaSpy to extract the complete database schema
3. Converting SchemaSpy output to DOT format with tables, relationships, and domain grouping
4. Optionally generating a PNG image

**Prerequisites:**
- kubectl (for Kubernetes access)
- PostgreSQL client (psql)
- Java (for SchemaSpy)
- Graphviz (optional, for PNG generation)
- Kubernetes environment with running PostgreSQL

**Installation:**
```bash
# Install Graphviz (optional, for PNG generation)
brew install graphviz
```

**Note:** The script will automatically download the latest SchemaSpy JAR file on first run (with user confirmation).

**Usage:**
```bash
./scripts/generate-schema-dot.sh
```

**Output:**
- `docs/database_schema.dot` — Graphviz DOT file
- `docs/database_schema.png` — PNG image (if Graphviz is installed)

**Features:**
- Uses industry-standard SchemaSpy for reliable schema extraction
- Automatically groups tables by domain (user, exercise, program, sets)
- Color-codes tables by domain
- Shows all foreign key relationships
- Organizes tables in subgraphs
- Extracts actual schema from the running Kubernetes PostgreSQL
- Requires minimal script maintenance (leverages SchemaSpy's robust parsing)
- Integrates with Kubernetes port-forwarding

## Gradle Documentation Tasks

### API Documentation
- `./gradlew generateApiDocs` — Generates all API documentation (OpenAPI JSON/YAML, markdown, index)
- `./gradlew cleanDocs` — Remove generated documentation files
- `./gradlew createDocsStructure` — Create documentation directory structure
- `./gradlew serveDocs` — Serve docs/ locally at http://localhost:8000

### Database Schema Documentation
- `./gradlew generateSchemaDot` — Generate DOT file from database schema (requires Kubernetes)

### KDoc (Dokka) Documentation
- `./gradlew dokkaHtml` — Generates KDoc HTML documentation for all Kotlin code
- `./gradlew dokkaGfm` — Generates KDoc in GitHub Flavored Markdown
- `./gradlew dokkaJavadoc` — Generates Javadoc-style documentation
- Output is in `build/dokka/` (also ignored by git)

### Documentation Features
- **Auto-Generated**: Markdown documentation is generated from OpenAPI specification
- **Manual Documentation Preserved**: Manual documentation files (CONJUGATE_WORKOUT_GENERATOR.md, etc.) are never deleted
- **Kubernetes Integration**: Uses port-forwarding to access the running application
- **Up-to-Date**: Always reflects the current API implementation
- **Interactive**: Links to Swagger UI for interactive exploration
- **Environment-Aware**: Automatically sets environment to 'local' for documentation tasks

## Adding New Scripts
- Place new scripts in this directory
- Add a section here describing their purpose and usage
