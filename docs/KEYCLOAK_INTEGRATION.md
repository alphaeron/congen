# Keycloak Integration

This document describes the Keycloak integration for the Congen application, including setup, configuration, and usage.

## Overview

The Congen application uses Keycloak for authentication and authorization. Keycloak provides:

- **Single Sign-On (SSO)**: Users can authenticate once and access multiple services
- **Role-based Access Control**: Fine-grained permissions based on user roles
- **JWT Tokens**: Secure token-based authentication for API access
- **User Management**: Centralized user account management

## Architecture

### Components

1. **Keycloak Server**: Authentication and authorization server
2. **Backend API**: Spring Boot application with OAuth2 resource server
3. **Frontend Application**: React application with Keycloak JavaScript adapter

### Authentication Flow

1. User accesses the frontend application
2. If not authenticated, user is redirected to Keycloak login
3. After successful authentication, user receives JWT token
4. Frontend includes JWT token in API requests
5. Backend validates JWT token and extracts user information
6. Backend applies role-based authorization using `@PreAuthorize` annotations

## Setup

### Prerequisites

- Docker and Kubernetes (for local development)
- Terraform 1.12.2 (for infrastructure provisioning)

### Local Development Setup

1. **Deploy application and Keycloak to Kubernetes**:
   ```bash
   ./gradlew deployAll -Penvironment=local
   ```

2. **Wait for Keycloak to be ready**:
   ```bash
   kubectl wait --for=condition=ready pod -l app=keycloak -n congen --timeout=300s
   ```
   Note: This step is automatically handled by the `deployAll` task.

3. **Provision Keycloak infrastructure with Terraform**:
   ```bash
   cd terraform/environments/local
   cp terraform.tfvars.example terraform.tfvars
   # Edit terraform.tfvars with your passwords
   terraform init
   terraform apply
   ```

4. **Start the backend application**:
   ```bash
   ./gradlew :backend:bootRun
   ```

5. **Start the frontend application**:
   ```bash
   cd frontend && npm start
   ```

### Environment Configuration

#### Keycloak Configuration

The Keycloak configuration is environment-specific:

| Environment | Keycloak URL | Admin Credentials |
|-------------|--------------|-------------------|
| Local | http://localhost:8080 | admin/admin |
| Staging | https://staging.congen.com/auth | Configured via secrets |
| Production | https://congen.com/auth | Configured via secrets |

#### Frontend Configuration

The frontend uses environment variables to configure Keycloak:

```typescript
const KEYCLOAK_CONFIG = {
  loc: {
    url: 'http://localhost:8080/auth',
    realm: 'congen',
    clientId: 'congen-frontend',
  },
  staging: {
    url: 'https://staging.congen.com/auth',
    realm: 'congen',
    clientId: 'congen-frontend',
  },
  production: {
    url: 'https://congen.com/auth',
    realm: 'congen',
    clientId: 'congen-frontend',
  },
};
```

#### Backend Configuration

The backend uses Spring Security OAuth2 resource server configuration:

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=${KEYCLOAK_AUTH_URL}/realms/${KEYCLOAK_REALM}
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=${KEYCLOAK_AUTH_URL}/realms/${KEYCLOAK_REALM}/protocol/openid-connect/certs
spring.security.oauth2.resourceserver.jwt.audiences=${KEYCLOAK_CLIENT_ID}
```

## User Roles

The application defines the following roles:

### User Roles

- **user**: Regular user with access to personal data and basic features
- **admin**: Administrator with full access to all features
- **service**: Service account for automated processes

### Role Mapping

| Role | Backend Access | Frontend Access |
|------|----------------|-----------------|
| user | Personal data, basic features | User dashboard, personal settings |
| admin | All endpoints | Admin panel, user management |
| service | Service endpoints | N/A (backend only) |

## API Security

### Protected Endpoints

All API endpoints require authentication except:

- Health check endpoints (`/api/v1/health/**`)
- Public documentation endpoints (when enabled)

### Authorization Annotations

The backend uses Spring Security `@PreAuthorize` annotations for fine-grained access control:

```kotlin
// User-specific access
@PreAuthorize("hasRole('admin') or hasRole('service') or #userId == principal.subject")

// Role-based access
@PreAuthorize("hasRole('admin') or hasRole('service')")

// Authenticated access
@PreAuthorize("isAuthenticated()")

// Program-specific access
@PreAuthorize("hasRole('admin') or hasRole('service') or @programService.isOwner(#programId, principal.subject)")
```

### Token Management

The frontend automatically handles JWT token management:

- **Token Refresh**: Automatically refreshes tokens before expiration
- **Error Handling**: Redirects to login on authentication failures
- **Request Interceptors**: Adds Authorization headers to API requests

## Frontend Integration

### Authentication Context

The frontend uses a React context for authentication state management:

```typescript
const { authenticated, loading, login, logout, keycloak } = useAuth();
```

### Protected Routes

Use the `ProtectedRoute` component to protect pages that require authentication:

```typescript
<Route 
  path="/dashboard" 
  element={
    <ProtectedRoute>
      <DashboardPage />
    </ProtectedRoute>
  } 
/>
```

### API Calls

Use the authenticated API client for API calls:

```typescript
import { useAuth } from '../auth/AuthContext';
import { authenticatedRequest } from '../api/authClient';

const { keycloak } = useAuth();

const data = await authenticatedRequest(keycloak, {
  method: 'GET',
  url: '/api/v1/exercises',
});
```

## Testing

### Backend Tests

Backend tests use `@TestPropertySource` to exclude security auto-configuration:

```kotlin
@TestPropertySource(
    properties = ["spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration"]
)
```

### Frontend Tests

Frontend tests mock the authentication context:

```typescript
jest.mock('../auth/AuthContext', () => ({
  useAuth: () => ({
    authenticated: true,
    loading: false,
    keycloak: mockKeycloak,
  }),
}));
```

## Troubleshooting

### Common Issues

1. **Keycloak Connection Failed**
   - Check if Keycloak is running: `kubectl get pods -n congen` (or use the `deployAll` task which handles this automatically)
   - Verify Keycloak URL in configuration
   - Check network connectivity

2. **Authentication Errors**
   - Verify client configuration in Keycloak
   - Check redirect URIs and web origins
   - Ensure JWT token is valid and not expired

3. **Authorization Errors**
   - Verify user has required roles
   - Check `@PreAuthorize` annotations
   - Ensure roles are properly mapped

### Debugging

Enable debug logging for authentication:

```properties
logging.level.org.springframework.security=DEBUG
logging.level.com.congen.auth=DEBUG
```

### Keycloak Admin Console

Access the Keycloak admin console at:
- Local: http://localhost:8080
- Credentials: admin/admin

## Security Considerations

### Best Practices

1. **Token Security**
   - Use HTTPS in production
   - Set appropriate token expiration times
   - Implement token refresh properly

2. **Role Management**
   - Follow principle of least privilege
   - Regularly review and update role assignments
   - Use specific roles rather than broad permissions

3. **Configuration Security**
   - Store sensitive configuration in Kubernetes secrets
   - Use environment-specific configurations
   - Regularly rotate credentials

### Production Deployment

1. **Keycloak Configuration**
   - Use external database (PostgreSQL)
   - Configure SSL/TLS certificates
   - Set up proper backup and monitoring

2. **Network Security**
   - Use network policies to restrict access
   - Implement proper ingress/egress rules
   - Monitor authentication logs

3. **Monitoring**
   - Monitor authentication success/failure rates
   - Track token refresh patterns
   - Alert on suspicious activity

## Migration Guide

### From No Authentication

1. Deploy Keycloak infrastructure
2. Run setup script to configure realm and clients
3. Update backend with security annotations
4. Update frontend with authentication components
5. Test authentication flow
6. Gradually migrate existing users

### From Other Authentication System

1. Export users from existing system
2. Import users into Keycloak
3. Map existing roles to Keycloak roles
4. Update application configuration
5. Test authentication flow
6. Deploy and monitor

## Support

For issues related to Keycloak integration:

1. Check the troubleshooting section
2. Review Keycloak logs: `kubectl logs -n congen -l app=keycloak` (for debugging only)
3. Check application logs for authentication errors
4. Verify configuration in Keycloak admin console 