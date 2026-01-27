locals {
  name_prefix = "${var.project_name}-${var.environment}"

  common_tags = {
    Environment = var.environment
    Project     = var.project_name
    ManagedBy   = "Terraform"
  }

  az_count = length(var.availability_zones)
}

data "aws_caller_identity" "current" {}
