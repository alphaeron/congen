variable "environment" {
  description = "Environment name (e.g., staging, production)"
  type        = string
  validation {
    condition     = contains(["staging", "production"], var.environment)
    error_message = "Environment must be either 'staging' or 'production'."
  }
}

variable "project_name" {
  description = "Project name used for resource naming"
  type        = string
  default     = "congen"
}

variable "aws_region" {
  description = "AWS region for resources"
  type        = string
}

variable "availability_zones" {
  description = "List of availability zones to use (must specify at least 2 for high availability)"
  type        = list(string)
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
  description = "CIDR blocks for database subnets (one per AZ, optional isolation)"
  type        = list(string)
  default     = ["10.0.21.0/24", "10.0.22.0/24"]
}

variable "enable_vpc_flow_logs" {
  description = "Enable VPC flow logs"
  type        = bool
  default     = true
}

variable "enable_cloudtrail" {
  description = "Enable CloudTrail for audit logging"
  type        = bool
  default     = true
}


variable "allowed_cidr_blocks" {
  description = "CIDR blocks allowed to access resources (for application pods)"
  type        = list(string)
  default     = []
}

variable "enable_nat_gateway" {
  description = "Enable NAT Gateway (false for staging if using VPC endpoints, true for production)"
  type        = bool
  default     = true
}

variable "single_nat_gateway" {
  description = "Use single NAT Gateway for staging cost savings"
  type        = bool
  default     = false
}

variable "enable_cross_region_backup" {
  description = "Enable cross-region backup replication (false for staging, true for production)"
  type        = bool
  default     = false
}

variable "backup_region" {
  description = "AWS region for cross-region backups (required if enable_cross_region_backup is true)"
  type        = string
  default     = null
}

variable "sns_alert_email" {
  description = "Email address for CloudWatch alarm notifications (optional)"
  type        = string
  default     = null
}

variable "eks_cluster_version" {
  description = "Kubernetes version for EKS cluster"
  type        = string
  default     = "1.28"
}

variable "eks_node_instance_types" {
  description = "EC2 instance types for EKS node groups (e.g., [\"t3.medium\"] for staging, [\"t3.large\", \"t3.xlarge\"] for production)"
  type        = list(string)
  default     = ["t3.medium"]
}

variable "eks_node_desired_size" {
  description = "Desired number of nodes per AZ (1 for staging, 2+ for production)"
  type        = number
  default     = 1
}

variable "eks_node_min_size" {
  description = "Minimum number of nodes per AZ"
  type        = number
  default     = 1
}

variable "eks_node_max_size" {
  description = "Maximum number of nodes per AZ (2 for staging, 4+ for production)"
  type        = number
  default     = 2
}

variable "eks_node_disk_size" {
  description = "Disk size in GB for EKS nodes"
  type        = number
  default     = 20
}

variable "eks_enable_cluster_logging" {
  description = "Enable EKS cluster logging (api, audit, authenticator, controllerManager, scheduler)"
  type        = list(string)
  default     = ["api", "audit", "authenticator"]
}

variable "eks_endpoint_private_access" {
  description = "Enable private access to the EKS cluster API endpoint (default: true, recommended for security)"
  type        = bool
  default     = true
}

variable "eks_endpoint_public_access" {
  description = "Enable public access to the EKS cluster API endpoint (default: false, set to true only if needed for kubectl access from outside VPC)"
  type        = bool
  default     = false
}

variable "eks_endpoint_public_access_cidrs" {
  description = "List of CIDR blocks that can access the EKS cluster API endpoint when public access is enabled (empty list means all IPs, not recommended for security)"
  type        = list(string)
  default     = []
}

variable "eks_node_group_max_unavailable" {
  description = "Maximum number of nodes unavailable during node group update"
  type        = number
  default     = 1
}

variable "domain_name" {
  description = "Domain name for Route53 hosted zone (e.g., congen.com)"
  type        = string
}

variable "subdomain" {
  description = "Subdomain for the application (e.g., staging, www, or empty for root)"
  type        = string
  default     = ""
}

variable "alb_enable_deletion_protection" {
  description = "Enable deletion protection for ALB (true for production)"
  type        = bool
  default     = false
}

variable "alb_idle_timeout" {
  description = "Idle timeout in seconds for ALB (default 60)"
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

variable "eks_cluster_log_retention_days" {
  description = "CloudWatch log retention in days for EKS cluster logs"
  type        = number
  default     = 7
}

variable "vpc_flow_log_retention_days" {
  description = "CloudWatch log retention in days for VPC flow logs"
  type        = number
  default     = 7
}

variable "cloudtrail_log_retention_days" {
  description = "CloudWatch log retention in days for CloudTrail logs"
  type        = number
  default     = 7
}

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

variable "ingress_controller_enable_pdb" {
  description = "Enable Pod Disruption Budget for the ingress controller"
  type        = bool
  default     = false
}

variable "ingress_controller_pdb_min_available" {
  description = "Minimum number of available pods for the ingress controller PDB (only used if PDB is enabled)"
  type        = number
  default     = 1
}

variable "ingress_controller_enable_pod_anti_affinity" {
  description = "Enable pod anti-affinity for the ingress controller to spread pods across nodes"
  type        = bool
  default     = false
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

variable "alb_target_group_health_check_enabled" {
  description = "Enable health checks for the ALB target group"
  type        = bool
  default     = true
}

variable "alb_target_group_health_check_path" {
  description = "Health check path for the ALB target group"
  type        = string
  default     = "/healthz"
}

variable "alb_target_group_health_check_protocol" {
  description = "Health check protocol for the ALB target group"
  type        = string
  default     = "HTTP"
}

variable "alb_target_group_health_check_matcher" {
  description = "HTTP status codes to use when checking for a successful response from a target"
  type        = string
  default     = "200"
}

variable "alb_target_group_health_check_interval" {
  description = "Approximate amount of time, in seconds, between health checks of an individual target"
  type        = number
  default     = 30
}

variable "alb_target_group_health_check_timeout" {
  description = "Amount of time, in seconds, during which no response means a failed health check"
  type        = number
  default     = 5
}

variable "alb_target_group_health_check_healthy_threshold" {
  description = "Number of consecutive health checks successes required before considering an unhealthy target healthy"
  type        = number
  default     = 2
}

variable "alb_target_group_health_check_unhealthy_threshold" {
  description = "Number of consecutive health check failures required before considering a target unhealthy"
  type        = number
  default     = 2
}

variable "alb_target_group_deregistration_delay" {
  description = "Amount of time for Elastic Load Balancing to wait before deregistering a target"
  type        = number
  default     = 30
}
