# Kubernetes Namespace for Vault
resource "kubernetes_namespace" "vault" {
  metadata {
    name = var.vault_namespace
    labels = merge(
      local.common_tags,
      {
        Name = var.vault_namespace
      }
    )
  }

  depends_on = [var.eks_node_group_id]
}

# Kubernetes Service Account for Vault with IRSA annotation
resource "kubernetes_service_account" "vault" {
  metadata {
    name      = "vault"
    namespace = kubernetes_namespace.vault.metadata[0].name
    annotations = {
      "eks.amazonaws.com/role-arn" = aws_iam_role.vault.arn
    }
    labels = merge(
      local.common_tags,
      {
        Name = "vault"
      }
    )
  }

  depends_on = [aws_iam_openid_connect_provider.eks]
}

# Kubernetes NetworkPolicy for Vault
resource "kubernetes_network_policy" "vault" {
  count = var.enable_vault_network_policy ? 1 : 0

  metadata {
    name      = "vault-network-policy"
    namespace = kubernetes_namespace.vault.metadata[0].name
    labels = merge(
      local.common_tags,
      {
        Name = "vault-network-policy"
      }
    )
  }

  spec {
    pod_selector {
      match_labels = {
        app = "vault"
      }
    }

    policy_types = ["Ingress", "Egress"]

    ingress {
      from {
        namespace_selector {
          match_expressions {
            key      = "kubernetes.io/metadata.name"
            operator = "In"
            values   = [var.vault_namespace]
          }
        }
      }
      ports {
        port     = "8200"
        protocol = "TCP"
      }
    }

    dynamic "ingress" {
      for_each = var.vault_allowed_namespaces
      content {
        from {
          namespace_selector {
            match_expressions {
              key      = "kubernetes.io/metadata.name"
              operator = "In"
              values   = [ingress.value]
            }
          }
        }
        ports {
          port     = "8200"
          protocol = "TCP"
        }
      }
    }

    egress {
      to {
        namespace_selector {
          match_expressions {
            key      = "kubernetes.io/metadata.name"
            operator = "In"
            values   = [var.vault_namespace]
          }
        }
      }
      ports {
        port     = "8201"
        protocol = "TCP"
      }
    }

    egress {
      to {
        pod_selector {
          match_labels = {
            app = "vault"
          }
        }
      }
      ports {
        port     = "8201"
        protocol = "TCP"
      }
    }

    egress {
      to {}
      ports {
        port     = "443"
        protocol = "TCP"
      }
    }
  }

  depends_on = [kubernetes_namespace.vault]
}

# Pod Disruption Budget for Vault (HA deployments)
resource "kubernetes_pod_disruption_budget" "vault" {
  count = var.vault_replicas > 1 ? 1 : 0

  metadata {
    name      = "vault-pdb"
    namespace = kubernetes_namespace.vault.metadata[0].name
    labels = merge(
      local.common_tags,
      {
        Name = "vault-pdb"
      }
    )
  }

  spec {
    min_available = var.vault_pdb_min_available

    selector {
      match_labels = {
        app = "vault"
      }
    }
  }

  depends_on = [helm_release.vault]
}

# Kubernetes Service Account for Terraform (used for Vault authentication)
resource "kubernetes_service_account" "terraform" {
  metadata {
    name      = var.vault_kubernetes_service_account
    namespace = var.vault_kubernetes_namespace
    labels = merge(
      local.common_tags,
      {
        Name = var.vault_kubernetes_service_account
      }
    )
  }
}
