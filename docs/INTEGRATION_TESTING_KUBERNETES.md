# Integration Testing with Kubernetes

This document describes how to run integration tests and generate documentation using the Kubernetes-based development environment.

## Overview

The Congen application uses a Kubernetes-based development environment with:
- **Local Kubernetes**: Minikube for local development
- **PostgreSQL**: Database running in Kubernetes
- **Application**: Spring Boot app deployed to Kubernetes
- **Port-forwarding**: For accessing services locally
- **Integration Tests**: Full-stack tests against the deployed application

## Prerequisites

### Required Tools
- **kubectl**: Kubernetes command-line tool (automatically used by Gradle tasks)
- **minikube**: Local Kubernetes cluster
- **Docker**: For building container images
- **Gradle**: For running tests and documentation generation

### Installation
```bash
# Install kubectl
brew install kubectl

# Install minikube
brew install minikube

# Install Docker Desktop
# Download from https://www.docker.com/products/docker-desktop
```

## Kubernetes Environment Setup

### Starting the Environment
```bash
# Start minikube with Docker driver
minikube start --driver=docker

# Enable required addons
minikube addons enable ingress
minikube addons enable metrics-server
```

### Deploying the Application
```bash
# Deploy to local Kubernetes environment
./gradlew deployToKubernetes -Penvironment=local
```

This command will:
1. Build the Docker image
2. Load it into minikube
3. Deploy all Kubernetes resources
4. Wait for PostgreSQL to be ready
5. Wait for the application to be ready

### Verifying Deployment
```bash
# Check pod status
kubectl get pods -n congen

# Check services
kubectl get services -n congen

# View application logs
kubectl logs -f deployment/congen -n congen
```

## Port-Forwarding

### Automatic Port-Forwarding
The Gradle tasks automatically handle port-forwarding:

- **Documentation Generation**: Uses `setupTestPortForward` and `cleanupPortForward` tasks
- **Integration Tests**: Automatically sets up port-forwarding to PostgreSQL (port 5432)
- **Application Access**: Port-forwarding to application service (port 8888)

### Manual Port-Forwarding
If you need to access services manually:

```bash
# Forward PostgreSQL and application (automated via Gradle task)
./gradlew setupTestPortForward

# Clean up port-forwarding
./gradlew cleanupPortForward
```

## Integration Testing

### Running Integration Tests
```bash
# Run all integration tests
./gradlew integrationTest

# Run with specific test
./gradlew integrationTest --tests "*HealthCheckIntegrationTest*"
```

### Integration Test Features
- **Full Stack Testing**: Tests against the actual deployed application
- **Database Integration**: Uses the real PostgreSQL database
- **Port-Forwarding**: Automatically manages port-forwarding
- **Cleanup**: Properly cleans up resources after tests

### Test Configuration
Integration tests use:
- **Profile**: `integration-test`
- **Database**: Kubernetes PostgreSQL instance
- **Application**: Deployed Spring Boot application
- **Port-Forwarding**: Automatic setup and cleanup

## Documentation Generation

### Available Documentation Tasks
```bash
# Generate all API documentation
./gradlew generateApiDocs

# Clean documentation files
./gradlew cleanDocs

# Create documentation structure
./gradlew createDocsStructure

# Generate database schema diagram
./gradlew generateSchemaDot
```

### Generated Documentation
The documentation generation creates:
- **OpenAPI JSON**: `docs/openapi.json`
- **OpenAPI YAML**: `docs/openapi.yaml`
- **API Documentation**: `docs/API_DOCUMENTATION.md` (auto-generated from OpenAPI)
- **Documentation Index**: `docs/README.md`
- **Database Schema**: `docs/database_schema.dot` and `docs/database_schema.png`

### Documentation Features
- **Auto-Generated**: Markdown documentation is generated from OpenAPI specification
- **Kubernetes Integration**: Uses port-forwarding to access the running application
- **Up-to-Date**: Always reflects the current API implementation
- **Interactive**: Links to Swagger UI for interactive exploration

## Development Workflow

### Typical Development Cycle
1. **Start Environment**:
   ```bash
   minikube start --driver=docker
   ./gradlew deployToKubernetes -Penvironment=local
   ```

2. **Run Tests**:
   ```bash
   ./gradlew integrationTest
   ```

3. **Generate Documentation**:
   ```bash
   ./gradlew generateApiDocs
   ```

4. **Access Application**:
   ```bash
   # Via port-forwarding
   kubectl port-forward -n congen service/congen 8888:8888
# Then visit http://localhost:8888/api/v1/swagger-ui.html
   
   # Via NodePort
   # Visit http://$(minikube ip):30080
   ```

### Environment Variables
The application uses these environment variables in Kubernetes:
- `SPRING_PROFILES_ACTIVE`: Set to `loc` for local development
- `PGWRITERHOST`: PostgreSQL service name (`postgres`)
- `PGREADERHOST`: PostgreSQL service name (`postgres`)
- `PGPORT`: PostgreSQL port (`5432`)

## Troubleshooting

### Common Issues

#### Port-Forwarding Problems
```bash
# Kill existing port-forward processes
pkill -f "kubectl port-forward"

# Check if ports are in use
lsof -i :5432
lsof -i :8888
```

#### Database Connection Issues
```bash
# Check PostgreSQL pod status
kubectl get pods -n congen -l app=postgres

# Check PostgreSQL logs
kubectl logs -n congen deployment/postgres

# Test database connection
kubectl exec -n congen deployment/postgres -- pg_isready -U postgres
```

#### Application Issues
```bash
# Check application pod status
kubectl get pods -n congen -l app=congen

# Check application logs
kubectl logs -f deployment/congen -n congen

# Check application health
kubectl exec -n congen deployment/congen -- curl -s http://localhost:8888/api/v1/health/
```

#### Minikube Issues
```bash
# Restart minikube
minikube stop
minikube start --driver=docker

# Reset minikube (destructive)
minikube delete
minikube start --driver=docker
```

### Debugging Commands
```bash
# Get detailed pod information
kubectl describe pod -n congen <pod-name>

# Check resource usage
kubectl top pods -n congen

# Check events
kubectl get events -n congen --sort-by='.lastTimestamp'

# Access pod shell
kubectl exec -it -n congen <pod-name> -- /bin/bash
```

## Best Practices

### Development
1. **Always use port-forwarding** for local development
2. **Run integration tests** before committing changes
3. **Generate documentation** after API changes
4. **Check pod logs** when debugging issues

### Testing
1. **Use the integration test profile** for full-stack testing
2. **Clean up resources** after tests complete
3. **Verify database state** before and after tests
4. **Use proper test isolation** to avoid conflicts

### Documentation
1. **Regenerate documentation** after API changes
2. **Review generated documentation** for accuracy
3. **Use interactive Swagger UI** for API exploration
4. **Keep documentation up-to-date** with code changes

## Architecture

### Kubernetes Resources
- **Namespace**: `congen`
- **Services**: 
  - `congen`: Application service (port 8888)
  - `postgres`: Database service (port 5432)
- **Deployments**:
  - `congen`: Main application
  - `postgres`: PostgreSQL database
  - `migration-service`: Database migrations
  - `reloader`: Configuration reloader

### Network Configuration
- **Ingress**: External access via NodePort (30080)
- **Port-Forwarding**: Local development access
- **Network Policies**: Restricted pod-to-pod communication
- **Service Mesh**: Not currently used

### Data Persistence
- **PostgreSQL**: Persistent volume claim
- **Migrations**: ConfigMap with Liquibase changelogs
- **Configuration**: ConfigMap and Secrets

---

*This documentation was last updated on $(date)* 