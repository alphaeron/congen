# KMS Key Policy for RDS Encryption
data "aws_iam_policy_document" "rds_kms" {
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
    sid    = "Allow RDS to use the key"
    effect = "Allow"
    principals {
      type        = "Service"
      identifiers = ["rds.amazonaws.com"]
    }
    actions = [
      "kms:Encrypt",
      "kms:Decrypt",
      "kms:ReEncrypt*",
      "kms:GenerateDataKey*",
      "kms:DescribeKey",
    ]
    resources = ["*"]
  }
}

resource "aws_kms_key" "rds" {
  description         = "KMS key for RDS encryption"
  enable_key_rotation = false
  policy              = data.aws_iam_policy_document.rds_kms.json

  lifecycle {
    prevent_destroy = true
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-rds-kms-key"
    }
  )
}

resource "aws_kms_alias" "rds" {
  name          = "alias/${local.name_prefix}-rds"
  target_key_id = aws_kms_key.rds.key_id
}

resource "aws_db_subnet_group" "main" {
  name       = "${local.name_prefix}-db-subnet-group"
  subnet_ids = var.database_subnet_ids

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-db-subnet-group"
    }
  )
}

resource "aws_db_parameter_group" "main" {
  name   = "${local.name_prefix}-db-parameter-group"
  family = "aurora-postgresql15"

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-db-parameter-group"
    }
  )
}

resource "aws_rds_cluster_parameter_group" "main" {
  name   = "${local.name_prefix}-cluster-parameter-group"
  family = "aurora-postgresql15"

  parameter {
    name  = "rds.force_ssl"
    value = "1"
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-cluster-parameter-group"
    }
  )
}

resource "random_password" "rds_master_password" {
  length           = 32
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

resource "aws_rds_cluster" "main" {
  cluster_identifier              = "${local.name_prefix}-db"
  engine                          = "aurora-postgresql"
  engine_version                  = var.rds_engine_version
  database_name                   = var.rds_database_name
  master_username                 = var.rds_username
  master_password                 = random_password.rds_master_password.result
  backup_retention_period         = var.rds_backup_retention_period
  preferred_backup_window         = var.rds_preferred_backup_window
  preferred_maintenance_window    = var.rds_preferred_maintenance_window
  db_subnet_group_name            = aws_db_subnet_group.main.name
  db_cluster_parameter_group_name = aws_rds_cluster_parameter_group.main.name
  vpc_security_group_ids          = [aws_security_group.rds.id]
  storage_encrypted               = true
  kms_key_id                      = aws_kms_key.rds.arn
  enabled_cloudwatch_logs_exports = ["postgresql"]
  deletion_protection             = var.rds_deletion_protection
  skip_final_snapshot             = var.rds_skip_final_snapshot
  final_snapshot_identifier       = var.rds_skip_final_snapshot ? null : "${local.name_prefix}-db-final-snapshot-${formatdate("YYYY-MM-DD-hhmm", timestamp())}"

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-db-cluster"
    }
  )
}

resource "aws_rds_cluster_instance" "main" {
  count                           = 1
  identifier                      = "${local.name_prefix}-db-instance-${count.index + 1}"
  cluster_identifier              = aws_rds_cluster.main.id
  instance_class                  = var.rds_instance_class
  engine                          = aws_rds_cluster.main.engine
  engine_version                  = aws_rds_cluster.main.engine_version
  publicly_accessible             = false
  db_parameter_group_name         = aws_db_parameter_group.main.name
  performance_insights_enabled    = var.rds_enable_performance_insights
  performance_insights_kms_key_id = var.rds_enable_performance_insights ? aws_kms_key.rds.arn : null
  monitoring_interval             = var.rds_enable_enhanced_monitoring ? 60 : 0
  monitoring_role_arn             = var.rds_enable_enhanced_monitoring ? aws_iam_role.rds_enhanced_monitoring[0].arn : null

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-db-instance-${count.index + 1}"
    }
  )
}

resource "aws_rds_cluster_instance" "read_replicas" {
  count                           = var.rds_read_replica_count
  identifier                      = "${local.name_prefix}-db-replica-${count.index + 1}"
  cluster_identifier              = aws_rds_cluster.main.id
  instance_class                  = var.rds_instance_class
  engine                          = aws_rds_cluster.main.engine
  engine_version                  = aws_rds_cluster.main.engine_version
  publicly_accessible             = false
  db_parameter_group_name         = aws_db_parameter_group.main.name
  performance_insights_enabled    = var.rds_enable_performance_insights
  performance_insights_kms_key_id = var.rds_enable_performance_insights ? aws_kms_key.rds.arn : null
  monitoring_interval             = var.rds_enable_enhanced_monitoring ? 60 : 0
  monitoring_role_arn             = var.rds_enable_enhanced_monitoring ? aws_iam_role.rds_enhanced_monitoring[0].arn : null

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-db-replica-${count.index + 1}"
    }
  )
}
