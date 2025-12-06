resource "aws_elasticache_subnet_group" "main" {
  name       = "${local.name_prefix}-cache-subnet-group"
  subnet_ids = var.private_subnet_ids

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-cache-subnet-group"
    }
  )
}

resource "aws_elasticache_parameter_group" "main" {
  name   = "${local.name_prefix}-cache-parameter-group"
  family = "memcached1.6"

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-cache-parameter-group"
    }
  )
}

resource "aws_elasticache_cluster" "main" {
  cluster_id           = "${local.name_prefix}-cache"
  engine               = "memcached"
  engine_version       = var.elasticache_engine_version
  node_type            = var.elasticache_node_type
  num_cache_nodes      = var.elasticache_num_cache_nodes
  port                 = var.elasticache_port
  parameter_group_name = aws_elasticache_parameter_group.main.name
  subnet_group_name    = aws_elasticache_subnet_group.main.name
  security_group_ids   = [aws_security_group.elasticache.id]
  az_mode              = var.elasticache_num_cache_nodes > 1 ? "cross-az" : "single-az"
  preferred_availability_zones = var.elasticache_num_cache_nodes > 1 ? slice(var.availability_zones, 0, min(var.elasticache_num_cache_nodes, length(var.availability_zones))) : [var.availability_zones[0]]
  apply_immediately    = false
  auto_minor_version_upgrade = var.elasticache_auto_minor_version_upgrade
  maintenance_window   = var.elasticache_maintenance_window
  snapshot_retention_limit = var.elasticache_snapshot_retention_limit
  snapshot_window       = var.elasticache_snapshot_window

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-cache"
    }
  )
}
