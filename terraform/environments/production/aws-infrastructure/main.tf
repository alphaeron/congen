provider "aws" {
  region = var.aws_region
}

module "aws_infrastructure" {
  source = "../../../modules/aws-infrastructure"

  environment        = "production"
  project_name       = var.project_name
  aws_region         = var.aws_region
  availability_zones = var.availability_zones

  # VPC Configuration
  vpc_cidr              = var.vpc_cidr
  public_subnet_cidrs   = var.public_subnet_cidrs
  private_subnet_cidrs  = var.private_subnet_cidrs
  database_subnet_cidrs = var.database_subnet_cidrs

  # High availability for production
  single_nat_gateway         = false
  enable_cross_region_backup = var.enable_cross_region_backup
  backup_region              = var.backup_region

  # EKS Configuration
  eks_cluster_version              = var.eks_cluster_version
  eks_node_instance_types          = var.eks_node_instance_types
  eks_node_desired_size            = var.eks_node_desired_size
  eks_node_min_size                = var.eks_node_min_size
  eks_node_max_size                = var.eks_node_max_size
  eks_node_disk_size               = var.eks_node_disk_size
  eks_enable_cluster_logging       = var.eks_enable_cluster_logging
  eks_cluster_log_retention_days   = var.eks_cluster_log_retention_days
  eks_endpoint_private_access      = true
  eks_endpoint_public_access       = false
  eks_endpoint_public_access_cidrs = []
  eks_node_group_max_unavailable   = var.eks_node_group_max_unavailable

  # ALB and Route53 Configuration
  domain_name                        = var.domain_name
  subdomain                          = var.subdomain
  alb_enable_deletion_protection     = true
  alb_idle_timeout                   = var.alb_idle_timeout
  acm_certificate_validation_ttl     = var.acm_certificate_validation_ttl
  acm_certificate_validation_timeout = var.acm_certificate_validation_timeout

  # Ingress Controller Configuration
  ingress_controller_namespace                = var.ingress_controller_namespace
  ingress_controller_service_name             = var.ingress_controller_service_name
  ingress_controller_service_port             = var.ingress_controller_service_port
  ingress_controller_replica_count            = var.ingress_controller_replica_count
  ingress_controller_helm_chart_version       = var.ingress_controller_helm_chart_version
  ingress_controller_cpu_request              = var.ingress_controller_cpu_request
  ingress_controller_cpu_limit                = var.ingress_controller_cpu_limit
  ingress_controller_memory_request           = var.ingress_controller_memory_request
  ingress_controller_memory_limit             = var.ingress_controller_memory_limit
  ingress_controller_enable_pdb               = var.ingress_controller_enable_pdb
  ingress_controller_pdb_min_available        = var.ingress_controller_pdb_min_available
  ingress_controller_enable_pod_anti_affinity = var.ingress_controller_enable_pod_anti_affinity

  # ALB Target Group Health Check
  alb_target_group_health_check_enabled             = var.alb_target_group_health_check_enabled
  alb_target_group_health_check_path                = var.alb_target_group_health_check_path
  alb_target_group_health_check_protocol            = var.alb_target_group_health_check_protocol
  alb_target_group_health_check_matcher             = var.alb_target_group_health_check_matcher
  alb_target_group_health_check_interval            = var.alb_target_group_health_check_interval
  alb_target_group_health_check_timeout             = var.alb_target_group_health_check_timeout
  alb_target_group_health_check_healthy_threshold   = var.alb_target_group_health_check_healthy_threshold
  alb_target_group_health_check_unhealthy_threshold = var.alb_target_group_health_check_unhealthy_threshold
  alb_target_group_deregistration_delay             = var.alb_target_group_deregistration_delay

  # Security Configuration
  enable_vpc_flow_logs          = var.enable_vpc_flow_logs
  vpc_flow_log_retention_days   = var.vpc_flow_log_retention_days
  enable_cloudtrail             = var.enable_cloudtrail
  cloudtrail_log_retention_days = var.cloudtrail_log_retention_days

  # Monitoring
  sns_alert_email = var.sns_alert_email
}
