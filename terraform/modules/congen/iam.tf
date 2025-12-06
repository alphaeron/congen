data "aws_iam_policy_document" "rds_enhanced_monitoring" {
  statement {
    actions = [
      "sts:AssumeRole",
    ]

    principals {
      type        = "Service"
      identifiers = ["monitoring.rds.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "rds_enhanced_monitoring" {
  count              = var.rds_enable_enhanced_monitoring ? 1 : 0
  name               = "${local.name_prefix}-rds-enhanced-monitoring"
  assume_role_policy = data.aws_iam_policy_document.rds_enhanced_monitoring.json

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-rds-enhanced-monitoring"
    }
  )
}

resource "aws_iam_role_policy_attachment" "rds_enhanced_monitoring" {
  count      = var.rds_enable_enhanced_monitoring ? 1 : 0
  role       = aws_iam_role.rds_enhanced_monitoring[0].name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonRDSEnhancedMonitoringRole"
}
