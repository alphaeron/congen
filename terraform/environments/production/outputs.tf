output "realm_id" {
  description = "Keycloak realm ID"
  value       = module.keycloak.realm_id
}

output "realm_name" {
  description = "Keycloak realm name"
  value       = module.keycloak.realm_name
}

output "backend_client_id" {
  description = "Backend client ID"
  value       = module.keycloak.backend_client_id
}

output "frontend_client_id" {
  description = "Frontend client ID"
  value       = module.keycloak.frontend_client_id
}

output "admin_username" {
  description = "Admin username"
  value       = module.keycloak.admin_username
}

output "backend_client_secret" {
  description = "Backend client secret"
  value       = module.keycloak.backend_client_secret
  sensitive   = true
} 