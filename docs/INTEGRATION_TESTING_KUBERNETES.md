# Integration Testing with Kubernetes

This document describes how to run integration tests for the application using Kubernetes and TestContainers.

## Overview

There are two main approaches for integration testing:

1. **TestContainers (Recommended for local/dev)**
2. **Kubernetes Cluster (for CI/CD or full-stack validation)**

---

## 1. TestContainers (Recommended)

- **Fastest feedback loop for local development**
- Runs PostgreSQL and other dependencies in Docker containers
- No need for a running Kubernetes cluster

### How to Run
```bash
./gradlew integrationTest
```
- Uses the `test` profile and TestContainers for the database.
- Cleans up after each test run.

---

## 2. Kubernetes Cluster Integration Testing

- Validates the application against a real Kubernetes deployment
- Useful for CI/CD and pre-deployment checks

### Prerequisites
- Minikube or other Kubernetes cluster running
- Application and database deployed to Kubernetes
- Port-forwarding enabled for database access

### How to Run
```bash
# Ensure Kubernetes is running and app is deployed
./scripts/setup-kubernetes.sh
./gradlew deployToKubernetes -Penvironment=local

# Port-forward PostgreSQL
kubectl port-forward -n congen service/postgres 5432:5432 &

# Run integration tests against Kubernetes
./gradlew integrationTest -Dspring.profiles.active=kubernetes-test
```
- Uses the `kubernetes-test` profile to connect to the real database.

---

## Test Profiles

- `test`: Default for TestContainers (local/dev)
- `kubernetes-test`: For connecting to a real PostgreSQL in Kubernetes

---

## Gradle Tasks

```bash
# Run unit tests
./gradlew test

# Run integration tests (TestContainers)
./gradlew integrationTest

# Run integration tests against Kubernetes
./gradlew integrationTest -Dspring.profiles.active=kubernetes-test

# Run all tests
./gradlew check
```

---

## Troubleshooting

- **Database connection issues:**
  - Ensure PostgreSQL is running in Kubernetes: `kubectl get pods -n congen -l app=postgres`
  - Check logs: `kubectl logs -n congen -l app=postgres`
  - Verify port-forward: `kubectl port-forward -n congen service/postgres 5432:5432`
- **TestContainers issues:**
  - Ensure Docker is running: `docker ps`
  - Clean up containers: `docker system prune -f`
- **Profile issues:**
  - Check active profile: `./gradlew integrationTest -Dspring.profiles.active=kubernetes-test --info`

---

## Best Practices

1. Use TestContainers for fast local feedback
2. Use Kubernetes integration tests for CI/CD and deployment validation
3. Clean up test data between runs
4. Use the correct profile for your environment

---

## Test Reports

- View test results:
  - `open build/reports/tests/test/index.html`
  - `open build/reports/tests/integrationTest/index.html`
- View coverage:
  - `./gradlew jacocoTestReport jacocoIntegrationTestReport`
  - `open build/reports/jacoco/test/html/index.html`
  - `open build/reports/jacoco/integrationTest/html/index.html` 