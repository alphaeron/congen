# HashiCorp Vault Terraform Module

This Terraform module deploys HashiCorp Vault to an EKS cluster using Helm, with S3 storage backend and AWS KMS auto-unseal.

## Features

- **Automated Deployment**: Deploys Vault via official Helm chart
- **S3 Storage Backend**: Encrypted S3 bucket with KMS encryption and versioning (provides point-in-time recovery)
- **AWS KMS Auto-Unseal**: No manual unsealing required
- **High Availability**: Supports 1 replica (dev) or 3+ replicas (HA)
- **Pod Disruption Budget**: Ensures minimum availability during node maintenance (HA deployments)
- **IAM Roles for Service Accounts (IRSA)**: Vault pods use IAM roles, not static credentials
- **Automatic Bootstrap**: Initializes Vault, enables KV v2, configures Kubernetes auth backend (generic configuration only)
- **Generic Vault Configuration**: Provides base Vault setup; application-specific secret injection roles/policies are handled by application modules (e.g., `congen`)
- **Security**: TLS enabled, runs in private subnets, least privilege IAM, NetworkPolicy, S3 bucket policy
- **Audit Logging**: Optional CloudWatch audit logging
- **Disaster Recovery**: Unseal key stored in AWS Secrets Manager (encrypted) - only needed if KMS auto-unseal fails. S3 versioning provides point-in-time recovery

## Prerequisites

- Terraform ~> 1.12.2
- AWS account with appropriate permissions
- EKS cluster already deployed
- `kubectl` installed and configured
- `helm` installed
- `jq` installed (for Vault initialization)
- AWS CLI configured with appropriate permissions

### Provider Configuration

The module requires Kubernetes and Helm providers to be configured in the calling environment:

```hcl
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
```

## Module Structure

```
terraform/modules/vault/
├── terraform.tf    # Terraform version and provider requirements
├── main.tf         # Local values and data sources
├── variables.tf    # Input variables
├── outputs.tf      # Output values
├── kms.tf          # KMS key and alias for Vault auto-unseal
├── s3.tf            # S3 storage bucket with encryption, versioning, and policies
├── iam.tf           # IAM roles, policies, and OIDC provider for IRSA
├── kubernetes.tf    # Kubernetes resources (namespace, service account, network policy, PDB)
├── secrets.tf       # Secrets Manager resources and Vault initialization/configuration
├── monitoring.tf     # CloudWatch log group for audit logging
├── vault.tf         # Helm release and Vault deployment configuration
├── vault_config.tf  # Vault configuration (KV v2, Kubernetes auth, policies)
└── README.md        # This file
```

## Usage

### Basic Example

```hcl
module "vault" {
  source = "../../modules/vault"

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
  vault_replicas     = 1  # Single replica for staging
  vault_storage_size = "10Gi"
}
```

### Production Example

```hcl
module "vault" {
  source = "../../modules/vault"

  project_name = "congen"
  environment  = "production"
  aws_region   = "us-east-1"

  # EKS cluster information
  eks_cluster_id                        = module.aws_infrastructure.eks_cluster_id
  eks_cluster_endpoint                  = module.aws_infrastructure.eks_cluster_endpoint
  eks_cluster_certificate_authority_data = module.aws_infrastructure.eks_cluster_certificate_authority_data
  eks_cluster_oidc_issuer_url          = module.aws_infrastructure.eks_cluster_oidc_issuer_url
  eks_node_group_id                     = module.aws_infrastructure.eks_node_group_id
  vpc_id                                = module.aws_infrastructure.vpc_id

  # Vault configuration (HA)
  vault_namespace    = "vault"
  vault_replicas     = 3  # High availability
  vault_storage_size = "20Gi"

  # Secret configuration
  vault_secret_path_prefix = "congen"
}
```

## Input Variables

| Name | Description | Type | Default |
|------|-------------|------|---------|
| `project_name` | Project name for resource naming | `string` | (required) |
| `environment` | Environment name | `string` | (required) |
| `aws_region` | AWS region | `string` | (required) |
| `eks_cluster_id` | EKS cluster ID | `string` | (required) |
| `eks_cluster_endpoint` | EKS cluster API endpoint | `string` | (required) |
| `eks_cluster_certificate_authority_data` | Base64 encoded certificate data | `string` | (required) |
| `eks_cluster_oidc_issuer_url` | EKS cluster OIDC issuer URL | `string` | (required) |
| `eks_node_group_id` | EKS node group ID (for dependency) | `string` | (required) |
| `vpc_id` | VPC ID where the EKS cluster is deployed (required for S3 bucket VPC restriction) | `string` | (required) |
| `vault_namespace` | Kubernetes namespace for Vault | `string` | `"vault"` |
| `vault_helm_chart_version` | Version of the Vault Helm chart | `string` | `"0.24.0"` |
| `vault_helm_timeout` | Timeout in seconds for Helm release deployment | `number` | `600` |
| `vault_replicas` | Number of Vault replicas (1 or 3+) | `number` | `3` |
| `vault_unseal_key_shares` | Number of unseal key shares to generate (1-5) | `number` | `1` |
| `vault_unseal_key_threshold` | Number of unseal keys required to unseal (must be <= shares) | `number` | `1` |
| `vault_storage_size` | Storage size for Vault data volume | `string` | `"10Gi"` |
| `vault_kubernetes_service_account` | Kubernetes service account for Vault auth | `string` | `"terraform"` |
| `vault_kubernetes_namespace` | Kubernetes namespace for service account | `string` | `"default"` |
| `vault_kubernetes_role` | Kubernetes auth role name | `string` | `"terraform"` |
| `vault_secret_path_prefix` | Path prefix for secrets | `string` | `"congen"` |
| `vault_image_tag` | Vault Docker image tag | `string` | `"1.15.2"` |
| `vault_injector_image_tag` | Vault Kubernetes injector Docker image tag | `string` | `"1.1.0"` |
| `vault_cpu_request` | CPU request for Vault pods | `string` | `"250m"` |
| `vault_cpu_limit` | CPU limit for Vault pods | `string` | `"500m"` |
| `vault_memory_request` | Memory request for Vault pods | `string` | `"256Mi"` |
| `vault_memory_limit` | Memory limit for Vault pods | `string` | `"512Mi"` |
| `enable_vault_network_policy` | Enable Kubernetes NetworkPolicy for Vault | `bool` | `true` |
| `vault_allowed_namespaces` | List of namespaces allowed to access Vault | `list(string)` | `["default"]` |
| `enable_vault_audit_logging` | Enable Vault audit logging to CloudWatch | `bool` | `false` |
| `vault_audit_log_retention_days` | CloudWatch log retention in days for audit logs | `number` | `30` |
| `vault_pdb_min_available` | Minimum number of Vault pods that must be available (PDB) | `number` | `1` |
| `vault_storage_noncurrent_version_expiration_days` | Number of days after which noncurrent versions of objects in Vault storage S3 bucket are expired | `number` | `90` |

## Outputs

| Name | Description |
|------|-------------|
| `vault_address` | Vault server address (Kubernetes service) |
| `vault_namespace` | Kubernetes namespace where Vault is deployed |
| `vault_s3_bucket_id` | S3 bucket ID used for Vault storage backend |
| `vault_kms_key_id` | KMS key ARN used for Vault auto-unseal |
| `vault_kms_key_alias` | KMS key alias for Vault auto-unseal |
| `vault_oidc_provider_arn` | ARN of the OIDC provider created for EKS |
| `vault_iam_role_arn` | ARN of the IAM role for Vault service account |
| `vault_audit_log_group_name` | CloudWatch log group name for Vault audit logs (if enabled) |
| `vault_unseal_key_secrets` | List of AWS Secrets Manager secret names for Vault unseal keys (disaster recovery only) |
| `vault_root_token_secret` | AWS Secrets Manager secret name for Vault root token |
| `vault_kubernetes_role` | Kubernetes auth role name created for Terraform |
| `vault_kubernetes_service_account` | Kubernetes service account name for Vault auth |
| `vault_kubernetes_namespace` | Kubernetes namespace where the service account for Vault auth is located |
| `vault_secret_path_prefix` | Path prefix configured for secrets in Vault |
| `vault_kubernetes_auth_backend_path` | Path of the Kubernetes auth backend in Vault |

## Security Features

### S3 Storage Backend
- **Encryption**: KMS-encrypted bucket with versioning
- **Access Control**: Private access only (no public access), VPC-restricted access
- **Versioning**: S3 versioning enabled for point-in-time recovery
- **Lifecycle**: Automatic cleanup of old versions (configurable, default: 90 days)

### KMS Auto-Unseal
- **Separate KMS Key**: Dedicated key for Vault auto-unseal
- **Prevent Destroy**: Lifecycle rule prevents accidental deletion
- **No Manual Unsealing**: Fully automated unsealing process

### IAM and Access Control
- **IRSA**: Vault pods use IAM Roles for Service Accounts
- **Least Privilege**: Vault only has access to its S3 bucket and KMS key
- **OIDC Provider**: Secure identity federation with EKS

### Network Security
- **Private Subnets**: Vault runs in private subnets, no internet access
- **TLS Enabled**: All Vault communication encrypted
- **Pod Anti-Affinity**: Vault replicas spread across nodes

### Secret Management
- **Root Token Storage**: Stored in AWS Secrets Manager, encrypted with KMS
- **Unseal Keys**: Configurable number of unseal keys (default: 1) stored in AWS Secrets Manager, encrypted with KMS (only needed for disaster recovery if KMS fails)
- **Kubernetes Auth**: Configured automatically for Terraform; application-specific roles/policies handled by application modules

### Disaster Recovery
- **S3 Versioning**: The S3 storage bucket has versioning enabled, providing point-in-time recovery of all Vault data
- **Lifecycle Management**: Old versions are automatically cleaned up after the configured retention period (default: 90 days)
- **Unseal Keys**: Stored in AWS Secrets Manager for disaster recovery scenarios

## Deployment Process

The module automatically:

1. **Creates Infrastructure**:
   - S3 bucket for storage (encrypted, versioned)
   - KMS key for auto-unseal
   - OIDC provider for EKS
   - IAM role and policies for Vault

2. **Deploys Vault**:
   - Creates Kubernetes namespace
   - Creates service account with IRSA annotation
   - Deploys Vault via Helm chart
   - Configures S3 storage backend
   - Configures AWS KMS auto-unseal

3. **Bootstraps Vault**:
   - Waits for Vault pods to be ready
   - Initializes Vault (first run only)
   - Initializes Vault with configurable unseal keys (default: 1 share, threshold 1) - sufficient for disaster recovery with KMS auto-unseal
   - Stores unseal keys and root token in AWS Secrets Manager (encrypted)
   - Enables KV secrets engine v2
   - Configures Kubernetes authentication backend
   - Creates Terraform policy and role (for Terraform to write secrets)
   - **Note**: Application-specific secret injection roles and policies are created by application modules (e.g., `congen`)

## Accessing Vault

### From Kubernetes Pods

Vault is accessible at: `https://vault.vault.svc.cluster.local:8200`

### From Terraform

Use the `vault_address` output:

```hcl
provider "vault" {
  address = module.vault.vault_address
  
  auth_login {
    path = "auth/kubernetes/login"
    
    parameters = {
      role = "terraform"
    }
  }
}
```

### From Local Machine (Port Forward)

```bash
kubectl port-forward -n vault svc/vault 8200:8200
```

Then access at: `https://localhost:8200`

## Troubleshooting

### Vault Pods Not Starting

1. Check pod status:
   ```bash
   kubectl get pods -n vault
   kubectl describe pod -n vault vault-0
   ```

2. Check logs:
   ```bash
   kubectl logs -n vault vault-0
   ```

3. Verify IAM role:
   ```bash
   kubectl describe sa -n vault vault
   ```

### Vault Not Initialized

1. Check initialization status:
   ```bash
   kubectl exec -n vault vault-0 -- vault status
   ```

2. Manually initialize if needed:
   ```bash
   kubectl exec -n vault vault-0 -- vault operator init
   ```

### Cannot Access Vault

1. Verify service:
   ```bash
   kubectl get svc -n vault
   ```

2. Check network policies:
   ```bash
   kubectl get networkpolicies -n vault
   ```

3. Verify DNS resolution:
   ```bash
   kubectl run -it --rm debug --image=busybox --restart=Never -- nslookup vault.vault.svc.cluster.local
   ```

## Disaster Recovery

### S3 Versioning for Point-in-Time Recovery

The S3 storage bucket has versioning enabled, which automatically preserves all historical versions of Vault data. This provides point-in-time recovery capabilities:

- **Automatic Versioning**: Every change to Vault data creates a new version in S3
- **Version Retention**: Old versions are retained for the configured period (default: 90 days, configurable via `vault_storage_noncurrent_version_expiration_days`)
- **Recovery**: You can restore any previous version of Vault data from S3 version history

### Disaster Recovery Procedure

In case of complete Vault failure:
1. Retrieve unseal keys from AWS Secrets Manager:
   ```bash
   aws secretsmanager get-secret-value --secret-id <name-prefix>/vault/unseal-key-0
   # Repeat for additional keys if using multiple key shares
   ```
2. Deploy new Vault cluster pointing to the same S3 storage bucket
3. The S3 bucket contains all Vault data with versioning enabled, so data is automatically available
4. Unseal using stored keys (need threshold number of keys) - KMS auto-unseal should handle this automatically
5. If needed, restore a specific version from S3 version history

## Cost Optimization

- **Staging**: Use 1 replica and smaller storage (10Gi)
- **Production**: Use 3 replicas for HA and larger storage (20Gi+)
- **S3 Lifecycle**: Old versions are automatically deleted after the configured retention period (default: 90 days, configurable via `vault_storage_noncurrent_version_expiration_days`)
- **Storage Size**: Adjust `vault_storage_size` based on expected secret volume
- **Version Retention**: Adjust `vault_storage_noncurrent_version_expiration_days` based on compliance requirements (shorter retention = lower costs)

## Dependencies

This module depends on:
- EKS cluster being deployed (via `aws-infrastructure` module)
- EKS node group being ready
- Kubernetes and Helm providers being configured

## Related Modules

- `aws-infrastructure`: Deploys EKS cluster and other AWS resources
- Application modules: Can use Vault for secret management
