terraform {
  required_version = "= 1.12.2"
  required_providers {
    keycloak = {
      source  = "keycloak/keycloak"
      version = "= 5.3.0"
    }
  }
}

# Create the Congen realm
resource "keycloak_realm" "congen" {
  realm                = var.realm_name
  enabled              = true
  display_name         = var.realm_display_name
  display_name_html    = var.realm_display_name_html
  registration_allowed = false
}

# Create roles
resource "keycloak_role" "user_role" {
  realm_id    = keycloak_realm.congen.id
  name        = "user"
  description = "Regular user role"
}

resource "keycloak_role" "admin_role" {
  realm_id    = keycloak_realm.congen.id
  name        = "admin"
  description = "Administrator role"
}

resource "keycloak_role" "service_role" {
  realm_id    = keycloak_realm.congen.id
  name        = "service"
  description = "Service account role"
}

# Create backend service account client
resource "keycloak_openid_client" "backend_client" {
  realm_id                     = keycloak_realm.congen.id
  client_id                    = var.backend_client_id
  name                         = var.backend_client_name
  enabled                      = true
  access_type                  = "CONFIDENTIAL"
  service_accounts_enabled     = true
  standard_flow_enabled        = true
  direct_access_grants_enabled = true
  valid_redirect_uris          = ["http://localhost:8080/"]
  web_origins                  = ["*"]
}

# Assign backend service account roles for user account creation
# These roles allow the backend service account to manage users in the congen realm
resource "keycloak_user_roles" "backend_client_realm_management_roles" {
  realm_id = keycloak_realm.congen.id
  user_id  = keycloak_openid_client.backend_client.service_account_user_id

  role_ids = [
    # These are the realm-management client roles that provide user management permissions
    data.keycloak_role.realm_management_manage_users.id,
    data.keycloak_role.realm_management_view_users.id
  ]
}

# Get the realm-management client
data "keycloak_openid_client" "realm_management" {
  realm_id  = keycloak_realm.congen.id
  client_id = "realm-management"
}

# Get the manage-users role from the realm-management client
data "keycloak_role" "realm_management_manage_users" {
  realm_id  = keycloak_realm.congen.id
  client_id = data.keycloak_openid_client.realm_management.id
  name      = "manage-users"
}

# Get the view-users role from the realm-management client
data "keycloak_role" "realm_management_view_users" {
  realm_id  = keycloak_realm.congen.id
  client_id = data.keycloak_openid_client.realm_management.id
  name      = "view-users"
}

# Create frontend client
resource "keycloak_openid_client" "frontend_client" {
  realm_id                     = keycloak_realm.congen.id
  client_id                    = var.frontend_client_id
  name                         = var.frontend_client_name
  enabled                      = true
  access_type                  = "PUBLIC"
  standard_flow_enabled        = true
  direct_access_grants_enabled = false
  service_accounts_enabled     = false
  valid_redirect_uris          = var.frontend_redirect_uris
  web_origins                  = var.frontend_web_origins
}

# Assign service role to backend service account
resource "keycloak_user_roles" "backend_service_roles" {
  realm_id = keycloak_realm.congen.id
  user_id  = keycloak_openid_client.backend_client.service_account_user_id

  role_ids = [
    keycloak_role.service_role.id
  ]
}

# Create default admin user
resource "keycloak_user" "admin_user" {
  realm_id = keycloak_realm.congen.id
  username = var.admin_username
  enabled  = true

  email      = var.admin_email
  first_name = var.admin_first_name
  last_name  = var.admin_last_name

  initial_password {
    value     = var.admin_password
    temporary = false
  }
}

# Assign admin role to admin user
resource "keycloak_user_roles" "admin_user_roles" {
  realm_id = keycloak_realm.congen.id
  user_id  = keycloak_user.admin_user.id

  role_ids = [
    keycloak_role.admin_role.id,
    keycloak_role.user_role.id
  ]
} 