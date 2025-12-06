provider "aws" {
  region = var.aws_region
}

# Configure Vault provider (needed for congen module)
provider "vault" {
  address         = var.vault_address
  namespace       = ""
  skip_tls_verify = false
  
  auth_login {
    path = "auth/kubernetes/login"
    
    parameters = {
      role = var.vault_kubernetes_role
    }
  }
}

# Configure Kubernetes provider (needed for Kubernetes resources)
provider "kubernetes" {
  host                   = var.eks_cluster_endpoint
  cluster_ca_certificate = base64decode(var.eks_cluster_certificate_authority_data)
  exec {
    api_version = "client.authentication.k8s.io/v1beta1"
    command     = "aws"
    args        = ["eks", "get-token", "--cluster-name", var.eks_cluster_id]
  }
}

module "congen" {
  source = "../../../modules/congen"
  
  project_name = var.project_name
  environment  = "staging"
  
  # Vault configuration (from vault module)
  vault_address                      = var.vault_address
  vault_secret_path_prefix           = var.vault_secret_path_prefix
  vault_kubernetes_auth_backend_path = var.vault_kubernetes_auth_backend_path
  vault_root_token_secret            = var.vault_root_token_secret
  
  # Infrastructure inputs (from aws-infrastructure module)
  vpc_id                      = var.vpc_id
  database_subnet_ids         = var.database_subnet_ids
  private_subnet_ids          = var.private_subnet_ids
  eks_node_security_group_id  = var.eks_node_security_group_id
  availability_zones          = var.availability_zones
  
  # Route53 and ALB (from aws-infrastructure module)
  route53_zone_id = var.route53_zone_id
  domain_name     = var.domain_name
  subdomain       = var.subdomain
  alb_dns_name    = var.alb_dns_name
  alb_zone_id     = var.alb_zone_id
  
  # EKS cluster information (from aws-infrastructure module, for Kubernetes resources)
  eks_cluster_endpoint                  = var.eks_cluster_endpoint
  eks_cluster_certificate_authority_data = var.eks_cluster_certificate_authority_data
  eks_node_group_id                     = var.eks_node_group_id
  
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
  rds_deletion_protection         = false
  rds_skip_final_snapshot         = true
  rds_enable_performance_insights = false
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
}
