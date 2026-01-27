# Get Vault root token from AWS Secrets Manager
data "aws_secretsmanager_secret_version" "vault_root_token" {
  secret_id = var.vault_root_token_secret
}

# Configure Vault provider for secret injection configuration
provider "vault" {
  alias           = "config"
  address         = var.vault_address
  token           = data.aws_secretsmanager_secret_version.vault_root_token.secret_string
  skip_tls_verify = false
}

# Kubernetes Service Account for Congen Vault Secret Injection (used by backend pods for Vault authentication)
resource "kubernetes_service_account" "congen_svc" {
  metadata {
    name      = "congen-svc"
    namespace = "congen"
    labels = merge(
      local.common_tags,
      {
        Name = "congen-svc"
      }
    )
  }

  depends_on = [var.eks_node_group_id]
}

# Policy for backend application pods to read secrets
resource "vault_policy" "backend_read" {
  provider = vault.config

  name = "backend-read"

  policy = <<-EOT
path "secret/data/${var.vault_secret_path_prefix}/*" {
  capabilities = ["read"]
}
path "secret/metadata/${var.vault_secret_path_prefix}/*" {
  capabilities = ["list", "read"]
}
EOT
}

# Kubernetes auth role for backend application pods
resource "vault_kubernetes_auth_backend_role" "backend" {
  provider = vault.config

  backend                          = var.vault_kubernetes_auth_backend_path
  role_name                        = "backend"
  bound_service_account_names      = ["congen-svc"]
  bound_service_account_namespaces = ["congen"]
  token_policies                   = [vault_policy.backend_read.name]
  token_ttl                        = 3600

  depends_on = [
    vault_policy.backend_read,
    kubernetes_service_account.congen_svc,
  ]
}
