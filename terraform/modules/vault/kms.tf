# KMS Key Policy for Vault Auto-Unseal
data "aws_iam_policy_document" "vault_kms" {
  statement {
    sid    = "Enable IAM User Permissions"
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"]
    }
    actions   = ["kms:*"]
    resources = ["*"]
  }

  statement {
    sid    = "Allow Vault Service Account"
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = [aws_iam_role.vault.arn]
    }
    actions = [
      "kms:Encrypt",
      "kms:Decrypt",
      "kms:DescribeKey",
      "kms:GenerateDataKey",
    ]
    resources = ["*"]
  }

  statement {
    sid    = "Allow Secrets Manager"
    effect = "Allow"
    principals {
      type        = "AWS"
      identifiers = ["arn:aws:iam::${data.aws_caller_identity.current.account_id}:root"]
    }
    actions = [
      "kms:Decrypt",
      "kms:DescribeKey",
    ]
    resources = ["*"]
    condition {
      test     = "StringEquals"
      variable = "kms:ViaService"
      values   = ["secretsmanager.${var.aws_region}.amazonaws.com"]
    }
  }
}

# KMS Key for Vault Auto-Unseal
resource "aws_kms_key" "vault" {
  description         = "KMS key for Vault auto-unseal"
  enable_key_rotation = false
  policy              = data.aws_iam_policy_document.vault_kms.json

  lifecycle {
    prevent_destroy = true
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-vault-kms-key"
    }
  )
}

resource "aws_kms_alias" "vault" {
  name          = "alias/${local.name_prefix}-vault"
  target_key_id = aws_kms_key.vault.key_id
}
