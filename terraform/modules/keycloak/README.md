# Keycloak Module

This module provides a reusable Terraform configuration for provisioning Keycloak resources for the Congen application.

## Overview

The Keycloak module creates a complete Keycloak realm configuration including:
- Realm and roles
- Frontend and backend clients
- Service account for backend operations
- Default admin user

## Usage

```hcl
module "keycloak" {
  source = "../../modules/keycloak"

  # Required variables
  backend_service_password = var.backend_service_password
  admin_password          = var.admin_password

  # Optional overrides
  realm_display_name = "Congen Production"
  frontend_redirect_uris = [
    "https://congen.com/*",
    "https://www.congen.com/*"
  ]
}
```

## Inputs

### Required Variables

| Name | Description | Type | Default |
|------|-------------|------|---------|
| `backend_service_password` | Password for backend service account | `string` | n/a |
| `admin_password` | Password for default admin user | `string` | n/a |

### Optional Variables

| Name | Description | Type | Default |
|------|-------------|------|---------|
| `realm_name` | Keycloak realm name | `string` | `"congen"` |
| `realm_display_name` | Keycloak realm display name | `string` | `"Congen"` |
| `realm_display_name_html` | Keycloak realm display name HTML | `string` | `"<div class=\"kc-logo-text\"><span>Congen</span></div>"` |
| `backend_client_id` | Backend client ID | `string` | `"congen-backend"` |
| `backend_client_name` | Backend client name | `string` | `"Congen Backend"` |
| `frontend_client_id` | Frontend client ID | `string` | `"congen-frontend"` |
| `frontend_client_name` | Frontend client name | `string` | `"Congen Frontend"` |
| `frontend_redirect_uris` | Frontend client redirect URIs | `list(string)` | `["http://localhost:3000/*", "http://localhost:3000", "https://staging.congen.com/*", "https://congen.com/*"]` |
| `frontend_web_origins` | Frontend client web origins | `list(string)` | `["http://localhost:3000", "https://staging.congen.com", "https://congen.com"]` |
| `backend_service_username` | Backend service account username | `string` | `"congen-backend-service"` |
| `backend_service_email` | Backend service account email | `string` | `"backend-service@congen.com"` |
| `backend_service_first_name` | Backend service account first name | `string` | `"Congen"` |
| `backend_service_last_name` | Backend service account last name | `string` | `"Backend Service"` |
| `admin_username` | Admin username | `string` | `"admin"` |
| `admin_email` | Admin email | `string` | `"admin@congen.com"` |
| `admin_first_name` | Admin first name | `string` | `"Admin"` |
| `admin_last_name` | Admin last name | `string` | `"User"` |

## Outputs

| Name | Description |
|------|-------------|
| `realm_id` | Keycloak realm ID |
| `realm_name` | Keycloak realm name |
| `backend_client_id` | Backend client ID |
| `frontend_client_id` | Frontend client ID |
| `backend_service_username` | Backend service account username |
| `admin_username` | Admin username |

## Resources Created

### Realm and Roles
- **Realm**: `congen` (configurable)
- **Roles**: `user`, `admin`, `service`

### Clients
- **Backend Client**: `congen-backend` (confidential, service accounts enabled)
- **Frontend Client**: `congen-frontend` (public, standard flow enabled)

### Users
- **Service Account**: `congen-backend-service` (assigned `service` role)
- **Admin User**: Configurable admin user (assigned `admin` and `user` roles)

## Security Features

### Service Account Authentication
- Dedicated service account for backend operations
- Principle of least privilege
- Separate from admin credentials
- Secure credential management

### Role-Based Access Control
- **user**: Regular user with access to personal data
- **admin**: Administrator with full access
- **service**: Service account for automated operations

### Client Configuration
- **Backend Client**: Confidential client with service accounts
- **Frontend Client**: Public client with standard OAuth2 flow
- Proper redirect URI validation
- CORS configuration

## Examples

### Local Development
```hcl
module "keycloak" {
  source = "../../modules/keycloak"

  backend_service_password = "local-service-password"
  admin_password          = "local-admin-password"

  frontend_redirect_uris = [
    "http://localhost:3000/*",
    "http://localhost:3000"
  ]
  frontend_web_origins = [
    "http://localhost:3000"
  ]
}
```

### Production
```hcl
module "keycloak" {
  source = "../../modules/keycloak"

  backend_service_password = var.production_service_password
  admin_password          = var.production_admin_password

  realm_display_name = "Congen Production"
  frontend_redirect_uris = [
    "https://congen.com/*",
    "https://www.congen.com/*"
  ]
  frontend_web_origins = [
    "https://congen.com",
    "https://www.congen.com"
  ]
}
```

## Dependencies

- Terraform = 1.12.2
- Keycloak provider = 5.3.0
- Keycloak server running and accessible

## Notes

- All sensitive variables are marked as `sensitive = true`
- Service account credentials should be rotated regularly
- Production deployments should use strong, unique passwords
- HTTPS is required for production environments 