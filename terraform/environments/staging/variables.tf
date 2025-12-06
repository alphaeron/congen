variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "congen"
}

variable "aws_region" {
  description = "AWS region for resources"
  type        = string
  default     = "us-east-1"
}

variable "availability_zones" {
  description = "List of availability zones to use (must specify at least 2 for high availability)"
  type        = list(string)
  default     = ["us-east-1a", "us-east-1b"]
}

variable "vpc_cidr" {
  description = "CIDR block for the VPC"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidrs" {
  description = "CIDR blocks for public subnets (one per AZ)"
  type        = list(string)
  default     = ["10.0.1.0/24", "10.0.2.0/24"]
}

variable "private_subnet_cidrs" {
  description = "CIDR blocks for private subnets (one per AZ)"
  type        = list(string)
  default     = ["10.0.11.0/24", "10.0.12.0/24"]
}

variable "database_subnet_cidrs" {
  description = "CIDR blocks for database subnets (one per AZ)"
  type        = list(string)
  default     = ["10.0.21.0/24", "10.0.22.0/24"]
}

variable "domain_name" {
  description = "Domain name for Route53 hosted zone (e.g., congen.com)"
  type        = string
}

variable "subdomain" {
  description = "Subdomain for the application (e.g., staging, www, or empty for root)"
  type        = string
  default     = "staging"
}

# EKS Configuration
variable "eks_cluster_version" {
  description = "Kubernetes version for EKS cluster"
  type        = string
  default     = "1.28"
}

variable "eks_node_instance_types" {
  description = "EC2 instance types for EKS node groups"
  type        = list(string)
  default     = ["t3.medium"]
}

variable "eks_node_desired_size" {
  description = "Desired number of nodes per AZ"
  type        = number
  default     = 1
}

variable "eks_node_min_size" {
  description = "Minimum number of nodes per AZ"
  type        = number
  default     = 1
}

variable "eks_node_max_size" {
  description = "Maximum number of nodes per AZ"
  type        = number
  default     = 2
}

variable "eks_node_disk_size" {
  description = "Disk size in GB for EKS nodes"
  type        = number
  default     = 20
}

variable "eks_enable_cluster_logging" {
  description = "Enable EKS cluster logging"
  type        = list(string)
  default     = ["api", "audit", "authenticator"]
}

variable "eks_cluster_log_retention_days" {
  description = "CloudWatch log retention in days for EKS cluster logs"
  type        = number
  default     = 7
}

variable "eks_node_group_max_unavailable" {
  description = "Maximum number of nodes unavailable during node group update"
  type        = number
  default     = 1
}

# ALB Configuration
variable "alb_idle_timeout" {
  description = "Idle timeout in seconds for ALB"
  type        = number
  default     = 60
}

variable "acm_certificate_validation_ttl" {
  description = "TTL in seconds for ACM certificate validation DNS records"
  type        = number
  default     = 60
}

variable "acm_certificate_validation_timeout" {
  description = "Timeout for ACM certificate validation (e.g., 5m, 10m)"
  type        = string
  default     = "5m"
}

# Ingress Controller Configuration
variable "ingress_controller_namespace" {
  description = "Kubernetes namespace where the ingress controller is deployed"
  type        = string
  default     = "ingress-nginx"
}

variable "ingress_controller_service_name" {
  description = "Name of the ingress controller service"
  type        = string
  default     = "ingress-nginx-controller"
}

variable "ingress_controller_service_port" {
  description = "Port number of the ingress controller service"
  type        = number
  default     = 80
}

variable "ingress_controller_replica_count" {
  description = "Number of replicas for the ingress controller"
  type        = number
  default     = 1
}

variable "ingress_controller_helm_chart_version" {
  description = "Version of the nginx-ingress Helm chart"
  type        = string
  default     = "4.8.3"
}

variable "ingress_controller_cpu_request" {
  description = "CPU request for the ingress controller pods"
  type        = string
  default     = "100m"
}

variable "ingress_controller_cpu_limit" {
  description = "CPU limit for the ingress controller pods"
  type        = string
  default     = "500m"
}

variable "ingress_controller_memory_request" {
  description = "Memory request for the ingress controller pods"
  type        = string
  default     = "128Mi"
}

variable "ingress_controller_memory_limit" {
  description = "Memory limit for the ingress controller pods"
  type        = string
  default     = "512Mi"
}

variable "ingress_controller_enable_pdb" {
  description = "Enable Pod Disruption Budget for the ingress controller"
  type        = bool
  default     = false
}

variable "ingress_controller_pdb_min_available" {
  description = "Minimum number of available pods for the ingress controller PDB"
  type        = number
  default     = 1
}

variable "ingress_controller_enable_pod_anti_affinity" {
  description = "Enable pod anti-affinity for the ingress controller"
  type        = bool
  default     = false
}

# ALB Target Group Health Check
variable "alb_target_group_health_check_enabled" {
  description = "Enable health checks for the ALB target group"
  type        = bool
  default     = true
}

variable "alb_target_group_health_check_path" {
  description = "Health check path"
  type        = string
  default     = "/healthz"
}

variable "alb_target_group_health_check_protocol" {
  description = "Health check protocol"
  type        = string
  default     = "HTTP"
}

variable "alb_target_group_health_check_matcher" {
  description = "Health check HTTP response codes"
  type        = string
  default     = "200"
}

variable "alb_target_group_health_check_interval" {
  description = "Health check interval in seconds"
  type        = number
  default     = 30
}

variable "alb_target_group_health_check_timeout" {
  description = "Health check timeout in seconds"
  type        = number
  default     = 5
}

variable "alb_target_group_health_check_healthy_threshold" {
  description = "Number of consecutive successful health checks required"
  type        = number
  default     = 2
}

variable "alb_target_group_health_check_unhealthy_threshold" {
  description = "Number of consecutive failed health checks required"
  type        = number
  default     = 2
}

variable "alb_target_group_deregistration_delay" {
  description = "Deregistration delay in seconds"
  type        = number
  default     = 300
}

# Security Configuration
variable "enable_vpc_flow_logs" {
  description = "Enable VPC flow logs"
  type        = bool
  default     = true
}

variable "vpc_flow_log_retention_days" {
  description = "CloudWatch log retention in days for VPC flow logs"
  type        = number
  default     = 7
}

variable "enable_cloudtrail" {
  description = "Enable CloudTrail"
  type        = bool
  default     = true
}

variable "cloudtrail_log_retention_days" {
  description = "CloudWatch log retention in days for CloudTrail logs"
  type        = number
  default     = 7
}

# Vault Configuration
variable "vault_namespace" {
  description = "Kubernetes namespace for Vault deployment"
  type        = string
  default     = "vault"
}

variable "vault_helm_chart_version" {
  description = "Version of the Vault Helm chart"
  type        = string
  default     = "0.24.0"
}

variable "vault_helm_timeout" {
  description = "Timeout in seconds for Helm release deployment"
  type        = number
  default     = 600
}

variable "vault_unseal_key_shares" {
  description = "Number of unseal key shares to generate (1-5)"
  type        = number
  default     = 1
}

variable "vault_unseal_key_threshold" {
  description = "Number of unseal keys required to unseal (must be <= shares)"
  type        = number
  default     = 1
}

variable "vault_secret_path_prefix" {
  description = "Path prefix for secrets in Vault"
  type        = string
  default     = "congen"
}

variable "vault_image_tag" {
  description = "Vault Docker image tag"
  type        = string
  default     = "1.15.2"
}

variable "vault_injector_image_tag" {
  description = "Vault Kubernetes injector Docker image tag"
  type        = string
  default     = "1.1.0"
}

variable "vault_cpu_request" {
  description = "CPU request for Vault pods"
  type        = string
  default     = "250m"
}

variable "vault_cpu_limit" {
  description = "CPU limit for Vault pods"
  type        = string
  default     = "500m"
}

variable "vault_memory_request" {
  description = "Memory request for Vault pods"
  type        = string
  default     = "256Mi"
}

variable "vault_memory_limit" {
  description = "Memory limit for Vault pods"
  type        = string
  default     = "512Mi"
}

variable "enable_vault_network_policy" {
  description = "Enable Kubernetes NetworkPolicy for Vault"
  type        = bool
  default     = true
}

variable "vault_allowed_namespaces" {
  description = "List of namespaces allowed to access Vault"
  type        = list(string)
  default     = ["congen"]
}

variable "vault_audit_log_retention_days" {
  description = "CloudWatch log retention in days for Vault audit logs"
  type        = number
  default     = 30
}

variable "vault_pdb_min_available" {
  description = "Minimum number of Vault pods that must be available (PDB)"
  type        = number
  default     = 1
}

variable "vault_storage_noncurrent_version_expiration_days" {
  description = "Number of days after which noncurrent versions of objects in Vault storage S3 bucket are expired"
  type        = number
  default     = 90
}

variable "vault_kubernetes_service_account" {
  description = "Kubernetes service account for Vault auth"
  type        = string
  default     = "terraform"
}

variable "vault_kubernetes_namespace" {
  description = "Kubernetes namespace for service account"
  type        = string
  default     = "default"
}

variable "vault_kubernetes_role" {
  description = "Kubernetes auth role name"
  type        = string
  default     = "terraform"
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

variable "rds_enable_enhanced_monitoring" {
  description = "Enable enhanced monitoring for RDS"
  type        = bool
  default     = true
}

# ElastiCache Configuration
variable "elasticache_node_type" {
  description = "ElastiCache node instance type (e.g., cache.t3.micro for staging, cache.r6g.large for production)"
  type        = string
  default     = "cache.t3.micro"
}

variable "elasticache_num_cache_nodes" {
  description = "Number of cache nodes (1-2 for staging, 3+ for production)"
  type        = number
  default     = 1
}

variable "elasticache_engine_version" {
  description = "Memcached engine version"
  type        = string
  default     = "1.6.20"
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

variable "elasticache_maintenance_window" {
  description = "Maintenance window for ElastiCache (e.g., sun:03:00-sun:04:00)"
  type        = string
  default     = "sun:05:00-sun:06:00"
}

variable "elasticache_snapshot_retention_limit" {
  description = "Number of days to retain ElastiCache snapshots (1 for staging, 5+ for production)"
  type        = number
  default     = 1
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
