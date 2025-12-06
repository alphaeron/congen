# CloudWatch Log Group for Vault Audit Logs
resource "aws_cloudwatch_log_group" "vault_audit" {
  count             = var.enable_vault_audit_logging ? 1 : 0
  name              = "/aws/vault/${local.name_prefix}/audit"
  retention_in_days = var.vault_audit_log_retention_days
  kms_key_id        = aws_kms_key.vault.arn

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-vault-audit-logs"
    }
  )
}
