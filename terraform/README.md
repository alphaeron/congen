# Congen Infrastructure

This directory contains Terraform configurations for provisioning the Congen application infrastructure across different environments.

## Overview

Congen is a workout generation application that uses:
- **Keycloak**: Authentication and authorization
- **PostgreSQL**: Database for application data
- **Kubernetes**: Container orchestration
- **Terraform**: Infrastructure as Code

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │    Backend      │    │   Keycloak      │
│   (React)       │◄──►│   (Spring Boot) │◄──►│   (Auth Server) │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                                ▼
                       ┌─────────────────┐
                       │   PostgreSQL    │
                       │   (Database)    │
                       └─────────────────┘
```

## Directory Structure

```
terraform/
├── modules/
│   └── keycloak/           # Reusable Keycloak module
├── environments/
│   ├── local/              # Local development (minikube)
│   │   ├── keycloak/       # Keycloak infrastructure
│   │   └── infrastructure/  # Cloud resources (future)
│   ├── staging/            # Staging environment
│   │   ├── keycloak/       # Keycloak infrastructure
│   │   └── infrastructure/ # Cloud resources (future)
│   └── production/         # Production environment
│       ├── keycloak/       # Keycloak infrastructure
│       └── infrastructure/  # Cloud resources (future)
└── README.md
```

## Quick Start

### Prerequisites

- Terraform = 1.12.2
- Kubernetes cluster (minikube, kind, or cloud provider)
- kubectl configured
- Keycloak server running

### Local Development Setup

1. **Start Kubernetes Cluster**:
   ```bash
   minikube start
   ```

2. **Deploy Application and Keycloak**:
   ```bash
   # Deploy all services to local Kubernetes (includes Keycloak)
   ./gradlew deployAll -Penvironment=local
   ```

3. **Deploy Terraform Configuration**:
   ```bash
   cd terraform/environments/local/keycloak
   cp terraform.tfvars.example terraform.tfvars
   # Edit terraform.tfvars with your passwords
   terraform init
   terraform apply
   ```

4. **Start Applications**:
   ```bash
   # Backend
   ./gradlew :backend:bootRun
   
   # Frontend (in another terminal)
   cd frontend && npm start
   ```

## Environment Deployments

### Local Development

**Purpose**: Development and testing on local machine
**Configuration**:
- Keycloak URL: `http://localhost:8080`
- Frontend redirects: `http://localhost:3000/*`
- Database: Local PostgreSQL
- Security: HTTP (acceptable for local)

**Deploy**:
```bash
# Deploy application to local Kubernetes
./gradlew deployAll -Penvironment=local

# Deploy Terraform configuration
cd terraform/environments/local/keycloak
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars
terraform init && terraform apply
```

### Local-Persist Development

**Purpose**: Development and testing on local machine with persistent storage
**Configuration**:
- Keycloak URL: `http://localhost:8080`
- Frontend redirects: `http://localhost:3000/*`
- Database: Local PostgreSQL with persistent hostPath storage
- Security: HTTP (acceptable for local)
- Storage: Data persists between deployments

**Deploy**:
```bash
# Deploy application to local Kubernetes with persistent storage
./gradlew deployAll -Penvironment=local-persist -PmountDir=/path/to/data

# Deploy Terraform configuration (uses same directory as local)
cd terraform/environments/local/keycloak
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars
terraform init && terraform apply
```

### Staging Environment

**Purpose**: Pre-production testing and validation
**Configuration**:
- Keycloak URL: `https://keycloak.staging.congen.com`
- Frontend redirects: `https://staging.congen.com/*`
- Database: Staging PostgreSQL cluster
- Security: HTTPS, staging credentials

**Deploy**:
```bash
# Deploy application to staging Kubernetes
./gradlew deployAll -Penvironment=staging

# Deploy Terraform configuration
cd terraform/environments/staging/keycloak
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with staging credentials
terraform init && terraform apply
```

### Production Environment

**Purpose**: Live production environment
**Configuration**:
- Keycloak URL: `https://keycloak.congen.com`
- Frontend redirects: `https://congen.com/*`, `https://www.congen.com/*`
- Database: Production PostgreSQL cluster
- Security: HTTPS, strong passwords, monitoring

**Deploy**:
```bash
# Deploy application to production Kubernetes
./gradlew deployAll -Penvironment=production

# Deploy Terraform configuration
cd terraform/environments/production/keycloak
cp terraform.tfvars.example terraform.tfvars
# Edit terraform.tfvars with strong production passwords
terraform init && terraform apply
```

## Infrastructure Components

### Keycloak Authentication

- **Realm**: `congen`
- **Roles**: `user`, `admin`, `service`
- **Clients**: Frontend (public) and Backend (confidential)
- **Service Account**: Dedicated account for backend operations

### Database

- **PostgreSQL**: Primary database
- **Liquibase**: Database migrations
- **Connection Pooling**: Optimized for performance

### Security

- **JWT Tokens**: Secure authentication
- **Role-Based Access**: Fine-grained permissions
- **HTTPS**: Required for staging/production
- **CORS**: Properly configured for each environment

## Development Workflow

### Adding New Features

1. **Local Development**:
   ```bash
   # Start local environment
   ./gradlew deployAll -Penvironment=local
   cd terraform/environments/local/keycloak && terraform apply
   
   # Develop and test
   ./gradlew :backend:bootRun
   cd frontend && npm start
   ```

2. **Staging Validation**:
   ```bash
   # Deploy to staging
   ./gradlew deployAll -Penvironment=staging
   cd terraform/environments/staging/keycloak && terraform apply
   
   # Test in staging environment
   ```

3. **Production Deployment**:
   ```bash
   # Deploy to production
   ./gradlew deployAll -Penvironment=production
   cd terraform/environments/production/keycloak && terraform apply
   ```

### Database Changes

1. **Create Migration**:
   ```bash
   # Add new Liquibase migration file
   # Update database schema
   ```

2. **Test Locally**:
   ```bash
   # Run migrations locally
   ./gradlew :backend:bootRun
   ```

3. **Deploy**:
   ```bash
   # Migrations run automatically on application startup
   # Deploy application to staging/production
   ```

## Monitoring and Logging

### Keycloak Monitoring

- **Admin Console**: `http://localhost:8080` (local)
- **Health Checks**: `/health` endpoint
- **Audit Logs**: User authentication and authorization

### Application Monitoring

- **Health Endpoints**: `/api/v1/health/**`
- **Metrics**: Prometheus metrics (if configured)
- **Logs**: Structured logging with SLF4J

## Troubleshooting

### Common Issues

1. **Keycloak Connection Failed**:
   ```bash
   # Check if Keycloak is running
   ./gradlew deployAll -Penvironment=local
   
   # Check application status
   ./gradlew :backend:bootRun
   ```

2. **Database Connection Issues**:
   ```bash
   # Check database status
   ./gradlew deployAll -Penvironment=local
   
   # Check application logs
   ./gradlew :backend:bootRun
   ```

3. **Terraform State Issues**:
   ```bash
   # Check state
   terraform show
   
   # Refresh state
   terraform refresh
   ```

### Debugging

Enable debug logging:

```bash
# Terraform
export TF_LOG=DEBUG
terraform apply

# Application
./gradlew :backend:bootRun --debug
```

## Security Best Practices

### Local Development
- Use simple passwords for convenience
- HTTP is acceptable for local development
- No sensitive data in local environment

### Staging/Production
- Use strong, unique passwords
- HTTPS required for all communication
- Regular credential rotation
- Monitor access logs
- Implement proper backup strategies

### Credential Management
- Never commit `terraform.tfvars` files
- Use Kubernetes secrets for sensitive data
- Rotate credentials regularly
- Use environment-specific credentials

## Contributing

### Adding New Environments

1. **Create Environment Directory**:
   ```bash
   mkdir -p terraform/environments/new-environment/keycloak
   mkdir -p terraform/environments/new-environment/infrastructure
   cp -r terraform/environments/staging/keycloak/* terraform/environments/new-environment/keycloak/
   ```

2. **Customize Configuration**:
   - Update `variables.tf` with environment-specific defaults
   - Modify `terraform.tfvars.example` with appropriate placeholders
   - Update URLs and redirect URIs

3. **Test Deployment**:
   ```bash
   cd terraform/environments/new-environment/keycloak
   terraform init && terraform plan
   ```

### Adding New Modules

1. **Create Module Directory**:
   ```bash
   mkdir -p terraform/modules/new-module
   ```

2. **Add Module Files**:
   - `main.tf`: Main resource definitions
   - `variables.tf`: Input variables
   - `outputs.tf`: Output values
   - `README.md`: Module documentation

3. **Use Module in Environments**:
   ```hcl
   module "new_module" {
     source = "../../../modules/new-module"
     # ... variables
   }
   ```

## Support

For issues with infrastructure:

1. Check the troubleshooting section
2. Review Terraform logs: `terraform logs`
3. Check application logs: `./gradlew :backend:bootRun`
4. Verify configuration in Keycloak admin console
5. Check application health endpoints: `/api/v1/health/**` 