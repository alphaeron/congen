variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "congen"
}

variable "environment" {
  description = "Environment name (e.g., staging, production)"
  type        = string
  validation {
    condition     = contains(["staging", "production"], var.environment)
    error_message = "Environment must be either 'staging' or 'production'."
  }
}

variable "vault_secret_path_prefix" {
  description = "Path prefix for secrets in Vault (should use module.vault.vault_secret_path_prefix output)"
  type        = string
}

# Infrastructure inputs (from aws-infrastructure module)
variable "vpc_id" {
  description = "VPC ID (from aws-infrastructure module)"
  type        = string
}

variable "database_subnet_ids" {
  description = "Database subnet IDs (from aws-infrastructure module)"
  type        = list(string)
}

variable "private_subnet_ids" {
  description = "Private subnet IDs (from aws-infrastructure module)"
  type        = list(string)
}

variable "eks_node_security_group_id" {
  description = "EKS node security group ID (from aws-infrastructure module)"
  type        = string
}

variable "availability_zones" {
  description = "Availability zones (from aws-infrastructure module)"
  type        = list(string)
}

# RDS Configuration
variable "rds_instance_class" {
  description = "RDS instance class (e.g., db.r6g.large for staging, db.r6g.xlarge for production)"
  type        = string
  default     = "db.r6g.large"
}

variable "rds_engine_version" {
  description = "PostgreSQL engine version for Aurora"
  type        = string
  default     = "15.4"
}

variable "rds_database_name" {
  description = "Database name"
  type        = string
  default     = "congen"
}

variable "rds_username" {
  description = "Master username for RDS"
  type        = string
  default     = "postgres"
  sensitive   = true
}

variable "rds_port" {
  description = "PostgreSQL port number (default 5432)"
  type        = number
  default     = 5432
}

variable "rds_backup_retention_period" {
  description = "Days to retain backups (7 for staging, 30 for production)"
  type        = number
  default     = 7
}

variable "rds_preferred_maintenance_window" {
  description = "Preferred maintenance window for RDS (e.g., sun:03:00-sun:04:00)"
  type        = string
  default     = "sun:03:00-sun:04:00"
}

variable "rds_preferred_backup_window" {
  description = "Preferred backup window for RDS (e.g., 03:00-04:00)"
  type        = string
  default     = "03:00-04:00"
}

variable "rds_read_replica_count" {
  description = "Number of read replicas (0 for staging, 1+ for production)"
  type        = number
  default     = 0
}

variable "rds_enable_performance_insights" {
  description = "Enable Performance Insights (false for staging, true for production)"
  type        = bool
  default     = false
}

variable "rds_enable_enhanced_monitoring" {
  description = "Enable enhanced monitoring for RDS"
  type        = bool
  default     = true
}

variable "rds_deletion_protection" {
  description = "Enable deletion protection for RDS cluster (true for production)"
  type        = bool
  default     = false
}

variable "rds_skip_final_snapshot" {
  description = "Skip final snapshot when deleting RDS cluster (false for production)"
  type        = bool
  default     = true
}

# ElastiCache Configuration
variable "elasticache_node_type" {
  description = "ElastiCache node instance type (e.g., cache.t3.medium for staging, cache.r6g.large for production)"
  type        = string
  default     = "cache.t3.medium"
}

variable "elasticache_num_cache_nodes" {
  description = "Number of cache nodes (1-2 for staging, 3+ for production)"
  type        = number
  default     = 2
}

variable "elasticache_engine_version" {
  description = "Memcached engine version"
  type        = string
  default     = "1.6.24"
}

variable "elasticache_port" {
  description = "Port number for ElastiCache (default 11211)"
  type        = number
  default     = 11211
}

variable "elasticache_auto_minor_version_upgrade" {
  description = "Enable automatic minor version upgrades for ElastiCache"
  type        = bool
  default     = true
}

variable "elasticache_snapshot_retention_limit" {
  description = "Number of days to retain ElastiCache snapshots (1 for staging, 5+ for production)"
  type        = number
  default     = 1
}

variable "elasticache_maintenance_window" {
  description = "Maintenance window for ElastiCache (e.g., sun:03:00-sun:04:00)"
  type        = string
  default     = "sun:03:00-sun:04:00"
}

variable "elasticache_snapshot_window" {
  description = "Snapshot window for ElastiCache (e.g., 03:00-05:00)"
  type        = string
  default     = "03:00-05:00"
}

# Monitoring
variable "sns_alert_email" {
  description = "Email address for CloudWatch alarm notifications (optional)"
  type        = string
  default     = null
}

# Vault Configuration (from vault module)
variable "vault_address" {
  description = "Vault server address (from vault module)"
  type        = string
}

variable "vault_root_token_secret" {
  description = "AWS Secrets Manager secret name for Vault root token (from vault module)"
  type        = string
}

variable "vault_kubernetes_auth_backend_path" {
  description = "Path of the Kubernetes auth backend in Vault (from vault module)"
  type        = string
}

variable "eks_cluster_endpoint" {
  description = "EKS cluster API endpoint (from aws-infrastructure module)"
  type        = string
}

variable "eks_cluster_certificate_authority_data" {
  description = "Base64 encoded certificate data for EKS cluster (from aws-infrastructure module)"
  type        = string
}

variable "eks_node_group_id" {
  description = "EKS node group ID (from aws-infrastructure module, for dependency)"
  type        = string
}

# Route53 Configuration (from aws-infrastructure module)
variable "route53_zone_id" {
  description = "Route53 hosted zone ID (from aws-infrastructure module)"
  type        = string
}

variable "domain_name" {
  description = "Domain name for Route53 (from aws-infrastructure module)"
  type        = string
}

variable "subdomain" {
  description = "Subdomain for the application (e.g., staging, www, or empty for root)"
  type        = string
  default     = ""
}

variable "alb_dns_name" {
  description = "DNS name of the Application Load Balancer (from aws-infrastructure module)"
  type        = string
}

variable "alb_zone_id" {
  description = "Zone ID of the Application Load Balancer (from aws-infrastructure module)"
  type        = string
}
