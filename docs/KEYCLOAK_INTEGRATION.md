# Keycloak Integration

This document describes the Keycloak integration for the Congen application, including setup, configuration, and usage.

## Overview

The Congen application uses Keycloak for authentication and authorization with a proper OAuth2 authorization code flow. Keycloak provides:

- **Single Sign-On (SSO)**: Users can authenticate once and access multiple services
- **Role-based Access Control**: Fine-grained permissions based on user roles
- **JWT Tokens**: Secure token-based authentication for API access
- **User Management**: Centralized user account management
- **Registration Flow**: Secure user registration through Keycloak

### Database Configuration

Keycloak uses the same PostgreSQL cluster as the backend application but with a separate database (`keycloak` vs `congen`). This ensures that Keycloak data is included in PostgreSQL backups, providing complete data protection for both application and authentication data.

## Architecture

### Components

1. **Keycloak Server**: Authentication and authorization server with custom Congen theme
2. **Backend API**: Spring Boot application with OAuth2 resource server
3. **Frontend Application**: React application with react-oidc-context

### Authentication Flow

1. User accesses the frontend application
2. If not authenticated, user is redirected to Keycloak login
3. After successful authentication, user receives JWT token
4. Frontend includes JWT token in API requests
5. Backend validates JWT token and extracts user information
6. Backend applies role-based authorization using `@PreAuthorize` annotations

### Registration Flow

1. User clicks "Create Account" on the frontend
2. User is redirected to Keycloak registration page with custom Congen theme
3. User completes registration on Keycloak (no passwords sent to our backend)
4. User is redirected back to our application with authentication token
5. If user doesn't have a profile yet, they're redirected to profile creation page
6. User completes their fitness profile information
7. Profile is created in our database linked to their Keycloak user ID

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
  local: {
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

- `POST /user/` - Public registration (deprecated, use Keycloak registration)
- `GET /exercises` - Public exercise information
- `GET /exercises/{name}` - Public exercise details

### New Profile Creation Endpoint

After Keycloak registration, users can create their fitness profiles:

- `POST /user/profile` - Create user profile (requires authentication)

This endpoint:
- Requires valid JWT token from Keycloak
- Creates user profile linked to Keycloak user ID
- Validates fitness data (age, height, weight)
- Supports unit conversion (KG/LBS)

## Custom Keycloak Theme

The application includes a custom Keycloak theme with Congen branding:

### Features

- **Congen Logo**: Custom SVG logo with brand colors
- **Modern Design**: Clean, modern interface matching the application
- **Responsive Layout**: Works on desktop and mobile devices
- **Brand Colors**: Uses Congen's primary color (#2236CC)

### Theme Components

- **CSS Styling**: Custom styles for forms, buttons, and layout
- **JavaScript Enhancements**: Smooth transitions and UX improvements
- **FreeMarker Templates**: Custom login and registration templates

## Security Benefits

### OAuth2 Authorization Code Flow

- **No Password Handling**: Passwords are never sent to our backend
- **Secure Token Exchange**: Uses authorization codes instead of direct credentials
- **Token Validation**: JWT tokens are cryptographically verified
- **Automatic Token Refresh**: Handled by react-oidc-context

### User Registration Security

- **Keycloak-Managed Passwords**: All password policies enforced by Keycloak
- **Email Verification**: Optional email verification through Keycloak
- **Account Lockout**: Automatic account lockout for failed attempts
- **Password Policies**: Configurable password strength requirements

## Troubleshooting

### Common Issues

1. **Registration Redirect Issues**
   - Ensure Keycloak registration is enabled
   - Check redirect URIs include `/profile`
   - Verify custom theme is properly mounted

2. **Authentication Failures**
   - Check JWT token expiration
   - Verify Keycloak realm configuration
   - Ensure client secrets are correct

3. **Profile Creation Issues**
   - Verify user is authenticated
   - Check Keycloak user ID mapping
   - Validate fitness data constraints

### Debugging

Enable debug logging for authentication:

```properties
logging.level.org.springframework.security=DEBUG
logging.level.com.congen=DEBUG
```

## Migration from Old Registration

The old registration endpoint (`POST /user/`) is deprecated. Users should:

1. Use Keycloak registration instead
2. Complete profile creation after authentication
3. No password data is stored in our database

This ensures full compliance with OAuth2 authorization code flow and eliminates password security risks. 