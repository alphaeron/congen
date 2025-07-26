# Keycloak Service Account Setup

This document describes how to set up Keycloak with proper service account authentication for the Congen backend application.

## Overview

The Congen application uses a dedicated service account for backend-to-Keycloak communication instead of admin credentials. This provides better security by following the principle of least privilege.

## Architecture

### Components

1. **Keycloak Server**: Authentication and authorization server
2. **Terraform Configuration**: Infrastructure as Code for Keycloak provisioning
3. **Service Account**: Dedicated user account with limited permissions
4. **Backend Application**: Uses service account for user management operations

### Security Benefits

- **Principle of Least Privilege**: Service account has only necessary permissions
- **Audit Trail**: Clear separation between admin and service operations
- **Credential Management**: Service account credentials can be rotated independently
- **Isolation**: Service account cannot access admin functions

## Setup Instructions

### 1. Prerequisites

- Terraform >= 1.0
- Keycloak server running and accessible
- Admin credentials for initial setup

### 2. Configure Terraform Variables

Copy the example variables file and configure your environment:

```bash
cd terraform/keycloak
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars` with your values:

```hcl
# Keycloak Admin Credentials
keycloak_admin_password = "your-keycloak-admin-password"

# Backend Service Account Password
backend_service_password = "your-backend-service-password"

# Default Admin User Password
admin_password = "your-admin-password"

# Optional: Override defaults
# keycloak_url = "https://keycloak.your-domain.com"
# admin_username = "your-admin-username"
# admin_email = "your-admin@your-domain.com"
```

### 3. Deploy Keycloak Configuration

```bash
# Initialize Terraform
terraform init

# Plan the deployment
terraform plan

# Apply the configuration
terraform apply
```

### 4. Configure Backend Application

Update your backend configuration with the service account credentials:

#### Environment Variables

```properties
# Keycloak Service Account Configuration
KEYCLOAK_SERVICE_ACCOUNT_USERNAME=service-account-congen-backend
KEYCLOAK_SERVICE_ACCOUNT_PASSWORD=your-backend-service-password
```

#### Kubernetes Secrets

For Kubernetes deployments, add the service account password to your secrets:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: backend-secret
type: Opaque
data:
  KEYCLOAK_SERVICE_ACCOUNT_PASSWORD: <base64-encoded-password>
```

### 5. Verify Setup

1. **Check Service Account**: Verify the service account exists in Keycloak
2. **Test Authentication**: Ensure the backend can authenticate with Keycloak
3. **Test User Creation**: Create a test user through the backend API

## Terraform Resources Created

### Realm and Roles

- **Realm**: `congen`
- **Roles**: `user`, `admin`, `service`

### Clients

- **Backend Client**: `congen-backend` (confidential, service accounts enabled)
- **Frontend Client**: `congen-frontend` (public, standard flow enabled)

### Users

- **Service Account**: `congen-backend-service` (assigned `service` role)
- **Admin User**: Configurable admin user (assigned `admin` and `user` roles)

## Service Account Permissions

The service account has the following permissions:

- Create users in the `congen` realm
- Update user profiles
- Delete users
- Assign roles to users
- Read role information

The service account does NOT have:
- Access to admin functions
- Ability to modify realm settings
- Access to other realms
- Ability to create/modify clients

## Migration from Admin Credentials

If you're migrating from admin credentials:

1. **Deploy Terraform Configuration**: Creates service account and assigns permissions
2. **Update Backend Configuration**: Change from admin to service account credentials
3. **Test Functionality**: Verify all user management operations work
4. **Remove Admin Credentials**: Clean up old admin credential configuration

### Configuration Changes

#### Before (Admin Credentials)
```properties
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin-password
```

#### After (Service Account)
```properties
KEYCLOAK_SERVICE_ACCOUNT_USERNAME=congen-backend-service
KEYCLOAK_SERVICE_ACCOUNT_PASSWORD=service-account-password
```

## Troubleshooting

### Common Issues

1. **Service Account Not Found**
   - Verify Terraform deployment completed successfully
   - Check service account username in Keycloak admin console

2. **Authentication Failed**
   - Verify service account password is correct
   - Check that service account is enabled
   - Ensure service account has required roles

3. **Permission Denied**
   - Verify service account has `service` role
   - Check that backend client has service accounts enabled
   - Ensure proper role mappings

### Debugging

Enable debug logging for Keycloak operations:

```properties
logging.level.com.congen.client=DEBUG
logging.level.org.springframework.web.reactive.function.client=DEBUG
```

### Keycloak Admin Console

Access the Keycloak admin console to verify setup:

1. Navigate to the `congen` realm
2. Check Users section for `congen-backend-service`
3. Verify role assignments
4. Check Clients section for `congen-backend`

## Security Best Practices

1. **Password Management**
   - Use strong, unique passwords for service accounts
   - Rotate passwords regularly
   - Store passwords securely (Kubernetes secrets, environment variables)

2. **Network Security**
   - Use HTTPS for all Keycloak communication
   - Implement proper network policies
   - Monitor authentication logs

3. **Access Control**
   - Regularly review service account permissions
   - Monitor service account usage
   - Implement least privilege access

4. **Monitoring**
   - Monitor authentication success/failure rates
   - Track service account token usage
   - Alert on suspicious activity

## Production Deployment

For production environments:

1. **Use External Database**: Configure Keycloak with PostgreSQL
2. **Enable SSL/TLS**: Use proper certificates for all communication
3. **Implement Backup**: Regular backups of Keycloak configuration
4. **Monitor Performance**: Track Keycloak server performance
5. **Security Hardening**: Follow Keycloak security best practices

## Support

For issues related to Keycloak service account setup:

1. Check the troubleshooting section
2. Review Terraform logs: `terraform logs`
3. Check Keycloak logs: `kubectl logs -n congen -l app=keycloak`
4. Verify configuration in Keycloak admin console
