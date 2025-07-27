# Base Kubernetes Resources

This directory contains the base Kubernetes resources that are shared across all environments (local, staging, production).

## Overview

The base resources provide a common foundation for all environments while allowing environment-specific customization through overlays and patches.

## Resources

### Infrastructure
- `namespace.yaml` - Kubernetes namespace for the Congen application

### Keycloak
- `keycloak-deployment.yaml` - Keycloak server deployment
- `keycloak-service.yaml` - Keycloak service for internal communication

### Application Components
- `backend-deployment.yaml` - Backend application deployment
- `backend-service.yaml` - Backend service
- `backend-configmap.yaml` - Backend configuration
- `frontend-deployment.yaml` - Frontend application deployment
- `frontend-service.yaml` - Frontend service

### Supporting Services
- `service-account.yaml` - Service account for the application
- `reloader-rbac.yaml` - RBAC configuration for reloader
- `reloader-deployment.yaml` - Reloader for configuration updates
- `migrations-configmap.yaml` - Database migration configuration
- `migration-service-deployment.yaml` - Database migration service

### Networking
- `network-policy.yaml` - Network policies for security (internet access only)

## Design Principles

### Shared Resources
All resources in this directory are designed to be shared across environments with minimal customization needed.

### Environment Variables
Resources use environment variables and ConfigMaps for configuration, allowing environment-specific values to be injected through overlays.

### Security
- Non-root user execution
- Read-only root filesystem where possible
- Security contexts configured
- Network policies for pod communication

### Resource Management
- Resource requests and limits defined
- Health checks configured
- Proper lifecycle management

## Customization

### Environment-Specific Configuration
Environment-specific configuration is handled through:
- **Overlays**: Environment-specific kustomization files
- **Patches**: Strategic merge patches for environment-specific changes
- **Secrets**: Environment-specific secret files

### Common Customizations
- Image tags (via image-tag-patch.yaml)
- Resource limits (via deployment patches)
- Environment variables (via configmap patches)
- Service configurations (via service patches)

## Usage

### Base Resources Only
```bash
# Apply base resources only (not recommended for production)
kubectl apply -k k8s/base
```

### With Environment Overlay
```bash
# Apply with environment-specific overlay
kubectl apply -k k8s/overlays/local/stage-1-infrastructure.yaml
kubectl apply -k k8s/overlays/local/stage-2-secrets.yaml
kubectl apply -k k8s/overlays/local/stage-3-keycloak.yaml
kubectl apply -k k8s/overlays/local/stage-4-applications.yaml
# For staging/production: also apply stage-5-ingress.yaml
```

## Resource Details

### Backend Deployment
- **Image**: Configurable via overlays
- **Port**: 8080
- **Health Checks**: `/api/v1/health/`
- **Resources**: Configurable via patches
- **Security**: Non-root user, read-only filesystem

### Frontend Deployment
- **Image**: Configurable via overlays
- **Port**: 3000
- **Health Checks**: Built-in React health checks
- **Resources**: Configurable via patches
- **Security**: Non-root user

### Keycloak Deployment
- **Image**: quay.io/keycloak/keycloak:latest
- **Port**: 8080 (internal)
- **Database**: Configurable via environment variables
- **Admin**: Configurable via secrets

### Database Migrations
- **Service**: Runs database migrations on startup
- **ConfigMap**: Contains all Liquibase migration files
- **Job**: One-time execution for schema updates

## Best Practices

### Adding New Resources
1. **Place in base**: If the resource is shared across environments
2. **Use environment variables**: For configurable values
3. **Follow naming conventions**: Consistent with existing resources
4. **Include security contexts**: Non-root users, read-only filesystems
5. **Add resource limits**: Prevent resource exhaustion

### Modifying Existing Resources
1. **Use patches**: Don't modify base resources directly
2. **Environment-specific**: Use overlays for environment differences
3. **Backward compatible**: Ensure changes work across environments
4. **Document changes**: Update this README when adding new resources

### Configuration Management
1. **ConfigMaps for non-sensitive data**: Environment variables, feature flags
2. **Secrets for sensitive data**: Passwords, API keys, certificates
3. **Environment-specific patches**: Use overlays for customization
4. **Validation**: Ensure configuration is valid for all environments

## Troubleshooting

### Common Issues
- **Image pull errors**: Check image tags in overlays
- **Configuration issues**: Verify ConfigMap and Secret references
- **Resource constraints**: Check resource limits and requests
- **Network connectivity**: Verify network policies and service configurations

### Debugging Commands
```bash
# Check base resources
kubectl apply -k k8s/base --dry-run=client

# Validate kustomization
kustomize build k8s/base

# Check specific resource
kubectl get deployment backend -n congen -o yaml
``` 