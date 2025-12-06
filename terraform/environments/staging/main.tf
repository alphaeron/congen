# Step 1: Deploy base AWS infrastructure
module "aws_infrastructure" {
  source = "./aws-infrastructure"
  
  project_name      = var.project_name
  aws_region        = var.aws_region
  availability_zones = var.availability_zones
  
  # VPC Configuration
  vpc_cidr             = var.vpc_cidr
  public_subnet_cidrs  = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  database_subnet_cidrs = var.database_subnet_cidrs
  
  # EKS Configuration
  eks_cluster_version        = var.eks_cluster_version
  eks_node_instance_types    = var.eks_node_instance_types
  eks_node_desired_size      = var.eks_node_desired_size
  eks_node_min_size          = var.eks_node_min_size
  eks_node_max_size          = var.eks_node_max_size
  eks_node_disk_size         = var.eks_node_disk_size
  eks_enable_cluster_logging = var.eks_enable_cluster_logging
  eks_cluster_log_retention_days = var.eks_cluster_log_retention_days
  eks_node_group_max_unavailable = var.eks_node_group_max_unavailable
  
  # ALB and Route53 Configuration
  domain_name                        = var.domain_name
  subdomain                          = var.subdomain
  alb_idle_timeout                   = var.alb_idle_timeout
  acm_certificate_validation_ttl     = var.acm_certificate_validation_ttl
  acm_certificate_validation_timeout = var.acm_certificate_validation_timeout
  
  # Ingress Controller Configuration
  ingress_controller_namespace              = var.ingress_controller_namespace
  ingress_controller_service_name          = var.ingress_controller_service_name
  ingress_controller_service_port          = var.ingress_controller_service_port
  ingress_controller_replica_count         = var.ingress_controller_replica_count
  ingress_controller_helm_chart_version    = var.ingress_controller_helm_chart_version
  ingress_controller_cpu_request           = var.ingress_controller_cpu_request
  ingress_controller_cpu_limit             = var.ingress_controller_cpu_limit
  ingress_controller_memory_request        = var.ingress_controller_memory_request
  ingress_controller_memory_limit         = var.ingress_controller_memory_limit
  ingress_controller_enable_pdb            = var.ingress_controller_enable_pdb
  ingress_controller_pdb_min_available    = var.ingress_controller_pdb_min_available
  ingress_controller_enable_pod_anti_affinity = var.ingress_controller_enable_pod_anti_affinity
  
  # ALB Target Group Health Check
  alb_target_group_health_check_enabled         = var.alb_target_group_health_check_enabled
  alb_target_group_health_check_path            = var.alb_target_group_health_check_path
  alb_target_group_health_check_protocol         = var.alb_target_group_health_check_protocol
  alb_target_group_health_check_matcher          = var.alb_target_group_health_check_matcher
  alb_target_group_health_check_interval        = var.alb_target_group_health_check_interval
  alb_target_group_health_check_timeout          = var.alb_target_group_health_check_timeout
  alb_target_group_health_check_healthy_threshold   = var.alb_target_group_health_check_healthy_threshold
  alb_target_group_health_check_unhealthy_threshold = var.alb_target_group_health_check_unhealthy_threshold
  alb_target_group_deregistration_delay          = var.alb_target_group_deregistration_delay
  
  # Security Configuration
  enable_vpc_flow_logs         = var.enable_vpc_flow_logs
  vpc_flow_log_retention_days  = var.vpc_flow_log_retention_days
  enable_cloudtrail            = var.enable_cloudtrail
  cloudtrail_log_retention_days = var.cloudtrail_log_retention_days
  
  # Monitoring
  sns_alert_email = var.sns_alert_email
}

# Step 2: Deploy Vault
module "vault" {
  source = "./vault"
  
  project_name = var.project_name
  aws_region   = var.aws_region
  
  # EKS cluster information (from aws-infrastructure module)
  eks_cluster_id                        = module.aws_infrastructure.eks_cluster_id
  eks_cluster_endpoint                   = module.aws_infrastructure.eks_cluster_endpoint
  eks_cluster_certificate_authority_data = module.aws_infrastructure.eks_cluster_certificate_authority_data
  eks_cluster_oidc_issuer_url           = module.aws_infrastructure.eks_cluster_oidc_issuer_url
  eks_node_group_id                     = module.aws_infrastructure.eks_node_group_id
  vpc_id                                = module.aws_infrastructure.vpc_id
  
  # Vault configuration (staging - single replica, cost optimized)
  vault_namespace    = var.vault_namespace
  vault_helm_chart_version = var.vault_helm_chart_version
  vault_helm_timeout       = var.vault_helm_timeout
  vault_unseal_key_shares  = var.vault_unseal_key_shares
  vault_unseal_key_threshold = var.vault_unseal_key_threshold
  vault_secret_path_prefix  = var.vault_secret_path_prefix
  vault_image_tag          = var.vault_image_tag
  vault_injector_image_tag = var.vault_injector_image_tag
  vault_cpu_request        = var.vault_cpu_request
  vault_cpu_limit         = var.vault_cpu_limit
  vault_memory_request    = var.vault_memory_request
  vault_memory_limit      = var.vault_memory_limit
  enable_vault_network_policy = var.enable_vault_network_policy
  vault_allowed_namespaces    = var.vault_allowed_namespaces
  vault_audit_log_retention_days = var.vault_audit_log_retention_days
  vault_pdb_min_available        = var.vault_pdb_min_available
  vault_storage_noncurrent_version_expiration_days = var.vault_storage_noncurrent_version_expiration_days
  vault_kubernetes_service_account = var.vault_kubernetes_service_account
  vault_kubernetes_namespace       = var.vault_kubernetes_namespace
  vault_kubernetes_role            = var.vault_kubernetes_role
  
  depends_on = [module.aws_infrastructure]
}

# Step 3: Deploy application-specific resources (creates RDS, ElastiCache, writes secrets to Vault)
module "congen" {
  source = "./congen"
  
  project_name = var.project_name
  aws_region   = var.aws_region
  
  # Vault configuration (from vault module)
  vault_address                      = module.vault.vault_address
  vault_secret_path_prefix           = module.vault.vault_secret_path_prefix
  vault_kubernetes_auth_backend_path = module.vault.vault_kubernetes_auth_backend_path
  vault_root_token_secret            = module.vault.vault_root_token_secret
  vault_kubernetes_role             = module.vault.vault_kubernetes_role
  
  # Infrastructure inputs (from aws-infrastructure module)
  vpc_id                      = module.aws_infrastructure.vpc_id
  database_subnet_ids         = module.aws_infrastructure.database_subnet_ids
  private_subnet_ids          = module.aws_infrastructure.private_subnet_ids
  eks_node_security_group_id  = module.aws_infrastructure.eks_node_security_group_id
  availability_zones          = module.aws_infrastructure.availability_zones
  
  # Route53 and ALB (from aws-infrastructure module)
  route53_zone_id = module.aws_infrastructure.route53_zone_id
  domain_name     = module.aws_infrastructure.route53_domain_name
  subdomain       = var.subdomain
  alb_dns_name    = module.aws_infrastructure.alb_dns_name
  alb_zone_id     = module.aws_infrastructure.alb_zone_id
  
  # EKS cluster information (from aws-infrastructure module, for Kubernetes resources)
  eks_cluster_id                        = module.aws_infrastructure.eks_cluster_id
  eks_cluster_endpoint                  = module.aws_infrastructure.eks_cluster_endpoint
  eks_cluster_certificate_authority_data = module.aws_infrastructure.eks_cluster_certificate_authority_data
  eks_node_group_id                     = module.aws_infrastructure.eks_node_group_id
  
  # RDS configuration (staging - cost optimized)
  rds_instance_class              = var.rds_instance_class
  rds_engine_version              = var.rds_engine_version
  rds_database_name               = var.rds_database_name
  rds_username                    = var.rds_username
  rds_port                        = var.rds_port
  rds_backup_retention_period     = var.rds_backup_retention_period
  rds_preferred_maintenance_window = var.rds_preferred_maintenance_window
  rds_preferred_backup_window     = var.rds_preferred_backup_window
  rds_read_replica_count          = var.rds_read_replica_count
  rds_enable_enhanced_monitoring  = var.rds_enable_enhanced_monitoring
  
  # ElastiCache configuration (staging - cost optimized)
  elasticache_node_type                = var.elasticache_node_type
  elasticache_num_cache_nodes           = var.elasticache_num_cache_nodes
  elasticache_engine_version           = var.elasticache_engine_version
  elasticache_port                     = var.elasticache_port
  elasticache_auto_minor_version_upgrade = var.elasticache_auto_minor_version_upgrade
  elasticache_maintenance_window       = var.elasticache_maintenance_window
  elasticache_snapshot_retention_limit = var.elasticache_snapshot_retention_limit
  elasticache_snapshot_window         = var.elasticache_snapshot_window
  
  # Monitoring
  sns_alert_email = var.sns_alert_email
  
  depends_on = [module.aws_infrastructure, module.vault]
}
