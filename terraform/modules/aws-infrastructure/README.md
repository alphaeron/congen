# AWS Infrastructure Terraform Module

This Terraform module provisions the base AWS infrastructure for running applications. It includes VPC networking, EKS cluster, Application Load Balancer, Route53 DNS, security groups, IAM roles, and CloudWatch monitoring.

**Note**: This module provides base infrastructure only. Application-specific resources (RDS, ElastiCache, and Vault secret writing) are handled by the separate `congen` module.

## Features

- **Secure VPC Architecture**: Multi-AZ deployment with public, private, and isolated database subnets
- **Maximum Security**: Everything possible is behind the VPC; only ALB is public (required for application access)
- **EKS Cluster**: Managed Kubernetes cluster with node groups, add-ons, and encryption (private endpoint by default)
- **Application Load Balancer**: HTTPS-enabled ALB with automated ingress controller deployment (public, required for internet access)
- **Route53 DNS**: Hosted zone for the application domain
- **Security**: Encryption at rest and in transit, least-privilege security groups, VPC flow logs, private endpoints
- **Monitoring**: VPC flow logs and CloudTrail for audit logging
- **Cost Optimization**: Parameterizable for staging (cost-optimized) and production (high availability)

## Prerequisites

- Terraform ~> 1.12.2
- AWS account with appropriate permissions

## Module Structure

```
terraform/modules/aws-infrastructure/
├── terraform.tf             # Terraform version and provider requirements
├── main.tf                  # Local values and data sources
├── variables.tf            # Input variables with descriptions
├── outputs.tf               # Output values (endpoints, IDs, etc.)
├── vpc.tf                   # VPC, subnets, route tables, NAT gateways, VPC endpoints
├── security.tf              # Security groups, network ACLs
├── eks.tf                   # EKS cluster, node groups, and add-ons
├── acm.tf                   # ACM certificate and validation
├── alb.tf                   # Application Load Balancer, target groups, and listeners
├── route53.tf               # Route53 hosted zone
├── kms.tf                   # KMS key for EKS cluster secrets encryption
├── ingress.tf                # Ingress controller deployment and ALB target registration
├── monitoring.tf            # CloudWatch logs (VPC flow logs, CloudTrail)
├── iam.tf                   # IAM roles and policies
└── README.md                # This file
```

## Usage

### Staging Environment

```hcl
# terraform/environments/staging/infrastructure/terraform.tf
terraform {
  required_version = "~> 1.12.2"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
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

# Kubernetes and Helm providers configured after module (see Provider Configuration section)

module "aws_infrastructure" {
  source = "../../../modules/aws-infrastructure"
  
  environment = "staging"
  project_name = "congen"
  aws_region = "us-east-1"
  availability_zones = ["us-east-1a", "us-east-1b"]
  
  single_nat_gateway = true
  enable_cross_region_backup = false
  
  # EKS configuration
  eks_cluster_version = "1.28"
  eks_node_instance_types = ["t3.medium"]
  eks_node_desired_size = 1
  eks_node_min_size = 1
  eks_node_max_size = 2
  
  # ALB and Route53 configuration
  domain_name = "congen.com"
  subdomain = "staging"
  alb_enable_deletion_protection = false
}
```

### Production Environment

```hcl
# terraform/environments/production/infrastructure/terraform.tf
terraform {
  required_version = "~> 1.12.2"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
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

# terraform/environments/production/infrastructure/main.tf
provider "aws" {
  region = "us-east-1"
}

# Kubernetes and Helm providers configured after module (see Provider Configuration section)

module "aws_infrastructure" {
  source = "../../../modules/aws-infrastructure"
  
  environment = "production"
  project_name = "congen"
  aws_region = "us-east-1"
  availability_zones = ["us-east-1a", "us-east-1b", "us-east-1c"]
  
  single_nat_gateway = false
  enable_cross_region_backup = true
  backup_region = "us-west-2"
  
  # EKS configuration
  eks_cluster_version = "1.28"
  eks_node_instance_types = ["t3.large", "t3.xlarge"]
  eks_node_desired_size = 2
  eks_node_min_size = 2
  eks_node_max_size = 4
  
  # ALB and Route53 configuration
  domain_name = "congen.com"
  subdomain = ""
  alb_enable_deletion_protection = true
  
  # Monitoring
  sns_alert_email = "alerts@congen.com"
}
```

## Input Variables

### Required Variables

| Name | Description | Type |
|------|-------------|------|
| `environment` | Environment name (staging or production) | `string` |
| `aws_region` | AWS region for resources | `string` |
| `availability_zones` | List of availability zones to use (must specify at least 2 for high availability) | `list(string)` |
| `domain_name` | Domain name for Route53 hosted zone (e.g., congen.com) | `string` |

### Optional Variables

#### Environment Configuration

| Name | Description | Type | Default |
|------|-------------|------|---------|
| `project_name` | Project name used for resource naming | `string` | `"congen"` |

#### VPC Configuration

| Name | Description | Type | Default |
|------|-------------|------|---------|
| `vpc_cidr` | CIDR block for the VPC | `string` | `"10.0.0.0/16"` |
| `public_subnet_cidrs` | CIDR blocks for public subnets | `list(string)` | `["10.0.1.0/24", "10.0.2.0/24"]` |
| `private_subnet_cidrs` | CIDR blocks for private subnets | `list(string)` | `["10.0.11.0/24", "10.0.12.0/24"]` |
| `database_subnet_cidrs` | CIDR blocks for database subnets | `list(string)` | `["10.0.21.0/24", "10.0.22.0/24"]` |

#### EKS Configuration

| Name | Description | Type | Default |
|------|-------------|------|---------|
| `eks_cluster_version` | Kubernetes version for EKS cluster | `string` | `"1.28"` |
| `eks_node_instance_types` | EC2 instance types for EKS node groups | `list(string)` | `["t3.medium"]` |
| `eks_node_desired_size` | Desired number of nodes per AZ | `number` | `1` |
| `eks_node_min_size` | Minimum number of nodes per AZ | `number` | `1` |
| `eks_node_max_size` | Maximum number of nodes per AZ | `number` | `2` |
| `eks_node_disk_size` | Disk size in GB for EKS nodes | `number` | `20` |
| `eks_enable_cluster_logging` | Enable EKS cluster logging | `list(string)` | `["api", "audit", "authenticator"]` |
| `eks_cluster_log_retention_days` | CloudWatch log retention in days for EKS cluster logs | `number` | `7` |
| `eks_endpoint_private_access` | Enable private access to the EKS cluster API endpoint (recommended for security) | `bool` | `true` |
| `eks_endpoint_public_access` | Enable public access to the EKS cluster API endpoint (set to true only if needed for kubectl access from outside VPC) | `bool` | `false` |
| `eks_endpoint_public_access_cidrs` | List of CIDR blocks that can access the EKS cluster API endpoint when public access is enabled (empty list means all IPs, not recommended) | `list(string)` | `[]` |
| `eks_node_group_max_unavailable` | Maximum number of nodes unavailable during node group update | `number` | `1` |

#### ALB and Route53 Configuration

| Name | Description | Type | Default |
|------|-------------|------|---------|
| `subdomain` | Subdomain for the application (e.g., staging, www, or empty for root) | `string` | `""` |
| `alb_enable_deletion_protection` | Enable deletion protection for ALB | `bool` | `false` |
| `alb_idle_timeout` | Idle timeout in seconds for ALB | `number` | `60` |
| `acm_certificate_validation_ttl` | TTL in seconds for ACM certificate validation DNS records | `number` | `60` |
| `acm_certificate_validation_timeout` | Timeout for ACM certificate validation (e.g., 5m, 10m) | `string` | `"5m"` |

#### Ingress Controller Configuration

| Name | Description | Type | Default |
|------|-------------|------|---------|
| `ingress_controller_namespace` | Kubernetes namespace where the ingress controller is deployed | `string` | `"ingress-nginx"` |
| `ingress_controller_service_name` | Name of the ingress controller service | `string` | `"ingress-nginx-controller"` |
| `ingress_controller_service_port` | Port number of the ingress controller service | `number` | `80` |
| `ingress_controller_replica_count` | Number of replicas for the ingress controller | `number` | `1` |
| `ingress_controller_helm_chart_version` | Version of the nginx-ingress Helm chart | `string` | `"4.8.3"` |
| `ingress_controller_cpu_request` | CPU request for the ingress controller pods | `string` | `"100m"` |
| `ingress_controller_cpu_limit` | CPU limit for the ingress controller pods | `string` | `"500m"` |
| `ingress_controller_memory_request` | Memory request for the ingress controller pods | `string` | `"128Mi"` |
| `ingress_controller_memory_limit` | Memory limit for the ingress controller pods | `string` | `"512Mi"` |
| `ingress_controller_enable_pdb` | Enable Pod Disruption Budget for the ingress controller | `bool` | `false` |
| `ingress_controller_pdb_min_available` | Minimum number of available pods for the ingress controller PDB (only used if PDB is enabled) | `number` | `1` |
| `ingress_controller_enable_pod_anti_affinity` | Enable pod anti-affinity for the ingress controller to spread pods across nodes | `bool` | `false` |

#### Security Configuration

| Name | Description | Type | Default |
|------|-------------|------|---------|
| `enable_vpc_flow_logs` | Enable VPC flow logs | `bool` | `true` |
| `vpc_flow_log_retention_days` | CloudWatch log retention in days for VPC flow logs | `number` | `7` |
| `enable_cloudtrail` | Enable CloudTrail | `bool` | `true` |
| `cloudtrail_log_retention_days` | CloudWatch log retention in days for CloudTrail logs | `number` | `7` |
| `allowed_cidr_blocks` | CIDR blocks allowed to access resources | `list(string)` | `[]` |

#### Cost Optimization

| Name | Description | Type | Default |
|------|-------------|------|---------|
| `enable_nat_gateway` | Enable NAT Gateway | `bool` | `true` |
| `single_nat_gateway` | Use single NAT Gateway | `bool` | `false` |
| `enable_cross_region_backup` | Enable cross-region backup | `bool` | `false` |
| `backup_region` | AWS region for cross-region backups | `string` | `null` |

#### Monitoring

| Name | Description | Type | Default |
|------|-------------|------|---------|
| `sns_alert_email` | Email for CloudWatch alarms | `string` | `null` |

## Outputs

| Name | Description |
|------|-------------|
| `vpc_id` | ID of the VPC |
| `vpc_cidr` | CIDR block of the VPC |
| `public_subnet_ids` | IDs of the public subnets |
| `private_subnet_ids` | IDs of the private subnets |
| `database_subnet_ids` | IDs of the database subnets |
| `eks_kms_key_id` | KMS key ARN used for EKS cluster secrets encryption |
| `eks_kms_key_alias` | KMS key alias for EKS |
| `sns_topic_arn` | ARN of the SNS topic for alerts |
| `cloudwatch_log_group_vpc_flow_logs` | CloudWatch log group for VPC flow logs |
| `availability_zones` | List of availability zones used |
| `eks_cluster_id` | EKS cluster ID |
| `eks_cluster_arn` | EKS cluster ARN |
| `eks_cluster_endpoint` | EKS cluster API endpoint |
| `eks_cluster_security_group_id` | Security group ID for EKS cluster |
| `eks_node_security_group_id` | Security group ID for EKS node group |
| `eks_node_group_id` | EKS node group ID |
| `alb_dns_name` | DNS name of the Application Load Balancer |
| `alb_arn` | ARN of the Application Load Balancer |
| `alb_zone_id` | Zone ID of the Application Load Balancer |
| `alb_security_group_id` | Security group ID for ALB |
| `alb_target_group_ingress_controller_arn` | ARN of the ingress controller target group |
| `route53_zone_id` | Route53 hosted zone ID |
| `route53_domain_name` | Route53 domain name |
| `route53_name_servers` | Route53 hosted zone name servers (for domain registrar configuration) |
| `acm_certificate_arn` | ARN of the ACM certificate for the ALB |
| `acm_certificate_domain` | Domain name of the ACM certificate |
| `eks_cluster_certificate_authority_data` | Base64 encoded certificate data for EKS cluster |
| `eks_cluster_oidc_issuer_url` | EKS cluster OIDC issuer URL |

## Related Modules

This module provides base infrastructure. For application-specific resources, use the `congen` module:

- **`congen` module**: Handles application-specific resources such as writing secrets to Vault
- **`vault` module**: Deploys HashiCorp Vault to the EKS cluster

## EKS, ALB, and Route53 Configuration

### EKS Cluster

The module provisions a fully managed EKS cluster with:

- **Cluster Features**:
  - Managed Kubernetes control plane
  - Encryption at rest for Kubernetes secrets using KMS
  - Cluster logging (API, audit, authenticator)
  - VPC CNI, CoreDNS, and kube-proxy add-ons

- **Node Group**:
  - Auto-scaling node group in private subnets
  - Configurable instance types and sizes
  - Automatic updates with max unavailable nodes

- **Security**:
  - Dedicated security groups for cluster and nodes
  - **Cluster endpoint is private-only by default** (accessible only from within VPC)
  - Public endpoint access disabled by default (can be enabled if needed for kubectl access from outside VPC)
  - If public access is enabled, it should be restricted to specific CIDR blocks (not 0.0.0.0/0)

### Application Load Balancer (ALB)

The ALB provides HTTPS termination and routes all traffic to a single Kubernetes ingress controller:

- **ACM Certificate**:
  - Automatically provisions ACM certificate for the domain/subdomain
  - Uses DNS validation with Route53 records
  - Certificate validation happens automatically
  - Certificate covers both subdomain (if specified) and root domain

- **Listeners**:
  - HTTPS (443): Primary listener with automatically provisioned ACM certificate, forwards to ingress controller
  - HTTP (80): Redirects to HTTPS

- **Target Group**:
  - **Ingress Controller**: Single target group routes all HTTPS traffic to the Kubernetes ingress controller service
  - The ingress controller handles internal routing to frontend, backend, and keycloak services based on path rules
  - Health check configured for ingress controller health endpoint

- **Features**:
  - Single point of entry - all traffic routes through ingress controller
  - Kubernetes ingress handles path-based routing internally
  - Access logs stored in S3 bucket
  - Cross-zone load balancing enabled
  - HTTP/2 enabled

- **Security**:
  - HTTPS-only communication (HTTP redirects to HTTPS)
  - Security group restricts ingress to HTTPS (443) from internet
  - Egress restricted to ingress controller service port on EKS nodes
  - TLS termination at ALB, HTTP within VPC (secure internal network)

### Route53 DNS

- **Hosted Zone**: Automatically creates a Route53 hosted zone for the domain
- **Name Servers**: Outputs name servers that must be configured at your domain registrar
- **Note**: DNS records pointing to the ALB are created by application modules (e.g., `congen` module)

### Security Architecture

**Public Resources (Minimum Required)**:
- **Application Load Balancer (ALB)**: Public-facing, required for internet access to the application
- **NAT Gateway**: In public subnets, required for private subnet egress to internet

**Private Resources (Everything Else)**:
- **EKS Cluster Endpoint**: Private-only by default (accessible only from within VPC)
- **EKS Node Groups**: Deployed in private subnets
- **RDS Database**: Private subnets, not publicly accessible
- **ElastiCache**: Private subnets, not publicly accessible
- **Vault**: Private subnets, not publicly accessible
- **VPC Endpoints**: Private subnets for S3, CloudWatch Logs, ECR
- **All Application Pods**: Run in private subnets via EKS nodes

**Access Methods**:
- **Application Access**: Internet → ALB (public) → Ingress Controller → Application Pods (private)
- **Kubernetes Access**: From within VPC (via bastion, VPN, or AWS Systems Manager Session Manager) or enable public endpoint with restricted CIDR blocks if needed

### Prerequisites

1. **Domain Name**:
   - You must own the domain name you're using
   - After provisioning, update your domain registrar's name servers with the values from the `route53_name_servers` output
   - The Route53 hosted zone is automatically created and managed by Terraform

2. **Kubernetes Services**:
   - Deploy your application services (frontend, backend, keycloak) to the EKS cluster
   - Services should be of type `ClusterIP`
   - Configure Kubernetes Ingress resource to route traffic to your services (see `k8s/base/stage-8/ingress.yaml`)

### Automated Ingress Controller Deployment

The module automatically deploys the nginx-ingress controller to your EKS cluster and registers it with the ALB target group:

- **Deployment**: Uses Helm to deploy nginx-ingress controller in the `ingress-nginx` namespace
- **Configuration**: 
  - Service type: `ClusterIP` (internal only)
  - Replicas: 1 for staging, 2 for production (with pod anti-affinity)
  - Pod Disruption Budget: Enabled for production
- **ALB Registration**: Automatically discovers ingress controller pod IPs and registers them with the ALB target group
- **No Manual Steps**: Everything is automated as part of `terraform apply`

### Kubernetes Ingress Configuration

The Kubernetes Ingress resource (in `k8s/base/stage-8/ingress.yaml`) should be configured to route traffic to your services:

```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: congen-ingress
  namespace: congen
  annotations:
    nginx.ingress.kubernetes.io/ssl-redirect: "false"  # ALB handles TLS
spec:
  ingressClassName: nginx
  rules:
  - host: staging.congen.com  # or your domain
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: frontend
            port:
              number: 80
      - path: /api/
        pathType: Prefix
        backend:
          service:
            name: backend
            port:
              number: 8888
      - path: /auth/
        pathType: Prefix
        backend:
          service:
            name: keycloak
            port:
              number: 8080
```

**Note**: The ingress controller receives HTTP traffic from the ALB (TLS is terminated at the ALB). The ingress controller then routes traffic internally to your services based on the path rules.

## Security Considerations

1. **Network Security**:
   - Security groups follow least-privilege principle
   - Database subnets have no internet access
   - VPC endpoints used to avoid internet routing
   - VPC flow logs enabled for network monitoring
   - EKS nodes in private subnets with restricted egress
   - ALB in public subnets with HTTPS-only ingress

2. **Data Security**:
   - All data encrypted at rest using KMS
   - All data encrypted in transit (SSL/TLS)

3. **Access Control**:
   - IAM roles follow least-privilege principle
   - CloudTrail enabled for audit logging

4. **Backup and Recovery**:
   - Cross-region backup support (optional)

## Monitoring

- VPC flow logs for network monitoring
- CloudTrail for audit logging
- CloudWatch log groups for EKS cluster logs

## Cost Optimization

### Staging Environment
- Single NAT Gateway
- No cross-region backups
- Smaller EKS node instance types

### Production Environment
- Multiple NAT Gateways (one per AZ)
- Cross-region backups enabled
- Larger EKS node instance types

## Troubleshooting

### Terraform State Issues

**Error**: `State file locked` or `State file not found`

**Solution**:
1. Use remote state backend (S3) with versioning and encryption
2. Configure state locking with DynamoDB
3. Never commit state files to version control

## Examples

See the `terraform/environments/staging/infrastructure/` and `terraform/environments/production/infrastructure/` directories for complete usage examples.

## Contributing

When adding new features:
1. Update `variables.tf` with new input variables
2. Update `outputs.tf` with new outputs
3. Update this README with new features and examples
4. Test with both staging and production configurations

## License

This module is part of the Congen project.

