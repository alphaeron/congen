# Kubernetes Overlays - Environment-Specific Configuration

This directory contains environment-specific overlays that customize the base Kubernetes resources for different deployment environments.

## Overview

Overlays provide environment-specific customization through:
- **Strategic merge patches**: Modify existing resources
- **Additional resources**: Environment-specific secrets and configurations
- **Staged deployment**: Separate deployment stages for dependency management
- **Secret management**: Dummy values in manifests, updated dynamically during deployment

## Directory Structure

Each environment (`local`, `staging`, `production`) contains:

- `stage-3-infrastructure.yaml` - **Stage 3**: Infrastructure components
- `stage-4-secrets.yaml` - **Stage 4**: Secrets bootstrapping (dummy values from base)
- `stage-5-keycloak.yaml` - **Stage 5**: Keycloak infrastructure
- `stage-6-applications.yaml` - **Stage 6**: Application components
- `stage-7-ingress.yaml` - **Stage 7**: Ingress configuration (staging/production only)
- `patches/` - Environment-specific patches

## Staged Deployment

### Stage 1: Infrastructure
Deploys foundational components that other stages depend on.

**Local Environment:**
- Namespace
- PostgreSQL deployment, service, configmap, PVC
- Network policy with PostgreSQL access

**Staging/Production Environments:**
- Namespace
- Network policy (internet access only)

**Deployment:**
```bash
./gradlew deployStage -Penvironment=local -Pstage=1
```

### Stage 2: Secrets Bootstrapping
Deploys secrets with dummy values to prepare for Terraform updates.

**All Environments:**
- Base configuration (includes congen-secret and keycloak-secret with dummy values)
- Preparation for Terraform secret updates

### Stage 3: Keycloak
Deploys Keycloak infrastructure and provisions Keycloak resources.

**All Environments:**
- Keycloak deployment and service
- Keycloak bootstrap and Terraform application

### Stage 4: Terraform and Secrets Update
Updates secrets with real values from Terraform outputs.

**All Environments:**
- Applies Terraform configuration
- Updates existing secrets with real values from Terraform outputs
- Skips secrets update if Terraform was up to date (no changes applied)
- Requires Stage 2 to be deployed first (secrets must exist)

**Deployment:**
```bash
./gradlew deployStage -Penvironment=local -Pstage=4
```

### Stage 5: Applications
Deploys application components with proper secrets.

**All Environments:**
- Backend, frontend, and supporting services
- Environment-specific patches
- Application secrets (now updated with real values from Stage 4)

**Deployment:**
```bash
./gradlew deployStage -Penvironment=local -Pstage=5
```

### Stage 6: Ingress
Deploys ingress with environment-specific configuration.

**Local Environment:**
- Ingress included in Stage 5 (no separate Stage 6)

**Staging/Production Environments:**
- Ingress with SSL/TLS configuration
- Environment-specific hostnames and certificates
- Proper routing for all services

**Deployment:**
```bash
./gradlew deployStage -Penvironment=staging -Pstage=6
```

## Environment-Specific Components

### Local Environment
- **PostgreSQL**: Deployed in Kubernetes for local development
- **Secrets**: Dummy values in manifests, updated dynamically during deployment
- **Database Credentials**: Hardcoded test values (postgres/postgres) via secret patch
- **Networking**: NodePort services, basic ingress, PostgreSQL network access
- **Resources**: PostgreSQL deployment, service, configmap, PVC

### Staging Environment
- **Database**: External PostgreSQL (not deployed in Kubernetes)
- **Secrets**: Dummy values in manifests, updated dynamically during deployment
- **Networking**: LoadBalancer services, SSL/TLS ingress, internet-only network access
- **Resources**: Environment-specific configurations only

### Production Environment
- **Database**: External PostgreSQL (not deployed in Kubernetes)
- **Secrets**: Dummy values in manifests, updated dynamically during deployment
- **Networking**: SSL/TLS ingress, HPA, internet-only network access
- **Resources**: Environment-specific configurations and HPA

## Patch Structure

### Common Patches
Each environment has patches for:
- `backend-deployment-patch.yaml` - Backend deployment customizations
- `backend-configmap-patch.yaml` - Backend configuration
- `backend-image-tag-patch.yaml` - Image tag specification
- `frontend-image-tag-patch.yaml` - Frontend image tag
- `backend-service-patch.yaml` - Backend service configuration
- `migration-service-labels-patch.yaml` - Migration service labels
- `keycloak-deployment-patch.yaml` - Keycloak deployment customizations

### Environment-Specific Patches
- **Local**: 
  - Network policy patch (adds PostgreSQL access)
  - Congen secret patch (hardcoded test database credentials)
- **Staging**: Production-like configurations
- **Production**: Full production configurations with HPA

## Deployment Options

### Manual Staged Deployment

```bash
# Stage 1: Infrastructure
kubectl apply -k k8s/overlays/local/stage-3-infrastructure.yaml

# Stage 2: Secrets bootstrapping
kubectl apply -k k8s/overlays/local/stage-4-secrets.yaml

# Stage 3: Keycloak infrastructure (after secrets are ready)
kubectl apply -k k8s/overlays/local/stage-5-keycloak.yaml

# Stage 4: Terraform and secrets update (after Keycloak is ready)
# Note: This requires Stage 2 to be deployed first (secrets must exist)
./scripts/update-k8s-secrets.sh -e local

# Stage 5: Applications (after secrets are updated)
kubectl apply -k k8s/overlays/local/stage-6-applications.yaml

# Stage 6: Ingress (staging/production only)
kubectl apply -k k8s/overlays/staging/stage-7-ingress.yaml
```

### Automated Staged Deployment

```bash
# Use Gradle for automated deployment
./gradlew deployAll -Penvironment=local
```

## Configuration Management

### Secrets
Each environment has its own secret files:
- `congen-secret.yaml` - Application secrets (environment-specific values)
- `keycloak-secret.yaml` - Keycloak secrets (environment-specific values)

### Patches
Patches customize base resources for each environment:
- **Image tags**: Environment-specific image versions
- **Resource limits**: Environment-appropriate resource allocation
- **Configuration**: Environment-specific settings
- **Networking**: Environment-specific service configurations

### Environment Variables
Environment-specific configuration is managed through:
- **ConfigMap patches**: Non-sensitive configuration
- **Secret references**: Sensitive data
- **Deployment patches**: Infrastructure-specific settings

## Best Practices

### Adding New Environments
1. **Copy existing environment**: Use local as a template
2. **Customize secrets**: Update secret files for new environment
3. **Adjust patches**: Modify patches for environment-specific needs
4. **Test deployment**: Verify all stages work correctly
5. **Update documentation**: Document environment-specific requirements

### Modifying Patches
1. **Use strategic merge**: Leverage Kustomize strategic merge patches
2. **Environment-specific**: Keep patches focused on environment differences
3. **Test changes**: Verify patches work across all environments
4. **Document changes**: Update this README when adding new patches

### Secret Management
1. **Dummy values**: All manifests contain dummy base64-encoded values
2. **Dynamic updates**: Secrets are updated during deployment from Terraform outputs
3. **No version control**: Real secrets are never committed to version control
4. **Centralized**: All secret values come from Terraform outputs
5. **Environment-specific**: Each environment gets its own secret values during deployment

## Troubleshooting

### Stage 1 Issues
- **Namespace**: `kubectl get namespace congen`
- **PostgreSQL (local)**: `kubectl get pods -l app=postgres -n congen`
- **Network Policy**: `kubectl get networkpolicy -n congen`
- **Secrets**: `kubectl get secrets -n congen`

### Stage 2 Issues
- **Keycloak deployment**: `kubectl get pods -l app=keycloak -n congen`
- **Keycloak logs**: `kubectl logs -l app=keycloak -n congen`
- **Terraform state**: `terraform state list` in environment directory

### Stage 3 Issues
- **Application pods**: `kubectl get pods -n congen`
- **Application logs**: `kubectl logs -l app=backend -n congen`
- **Secret verification**: `kubectl get secret congen-secret -n congen -o yaml`

### Common Commands
```bash
# Check all resources
kubectl get all -n congen

# Check events
kubectl get events -n congen --sort-by='.lastTimestamp'
``` 