# Kubernetes Deployment Guide

This document describes how to deploy the Congen application using Kubernetes with Minikube for local development and Skaffold for streamlined development workflows.

## Image Tagging and Deployment Strategy

- **No 'latest' tag:** Images are always tagged with a unique, versioned tag (e.g., `main-<git-hash>-<env>-<timestamp>`). The `latest` tag is not used or referenced in any deployment.
- **Image management:** The image is set via a dedicated `image-tag-patch.yaml` in each overlay (local, staging, production). The main deployment and environment patches do not set the image or pull policy.
- **ImagePullPolicy:** This is set in the base deployment (`k8s/base/congen-deployment.yaml`) and inherited by all overlays. The default is `IfNotPresent`.
- **Patch structure:** Overlays’ deployment patches (`congen-deployment-patch.yaml`) do not set the image or pull policy. Only the image-tag-patch controls the deployed image version.

## Prerequisites

### Required Tools

1. **Minikube** - Local Kubernetes cluster
   ```bash
   # Install Minikube
   brew install minikube
   
   # Start Minikube
   minikube start
   ```

2. **kubectl** - Kubernetes command-line tool
   ```bash
   # Install kubectl
   brew install kubectl
   
   # Verify installation
   kubectl version --client
   ```

3. **Skaffold** - Kubernetes development tool
   ```bash
   # Install Skaffold
   brew install skaffold
   
   # Verify installation
   skaffold version
   ```

4. **Docker** - Container runtime
   ```bash
   # Install Docker Desktop
   brew install --cask docker
   ```

5. **Kustomize** - Kubernetes configuration management
   ```bash
   # Install Kustomize
   brew install kustomize
   ```

## Local Development Setup

### 1. Start Minikube

```bash
minikube start --memory=8192 --cpus=4 --disk-size=20g
minikube addons enable ingress
minikube addons enable metrics-server

# Point your shell to minikube's docker-daemon
# This is required so that images you build are available to the cluster
# You must do this in every new shell before building images for local deployment

eval $(minikube docker-env)
```

### 2. Build and Deploy (All Environments)

#### Unified Deployment Task

Use the unified deployment task for all environments:

```bash
./gradlew deployToKubernetes -Penvironment=local|staging|production
```

#### Environment-Specific Requirements

**Local Environment:**
- Requires minikube to be running
- Automatically builds image in minikube's Docker daemon
- No registry push required

**Staging/Production Environments:**
- Requires remote registry configuration
- Automatically builds and pushes image to remote registry
- Production requires user confirmation

```bash
# For staging/production, set your registry
./gradlew deployToKubernetes -Penvironment=staging -PremoteRegistry=your-registry.example.com

# For production with confirmation
./gradlew deployToKubernetes -Penvironment=production -PremoteRegistry=your-registry.example.com
```

#### What the Unified Task Does

- Builds Docker image using JIB
- Handles image for the environment:
  - Local: builds in minikube Docker daemon
  - Staging/Production: pushes to remote registry
- Generates image tag patch with correct image reference
- Creates a ConfigMap for application configuration (including database schema and application settings) from the files in resources/migrations and applies it to the cluster
- Deploys to Kubernetes using kustomize overlays
- Waits for deployment to be ready
- Provides environment-specific feedback

**ConfigMap Details:**
- The ConfigMap is generated automatically from the contents of `resources/migrations` and other configuration files.
- It is applied as part of the deployment and made available to the application pods.
- You do not need to manually manage this ConfigMap; it is always kept in sync with your source files during deployment.

**Note:**
- The image tag in the deployment always matches the built image
- The imagePullPolicy is inherited from the base deployment
- All overlays use image-tag-patch.yaml to control the deployed image version

### 3. Access the Application

```bash
# Get Minikube IP
minikube ip

# Access the application (NodePort 30080)
curl http://$(minikube ip):30080/actuator/health

# Or use port forwarding
kubectl port-forward -n congen service/congen 8080:8080
```

### 4. Database Access

```bash
# Port forward PostgreSQL
kubectl port-forward -n congen service/postgres 5432:5432

# Connect to database
psql -h localhost -p 5432 -U postgres -d postgres
```

## Environment-Specific Deployments

### Local Development

```bash
# Deploy local configuration
kubectl apply -k k8s/overlays/local

# Delete local deployment
kubectl delete -k k8s/overlays/local
```

### Staging Environment

```bash
# Deploy staging configuration
kubectl apply -k k8s/overlays/staging

# Delete staging deployment
kubectl delete -k k8s/overlays/staging
```

### Production Environment

```bash
# Deploy production configuration
kubectl apply -k k8s/overlays/production

# Delete production deployment
kubectl delete -k k8s/overlays/production
```

## Build System Integration

### JIB Configuration

The project uses Google's JIB (Java Image Builder) for building Docker images without requiring a Dockerfile. JIB provides:

- **Faster builds**: No Docker daemon required
- **Reproducible images**: Consistent layer caching
- **Security**: Minimal attack surface with distroless base images
- **Optimization**: Automatic layer optimization

#### JIB Configuration in build.gradle

```gradle
jib {
    from {
        image = 'docker://amazoncorretto:17.0.11-alpine3.19'
    }
    to {
        image = 'congen'
        tags = [tag] // Only versioned tags are used; 'latest' is not used
    }
    container {
        appRoot = '/app'
        workingDirectory = '/app/classes'
        creationTime = 'USE_CURRENT_TIMESTAMP'
        entrypoint = [
            'java',
            '-XX:+UseG1GC', '-Xms2048m', '-Xmx2048m',
            '-XX:MetaspaceSize=256M', '-XX:MaxMetaspaceSize=256M',
            '-XX:ActiveProcessorCount=2',
            '-Djava.security.edg=file:/dev/./urandom',
            '-classpath', '/app/resources:/app/classes:/app/libs/*',
            'com.congen.CongenApplicationKt'
        ]
    }
}
```

### Gradle Tasks

The following Gradle tasks are available for Kubernetes operations:

- `jibDockerBuild` - Build Docker image locally using JIB
- `jib` - Build and push Docker image to remote registry using JIB
- `deployToKubernetes` - Deploy to specified Kubernetes environment (use -Penvironment=local|staging|production)
- `deleteLocalDeployment` - Remove local deployment
- `skaffoldDev` - Start Skaffold in development mode
- `skaffoldRun` - Run Skaffold once
- `skaffoldDelete` - Delete Skaffold deployment
- `createMigrationsConfigMap` - Auto-generate ConfigMap for database migrations from Liquibase files

### Usage Examples

```bash
# Complete local development workflow
./gradlew clean build jibDockerBuild deployToKubernetes -Penvironment=local

# Development with Skaffold
./gradlew skaffoldDev

# Clean up
./gradlew skaffoldDelete
```

## Configuration Management

### Environment Variables

The application uses a unified ConfigMap approach for all configuration:

- **Base ConfigMap**: `k8s/base/congen-configmap.yaml` - Contains all environment variables
- **Environment Patches**: 
  - **Local**: `k8s/overlays/local/local-patches/congen-configmap-patch.yaml`
  - **Staging**: `k8s/overlays/staging/staging-patches/congen-configmap-patch.yaml`
  - **Production**: `k8s/overlays/production/production-patches/congen-configmap-patch.yaml`

### Configuration Variables

The configuration is split across different Kubernetes resources based on best practices:

#### ConfigMap (Environment-specific configuration)
- **Application**: `SPRING_PROFILES_ACTIVE`
- **CORS**: `CORS_ALLOWED_ORIGINS`, `CORS_ALLOWED_METHODS`, `CORS_ALLOWED_HEADERS`, `CORS_EXPOSED_HEADERS`, `CORS_MAX_AGE`
- **PostgreSQL**: `PGSSLMODE` (environment-specific SSL configuration)
- **OpenAPI Documentation**: `SPRINGDOC_API_DOCS_ENABLED`, `SPRINGDOC_SWAGGER_UI_ENABLED`, `SPRINGDOC_SWAGGER_UI_PATH`, `SPRINGDOC_API_DOCS_PATH`, `SPRINGDOC_SWAGGER_UI_OPERATIONS_SORTER`, `SPRINGDOC_SWAGGER_UI_TAGS_SORTER`, `SPRINGDOC_SWAGGER_UI_DOC_EXPANSION`, `SPRINGDOC_SWAGGER_UI_DISPLAY_REQUEST_DURATION`, `SPRINGDOC_SWAGGER_UI_FILTER`

#### Deployment (Infrastructure-specific configuration)
- **Server**: `SERVER_PORT` (always 8080)
- **PostgreSQL Service Discovery**: `PGWRITERHOST`, `PGREADERHOST`, `PGPORT` (always postgres:5432)
- **PostgreSQL Credentials**: `PGUSER`, `PGPASSWORD`, `PGDATABASE` (from environment-specific secrets)

#### Environment-Specific Secrets (Sensitive data)
- **Local**: `k8s/overlays/local/local-secret.yaml` with local PostgreSQL credentials
- **Staging**: `k8s/overlays/staging/staging-secret.yaml` with staging database credentials
- **Production**: `k8s/overlays/production/production-secret.yaml` with production database credentials

**Important**: The staging and production secret files contain placeholder values. Replace them with actual base64-encoded credentials before deployment.

### Managing Secrets

To update database credentials in staging or production:

1. **Encode the new credentials**:
   ```bash
   echo -n "your-username" | base64
   echo -n "your-password" | base64
   echo -n "your-database" | base64
   ```

2. **Update the secret file**:
   - Staging: `k8s/overlays/staging/staging-secret.yaml`
   - Production: `k8s/overlays/production/production-secret.yaml`

3. **Apply the changes**:
   ```bash
   kubectl apply -k k8s/overlays/staging
   # or
   kubectl apply -k k8s/overlays/production
   ```

### Database Migrations

The database migrations ConfigMap is automatically generated from the Liquibase migration files:

- **Source**: `resources/migrations/` - Contains all Liquibase migration files
- **Generated**: `k8s/base/migrations-configmap.yaml` - Auto-generated ConfigMap (gitignored)
- **Task**: `createMigrationsConfigMap` - Gradle task that generates the ConfigMap

**Important**: The migrations ConfigMap is automatically generated during build and deployment. Do not manually edit `k8s/base/migrations-configmap.yaml` as it will be overwritten.

To manually regenerate the ConfigMap:
```bash
./gradlew createMigrationsConfigMap
```

### Database Configuration

PostgreSQL configuration is managed through (local environment only):

- ConfigMap: `k8s/overlays/local/postgres-configmap.yaml`
- Secret: `k8s/overlays/local/local-secret.yaml`
- PersistentVolumeClaim: `k8s/overlays/local/postgres-pvc.yaml`
- Deployment: `k8s/overlays/local/postgres-deployment.yaml`
- Service: `k8s/overlays/local/postgres-service.yaml`

**Note:** PostgreSQL is only deployed in the local environment. Staging and production environments should use managed database services.

### API Documentation Configuration

SpringDoc API documentation is configured per environment:

- **Local**: Enabled via `k8s/overlays/local/local-patches/congen-configmap-patch.yaml`
- **Staging**: Enabled via `k8s/overlays/staging/staging-patches/congen-configmap-patch.yaml`
- **Production**: Disabled (not included in production ConfigMap patch for security)

**Access URLs:**
- Swagger UI: `http://localhost:8080/swagger-ui.html` (local/staging)
- API Docs: `http://localhost:8080/api-docs` (local/staging)

## Monitoring and Debugging

### Check Pod Status

```bash
# Get all pods in congen namespace
kubectl get pods -n congen

# Get detailed pod information
kubectl describe pod <pod-name> -n congen

# View pod logs
kubectl logs <pod-name> -n congen -f
```

### Check Services

```bash
# Get all services
kubectl get services -n congen

# Get service details
kubectl describe service congen -n congen
```

### Check ConfigMaps

```bash
# List ConfigMaps
kubectl get configmaps -n congen

# View ConfigMap contents
kubectl describe configmap congen-config -n congen
```

### Database Migrations

```bash
# Check migration job status
kubectl get jobs -n congen

# View migration logs
kubectl logs job/liquibase-migration -n congen
```

For comprehensive information about the database migration system, see [resources/migrations/README.md](../resources/migrations/README.md).

## Troubleshooting

### Common Issues

1. **Image Pull Errors**
   ```bash
   # Ensure Docker is pointing to Minikube
   eval $(minikube docker-env)
   
   # Rebuild image using JIB
   ./gradlew jibDockerBuild
   
   # Or build directly with JIB
   ./gradlew jibDockerBuild
   ```

2. **JIB Build Issues**
   ```bash
   # Clean and rebuild
   ./gradlew clean jibDockerBuild
   
   # Check JIB configuration
   ./gradlew jibDockerBuild --info
   
   # Build for specific platform
   ./gradlew jibDockerBuild -Djib.from.platforms=linux/amd64
   ```

2. **Port Forwarding Issues**
   ```bash
   # Check if ports are in use
   lsof -i :8080
   lsof -i :5432
   
   # Use different ports if needed
   kubectl port-forward -n congen service/congen 8081:8080
   ```

3. **Database Connection Issues**
   ```bash
   # Check PostgreSQL pod status
   kubectl get pods -n congen -l app=postgres
   
   # Check PostgreSQL logs
   kubectl logs -n congen -l app=postgres
   ```

4. **Resource Issues**
   ```bash
   # Check Minikube resources
   minikube status
   
   # Increase Minikube resources
   minikube stop
   minikube start --memory=8192 --cpus=4
   ```

### Clean Up

```bash
# Delete all resources
kubectl delete namespace congen

# Stop Minikube
minikube stop

# Delete Minikube cluster
minikube delete
```

## Security Considerations

### Local Development

- Use `imagePullPolicy: Never` for local images
- Limit resource usage for local development
- Use NodePort services for local access

### Production

- Use proper secrets management for sensitive data
- Implement network policies
- Use LoadBalancer services with proper ingress
- Enable RBAC and service accounts
- Use resource limits and requests

## Migration from Docker Compose

The Kubernetes setup replaces the previous Docker Compose configuration:

- **PostgreSQL**: Now deployed as Kubernetes StatefulSet with PVC
- **Liquibase**: Now deployed as Kubernetes Job
- **Application**: Now deployed as Kubernetes Deployment with ConfigMaps

### Benefits

- Better resource management
- Improved scalability
- Environment-specific configurations
- Better monitoring and debugging capabilities
- Production-ready deployment patterns

## Configuration Migration

The project has migrated from Spring profile-based configuration to Kubernetes ConfigMap-based configuration:

### Before (Spring Profiles)
- Environment-specific properties files: `application-staging.properties`, `application-prod.properties`, etc.
- Configuration scattered across multiple files
- Hard to manage environment-specific changes

### After (Kubernetes ConfigMaps)
- Single `application.properties` with environment variable placeholders
- Environment-specific configuration in Kubernetes ConfigMap patches
- Centralized configuration management through Kustomize overlays
- Easy environment-specific customization without code changes

### Migration Benefits
- **Unified Configuration**: All environment-specific settings in one place
- **Environment Isolation**: Clear separation between environments
- **Easy Updates**: Change configuration without rebuilding application
- **Security**: Sensitive configuration can be managed through Kubernetes Secrets
- **Consistency**: Same configuration approach across all environments

### Configuration Best Practices

The project follows Kubernetes best practices for configuration management:

#### **ConfigMap** - Environment-specific configuration
- Values that change between environments (CORS origins, SSL settings)
- Application behavior settings (feature flags, API documentation)
- Non-sensitive configuration that might need updates

#### **Deployment** - Infrastructure-specific configuration
- Service discovery values (hostnames, ports)
- Values that are constant across all environments
- Infrastructure-specific settings

#### **Secret** - Sensitive data
- Database credentials
- API keys
- Certificates
- Any data that should be encrypted at rest

This separation provides:
- **Security**: Sensitive data is properly encrypted
- **Flexibility**: Environment-specific changes without rebuilding
- **Maintainability**: Clear separation of concerns
- **Compliance**: Follows Kubernetes security best practices

### Environment-Specific Credential Management

PostgreSQL credentials are managed differently per environment:

#### **Local Development**
- **Credentials**: From `congen-secret` Kubernetes Secret (`postgres/postgres/postgres`)
- **Database**: Local PostgreSQL deployment in Kubernetes
- **SSL**: Disabled for local development
- **API Documentation**: SpringDoc enabled for local development

#### **Staging Environment**
- **Credentials**: Stored in `congen-secret` Kubernetes Secret
- **Database**: Managed PostgreSQL service (external)
- **SSL**: Enabled for secure connections

#### **Production Environment**
- **Credentials**: Stored in `congen-secret` Kubernetes Secret
- **Database**: Managed PostgreSQL service (external)
- **SSL**: Enabled for secure connections

**Benefits:**
- **Security**: Production credentials are never in code
- **Isolation**: Each environment has its own database credentials
- **Compliance**: Follows security best practices for credential management 