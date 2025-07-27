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
  client_id     = "terraform"
  client_secret = var.keycloak_client_secret
  url           = var.keycloak_url
}

module "keycloak" {
  source = "../../modules/keycloak"

  # Pass through all variables
  realm_name              = var.realm_name
  realm_display_name      = var.realm_display_name
  realm_display_name_html = var.realm_display_name_html
  backend_client_id       = var.backend_client_id
  backend_client_name     = var.backend_client_name
  frontend_client_id      = var.frontend_client_id
  frontend_client_name    = var.frontend_client_name
  frontend_redirect_uris  = var.frontend_redirect_uris
  frontend_web_origins    = var.frontend_web_origins
  admin_username          = var.admin_username
  admin_email             = var.admin_email
  admin_first_name        = var.admin_first_name
  admin_last_name         = var.admin_last_name
  admin_password          = var.admin_password
} 