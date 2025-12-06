output "vault_address" {
  description = "Vault server address (Kubernetes service address)"
  value       = module.vault.vault_address
}

output "vault_namespace" {
  description = "Kubernetes namespace where Vault is deployed"
  value       = module.vault.vault_namespace
}

output "vault_s3_bucket_id" {
  description = "S3 bucket ID used for Vault storage backend"
  value       = module.vault.vault_s3_bucket_id
}

output "vault_kms_key_id" {
  description = "KMS key ARN used for Vault auto-unseal"
  value       = module.vault.vault_kms_key_id
}

output "vault_kms_key_alias" {
  description = "KMS key alias for Vault auto-unseal"
  value       = module.vault.vault_kms_key_alias
}

output "vault_oidc_provider_arn" {
  description = "ARN of the OIDC provider created for EKS"
  value       = module.vault.vault_oidc_provider_arn
}

output "vault_iam_role_arn" {
  description = "ARN of the IAM role for Vault service account"
  value       = module.vault.vault_iam_role_arn
}

output "vault_unseal_key_secrets" {
  description = "List of AWS Secrets Manager secret names for Vault unseal keys (disaster recovery only)"
  value       = module.vault.vault_unseal_key_secrets
}

output "vault_root_token_secret" {
  description = "AWS Secrets Manager secret name for Vault root token"
  value       = module.vault.vault_root_token_secret
}

output "vault_kubernetes_role" {
  description = "Kubernetes auth role name created for Terraform"
  value       = module.vault.vault_kubernetes_role
}

output "vault_kubernetes_service_account" {
  description = "Kubernetes service account name for Vault auth"
  value       = module.vault.vault_kubernetes_service_account
}

output "vault_kubernetes_namespace" {
  description = "Kubernetes namespace where the service account for Vault auth is located"
  value       = module.vault.vault_kubernetes_namespace
}

output "vault_secret_path_prefix" {
  description = "Path prefix configured for secrets in Vault"
  value       = module.vault.vault_secret_path_prefix
}

output "vault_kubernetes_auth_backend_path" {
  description = "Path of the Kubernetes auth backend in Vault"
  value       = module.vault.vault_kubernetes_auth_backend_path
}
