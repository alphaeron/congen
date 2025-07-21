# Integration Tests

This directory contains integration tests that require a Kubernetes environment to run.

## Prerequisites

Before running integration tests, ensure you have the following resources available:

1. **Minikube** - Required to run Kubernetes cluster locally
2. **kubectl** - Kubernetes command-line tool
3. **Kubernetes Cluster** - The integration tests require a running Kubernetes cluster with PostgreSQL

## Running Integration Tests

### 1. Start Required Resources

Start Minikube and ensure it's running:

```bash
minikube start
```

### 2. Run Integration Tests

Execute the integration tests with:

```bash
./gradlew integrationTest
```

The integration test task will automatically:
- Set up the Kubernetes environment with PostgreSQL
- Run the integration tests
- Clean up the test environment

### 3. Manual Cleanup (if needed)

If you need to manually clean up the test environment:

```bash
./gradlew cleanupKubernetesTestEnv
```

## Available Gradle Tasks

- `./gradlew test` - Run unit tests only
- `./gradlew integrationTest` - Run integration tests only
- `./gradlew checkKubernetesTestEnv` - Check Kubernetes test environment status
- `./gradlew cleanupKubernetesTestEnv` - Clean up Kubernetes test environment

## Test Structure

Integration tests are located in `src/integrationTest/kotlin/com/congen/` and include:

- `HealthCheckIntegrationTest.kt` - Tests the health check endpoint with real database connectivity
- `CongenApplicationTests.kt` - Full application integration tests
- Various controller integration tests that test the full application stack

## Configuration

Integration tests use the `application-integration-test.properties` configuration file located in `src/integrationTest/resources/` with the `integration-test` Spring profile.

## Notes

- Integration tests automatically set up and tear down the Kubernetes environment
- These tests may take longer to run than unit tests due to external dependencies
- The tests run against a real PostgreSQL database in Kubernetes 