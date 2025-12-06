output "vault_database_secret_path" {
  description = "Full path to database secrets in Vault"
  value       = module.congen.vault_database_secret_path
}

output "vault_cache_secret_path" {
  description = "Full path to cache secrets in Vault"
  value       = module.congen.vault_cache_secret_path
}

output "rds_writer_endpoint" {
  description = "RDS cluster writer endpoint"
  value       = module.congen.rds_writer_endpoint
}

output "rds_reader_endpoint" {
  description = "RDS cluster reader endpoint"
  value       = module.congen.rds_reader_endpoint
}

output "rds_database_name" {
  description = "RDS database name"
  value       = module.congen.rds_database_name
}

output "rds_cluster_id" {
  description = "RDS cluster identifier"
  value       = module.congen.rds_cluster_id
}

output "elasticache_configuration_endpoint" {
  description = "ElastiCache configuration endpoint (for Memcached clusters)"
  value       = module.congen.elasticache_configuration_endpoint
}

output "elasticache_port" {
  description = "ElastiCache port"
  value       = module.congen.elasticache_port
}

output "elasticache_cluster_id" {
  description = "ElastiCache cluster identifier"
  value       = module.congen.elasticache_cluster_id
}

output "rds_security_group_id" {
  description = "Security group ID for RDS"
  value       = module.congen.rds_security_group_id
}

output "elasticache_security_group_id" {
  description = "Security group ID for ElastiCache"
  value       = module.congen.elasticache_security_group_id
}

output "kms_key_id" {
  description = "KMS key ARN used for RDS encryption"
  value       = module.congen.kms_key_id
}

output "kms_key_alias" {
  description = "KMS key alias for RDS"
  value       = module.congen.kms_key_alias
}

output "sns_topic_arn" {
  description = "ARN of the SNS topic for alerts"
  value       = module.congen.sns_topic_arn
}

output "route53_record_name" {
  description = "Route53 record name (FQDN)"
  value       = module.congen.route53_record_name
}
