resource "vault_kv_secret_v2" "database" {
  mount = "secret"
  name  = "${var.vault_secret_path_prefix}/${var.environment}/database"

  data_json = jsonencode({
    username        = var.rds_username
    password        = random_password.rds_master_password.result
    database_name   = aws_rds_cluster.main.database_name
    writer_endpoint = aws_rds_cluster.main.endpoint
    reader_endpoint = aws_rds_cluster.main.reader_endpoint
    port            = tostring(var.rds_port)
  })

  depends_on = [
    aws_rds_cluster.main,
    random_password.rds_master_password
  ]
}

resource "vault_kv_secret_v2" "cache" {
  mount = "secret"
  name  = "${var.vault_secret_path_prefix}/${var.environment}/cache"

  data_json = jsonencode({
    configuration_endpoint = aws_elasticache_cluster.main.configuration_endpoint
    port                   = tostring(var.elasticache_port)
    use_elasticache        = "true"
  })

  depends_on = [
    aws_elasticache_cluster.main
  ]
}
