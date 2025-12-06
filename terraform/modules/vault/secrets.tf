# Resource Policy for Secrets Manager (restricts access to HTTPS only)
# Note: Secrets Manager doesn't support VPC-based conditions in resource policies
# VPC restriction is enforced via IAM policies and security groups
data "aws_iam_policy_document" "vault_secrets_manager" {
  statement {
    sid    = "DenyInsecureConnections"
    effect = "Deny"
    principals {
      type        = "*"
      identifiers = ["*"]
    }
    actions   = ["secretsmanager:*"]
    resources = ["*"]
    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }

  statement {
    sid    = "AllowAccountAccess"
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"]
    }
    actions = [
      "secretsmanager:GetSecretValue",
      "secretsmanager:DescribeSecret",
      "secretsmanager:PutSecretValue",
    ]
    resources = ["*"]
  }
}

# AWS Secrets Manager secrets for Vault unseal keys (disaster recovery only)
# With KMS auto-unseal, unseal keys are only needed for disaster recovery
resource "aws_secretsmanager_secret" "vault_unseal_keys" {
  count = var.vault_unseal_key_shares
  name  = "${local.name_prefix}/vault/unseal-key-${count.index}"

  description = "Vault unseal key ${count.index} for disaster recovery (KMS auto-unseal handles normal operations)"
  kms_key_id  = aws_kms_key.vault.arn

  policy = data.aws_iam_policy_document.vault_secrets_manager.json

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}/vault/unseal-key-${count.index}"
    }
  )
}

resource "aws_secretsmanager_secret" "vault_root_token" {
  name        = "${local.name_prefix}/vault/root-token"
  description = "Vault root token for initial configuration"
  kms_key_id  = aws_kms_key.vault.arn

  policy = data.aws_iam_policy_document.vault_secrets_manager.json

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}/vault/root-token"
    }
  )
}

# Initialize Vault (only on first deployment)
resource "null_resource" "vault_init" {
  provisioner "local-exec" {
    command = <<-EOT
      # Check if Vault is already initialized
      if kubectl exec -n ${var.vault_namespace} vault-0 -- vault status > /dev/null 2>&1; then
        echo "Vault is already initialized"
        exit 0
      fi
      
      # Initialize Vault with configurable unseal keys
      # With KMS auto-unseal, unseal keys are only needed for disaster recovery
      echo "Initializing Vault..."
      INIT_OUTPUT=$(kubectl exec -n ${var.vault_namespace} vault-0 -- vault operator init -key-shares=${var.vault_unseal_key_shares} -key-threshold=${var.vault_unseal_key_threshold} -format=json)
      
      # Store unseal keys in Secrets Manager (disaster recovery only)
      KEY_SHARES=${var.vault_unseal_key_shares}
      for i in $(seq 0 $((KEY_SHARES - 1))); do
        KEY=$(echo "$INIT_OUTPUT" | jq -r ".unseal_keys_b64[$i]")
        if [ "$KEY" != "null" ] && [ -n "$KEY" ]; then
          aws secretsmanager put-secret-value \
            --secret-id "${aws_secretsmanager_secret.vault_unseal_keys[i].arn}" \
            --secret-string "$KEY"
        fi
      done
      
      # Store root token in Secrets Manager
      ROOT_TOKEN=$(echo "$INIT_OUTPUT" | jq -r '.root_token')
      aws secretsmanager put-secret-value \
        --secret-id "${local.name_prefix}/vault/root-token" \
        --secret-string "$ROOT_TOKEN"
    EOT
  }

  depends_on = [
    null_resource.vault_ready,
    aws_secretsmanager_secret.vault_unseal_keys,
    aws_secretsmanager_secret.vault_root_token,
  ]
}


