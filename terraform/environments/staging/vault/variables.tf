variable "project_name" {
  description = "Project name for resource naming"
  type        = string
  default     = "congen"
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "us-east-1"
}

# EKS cluster information (from aws-infrastructure module)
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

# Vault Configuration
variable "vault_namespace" {
  description = "Kubernetes namespace for Vault deployment"
  type        = string
  default     = "vault"
}

variable "vault_helm_chart_version" {
  description = "Version of the Vault Helm chart"
  type        = string
  default     = "0.24.0"
}

variable "vault_helm_timeout" {
  description = "Timeout in seconds for Helm release deployment"
  type        = number
  default     = 600
}

variable "vault_unseal_key_shares" {
  description = "Number of unseal key shares to generate (1-5)"
  type        = number
  default     = 1
}

variable "vault_unseal_key_threshold" {
  description = "Number of unseal keys required to unseal (must be <= shares)"
  type        = number
  default     = 1
}

variable "vault_secret_path_prefix" {
  description = "Path prefix for secrets in Vault"
  type        = string
  default     = "congen"
}

variable "vault_image_tag" {
  description = "Vault Docker image tag"
  type        = string
  default     = "1.15.2"
}

variable "vault_injector_image_tag" {
  description = "Vault Kubernetes injector Docker image tag"
  type        = string
  default     = "1.1.0"
}

variable "vault_cpu_request" {
  description = "CPU request for Vault pods"
  type        = string
  default     = "250m"
}

variable "vault_cpu_limit" {
  description = "CPU limit for Vault pods"
  type        = string
  default     = "500m"
}

variable "vault_memory_request" {
  description = "Memory request for Vault pods"
  type        = string
  default     = "256Mi"
}

variable "vault_memory_limit" {
  description = "Memory limit for Vault pods"
  type        = string
  default     = "512Mi"
}

variable "enable_vault_network_policy" {
  description = "Enable Kubernetes NetworkPolicy for Vault"
  type        = bool
  default     = true
}

variable "vault_allowed_namespaces" {
  description = "List of namespaces allowed to access Vault"
  type        = list(string)
  default     = ["congen"]
}

variable "vault_audit_log_retention_days" {
  description = "CloudWatch log retention in days for Vault audit logs"
  type        = number
  default     = 30
}

variable "vault_pdb_min_available" {
  description = "Minimum number of Vault pods that must be available (PDB)"
  type        = number
  default     = 1
}

variable "vault_storage_noncurrent_version_expiration_days" {
  description = "Number of days after which noncurrent versions of objects in Vault storage S3 bucket are expired"
  type        = number
  default     = 90
}

variable "vault_kubernetes_service_account" {
  description = "Kubernetes service account for Vault auth"
  type        = string
  default     = "terraform"
}

variable "vault_kubernetes_namespace" {
  description = "Kubernetes namespace for service account"
  type        = string
  default     = "default"
}

variable "vault_kubernetes_role" {
  description = "Kubernetes auth role name"
  type        = string
  default     = "terraform"
}
