# S3 Bucket for Vault Storage Backend
resource "aws_s3_bucket" "vault_storage" {
  bucket = "${local.name_prefix}-vault-storage-${data.aws_caller_identity.current.account_id}"

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-vault-storage"
    }
  )
}

resource "aws_s3_bucket_versioning" "vault_storage" {
  bucket = aws_s3_bucket.vault_storage.id

  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_s3_bucket_server_side_encryption_configuration" "vault_storage" {
  bucket = aws_s3_bucket.vault_storage.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm     = "aws:kms"
      kms_master_key_id = aws_kms_key.vault.arn
    }
    bucket_key_enabled = true
  }
}

resource "aws_s3_bucket_public_access_block" "vault_storage" {
  bucket = aws_s3_bucket.vault_storage.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_lifecycle_configuration" "vault_storage" {
  bucket = aws_s3_bucket.vault_storage.id

  rule {
    id     = "delete-old-versions"
    status = "Enabled"

    noncurrent_version_expiration {
      noncurrent_days = var.vault_storage_noncurrent_version_expiration_days
    }
  }
}

# S3 Bucket Policy for additional security
data "aws_iam_policy_document" "vault_storage_bucket" {
  statement {
    sid    = "DenyInsecureConnections"
    effect = "Deny"
    principals {
      type        = "*"
      identifiers = ["*"]
    }
    actions = ["s3:*"]
    resources = [
      aws_s3_bucket.vault_storage.arn,
      "${aws_s3_bucket.vault_storage.arn}/*",
    ]
    condition {
      test     = "Bool"
      variable = "aws:SecureTransport"
      values   = ["false"]
    }
  }

  statement {
    sid    = "DenyAccessOutsideVPC"
    effect = "Deny"
    principals {
      type        = "*"
      identifiers = ["*"]
    }
    actions = ["s3:*"]
    resources = [
      aws_s3_bucket.vault_storage.arn,
      "${aws_s3_bucket.vault_storage.arn}/*",
    ]
    condition {
      test     = "StringNotEquals"
      variable = "aws:SourceVpc"
      values   = [var.vpc_id]
    }
  }
}

resource "aws_s3_bucket_policy" "vault_storage" {
  bucket = aws_s3_bucket.vault_storage.id
  policy = data.aws_iam_policy_document.vault_storage_bucket.json
}

