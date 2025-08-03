variable "keycloak_url" {
  description = "Keycloak server URL"
  type        = string
  default     = "http://localhost:8080"
}

variable "keycloak_client_secret" {
  description = "Keycloak client secret for Terraform provider"
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
    "http://localhost:3000",
    "http://localhost:3000/profile/create",
    "http://localhost:3000/auth/callback"
  ]
}

variable "frontend_web_origins" {
  description = "Frontend client web origins"
  type        = list(string)
  default = [
    "http://localhost:3000"
  ]
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