# Kubernetes Deployment

This directory contains the Kubernetes manifests for deploying the Congen application using a staged deployment approach.

## Overview

The staged deployment approach addresses the chicken-and-egg problem of deploying applications that depend on Keycloak secrets that are only available after Keycloak is provisioned. The deployment is split into six main stages:

1. **Stage 1**: Deploy infrastructure components (namespace, PostgreSQL for local, network policies)
2. **Stage 2**: Deploy secrets with dummy values (bootstrapping)
3. **Stage 3**: Deploy Keycloak infrastructure
4. **Stage 4**: Apply Terraform and update secrets with real values
5. **Stage 5**: Deploy Congen application components with proper secrets
6. **Stage 6**: Deploy ingress with environment-specific configuration (staging/production)

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
│  Stage 4: Terraform & Secrets Update                        │
│  ├── Apply Terraform to create realm, clients, users        │
│  ├── Update Kubernetes secrets with Terraform outputs       │
│  ├── Skip secrets update if Terraform was up to date        │
│  └── Replace dummy values with real credentials             │
├─────────────────────────────────────────────────────────────┤
│  Stage 5: Application Components                            │
│  ├── Deploy backend, frontend, and supporting services      │
│  └── Verify all components are running                      │
├─────────────────────────────────────────────────────────────┤
│  Stage 6: Ingress Configuration                             │
│  ├── Deploy ingress with environment-specific patches       │
│  ├── Configure SSL/TLS for staging/production               │
│  └── Set up proper routing for all services                 │
└─────────────────────────────────────────────────────────────┘
```

## Directory Structure

```
k8s/
├── base/                          # Base Kubernetes resources (shared across environments)
│   ├── namespace.yaml
│   ├── keycloak-deployment.yaml
│   ├── keycloak-service.yaml
│   ├── backend-deployment.yaml
│   ├── frontend-deployment.yaml
│   ├── congen-secret.yaml         # Base secret with dummy values
│   ├── keycloak-secret.yaml       # Keycloak secret with dummy values
│   └── ... (other shared resources)
└── overlays/                      # Environment-specific overlays
    ├── local/                     # Local environment
    │   ├── stage-1-infrastructure.yaml  # Stage 1: Infrastructure + PostgreSQL
    │   ├── stage-2-secrets.yaml         # Stage 2: Secrets bootstrapping
    │   ├── stage-3-keycloak.yaml        # Stage 3: Keycloak infrastructure
    │   ├── stage-4-applications.yaml    # Stage 4: Applications (includes ingress)
    │   ├── patches/               # Environment-specific patches
    │   └── postgres-*.yaml        # PostgreSQL resources (local only)
    ├── staging/                   # Staging environment
    │   ├── stage-1-infrastructure.yaml  # Stage 1: Infrastructure (no PostgreSQL)
    │   ├── stage-2-secrets.yaml         # Stage 2: Secrets bootstrapping
    │   ├── stage-3-keycloak.yaml        # Stage 3: Keycloak infrastructure
    │   ├── stage-4-applications.yaml    # Stage 4: Applications
    │   ├── stage-5-ingress.yaml         # Stage 5: Ingress with SSL/TLS
    │   └── patches/               # Environment-specific patches
    └── production/                # Production environment
        ├── stage-1-infrastructure.yaml  # Stage 1: Infrastructure (no PostgreSQL)
        ├── stage-2-secrets.yaml         # Stage 2: Secrets bootstrapping
        ├── stage-3-keycloak.yaml        # Stage 3: Keycloak infrastructure
        ├── stage-4-applications.yaml    # Stage 4: Applications
        ├── stage-5-ingress.yaml         # Stage 5: Ingress with SSL/TLS
        ├── patches/               # Environment-specific patches
        └── hpa.yaml               # Production HPA
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

# Stage-specific deployment
./gradlew deployStage -Penvironment=local -Pstage=1    # Infrastructure only
./gradlew deployStage -Penvironment=local -Pstage=2    # Secrets bootstrapping only
./gradlew deployStage -Penvironment=local -Pstage=3    # Keycloak infrastructure only
./gradlew deployStage -Penvironment=local -Pstage=4    # Terraform and secrets update only
./gradlew deployStage -Penvironment=local -Pstage=5    # Application components only
./gradlew deployStage -Penvironment=staging -Pstage=6  # Ingress only (staging/production)
```



### Manual Staged Deployment

If you prefer to run each stage manually:

#### Stage 1: Deploy Infrastructure

```bash
# 1. Deploy infrastructure components (namespace, PostgreSQL)
kubectl apply -k k8s/overlays/local/stage-1-infrastructure.yaml

# 2. Wait for PostgreSQL to be ready (if applicable)
kubectl wait --for=condition=ready pod -l app=postgres -n congen --timeout=300s
```

#### Stage 2: Deploy Secrets Bootstrapping

```bash
# Deploy secrets with dummy values
kubectl apply -k k8s/overlays/local/stage-2-secrets.yaml

# Verify secrets are created
kubectl get secrets -n congen
```

#### Stage 3: Deploy Keycloak Infrastructure

```bash
# 1. Deploy Keycloak to Kubernetes
kubectl apply -k k8s/overlays/local/stage-3-keycloak.yaml

# 2. Wait for Keycloak to be ready
kubectl wait --for=condition=ready pod -l app=keycloak -n congen --timeout=300s

# 3. Set up port forwarding
kubectl port-forward -n congen service/keycloak 8080:8081 &

# 4. Bootstrap Keycloak with Terraform client
# Note: This is handled automatically by the Gradle deployment tasks
```

#### Stage 4: Apply Terraform and Update Secrets

```bash
# 1. Apply Terraform configuration
cd terraform/environments/local
terraform init
terraform apply -auto-approve
cd -

# 2. Update Kubernetes secrets with Terraform outputs
# Note: This requires Stage 2 to be deployed first (secrets must exist)
# Note: Secrets update is skipped if Terraform was up to date (no changes applied)
./scripts/update-k8s-secrets.sh -e local

# Verify secrets are updated
kubectl get secret congen-secret -n congen -o yaml
```

#### Stage 5: Deploy Application Components

```bash
# Deploy all application components
kubectl apply -k k8s/overlays/local/stage-4-applications.yaml

# Verify deployment
kubectl get pods -n congen
kubectl get services -n congen
```

#### Stage 6: Deploy Ingress (Staging/Production Only)

```bash
# Deploy ingress with environment-specific configuration
kubectl apply -k k8s/overlays/staging/stage-5-ingress.yaml

# Verify ingress
kubectl get ingress -n congen
```

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

- **`environment`**: Target environment (local, staging, production) - **Required**
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