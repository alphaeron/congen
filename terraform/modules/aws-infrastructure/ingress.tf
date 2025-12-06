resource "kubernetes_namespace" "ingress_nginx" {
  metadata {
    name = var.ingress_controller_namespace
    labels = merge(
      local.common_tags,
      {
        Name = var.ingress_controller_namespace
      }
    )
  }

  depends_on = [aws_eks_node_group.main]
}

resource "helm_release" "ingress_nginx" {
  name       = "ingress-nginx"
  repository = "https://kubernetes.github.io/ingress-nginx"
  chart      = "ingress-nginx"
  version    = var.ingress_controller_helm_chart_version
  namespace  = kubernetes_namespace.ingress_nginx.metadata[0].name

  values = [
    yamlencode({
      controller = {
        ingressClassResource = {
          enabled = true
          name    = "nginx"
          default = false
        }
        service = {
          type = "ClusterIP"
        }
        replicaCount = var.ingress_controller_replica_count
        resources = {
          requests = {
            cpu    = var.ingress_controller_cpu_request
            memory = var.ingress_controller_memory_request
          }
          limits = {
            cpu    = var.ingress_controller_cpu_limit
            memory = var.ingress_controller_memory_limit
          }
        }
        podDisruptionBudget = {
          enabled     = var.ingress_controller_enable_pdb
          minAvailable = var.ingress_controller_enable_pdb ? var.ingress_controller_pdb_min_available : null
        }
        affinity = var.ingress_controller_enable_pod_anti_affinity ? {
          podAntiAffinity = {
            preferredDuringSchedulingIgnoredDuringExecution = [
              {
                weight = 100
                podAffinityTerm = {
                  labelSelector = {
                    matchExpressions = [
                      {
                        key      = "app.kubernetes.io/name"
                        operator = "In"
                        values   = ["ingress-nginx"]
                      }
                    ]
                  }
                  topologyKey = "kubernetes.io/hostname"
                }
              }
            ]
          }
        } : null
      }
    })
  ]

  depends_on = [
    kubernetes_namespace.ingress_nginx,
    aws_eks_node_group.main,
  ]

  timeout = 600
}

data "kubernetes_endpoints" "ingress_nginx" {
  metadata {
    name      = var.ingress_controller_service_name
    namespace = var.ingress_controller_namespace
  }

  depends_on = [helm_release.ingress_nginx]
}

locals {
  ingress_controller_ips = try(
    flatten([
      for subset in data.kubernetes_endpoints.ingress_nginx.subset : [
        for address in subset.address : address.ip
      ]
    ]),
    []
  )
}

resource "aws_lb_target_group_attachment" "ingress_controller" {
  count            = length(local.ingress_controller_ips)
  target_group_arn = aws_lb_target_group.ingress_controller.arn
  target_id        = local.ingress_controller_ips[count.index]
  port             = var.ingress_controller_service_port

  depends_on = [
    data.kubernetes_endpoints.ingress_nginx,
    aws_lb_target_group.ingress_controller,
  ]
}
