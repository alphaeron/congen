provider "aws" {
  region = var.aws_region
}

# Configure Kubernetes provider (needed for Vault)
provider "kubernetes" {
  host                   = var.eks_cluster_endpoint
  cluster_ca_certificate = base64decode(var.eks_cluster_certificate_authority_data)
  exec {
    api_version = "client.authentication.k8s.io/v1beta1"
    command     = "aws"
    args        = ["eks", "get-token", "--cluster-name", var.eks_cluster_id]
  }
}

provider "helm" {
  kubernetes {
    host                   = var.eks_cluster_endpoint
    cluster_ca_certificate = base64decode(var.eks_cluster_certificate_authority_data)
    exec {
      api_version = "client.authentication.k8s.io/v1beta1"
      command     = "aws"
      args        = ["eks", "get-token", "--cluster-name", var.eks_cluster_id]
    }
  }
}

module "vault" {
  source = "../../../modules/vault"

  project_name = var.project_name
  environment  = "staging"
  aws_region   = var.aws_region

  # EKS cluster information (from aws-infrastructure module)
  eks_cluster_id                         = var.eks_cluster_id
  eks_cluster_endpoint                   = var.eks_cluster_endpoint
  eks_cluster_certificate_authority_data = var.eks_cluster_certificate_authority_data
  eks_cluster_oidc_issuer_url            = var.eks_cluster_oidc_issuer_url
  eks_node_group_id                      = var.eks_node_group_id
  vpc_id                                 = var.vpc_id

  # Vault configuration (staging - single replica, cost optimized)
  vault_namespace    = var.vault_namespace
  vault_replicas     = 1
  vault_storage_size = "10Gi"

  # Vault configuration
  vault_helm_chart_version                         = var.vault_helm_chart_version
  vault_helm_timeout                               = var.vault_helm_timeout
  vault_unseal_key_shares                          = var.vault_unseal_key_shares
  vault_unseal_key_threshold                       = var.vault_unseal_key_threshold
  vault_secret_path_prefix                         = var.vault_secret_path_prefix
  vault_image_tag                                  = var.vault_image_tag
  vault_injector_image_tag                         = var.vault_injector_image_tag
  vault_cpu_request                                = var.vault_cpu_request
  vault_cpu_limit                                  = var.vault_cpu_limit
  vault_memory_request                             = var.vault_memory_request
  vault_memory_limit                               = var.vault_memory_limit
  enable_vault_network_policy                      = var.enable_vault_network_policy
  vault_allowed_namespaces                         = var.vault_allowed_namespaces
  enable_vault_audit_logging                       = false
  vault_audit_log_retention_days                   = var.vault_audit_log_retention_days
  vault_pdb_min_available                          = var.vault_pdb_min_available
  vault_storage_noncurrent_version_expiration_days = var.vault_storage_noncurrent_version_expiration_days
  vault_kubernetes_service_account                 = var.vault_kubernetes_service_account
  vault_kubernetes_namespace                       = var.vault_kubernetes_namespace
  vault_kubernetes_role                            = var.vault_kubernetes_role
}
