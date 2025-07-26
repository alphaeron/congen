output "realm_id" {
  description = "Keycloak realm ID"
  value       = keycloak_realm.congen.id
}

output "realm_name" {
  description = "Keycloak realm name"
  value       = keycloak_realm.congen.realm
}

output "backend_client_id" {
  description = "Backend client ID"
  value       = keycloak_openid_client.backend_client.client_id
}

output "backend_client_name" {
  description = "Backend client name"
  value       = keycloak_openid_client.backend_client.name
}

output "backend_service_username" {
  description = "Backend service account username"
  value       = keycloak_user.backend_service_account.username
}

output "frontend_client_id" {
  description = "Frontend client ID"
  value       = keycloak_openid_client.frontend_client.client_id
}

output "admin_username" {
  description = "Admin username"
  value       = keycloak_user.admin_user.username
} 