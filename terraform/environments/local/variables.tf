variable "keycloak_url" {
  description = "Keycloak server URL"
  type        = string
  default     = "http://localhost:8081"
}

variable "keycloak_admin_username" {
  description = "Keycloak admin username"
  type        = string
  default     = "admin"
}

variable "keycloak_admin_password" {
  description = "Keycloak admin password"
  type        = string
  sensitive   = true
}

variable "realm_name" {
  description = "Keycloak realm name"
  type        = string
  default     = "congen"
}

variable "realm_display_name" {
  description = "Keycloak realm display name"
  type        = string
  default     = "Congen"
}

variable "realm_display_name_html" {
  description = "Keycloak realm display name HTML"
  type        = string
  default     = "<div class=\"kc-logo-text\"><span>Congen</span></div>"
}

variable "backend_client_id" {
  description = "Backend client ID"
  type        = string
  default     = "congen-backend"
}

variable "backend_client_name" {
  description = "Backend client name"
  type        = string
  default     = "Congen Backend"
}

variable "frontend_client_id" {
  description = "Frontend client ID"
  type        = string
  default     = "congen-frontend"
}

variable "frontend_client_name" {
  description = "Frontend client name"
  type        = string
  default     = "Congen Frontend"
}

variable "frontend_redirect_uris" {
  description = "Frontend client redirect URIs"
  type        = list(string)
  default = [
    "http://localhost:3000/*",
    "http://localhost:3000"
  ]
}

variable "frontend_web_origins" {
  description = "Frontend client web origins"
  type        = list(string)
  default = [
    "http://localhost:3000"
  ]
}

variable "backend_service_username" {
  description = "Backend service account username"
  type        = string
  default     = "congen-backend-service"
}

variable "backend_service_email" {
  description = "Backend service account email"
  type        = string
  default     = "backend-service@congen.com"
}

variable "backend_service_first_name" {
  description = "Backend service account first name"
  type        = string
  default     = "Congen"
}

variable "backend_service_last_name" {
  description = "Backend service account last name"
  type        = string
  default     = "Backend Service"
}

variable "backend_service_password" {
  description = "Backend service account password"
  type        = string
  sensitive   = true
}

variable "admin_username" {
  description = "Admin username"
  type        = string
  default     = "admin"
}

variable "admin_email" {
  description = "Admin email"
  type        = string
  default     = "admin@congen.com"
}

variable "admin_first_name" {
  description = "Admin first name"
  type        = string
  default     = "Admin"
}

variable "admin_last_name" {
  description = "Admin last name"
  type        = string
  default     = "User"
}

variable "admin_password" {
  description = "Admin password"
  type        = string
  sensitive   = true
} 