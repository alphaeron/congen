output "vault_address" {
  description = "Vault server address (Kubernetes service address)"
  value       = "https://vault.${var.vault_namespace}.svc.cluster.local:8200"
}

output "vault_namespace" {
  description = "Kubernetes namespace where Vault is deployed"
  value       = var.vault_namespace
}

output "vault_s3_bucket_id" {
  description = "S3 bucket ID used for Vault storage backend"
  value       = aws_s3_bucket.vault_storage.id
}

output "vault_kms_key_id" {
  description = "KMS key ARN used for Vault auto-unseal"
  value       = aws_kms_key.vault.arn
}

output "vault_kms_key_alias" {
  description = "KMS key alias for Vault auto-unseal"
  value       = aws_kms_alias.vault.name
}

output "vault_oidc_provider_arn" {
  description = "ARN of the OIDC provider created for EKS"
  value       = aws_iam_openid_connect_provider.eks.arn
}

output "vault_iam_role_arn" {
  description = "ARN of the IAM role for Vault service account"
  value       = aws_iam_role.vault.arn
}

output "vault_audit_log_group_name" {
  description = "CloudWatch log group name for Vault audit logs (if enabled)"
  value       = var.enable_vault_audit_logging ? aws_cloudwatch_log_group.vault_audit[0].name : null
}

output "vault_unseal_key_secrets" {
  description = "List of AWS Secrets Manager secret names for Vault unseal keys (disaster recovery only)"
  value       = [for secret in aws_secretsmanager_secret.vault_unseal_keys : secret.name]
}

output "vault_root_token_secret" {
  description = "AWS Secrets Manager secret name for Vault root token"
  value       = aws_secretsmanager_secret.vault_root_token.name
}

output "vault_kubernetes_role" {
  description = "Kubernetes auth role name created for Terraform"
  value       = var.vault_kubernetes_role
}

output "vault_kubernetes_service_account" {
  description = "Kubernetes service account name for Vault auth"
  value       = var.vault_kubernetes_service_account
}

output "vault_kubernetes_namespace" {
  description = "Kubernetes namespace where the service account for Vault auth is located"
  value       = var.vault_kubernetes_namespace
}

output "vault_secret_path_prefix" {
  description = "Path prefix configured for secrets in Vault"
  value       = var.vault_secret_path_prefix
}

output "vault_kubernetes_auth_backend_path" {
  description = "Path of the Kubernetes auth backend in Vault"
  value       = vault_auth_backend.kubernetes.path
}
