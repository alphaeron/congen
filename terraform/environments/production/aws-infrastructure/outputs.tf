output "vpc_id" {
  description = "ID of the VPC"
  value       = module.aws_infrastructure.vpc_id
}

output "vpc_cidr" {
  description = "CIDR block of the VPC"
  value       = module.aws_infrastructure.vpc_cidr
}

output "public_subnet_ids" {
  description = "IDs of the public subnets"
  value       = module.aws_infrastructure.public_subnet_ids
}

output "private_subnet_ids" {
  description = "IDs of the private subnets"
  value       = module.aws_infrastructure.private_subnet_ids
}

output "database_subnet_ids" {
  description = "IDs of the database subnets"
  value       = module.aws_infrastructure.database_subnet_ids
}

output "eks_cluster_id" {
  description = "EKS cluster ID"
  value       = module.aws_infrastructure.eks_cluster_id
}

output "eks_cluster_arn" {
  description = "EKS cluster ARN"
  value       = module.aws_infrastructure.eks_cluster_arn
}

output "eks_cluster_endpoint" {
  description = "EKS cluster API endpoint"
  value       = module.aws_infrastructure.eks_cluster_endpoint
}

output "eks_cluster_certificate_authority_data" {
  description = "Base64 encoded certificate data for EKS cluster"
  value       = module.aws_infrastructure.eks_cluster_certificate_authority_data
}

output "eks_cluster_oidc_issuer_url" {
  description = "EKS cluster OIDC issuer URL"
  value       = module.aws_infrastructure.eks_cluster_oidc_issuer_url
}

output "eks_cluster_security_group_id" {
  description = "Security group ID for EKS cluster"
  value       = module.aws_infrastructure.eks_cluster_security_group_id
}

output "eks_node_security_group_id" {
  description = "Security group ID for EKS node group"
  value       = module.aws_infrastructure.eks_node_security_group_id
}

output "eks_node_group_id" {
  description = "EKS node group ID"
  value       = module.aws_infrastructure.eks_node_group_id
}

output "eks_kms_key_id" {
  description = "KMS key ARN used for EKS cluster secrets encryption"
  value       = module.aws_infrastructure.eks_kms_key_id
}

output "eks_kms_key_alias" {
  description = "KMS key alias for EKS"
  value       = module.aws_infrastructure.eks_kms_key_alias
}

output "alb_dns_name" {
  description = "DNS name of the Application Load Balancer"
  value       = module.aws_infrastructure.alb_dns_name
}

output "alb_arn" {
  description = "ARN of the Application Load Balancer"
  value       = module.aws_infrastructure.alb_arn
}

output "alb_zone_id" {
  description = "Zone ID of the Application Load Balancer"
  value       = module.aws_infrastructure.alb_zone_id
}

output "alb_security_group_id" {
  description = "Security group ID for ALB"
  value       = module.aws_infrastructure.alb_security_group_id
}

output "route53_zone_id" {
  description = "Route53 hosted zone ID"
  value       = module.aws_infrastructure.route53_zone_id
}

output "route53_domain_name" {
  description = "Route53 domain name"
  value       = module.aws_infrastructure.route53_domain_name
}

output "route53_name_servers" {
  description = "Route53 hosted zone name servers (for domain registrar configuration)"
  value       = module.aws_infrastructure.route53_name_servers
}

output "acm_certificate_arn" {
  description = "ARN of the ACM certificate for the ALB"
  value       = module.aws_infrastructure.acm_certificate_arn
}

output "availability_zones" {
  description = "List of availability zones used"
  value       = module.aws_infrastructure.availability_zones
}
