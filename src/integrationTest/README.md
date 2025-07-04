# Integration Tests

This directory contains integration tests that require external resources to run.

## Prerequisites

Before running integration tests, ensure you have the following resources available:

1. **Docker and Docker Compose** - Required to run PostgreSQL database
2. **PostgreSQL Database** - The integration tests require a running PostgreSQL instance

## Running Integration Tests

### 1. Start Required Resources

Start the PostgreSQL database using Docker Compose:

```bash
docker-compose up -d postgres
```

Wait for PostgreSQL to be ready (check with `docker-compose ps`).

### 2. Run Integration Tests

Execute the integration tests with:

```bash
./gradlew integrationTest -PrunIntegrationTests
```

### 3. Clean Up

Stop the PostgreSQL database when finished:

```bash
docker-compose down
```

## Available Gradle Tasks

- `./gradlew test` - Run unit tests only
- `./gradlew integrationTest -PrunIntegrationTests` - Run integration tests only
- `./gradlew test integrationTest -PrunIntegrationTests` - Run both unit and integration tests

## Test Structure

Integration tests are located in `src/integrationTest/kotlin/com/congen/` and include:

- `HealthCheckIntegrationTest.kt` - Tests the health check endpoint with real database connectivity
- `CongenApplicationTests.kt` - Full application integration tests

## Notes

- Integration tests require the `-PrunIntegrationTests` property to execute (safety measure)
- These tests may take longer to run than unit tests due to external dependencies
- Ensure your database connection settings in `application.properties` match your local PostgreSQL setup 