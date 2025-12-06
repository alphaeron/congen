# Create the Congen realm
resource "keycloak_realm" "congen" {
  realm                          = var.realm_name
  enabled                        = true
  display_name                   = var.realm_display_name
  display_name_html              = var.realm_display_name_html
  registration_allowed           = true
  registration_email_as_username = true
  edit_username_allowed          = false
  reset_password_allowed         = true
  remember_me                    = true
  login_with_email_allowed       = true
  duplicate_emails_allowed       = false
  login_theme                    = "congen"
  account_theme                  = "congen-account-theme"

  # Security: Configure low token lifetimes
  access_token_lifespan                   = "15m" # 15 minutes
  access_token_lifespan_for_implicit_flow = "15m"
  sso_session_idle_timeout                = "30m" # 30 minutes
  sso_session_max_lifespan                = "10h" # 10 hours
  refresh_token_max_reuse                 = 3     # Allow refresh token reuse for silent refresh
  revoke_refresh_token                    = true  # Don't immediately revoke refresh tokens
}

# Create roles
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
  valid_redirect_uris          = var.backend_redirect_uris
  web_origins                  = var.backend_web_origins
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
  implicit_flow_enabled        = false
  direct_access_grants_enabled = false
  service_accounts_enabled     = false
  valid_redirect_uris          = var.frontend_redirect_uris
  web_origins                  = var.frontend_web_origins
  login_theme                  = "congen"
}

# Create audience protocol mapper for frontend client
resource "keycloak_openid_audience_protocol_mapper" "frontend_to_backend_audience_mapper" {
  realm_id  = keycloak_realm.congen.id
  client_id = keycloak_openid_client.frontend_client.id
  name      = "frontend-to-backend-audience-mapper"

  included_custom_audience = var.backend_client_id
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
    keycloak_role.admin_role.id
  ]
}

# Get the account client (built-in Keycloak client)
data "keycloak_openid_client" "account" {
  realm_id  = keycloak_realm.congen.id
  client_id = "account"
}

# Get the manage-account role from the account client
data "keycloak_role" "account_manage_account" {
  realm_id  = keycloak_realm.congen.id
  client_id = data.keycloak_openid_client.account.id
  name      = "manage-account"
}

# Get the view-profile role from the account client
data "keycloak_role" "account_view_profile" {
  realm_id  = keycloak_realm.congen.id
  client_id = data.keycloak_openid_client.account.id
  name      = "view-profile"
}

# Assign account console roles to admin user
resource "keycloak_user_roles" "admin_account_roles" {
  realm_id = keycloak_realm.congen.id
  user_id  = keycloak_user.admin_user.id

  role_ids = [
    data.keycloak_role.account_manage_account.id,
    data.keycloak_role.account_view_profile.id
  ]
} 