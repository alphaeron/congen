# OIDC Provider for EKS (required for IRSA)
data "tls_certificate" "eks" {
  url = var.eks_cluster_oidc_issuer_url
}

resource "aws_iam_openid_connect_provider" "eks" {
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = [data.tls_certificate.eks.certificates[0].sha1_fingerprint]
  url             = var.eks_cluster_oidc_issuer_url

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-eks-oidc"
    }
  )
}

# IAM Role for Vault Service Account (IRSA)
data "aws_iam_policy_document" "vault_irsa" {
  statement {
    effect = "Allow"

    principals {
      type        = "Federated"
      identifiers = [aws_iam_openid_connect_provider.eks.arn]
    }

    actions = ["sts:AssumeRoleWithWebIdentity"]

    condition {
      test     = "StringEquals"
      variable = "${replace(aws_iam_openid_connect_provider.eks.url, "https://", "")}:sub"
      values   = ["system:serviceaccount:${var.vault_namespace}:vault"]
    }

    condition {
      test     = "StringEquals"
      variable = "${replace(aws_iam_openid_connect_provider.eks.url, "https://", "")}:aud"
      values   = ["sts.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "vault" {
  name               = "${local.name_prefix}-vault-role"
  assume_role_policy = data.aws_iam_policy_document.vault_irsa.json

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-vault-role"
    }
  )
}

# IAM Policy for Vault to access S3 and KMS
data "aws_iam_policy_document" "vault_s3_kms" {
  statement {
    effect = "Allow"
    actions = [
      "s3:PutObject",
      "s3:GetObject",
      "s3:DeleteObject",
      "s3:ListBucket",
    ]
    resources = [
      aws_s3_bucket.vault_storage.arn,
      "${aws_s3_bucket.vault_storage.arn}/*",
    ]
  }

  statement {
    effect = "Allow"
    actions = [
      "kms:Encrypt",
      "kms:Decrypt",
      "kms:DescribeKey",
      "kms:GenerateDataKey",
    ]
    resources = [aws_kms_key.vault.arn]
  }
}

resource "aws_iam_role_policy" "vault_s3_kms" {
  name   = "${local.name_prefix}-vault-s3-kms-policy"
  role   = aws_iam_role.vault.id
  policy = data.aws_iam_policy_document.vault_s3_kms.json
}

# IAM Policy for Vault to write audit logs to CloudWatch
data "aws_iam_policy_document" "vault_audit_logs" {
  count = var.enable_vault_audit_logging ? 1 : 0
  statement {
    effect = "Allow"
    actions = [
      "logs:CreateLogGroup",
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "logs:DescribeLogGroups",
      "logs:DescribeLogStreams",
    ]
    resources = [
      aws_cloudwatch_log_group.vault_audit[0].arn,
      "${aws_cloudwatch_log_group.vault_audit[0].arn}:*",
    ]
  }

  statement {
    effect = "Allow"
    actions = [
      "kms:Decrypt",
      "kms:DescribeKey",
    ]
    resources = [aws_kms_key.vault.arn]
    condition {
      test     = "StringEquals"
      variable = "kms:ViaService"
      values   = ["logs.${var.aws_region}.amazonaws.com"]
    }
  }
}

resource "aws_iam_role_policy" "vault_audit_logs" {
  count  = var.enable_vault_audit_logging ? 1 : 0
  name   = "${local.name_prefix}-vault-audit-logs-policy"
  role   = aws_iam_role.vault.id
  policy = data.aws_iam_policy_document.vault_audit_logs[0].json
}
