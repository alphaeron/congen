locals {
  name_prefix = "${var.project_name}-${var.environment}"

  common_tags = {
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "Terraform"
  }

  vault_address = "https://vault.${var.vault_namespace}.svc.cluster.local:8200"
  
  # Vault root token for provider configuration
  vault_root_token = data.aws_secretsmanager_secret_version.vault_root_token.secret_string
}

data "aws_caller_identity" "current" {}

