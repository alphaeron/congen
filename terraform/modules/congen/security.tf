resource "aws_security_group" "rds" {
  name        = "${local.name_prefix}-rds-sg"
  description = "Security group for Aurora RDS cluster"
  vpc_id      = var.vpc_id

  ingress {
    description     = "PostgreSQL from EKS nodes"
    from_port       = var.rds_port
    to_port         = var.rds_port
    protocol        = "tcp"
    security_groups = [var.eks_node_security_group_id]
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-rds-sg"
    }
  )
}

resource "aws_security_group" "elasticache" {
  name        = "${local.name_prefix}-elasticache-sg"
  description = "Security group for ElastiCache Memcached cluster"
  vpc_id      = var.vpc_id

  ingress {
    description     = "Memcached from EKS nodes"
    from_port       = var.elasticache_port
    to_port         = var.elasticache_port
    protocol        = "tcp"
    security_groups = [var.eks_node_security_group_id]
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-elasticache-sg"
    }
  )
}
