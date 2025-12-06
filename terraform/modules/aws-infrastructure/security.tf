resource "aws_security_group" "eks_cluster" {
  name        = "${local.name_prefix}-eks-cluster-sg"
  description = "Security group for EKS cluster control plane"
  vpc_id      = aws_vpc.main.id

  # EKS control plane requires outbound access to AWS services for cluster management
  # This includes EKS API, CloudWatch, and other AWS services
  # Note: Traffic goes through VPC endpoints where available (S3, CloudWatch Logs, ECR)
  egress {
    description = "Allow all outbound traffic from cluster (required for EKS control plane operations)"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-eks-cluster-sg"
    }
  )
}

resource "aws_security_group" "eks_node" {
  name        = "${local.name_prefix}-eks-node-sg"
  description = "Security group for EKS node group"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "HTTP from ALB to ingress controller"
    from_port       = var.ingress_controller_service_port
    to_port         = var.ingress_controller_service_port
    protocol        = "tcp"
    security_groups = [aws_security_group.alb.id]
  }

  ingress {
    description     = "All traffic from EKS cluster"
    from_port       = 0
    to_port         = 0
    protocol        = "-1"
    security_groups = [aws_security_group.eks_cluster.id]
  }

  ingress {
    description     = "All traffic from other EKS nodes"
    from_port       = 0
    to_port         = 0
    protocol        = "-1"
    self            = true
  }

  # EKS nodes require outbound access for:
  # - Pulling container images from ECR (via VPC endpoint)
  # - CloudWatch Logs (via VPC endpoint)
  # - S3 access (via VPC endpoint)
  # - Kubernetes API server communication
  # - Container registry authentication
  # Note: Most AWS service traffic uses VPC endpoints to stay within VPC
  egress {
    description = "Allow all outbound traffic (required for node operations, most traffic uses VPC endpoints)"
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-eks-node-sg"
    }
  )
}

resource "aws_security_group" "vpc_endpoint" {
  name        = "${local.name_prefix}-vpc-endpoint-sg"
  description = "Security group for VPC endpoints"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "HTTPS from EKS nodes"
    from_port       = 443
    to_port         = 443
    protocol        = "tcp"
    security_groups = [aws_security_group.eks_node.id]
  }

  egress {
    description     = "HTTPS responses to EKS nodes"
    from_port       = 443
    to_port         = 443
    protocol        = "tcp"
    security_groups = [aws_security_group.eks_node.id]
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-vpc-endpoint-sg"
    }
  )
}

resource "aws_network_acl" "main" {
  vpc_id = aws_vpc.main.id

  # Allow HTTPS from internet (for ALB ingress)
  ingress {
    rule_no    = 100
    protocol   = "tcp"
    action     = "allow"
    cidr_block = "0.0.0.0/0"
    from_port  = 443
    to_port    = 443
  }

  # Allow ephemeral ports for internal VPC connections
  ingress {
    rule_no    = 130
    protocol   = "tcp"
    action     = "allow"
    cidr_block = var.vpc_cidr
    from_port  = 32768
    to_port    = 60999
  }

  egress {
    rule_no    = 100
    protocol   = "tcp"
    action     = "allow"
    cidr_block = var.vpc_cidr
    from_port  = 443
    to_port    = 443
  }

  egress {
    rule_no    = 130
    protocol   = "tcp"
    action     = "allow"
    cidr_block = var.vpc_cidr
    from_port  = 32768
    to_port    = 60999
  }

  tags = merge(
    local.common_tags,
    {
      Name = "${local.name_prefix}-nacl"
    }
  )
}
