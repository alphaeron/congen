# Wait for Vault to be unsealed and ready
resource "null_resource" "vault_unsealed" {
  provisioner "local-exec" {
    command = <<-EOT
      for i in {1..30}; do
        if kubectl exec -n ${var.vault_namespace} vault-0 -- vault status > /dev/null 2>&1; then
          exit 0
        fi
        sleep 2
      done
      exit 1
    EOT
  }

  depends_on = [
    null_resource.vault_init,
    helm_release.vault,
  ]
}

# Get Vault root token from AWS Secrets Manager
data "aws_secretsmanager_secret_version" "vault_root_token" {
  secret_id = aws_secretsmanager_secret.vault_root_token.name

  depends_on = [
    null_resource.vault_init,
  ]
}

# Get Kubernetes service account token for Vault Kubernetes auth configuration
data "external" "terraform_service_account_token" {
  program = [
    "sh",
    "-c",
    "kubectl create token ${var.vault_kubernetes_service_account} -n ${var.vault_kubernetes_namespace} --duration=1h | jq -R '{token: .}'"
  ]

  depends_on = [
    kubernetes_service_account.terraform,
  ]
}

# Configure Vault provider for configuration
# Token is retrieved from AWS Secrets Manager via local value
# Note: Provider blocks are evaluated early, so we use the local which references the data source
provider "vault" {
  alias           = "config"
  address         = local.vault_address
  token           = local.vault_root_token
  skip_tls_verify = false
}

# Enable KV secrets engine v2
resource "vault_mount" "secret" {
  provider = vault.config

  path        = "secret"
  type        = "kv"
  description = "KV secrets engine v2 for application secrets"
  options = {
    version = "2"
  }

  depends_on = [
    null_resource.vault_unsealed,
  ]
}

# Enable Kubernetes auth method
resource "vault_auth_backend" "kubernetes" {
  provider = vault.config

  type = "kubernetes"

  depends_on = [
    null_resource.vault_unsealed,
  ]
}

# Configure Kubernetes auth backend
resource "vault_kubernetes_auth_backend_config" "config" {
  provider = vault.config

  backend            = vault_auth_backend.kubernetes.path
  kubernetes_host    = "https://${var.eks_cluster_endpoint}:443"
  kubernetes_ca_cert = base64decode(var.eks_cluster_certificate_authority_data)
  token_reviewer_jwt = data.external.terraform_service_account_token.result.token

  depends_on = [
    vault_auth_backend.kubernetes,
    data.external.terraform_service_account_token,
  ]
}

# Policy for Terraform to write secrets
resource "vault_policy" "terraform_write" {
  provider = vault.config

  name = "terraform-write"

  policy = <<-EOT
path "secret/data/${var.vault_secret_path_prefix}/*" {
  capabilities = ["create", "update", "read"]
}
path "secret/metadata/${var.vault_secret_path_prefix}/*" {
  capabilities = ["list", "read"]
}
EOT

  depends_on = [
    vault_mount.secret,
  ]
}

# Kubernetes auth role for Terraform
resource "vault_kubernetes_auth_backend_role" "terraform" {
  provider = vault.config

  backend                          = vault_auth_backend.kubernetes.path
  role_name                        = var.vault_kubernetes_role
  bound_service_account_names      = [var.vault_kubernetes_service_account]
  bound_service_account_namespaces = [var.vault_kubernetes_namespace]
  token_policies                   = [vault_policy.terraform_write.name]
  token_ttl                        = 3600

  depends_on = [
    vault_kubernetes_auth_backend_config.config,
    vault_policy.terraform_write,
  ]
}

# Enable audit logging if configured
resource "vault_audit" "file" {
  count    = var.enable_vault_audit_logging ? 1 : 0
  provider = vault.config

  type = "file"

  options = {
    file_path = "/vault/logs/audit.log"
  }

  depends_on = [
    null_resource.vault_unsealed,
  ]
}

