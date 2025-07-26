terraform {
  required_version = "= 1.12.2"
  required_providers {
    keycloak = {
      source  = "keycloak/keycloak"
      version = "= 5.3.0"
    }
  }
}

provider "keycloak" {
  client_id     = "admin-cli"
  username      = var.keycloak_admin_username
  password      = var.keycloak_admin_password
  url           = var.keycloak_url
  initial_login = true
}

module "keycloak" {
  source = "../../modules/keycloak"

  # Environment-specific overrides
  frontend_redirect_uris = var.frontend_redirect_uris
  frontend_web_origins   = var.frontend_web_origins

  # Pass through other variables
  realm_name                 = var.realm_name
  realm_display_name         = var.realm_display_name
  realm_display_name_html    = var.realm_display_name_html
  backend_client_id          = var.backend_client_id
  backend_client_name        = var.backend_client_name
  frontend_client_id         = var.frontend_client_id
  frontend_client_name       = var.frontend_client_name
  backend_service_username   = var.backend_service_username
  backend_service_email      = var.backend_service_email
  backend_service_first_name = var.backend_service_first_name
  backend_service_last_name  = var.backend_service_last_name
  backend_service_password   = var.backend_service_password
  admin_username             = var.admin_username
  admin_email                = var.admin_email
  admin_first_name           = var.admin_first_name
  admin_last_name            = var.admin_last_name
  admin_password             = var.admin_password
} 