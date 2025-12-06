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

# Monitoring
variable "sns_alert_email" {
  description = "Email address for CloudWatch alarm notifications (optional)"
  type        = string
  default     = null
}
