# Kubernetes Deployment

This directory contains the Kubernetes manifests for deploying the Congen application using a staged deployment approach.

## Overview

The staged deployment approach addresses the chicken-and-egg problem of deploying applications that depend on Keycloak secrets that are only available after Keycloak is provisioned. The deployment is split into stages as described below.

This allows the keycloak terraform to be applied before application components (which depend on it) are created.

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Staged Deployment                        │
├─────────────────────────────────────────────────────────────┤
│  Stage 1: Infrastructure                                    │
│  ├── Deploy namespace                                       │
│  ├── Deploy PostgreSQL (local environment)                  │
│  ├── Configure network policies                             │
│  └── Wait for infrastructure to be ready                    │
├─────────────────────────────────────────────────────────────┤
│  Stage 2: Secrets Bootstrapping                             │
│  ├── Deploy base configuration (includes congen-secret)     │
│  ├── Deploy keycloak-secret with dummy values               │
│  └── Prepare for Terraform secret updates                   │
├─────────────────────────────────────────────────────────────┤
│  Stage 3: Keycloak Infrastructure                           │
│  ├── Deploy Keycloak to Kubernetes                          │
│  ├── Bootstrap Keycloak with Terraform client               │
│  └── Wait for Keycloak to be ready                          │
├─────────────────────────────────────────────────────────────┤
│  Stage 4: Application Components                            │
│  ├── Deploy backend, frontend, and supporting services      │
│  └── Verify all components are running                      │
├─────────────────────────────────────────────────────────────┤
│  Stage 5: Ingress Configuration                             │
│  ├── Deploy ingress with environment-specific patches       │
│  ├── Configure SSL/TLS for staging/production               │
│  └── Set up proper routing for all services                 │
├─────────────────────────────────────────────────────────────┤
│  Stage 6: HPA (staging/production only)                     │
│  └── Provision HPA after creating all other resources       │
└─────────────────────────────────────────────────────────────┘
```

## Prerequisites

- Kubernetes cluster (minikube, kind, or cloud provider)
- kubectl configured to access the cluster
- Terraform installed
- Keycloak admin credentials

## Deployment Process

### Gradle Integration (Recommended)

Use Gradle tasks for deployment with automatic image building and environment configuration:

```bash
# Full deployment for local environment
./gradlew deployAll -Penvironment=local

# Full deployment for staging with remote registry
./gradlew deployAll -Penvironment=staging -PremoteRegistry=your-registry.example.com

# Full deployment for production (requires confirmation)
./gradlew deployAll -Penvironment=production -PremoteRegistry=your-registry.example.com

## Environment-Specific Configuration

### Local Environment

- **Keycloak URL**: `http://localhost:8080`
- **Database**: Local PostgreSQL (deployed in Kubernetes)
- **Secrets**: Development values with hardcoded test database credentials
- **Network Policy**: Includes PostgreSQL access (port 5432)

### Staging Environment

- **Keycloak URL**: `https://keycloak.staging.congen.com`
- **Database**: External PostgreSQL (not deployed in Kubernetes)
- **Secrets**: Staging-specific values
- **Network Policy**: Internet access only (no PostgreSQL access)

### Production Environment

- **Keycloak URL**: `https://keycloak.congen.com`
- **Database**: External PostgreSQL (not deployed in Kubernetes)
- **Secrets**: Production values (from secure sources)
- **Network Policy**: Internet access only (no PostgreSQL access)

## Security Considerations

### Secret Management

- **Dummy Values**: All manifests contain dummy base64-encoded values
- **Dynamic Updates**: Secrets are updated during deployment from Terraform outputs
- **No Version Control**: Real secrets are never committed to version control
- **Centralized**: All secret values come from Terraform outputs
- **Environment-Specific**: Each environment gets its own secret values
- **Automated**: Secrets are automatically updated during Stage 4 deployment

### Access Control

- Keycloak service account has minimal required permissions
- Backend client uses service account authentication
- Frontend client is public with proper redirect URIs

### Network Security

- Keycloak runs on internal port 8080
- External access through ingress or port forwarding
- Network policies restrict inter-pod communication
- Environment-specific network access (PostgreSQL for local, internet only for staging/production)

## Intelligent Behavior

The deployment system includes several intelligent features that automatically optimize the deployment process:

### Keycloak Bootstrap
- Automatically checks if Terraform client exists before bootstrapping
- Skips bootstrap if client already exists
- Safe to run multiple times without errors

### Terraform Application
- Automatically checks for changes using `terraform plan`
- Skips apply if infrastructure is up to date
- Only applies changes when necessary
- Reduces deployment time and risk

### Secrets Update Optimization
- Skips secrets update if Terraform was up to date (no changes applied)
- Prevents unnecessary secret updates when infrastructure hasn't changed
- Maintains secrets consistency with Terraform state

### Stage-Specific Deployment
- Each stage deploys only its specific components
- No redundant re-deployment of previous stages
- Faster and more efficient targeted updates
- Secrets are bootstrapped early and updated with real values later

## Best Practices

### Deployment

- Always use the staged deployment approach for new environments
- Test deployments in local environment first
- Use Gradle tasks for automated image building and deployment
- Monitor deployment logs and events

### Configuration

- Keep environment-specific values in appropriate overlay directories
- Use Terraform for all Keycloak resource management and secret generation
- All secrets use dummy values in manifests, updated dynamically during deployment
- Use ConfigMaps for non-sensitive configuration
- Secrets are centralized in base configuration, environment-specific updates happen during deployment

### Monitoring

- Set up proper health checks for all components
- Monitor Keycloak and application logs
- Use Kubernetes events for troubleshooting
- Implement proper alerting for deployment failures

## Validation and Linting

The project includes comprehensive Kubernetes manifest validation and linting:

### kubeLinter
Validates Kubernetes manifests for best practices and security:
```bash
./gradlew kubeLinter
```

**What it validates:**
- All overlay directories (local, staging, production) including patches and stage files
- Base directory resources
- Excludes common development-specific rules (latest-tag, no-read-only-root-fs, etc.)
- Ignores missing kustomization.yaml files in overlay directories

### kubectlDryRun
Performs a dry-run validation of the base Kubernetes configuration:
```bash
./gradlew kubectlDryRun
```

### kubeval
Validates Kubernetes manifests against the official Kubernetes schema:
```bash
./gradlew kubeval
```

### Complete Validation
Run all Kubernetes validation tasks:
```bash
./gradlew kubeCheck
```

## Gradle Tasks Reference

### Main Deployment Tasks

- **`deployAll`** - Deploy all stages (main deployment task)
- **`deployStage`** - Deploy a specific stage (use `-Pstage=<1-6>`)

### Validation Tasks

- **`kubeLinter`** - Lint Kubernetes manifests for best practices
- **`kubectlDryRun`** - Validate base configuration with kubectl dry-run
- **`kubeval`** - Validate manifests against Kubernetes schema
- **`kubeCheck`** - Run all Kubernetes validation tasks

### Utility Tasks

- **`setupLocalKubernetes`** - Setup minikube for local development
- **`setupTestPortForward`** - Setup port forwarding for integration tests
- **`cleanupPortForward`** - Cleanup port forwarding

### Configuration Properties

- **`environment`**: Target environment (local, local-persist, staging, production) - **Required**
- **`stage`**: Stage number (1-6) for `deployStage` task - **Required for deployStage**
- **`remoteRegistry`**: Remote registry URL (required for staging/production)
- **`remoteRegistryNamespace`**: Registry namespace (default: congen)
- **`localRegistryPort`**: Local registry port (default: 5001)

## Support

For issues with staged deployment:

1. Check the troubleshooting section in the overlays README
2. Review deployment logs and events
3. Verify Terraform state and outputs
4. Check Keycloak configuration and logs
5. Ensure all prerequisites are met 