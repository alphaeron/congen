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

output "backend_service_username" {
  description = "Backend service account username"
  value       = module.keycloak.backend_service_username
}

output "admin_username" {
  description = "Admin username"
  value       = module.keycloak.admin_username
} 