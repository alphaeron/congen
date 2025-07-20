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

**Note:** All files in `docs/` are ignored by git (see .gitignore).

### generate-schema-dot.sh

Automatically generates a Graphviz DOT file from Liquibase migrations by:
1. Starting a temporary PostgreSQL container
2. Running all Liquibase migrations
3. Using SchemaSpy to extract the complete database schema
4. Converting SchemaSpy output to DOT format with tables, relationships, and domain grouping
5. Optionally generating a PNG image

**Prerequisites:**
- Docker
- PostgreSQL client (psql)
- Java (for SchemaSpy)
- Graphviz (optional, for PNG generation)

**Installation:**
```bash
# Install Graphviz (optional, for PNG generation)
brew install graphviz
```

**Note:** The script will automatically download the latest SchemaSpy JAR file on first run (with user confirmation). Currently detects version 6.2.4.

**Usage:**
```bash
./scripts/generate-schema-dot.sh
```

**Output:**
- `database_schema_auto.dot` — Graphviz DOT file
- `database_schema_auto.png` — PNG image (if Graphviz is installed)

**Features:**
- Uses industry-standard SchemaSpy for reliable schema extraction
- Automatically groups tables by domain (user, exercise, program, sets)
- Color-codes tables by domain
- Shows all foreign key relationships
- Organizes tables in subgraphs
- Extracts actual schema from running migrations
- Requires minimal script maintenance (leverages SchemaSpy's robust parsing)

## Gradle Documentation Tasks

### API Documentation
- `./gradlew generateApiDocs` — Runs the script above and generates all API docs
- `./gradlew generateOpenApiJson` — Only OpenAPI JSON
- `./gradlew generateOpenApiYaml` — Only OpenAPI YAML
- `./gradlew serveDocs` — Serve docs/ locally at http://localhost:8000
- `./gradlew cleanDocs` — Remove generated docs

### Database Schema Documentation
- `./gradlew generateSchemaDot` — Generate DOT file from Liquibase migrations

### KDoc (Dokka) Documentation
- `./gradlew dokkaHtml` — Generates KDoc HTML documentation for all Kotlin code
- Output is in `build/dokka/` (also ignored by git)

## Adding New Scripts
- Place new scripts in this directory
- Add a section here describing their purpose and usage
