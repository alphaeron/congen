# Helm Release for Vault
resource "helm_release" "vault" {
  name       = "vault"
  repository = "https://helm.releases.hashicorp.com"
  chart      = "vault"
  version    = var.vault_helm_chart_version
  namespace  = kubernetes_namespace.vault.metadata[0].name

  values = [
    yamlencode({
      global = {
        tlsDisable = false
      }

      server = {
        service = {
          type = "ClusterIP"
        }

        image = {
          repository = "hashicorp/vault"
          tag        = var.vault_image_tag
        }

        ha = {
          enabled  = var.vault_replicas > 1
          replicas = var.vault_replicas
          config   = <<-EOT
            ui = true
            
            listener "tcp" {
              address = "[::]:8200"
              cluster_address = "[::]:8201"
              tls_cert_file = "/vault/tls/tls.crt"
              tls_key_file = "/vault/tls/tls.key"
            }
            
            storage "s3" {
              bucket = "${aws_s3_bucket.vault_storage.id}"
              region = "${var.aws_region}"
            }
            
            seal "awskms" {
              region = "${var.aws_region}"
              kms_key_id = "${aws_kms_key.vault.key_id}"
            }
            
            cluster_addr = "https://[::]:8201"
            api_addr = "https://[::]:8200"
          EOT
        }

        dataStorage = {
          enabled      = true
          size         = var.vault_storage_size
          storageClass = ""
        }

        serviceAccount = {
          create = false
          name   = kubernetes_service_account.vault.metadata[0].name
        }

        affinity = {
          podAntiAffinity = {
            requiredDuringSchedulingIgnoredDuringExecution = [
              {
                labelSelector = {
                  matchLabels = {
                    app = "vault"
                  }
                }
                topologyKey = "kubernetes.io/hostname"
              }
            ]
          }
        }

        securityContext = {
          runAsNonRoot = true
          runAsUser    = 100
          fsGroup      = 1000
        }

        extraEnvironmentVars = {
          AWS_REGION = var.aws_region
          VAULT_ADDR = "https://[::]:8200"
        }

        resources = {
          requests = {
            cpu    = var.vault_cpu_request
            memory = var.vault_memory_request
          }
          limits = {
            cpu    = var.vault_cpu_limit
            memory = var.vault_memory_limit
          }
        }
      }

      injector = {
        enabled = true
        agentImage = {
          repository = "hashicorp/vault-k8s"
          tag        = var.vault_injector_image_tag
        }
      }
    })
  ]

  depends_on = [
    kubernetes_service_account.vault,
    aws_s3_bucket.vault_storage,
    aws_kms_key.vault,
    var.eks_node_group_id,
  ]

  timeout = var.vault_helm_timeout
}

# Wait for Vault to be ready
resource "null_resource" "vault_ready" {
  provisioner "local-exec" {
    command = <<-EOT
      echo "Waiting for Vault pods to be ready..."
      kubectl wait --for=condition=ready pod \
        -l app.kubernetes.io/name=vault \
        -n ${var.vault_namespace} \
        --timeout=${var.vault_helm_timeout}s || true
    EOT
  }

  depends_on = [helm_release.vault]
}
