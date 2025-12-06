# Congen Application Terraform Module

This Terraform module handles application-specific resources for the Congen application, including writing database and cache credentials to HashiCorp Vault.

**Note**: This module depends on both the `aws-infrastructure` and `vault` modules. The base infrastructure must be provisioned first, followed by Vault, and then this module can write secrets.

## Features

- **RDS Aurora PostgreSQL**: Creates and manages the Aurora PostgreSQL database cluster
- **ElastiCache Memcached**: Creates and manages the ElastiCache Memcached cluster
- **Vault Secret Management**: Writes database and cache credentials to HashiCorp Vault
- **Vault Secret Injection**: Configures Vault Kubernetes auth role and policy for backend pods to read secrets
- **Route53 DNS**: Creates DNS record pointing to the Application Load Balancer
- **Application-Specific Configuration**: Handles all Congen-specific resource configurations
- **Secure Secret Storage**: All secrets stored in Vault, never in Terraform outputs

## Prerequisites

- Terraform ~> 1.12.2
- AWS account with appropriate permissions
- `aws-infrastructure` module deployed (provides VPC, EKS, and other base resources)
- `vault` module deployed (provides Vault for secret storage)
- Vault provider configured in the calling environment (see Provider Configuration section below)

## Module Structure

```
terraform/modules/congen/
├── terraform.tf              # Terraform version and provider requirements
├── main.tf                   # Local values
├── variables.tf              # Input variables
├── outputs.tf                # Output values
├── rds.tf                    # RDS Aurora PostgreSQL cluster
├── elasticache.tf            # ElastiCache Memcached cluster
├── security.tf                # Security groups for RDS and ElastiCache
├── iam.tf                    # IAM roles for RDS enhanced monitoring
├── monitoring.tf             # CloudWatch alarms for RDS and ElastiCache
├── secrets.tf                # Vault secret writing
├── vault_secret_injection.tf # Vault Kubernetes auth configuration for secret injection
├── route53.tf                # Route53 DNS record for the application
└── README.md                 # This file
```

## Usage

### Complete Example (Staging)

```hcl
# terraform/environments/staging/infrastructure/terraform.tf
terraform {
  required_version = "~> 1.12.2"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    vault = {
      source  = "hashicorp/vault"
      version = "~> 4.0"
    }
    random = {
      source  = "hashicorp/random"
      version = "~> 3.0"
    }
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.23"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.11"
    }
  }
}

# terraform/environments/staging/infrastructure/main.tf
provider "aws" {
  region = "us-east-1"
}

# Step 1: Deploy base infrastructure
module "aws_infrastructure" {
  source = "../../../modules/aws-infrastructure"
  
  environment = "staging"
  project_name = "congen"
  aws_region = "us-east-1"
  availability_zones = ["us-east-1a", "us-east-1b"]
  
  # ... other aws-infrastructure configuration ...
}

# Step 2: Configure Kubernetes and Helm providers (needed for Vault)
provider "kubernetes" {
  host                   = module.aws_infrastructure.eks_cluster_endpoint
  cluster_ca_certificate = base64decode(module.aws_infrastructure.eks_cluster_certificate_authority_data)
  exec {
    api_version = "client.authentication.k8s.io/v1beta1"
    command     = "aws"
    args        = ["eks", "get-token", "--cluster-name", module.aws_infrastructure.eks_cluster_id]
  }
}

provider "helm" {
  kubernetes {
    host                   = module.aws_infrastructure.eks_cluster_endpoint
    cluster_ca_certificate = base64decode(module.aws_infrastructure.eks_cluster_certificate_authority_data)
    exec {
      api_version = "client.authentication.k8s.io/v1beta1"
      command     = "aws"
      args        = ["eks", "get-token", "--cluster-name", module.aws_infrastructure.eks_cluster_id]
    }
  }
}

# Step 3: Deploy Vault
module "vault" {
  source = "../../../modules/vault"
  
  project_name = "congen"
  environment  = "staging"
  aws_region   = "us-east-1"
  
  # EKS cluster information (from aws-infrastructure module)
  eks_cluster_id                        = module.aws_infrastructure.eks_cluster_id
  eks_cluster_endpoint                  = module.aws_infrastructure.eks_cluster_endpoint
  eks_cluster_certificate_authority_data = module.aws_infrastructure.eks_cluster_certificate_authority_data
  eks_cluster_oidc_issuer_url          = module.aws_infrastructure.eks_cluster_oidc_issuer_url
  eks_node_group_id                     = module.aws_infrastructure.eks_node_group_id
  vpc_id                                = module.aws_infrastructure.vpc_id
  
  # Vault configuration
  vault_namespace    = "vault"
  vault_replicas     = 1
  vault_storage_size = "10Gi"
}

# Step 4: Configure Vault provider (needed for congen module)
provider "vault" {
  address         = module.vault.vault_address
  namespace       = ""
  skip_tls_verify = false
  
  auth_login {
    path = "auth/kubernetes/login"
    
    parameters = {
      role = module.vault.vault_kubernetes_role
    }
  }
}

  # Step 5: Deploy application-specific resources (creates RDS, ElastiCache, writes secrets to Vault)
module "congen" {
  source = "../../../modules/congen"
  
  project_name = "congen"
  environment  = "staging"
  
  # Vault configuration (from vault module)
  vault_address                      = module.vault.vault_address
  vault_secret_path_prefix           = module.vault.vault_secret_path_prefix
  vault_kubernetes_auth_backend_path = module.vault.vault_kubernetes_auth_backend_path
  vault_root_token_secret            = module.vault.vault_root_token_secret
  
  # Infrastructure inputs (from aws-infrastructure module)
  vpc_id                      = module.aws_infrastructure.vpc_id
  database_subnet_ids         = module.aws_infrastructure.database_subnet_ids
  private_subnet_ids          = module.aws_infrastructure.private_subnet_ids
  eks_node_security_group_id  = module.aws_infrastructure.eks_node_security_group_id
  availability_zones          = module.aws_infrastructure.availability_zones
  
  # Route53 and ALB (from aws-infrastructure module)
  route53_zone_id = module.aws_infrastructure.route53_zone_id
  domain_name     = module.aws_infrastructure.route53_domain_name
  subdomain       = "staging"
  alb_dns_name    = module.aws_infrastructure.alb_dns_name
  alb_zone_id     = module.aws_infrastructure.alb_zone_id
  
  # RDS configuration
  rds_instance_class              = "db.r6g.large"
  rds_engine_version              = "15.4"
  rds_database_name               = "congen"
  rds_username                    = "postgres"
  rds_backup_retention_period     = 7
  rds_multi_az                    = false
  rds_read_replica_count          = 0
  rds_enable_performance_insights = false
  rds_enable_enhanced_monitoring  = false
  
  # ElastiCache configuration
  elasticache_node_type            = "cache.t3.micro"
  elasticache_num_cache_nodes      = 1
  elasticache_engine_version       = "1.6.20"
  elasticache_auto_minor_version_upgrade = true
  
  # EKS cluster information (from aws-infrastructure module, for Kubernetes resources)
  eks_cluster_endpoint                  = module.aws_infrastructure.eks_cluster_endpoint
  eks_cluster_certificate_authority_data = module.aws_infrastructure.eks_cluster_certificate_authority_data
  eks_node_group_id                     = module.aws_infrastructure.eks_node_group_id
}
```

## Input Variables

### Required Variables

| Name | Description | Type |
|------|-------------|------|
| `project_name` | Project name used for resource naming | `string` |
| `environment` | Environment name (staging or production) | `string` |
| `vault_address` | Vault server address (from vault module) | `string` |
| `vault_secret_path_prefix` | Path prefix for secrets in Vault (from vault module) | `string` |
| `vault_kubernetes_auth_backend_path` | Kubernetes auth backend path in Vault (from vault module) | `string` |
| `vault_root_token_secret` | AWS Secrets Manager secret name for Vault root token (from vault module) | `string` |
| `vpc_id` | VPC ID (from aws-infrastructure module) | `string` |
| `database_subnet_ids` | Database subnet IDs (from aws-infrastructure module) | `list(string)` |
| `private_subnet_ids` | Private subnet IDs (from aws-infrastructure module) | `list(string)` |
| `eks_node_security_group_id` | EKS node security group ID (from aws-infrastructure module) | `string` |
| `availability_zones` | Availability zones (from aws-infrastructure module) | `list(string)` |
| `route53_zone_id` | Route53 hosted zone ID (from aws-infrastructure module) | `string` |
| `domain_name` | Domain name (from aws-infrastructure module) | `string` |
| `alb_dns_name` | ALB DNS name (from aws-infrastructure module) | `string` |
| `alb_zone_id` | ALB zone ID (from aws-infrastructure module) | `string` |
| `eks_cluster_endpoint` | EKS cluster API endpoint (from aws-infrastructure module) | `string` |
| `eks_cluster_certificate_authority_data` | Base64 encoded certificate data for EKS cluster (from aws-infrastructure module) | `string` |
| `eks_node_group_id` | EKS node group ID (from aws-infrastructure module, for dependency) | `string` |

### Optional Variables

#### Route53 Configuration

| Name | Description | Type | Default |
|------|-------------|------|---------|
| `subdomain` | Subdomain for the application (e.g., staging, www, or empty for root) | `string` | `""` |

#### RDS Configuration

| Name | Description | Type | Default |
|------|-------------|------|---------|
| `rds_instance_class` | RDS instance class (e.g., db.r6g.large for staging, db.r6g.xlarge for production) | `string` | `"db.r6g.large"` |
| `rds_engine_version` | PostgreSQL engine version for Aurora | `string` | `"15.4"` |
| `rds_database_name` | Database name | `string` | `"congen"` |
| `rds_username` | Master username for RDS | `string` (sensitive) | `"postgres"` |
| `rds_port` | PostgreSQL port number | `number` | `5432` |
| `rds_backup_retention_period` | Days to retain backups (7 for staging, 30 for production) | `number` | `7` |
| `rds_preferred_maintenance_window` | Preferred maintenance window (e.g., sun:03:00-sun:04:00) | `string` | `"sun:03:00-sun:04:00"` |
| `rds_preferred_backup_window` | Preferred backup window (e.g., 03:00-04:00) | `string` | `"03:00-04:00"` |
| `rds_read_replica_count` | Number of read replicas (0 for staging, 1+ for production) | `number` | `0` |
| `rds_multi_az` | Enable multi-AZ (true for production, false for staging) | `bool` | `false` |
| `rds_deletion_protection` | Enable deletion protection | `bool` | `false` |
| `rds_skip_final_snapshot` | Skip final snapshot on deletion | `bool` | `false` |
| `rds_enable_performance_insights` | Enable Performance Insights (false for staging, true for production) | `bool` | `false` |
| `rds_enable_enhanced_monitoring` | Enable enhanced monitoring | `bool` | `false` |

#### ElastiCache Configuration

| Name | Description | Type | Default |
|------|-------------|------|---------|
| `elasticache_node_type` | Node instance type (e.g., cache.t3.micro for staging, cache.r6g.large for production) | `string` | `"cache.t3.micro"` |
| `elasticache_num_cache_nodes` | Number of cache nodes (1-2 for staging, 3+ for production) | `number` | `1` |
| `elasticache_engine_version` | Memcached engine version | `string` | `"1.6.20"` |
| `elasticache_port` | Port number | `number` | `11211` |
| `elasticache_auto_minor_version_upgrade` | Auto minor version upgrade | `bool` | `true` |
| `elasticache_maintenance_window` | Maintenance window (e.g., sun:05:00-sun:06:00) | `string` | `"sun:05:00-sun:06:00"` |
| `elasticache_snapshot_retention_limit` | Number of days to retain snapshots | `number` | `0` |
| `elasticache_snapshot_window` | Snapshot window (e.g., 03:00-05:00) | `string` | `"03:00-05:00"` |

## Outputs

| Name | Description |
|------|-------------|
| `vault_database_secret_path` | Full path to database secrets in Vault |
| `vault_cache_secret_path` | Full path to cache secrets in Vault |
| `rds_writer_endpoint` | RDS cluster writer endpoint |
| `rds_reader_endpoint` | RDS cluster reader endpoint |
| `rds_database_name` | RDS database name |
| `rds_cluster_id` | RDS cluster identifier |
| `elasticache_configuration_endpoint` | ElastiCache configuration endpoint (for Memcached clusters) |
| `elasticache_port` | ElastiCache port |
| `elasticache_cluster_id` | ElastiCache cluster identifier |
| `rds_security_group_id` | Security group ID for RDS |
| `elasticache_security_group_id` | Security group ID for ElastiCache |
| `kms_key_id` | KMS key ARN used for RDS encryption |
| `kms_key_alias` | KMS key alias for RDS |
| `sns_topic_arn` | ARN of the SNS topic for alerts |
| `route53_record_name` | Route53 record name (FQDN) |

## Provider Configuration

### Vault Provider (REQUIRED)

The Vault provider MUST be configured in your environment's `main.tf` or `providers.tf` file before using this module. The provider configuration uses outputs from the `vault` module:

```hcl
provider "vault" {
  address         = module.vault.vault_address
  namespace       = ""
  skip_tls_verify = false
  
  auth_login {
    path = "auth/kubernetes/login"
    
    parameters = {
      role = module.vault.vault_kubernetes_role
    }
  }
}
```

**Important**: This is a Terraform requirement - providers are always configured at the root level, not in modules.

## Deployment Order

To avoid circular dependencies, deploy modules in this order:

1. **`aws-infrastructure`**: Deploys base AWS infrastructure (VPC, EKS, ALB, Route53, etc.)
2. **`vault`**: Deploys HashiCorp Vault to the EKS cluster (requires EKS from step 1)
3. **`congen`**: Creates RDS and ElastiCache, writes application secrets to Vault, creates Route53 DNS record (requires both infrastructure and Vault)

This order ensures:
- EKS cluster exists before Vault deployment
- Vault exists before writing secrets
- Base infrastructure exists before creating application resources
- No circular dependencies

## Secret Structure

Secrets are stored in Vault with the following structure:

### Database Secrets (`secret/data/{vault_secret_path_prefix}/{environment}/database`)

- `username`: Database username
- `password`: Database password (from aws-infrastructure module)
- `database_name`: Database name
- `writer_endpoint`: RDS writer endpoint
- `reader_endpoint`: RDS reader endpoint
- `port`: Database port (default: 5432)

### Cache Secrets (`secret/data/{vault_secret_path_prefix}/{environment}/cache`)

- `configuration_endpoint`: ElastiCache configuration endpoint
- `port`: ElastiCache port (default: 11211)
- `use_elasticache`: Boolean as string ("true")

## Security Considerations

1. **Secret Storage**: All secrets are stored in Vault, never in Terraform outputs (except sensitive outputs for module communication)
2. **Sensitive Variables**: RDS username and password are marked as sensitive
3. **Vault Authentication**: Uses Kubernetes service account authentication with short TTLs
4. **Least Privilege**: Vault policies restrict access to only necessary paths

## Troubleshooting

### Vault Authentication Issues

**Error**: `Error authenticating to Vault`

**Solution**:
1. Verify Vault is accessible from Terraform execution environment
2. Check Kubernetes service account exists and has correct permissions
3. Verify Vault Kubernetes auth is configured correctly
4. Check Vault role has correct policies attached
5. Ensure Vault provider is configured before the congen module

### Secret Writing Issues

**Error**: `Error writing secret to Vault`

**Solution**:
1. Verify Vault is initialized and unsealed
2. Check Vault KV secrets engine v2 is enabled at mount path `secret/`
3. Verify Terraform has correct Vault policy permissions
4. Check secret path prefix is correct

## Related Modules

- **`aws-infrastructure`**: Provides base AWS infrastructure (VPC, EKS, ALB, Route53 hosted zone, etc.)
- **`vault`**: Deploys HashiCorp Vault to the EKS cluster

## License

This module is part of the Congen project.

