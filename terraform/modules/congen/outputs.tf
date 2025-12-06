output "vault_database_secret_path" {
  description = "Full path to database secrets in Vault"
  value       = "${var.vault_secret_path_prefix}/${var.environment}/database"
}

output "vault_cache_secret_path" {
  description = "Full path to cache secrets in Vault"
  value       = "${var.vault_secret_path_prefix}/${var.environment}/cache"
}

output "rds_writer_endpoint" {
  description = "RDS cluster writer endpoint"
  value       = aws_rds_cluster.main.endpoint
}

output "rds_reader_endpoint" {
  description = "RDS cluster reader endpoint"
  value       = aws_rds_cluster.main.reader_endpoint
}

output "rds_database_name" {
  description = "RDS database name"
  value       = aws_rds_cluster.main.database_name
}

output "rds_cluster_id" {
  description = "RDS cluster identifier"
  value       = aws_rds_cluster.main.cluster_identifier
}

output "elasticache_configuration_endpoint" {
  description = "ElastiCache configuration endpoint (for Memcached clusters)"
  value       = aws_elasticache_cluster.main.configuration_endpoint
}

output "elasticache_port" {
  description = "ElastiCache port"
  value       = aws_elasticache_cluster.main.port
}

output "elasticache_cluster_id" {
  description = "ElastiCache cluster identifier"
  value       = aws_elasticache_cluster.main.id
}

output "rds_security_group_id" {
  description = "Security group ID for RDS"
  value       = aws_security_group.rds.id
}

output "elasticache_security_group_id" {
  description = "Security group ID for ElastiCache"
  value       = aws_security_group.elasticache.id
}

output "kms_key_id" {
  description = "KMS key ARN used for RDS encryption"
  value       = aws_kms_key.rds.arn
}

output "kms_key_alias" {
  description = "KMS key alias for RDS"
  value       = aws_kms_alias.rds.name
}

output "sns_topic_arn" {
  description = "ARN of the SNS topic for alerts"
  value       = aws_sns_topic.alerts.arn
}

output "route53_record_name" {
  description = "Route53 record name (FQDN)"
  value       = aws_route53_record.alb.fqdn
}
