variable "project_name" {
  description = "Project name for resource naming"
  type        = string
}

variable "environment" {
  description = "Environment name (staging, production, etc.)"
  type        = string
}

variable "aws_region" {
  description = "AWS region"
  type        = string
}

variable "eks_cluster_id" {
  description = "EKS cluster ID"
  type        = string
}

variable "eks_cluster_endpoint" {
  description = "EKS cluster API endpoint"
  type        = string
}

variable "eks_cluster_certificate_authority_data" {
  description = "Base64 encoded certificate data for EKS cluster"
  type        = string
}

variable "eks_cluster_oidc_issuer_url" {
  description = "EKS cluster OIDC issuer URL"
  type        = string
}

variable "eks_node_group_id" {
  description = "EKS node group ID (for dependency)"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID where the EKS cluster is deployed (required for S3 bucket VPC restriction)"
  type        = string
}

variable "vault_namespace" {
  description = "Kubernetes namespace for Vault deployment"
  type        = string
  default     = "vault"
}

variable "vault_helm_chart_version" {
  description = "Version of the Vault Helm chart to deploy"
  type        = string
  default     = "0.24.0"
}

variable "vault_helm_timeout" {
  description = "Timeout in seconds for Helm release deployment (default: 600)"
  type        = number
  default     = 600
}

variable "vault_replicas" {
  description = "Number of Vault replicas for high availability (must be 1 or 3+)"
  type        = number
  default     = 3
  validation {
    condition     = var.vault_replicas == 1 || var.vault_replicas >= 3
    error_message = "Vault replicas must be 1 (dev) or 3+ (HA)."
  }
}

variable "vault_unseal_key_shares" {
  description = "Number of unseal key shares to generate during initialization (default: 1, sufficient with KMS auto-unseal)"
  type        = number
  default     = 1
  validation {
    condition     = var.vault_unseal_key_shares >= 1 && var.vault_unseal_key_shares <= 5
    error_message = "Vault unseal key shares must be between 1 and 5."
  }
}

variable "vault_unseal_key_threshold" {
  description = "Number of unseal keys required to unseal Vault (default: 1, must be <= vault_unseal_key_shares)"
  type        = number
  default     = 1
  validation {
    condition     = var.vault_unseal_key_threshold >= 1 && var.vault_unseal_key_threshold <= var.vault_unseal_key_shares
    error_message = "Vault unseal key threshold must be between 1 and vault_unseal_key_shares."
  }
}

variable "vault_storage_size" {
  description = "Storage size for Vault data volume (e.g., 10Gi)"
  type        = string
  default     = "10Gi"
}

variable "vault_kubernetes_service_account" {
  description = "Kubernetes service account for Vault auth (default: terraform)"
  type        = string
  default     = "terraform"
}

variable "vault_kubernetes_namespace" {
  description = "Kubernetes namespace for service account (default: default)"
  type        = string
  default     = "default"
}

variable "vault_kubernetes_role" {
  description = "Kubernetes auth role name for Terraform (default: terraform)"
  type        = string
  default     = "terraform"
}

variable "vault_secret_path_prefix" {
  description = "Path prefix for secrets (default: congen)"
  type        = string
  default     = "congen"
}

variable "vault_image_tag" {
  description = "Vault Docker image tag (default: 1.15.2)"
  type        = string
  default     = "1.15.2"
}

variable "vault_injector_image_tag" {
  description = "Vault Kubernetes injector Docker image tag (default: 1.1.0)"
  type        = string
  default     = "1.1.0"
}

variable "vault_cpu_request" {
  description = "CPU request for Vault pods (default: 250m)"
  type        = string
  default     = "250m"
}

variable "vault_cpu_limit" {
  description = "CPU limit for Vault pods (default: 500m)"
  type        = string
  default     = "500m"
}

variable "vault_memory_request" {
  description = "Memory request for Vault pods (default: 256Mi)"
  type        = string
  default     = "256Mi"
}

variable "vault_memory_limit" {
  description = "Memory limit for Vault pods (default: 512Mi)"
  type        = string
  default     = "512Mi"
}

variable "enable_vault_network_policy" {
  description = "Enable Kubernetes NetworkPolicy for Vault (default: true, recommended for security)"
  type        = bool
  default     = true
}

variable "vault_allowed_namespaces" {
  description = "List of namespaces allowed to access Vault (for NetworkPolicy)"
  type        = list(string)
  default     = ["default"]
}

variable "enable_vault_audit_logging" {
  description = "Enable Vault audit logging to CloudWatch (default: false)"
  type        = bool
  default     = false
}

variable "vault_audit_log_retention_days" {
  description = "CloudWatch log retention in days for Vault audit logs (default: 30)"
  type        = number
  default     = 30
}

variable "vault_pdb_min_available" {
  description = "Minimum number of Vault pods that must be available (for Pod Disruption Budget, default: 1)"
  type        = number
  default     = 1
}

variable "vault_storage_noncurrent_version_expiration_days" {
  description = "Number of days after which noncurrent versions of objects in Vault storage S3 bucket are expired (default: 90)"
  type        = number
  default     = 90
}

