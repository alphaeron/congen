resource "aws_sns_topic" "alerts" {
  name = "${local.name_prefix}-alerts"

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-alerts"
    }
  )
}

resource "aws_sns_topic_subscription" "email" {
  count     = var.sns_alert_email != null ? 1 : 0
  topic_arn = aws_sns_topic.alerts.arn
  protocol  = "email"
  endpoint  = var.sns_alert_email
}

resource "aws_cloudwatch_metric_alarm" "rds_cpu" {
  alarm_name          = "${local.name_prefix}-rds-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "This metric monitors RDS CPU utilization"
  alarm_actions       = var.sns_alert_email != null ? [aws_sns_topic.alerts.arn] : []

  dimensions = {
    DBClusterIdentifier = aws_rds_cluster.main.id
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-rds-cpu-high"
    }
  )
}

resource "aws_cloudwatch_metric_alarm" "rds_connections" {
  alarm_name          = "${local.name_prefix}-rds-connections-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "DatabaseConnections"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "This metric monitors RDS database connections"
  alarm_actions       = var.sns_alert_email != null ? [aws_sns_topic.alerts.arn] : []

  dimensions = {
    DBClusterIdentifier = aws_rds_cluster.main.id
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-rds-connections-high"
    }
  )
}

resource "aws_cloudwatch_metric_alarm" "rds_freeable_memory" {
  alarm_name          = "${local.name_prefix}-rds-memory-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = 2
  metric_name         = "FreeableMemory"
  namespace           = "AWS/RDS"
  period              = 300
  statistic           = "Average"
  threshold           = 1000000000
  alarm_description   = "This metric monitors RDS freeable memory"
  alarm_actions       = var.sns_alert_email != null ? [aws_sns_topic.alerts.arn] : []

  dimensions = {
    DBClusterIdentifier = aws_rds_cluster.main.id
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-rds-memory-low"
    }
  )
}

resource "aws_cloudwatch_metric_alarm" "elasticache_cpu" {
  alarm_name          = "${local.name_prefix}-elasticache-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/ElastiCache"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "This metric monitors ElastiCache CPU utilization"
  alarm_actions       = var.sns_alert_email != null ? [aws_sns_topic.alerts.arn] : []

  dimensions = {
    CacheClusterId = aws_elasticache_cluster.main.id
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-elasticache-cpu-high"
    }
  )
}

resource "aws_cloudwatch_metric_alarm" "elasticache_evictions" {
  alarm_name          = "${local.name_prefix}-elasticache-evictions-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "Evictions"
  namespace           = "AWS/ElastiCache"
  period              = 300
  statistic           = "Sum"
  threshold           = 100
  alarm_description   = "This metric monitors ElastiCache evictions"
  alarm_actions       = var.sns_alert_email != null ? [aws_sns_topic.alerts.arn] : []

  dimensions = {
    CacheClusterId = aws_elasticache_cluster.main.id
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-elasticache-evictions-high"
    }
  )
}
