# AWS Infrastructure Outputs
output "vpc_id" {
  description = "ID of the VPC"
  value       = module.aws_infrastructure.vpc_id
}

output "eks_cluster_id" {
  description = "EKS cluster ID"
  value       = module.aws_infrastructure.eks_cluster_id
}

output "eks_cluster_endpoint" {
  description = "EKS cluster API endpoint"
  value       = module.aws_infrastructure.eks_cluster_endpoint
}

output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = module.aws_infrastructure.alb_dns_name
}

output "route53_zone_id" {
  description = "Route53 hosted zone ID"
  value       = module.aws_infrastructure.route53_zone_id
}

output "route53_name_servers" {
  description = "Route53 hosted zone name servers (for domain registrar configuration)"
  value       = module.aws_infrastructure.route53_name_servers
}

output "route53_record_name" {
  description = "Route53 record name (FQDN) for the application"
  value       = module.congen.route53_record_name
}

# Vault Outputs
output "vault_address" {
  description = "Vault server address"
  value       = module.vault.vault_address
}

output "vault_namespace" {
  description = "Kubernetes namespace where Vault is deployed"
  value       = module.vault.vault_namespace
}

# RDS Outputs
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

# ElastiCache Outputs
output "elasticache_configuration_endpoint" {
  description = "ElastiCache configuration endpoint"
  value       = module.congen.elasticache_configuration_endpoint
}

output "elasticache_port" {
  description = "ElastiCache port"
  value       = module.congen.elasticache_port
}

# Vault Secret Paths
output "vault_database_secret_path" {
  description = "Full path to database secrets in Vault"
  value       = module.congen.vault_database_secret_path
}

output "vault_cache_secret_path" {
  description = "Full path to cache secrets in Vault"
  value       = module.congen.vault_cache_secret_path
}
