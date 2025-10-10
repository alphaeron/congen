#!/bin/bash

# Keycloak Terraform Bootstrap Script
# This script sets up the necessary client credentials grant for Terraform to manage Keycloak

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default configuration
KEYCLOAK_URL=""
MASTER_REALM="master"
TERRAFORM_CLIENT_ID="terraform"
ADMIN_USERNAME=""
ADMIN_PASSWORD=""
ENVIRONMENT=""

# Function to display usage
usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Bootstrap Keycloak with Terraform client credentials grant.

OPTIONS:
    -u, --url URL           Keycloak server URL (REQUIRED)
    -e, --environment ENV   Environment name (REQUIRED: local, local-persist, staging, production)
    -r, --realm REALM       Master realm name (default: master)
    -c, --client-id ID      Terraform client ID (default: terraform)
    -a, --admin-user USER   Admin username (will prompt if not provided)
    -p, --admin-pass PASS   Admin password (will prompt if not provided)
    -h, --help              Show this help message

EXAMPLES:
    $0 -u http://localhost:8080 -e local                    # Basic usage, will prompt for credentials
    $0 --url https://keycloak.example.com --environment staging         # Use long option
    $0 -u http://localhost:8080 -e production -a admin -p mypass # Provide all credentials

EOF
}

# Parse command line arguments
while getopts "u:e:r:c:a:p:h-:" opt; do
    case ${opt} in
        u) KEYCLOAK_URL="${OPTARG}" ;;
        e) ENVIRONMENT="${OPTARG}" ;;
        r) MASTER_REALM="${OPTARG}" ;;
        c) TERRAFORM_CLIENT_ID="${OPTARG}" ;;
        a) ADMIN_USERNAME="${OPTARG}" ;;
        p) ADMIN_PASSWORD="${OPTARG}" ;;
        h) usage; exit 0 ;;
        -)
            case "${OPTARG}" in
                url) KEYCLOAK_URL="${!OPTIND}"; OPTIND=$((OPTIND + 1)) ;;
                environment) ENVIRONMENT="${!OPTIND}"; OPTIND=$((OPTIND + 1)) ;;
                realm) MASTER_REALM="${!OPTIND}"; OPTIND=$((OPTIND + 1)) ;;
                client-id) TERRAFORM_CLIENT_ID="${!OPTIND}"; OPTIND=$((OPTIND + 1)) ;;
                admin-user) ADMIN_USERNAME="${!OPTIND}"; OPTIND=$((OPTIND + 1)) ;;
                admin-pass) ADMIN_PASSWORD="${!OPTIND}"; OPTIND=$((OPTIND + 1)) ;;
                help) usage; exit 0 ;;
                *) echo "Unknown option --${OPTARG}" >&2; usage; exit 1 ;;
            esac ;;
        *) echo "Unknown option -${opt}" >&2; usage; exit 1 ;;
    esac
done

# Validate required arguments
if [[ -z "${KEYCLOAK_URL}" ]]; then
    print_error "Keycloak URL is required. Use -u or --url to specify it."
    usage
    exit 1
fi

if [[ -z "${ENVIRONMENT}" ]]; then
    print_error "Environment is required. Use -e or --environment to specify it (local, local-persist, staging, production)."
    usage
    exit 1
fi

# Validate environment value
if [[ "${ENVIRONMENT}" != "local" && "${ENVIRONMENT}" != "local-persist" && "${ENVIRONMENT}" != "staging" && "${ENVIRONMENT}" != "production" ]]; then
    echo -e "${RED}[ERROR]${NC} Invalid environment: ${ENVIRONMENT}. Must be one of: local, local-persist, staging, production"
    usage
    exit 1
fi

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

# Get admin credentials from terraform.tfvars if not provided
if [[ -z "${ADMIN_USERNAME}" ]]; then
    # Try to get from terraform.tfvars
    if [[ "${ENVIRONMENT}" == "local-persist" ]]; then
        tfvars_file="terraform/environments/local/terraform.tfvars"
    else
        tfvars_file="terraform/environments/${ENVIRONMENT}/terraform.tfvars"
    fi
    if [[ -f "${tfvars_file}" ]]; then
        # Check if admin_username is defined in tfvars
        tfvars_username=$(grep "^admin_username" "${tfvars_file}" | cut -d'=' -f2 | tr -d ' "' || true)
        if [[ -n "${tfvars_username}" ]]; then
            ADMIN_USERNAME="${tfvars_username}"
            print_status "Using admin username from terraform.tfvars: ${ADMIN_USERNAME}"
        else
            # Use default from variables.tf
            ADMIN_USERNAME="admin"
            print_status "Using default admin username: ${ADMIN_USERNAME}"
        fi
    else
        # Use default from variables.tf
        ADMIN_USERNAME="admin"
        print_status "Using default admin username: ${ADMIN_USERNAME}"
    fi
fi

if [[ -z "${ADMIN_PASSWORD}" ]]; then
    # Try to get from terraform.tfvars
    if [[ "${ENVIRONMENT}" == "local-persist" ]]; then
        tfvars_file="terraform/environments/local/terraform.tfvars"
    else
        tfvars_file="terraform/environments/${ENVIRONMENT}/terraform.tfvars"
    fi
    if [[ -f "${tfvars_file}" ]]; then
        # Check if admin_password is defined in tfvars
        tfvars_password=$(grep "^admin_password" "${tfvars_file}" | cut -d'=' -f2 | tr -d ' "' || true)
        if [[ -n "${tfvars_password}" ]]; then
            ADMIN_PASSWORD="${tfvars_password}"
            print_status "Using admin password from terraform.tfvars"
        else
            print_error "Admin password not found in terraform.tfvars and not provided via command line"
            print_error "Please set admin_password in ${tfvars_file} or provide via -p option"
            exit 1
        fi
    else
        print_error "terraform.tfvars file not found at ${tfvars_file}"
        print_error "Please provide admin password via -p option"
        exit 1
    fi
fi

# Function to get admin token
get_admin_token() {
    # Get token using direct curl command
    local token_response
    token_response=$(curl -s -X POST \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "username=${ADMIN_USERNAME}&password=${ADMIN_PASSWORD}&grant_type=password&client_id=admin-cli" \
        "${KEYCLOAK_URL}/realms/${MASTER_REALM}/protocol/openid-connect/token")
    
    local access_token
    access_token=$(echo "${token_response}" | jq -r '.access_token')
    
    # Validate token
    if [[ "${access_token}" == "null" || -z "${access_token}" ]]; then
        print_error "Failed to get admin token"
        echo "Response: ${token_response}"
        exit 1
    fi

    echo "${access_token}"
}

# Function to check if Terraform client exists
check_terraform_client_exists() {
    local admin_token="$1"
    
    print_status "Checking if Terraform client exists..."
    
    local check_response
    check_response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X GET \
        -H "Authorization: Bearer ${admin_token}" \
        -H "Content-Type: application/json" \
        "${KEYCLOAK_URL}/admin/realms/${MASTER_REALM}/clients")
    
    local http_status
    http_status=$(echo "${check_response}" | grep "HTTP_STATUS:" | cut -d: -f2 || true)
    local response_body
    response_body=$(echo "${check_response}" | grep -v "HTTP_STATUS:")
    
    if [[ "${http_status}" == "200" ]]; then
        local client_exists
        client_exists=$(echo "${response_body}" | jq -r ".[] | select(.clientId == \"${TERRAFORM_CLIENT_ID}\") | .clientId")
        if [[ "${client_exists}" == "${TERRAFORM_CLIENT_ID}" ]]; then
            print_success "Terraform client already exists"
            return 0
        else
            print_status "Terraform client does not exist"
            return 1
        fi
    else
        print_error "Failed to check Terraform client existence"
        echo "HTTP Status: ${http_status}"
        echo "Response: ${response_body}"
        return 1
    fi
}

# Function to create Terraform client
create_terraform_client() {
    local admin_token="$1"
    
    print_status "Creating Terraform client..."
    
    local client_payload
    client_payload=$(cat <<EOF
{
    "clientId": "${TERRAFORM_CLIENT_ID}",
    "name": "Terraform",
    "enabled": true,
    "protocol": "openid-connect",
    "publicClient": false,
    "standardFlowEnabled": false,
    "directAccessGrantsEnabled": false,
    "serviceAccountsEnabled": true,
    "clientAuthenticatorType": "client-secret",
    "fullScopeAllowed": true,
    "redirectUris": [],
    "webOrigins": []
}
EOF
)
    
    print_status "Client payload: ${client_payload}"
    
    local create_response
    create_response=$(curl -s -w "\nHTTP_STATUS:%{http_code}" -X POST \
        -H "Authorization: Bearer ${admin_token}" \
        -H "Content-Type: application/json" \
        -d "${client_payload}" \
        "${KEYCLOAK_URL}/admin/realms/${MASTER_REALM}/clients")
    
    local http_status
    http_status=$(echo "${create_response}" | grep "HTTP_STATUS:" | cut -d: -f2 || true)
    local response_body
    response_body=$(echo "${create_response}" | grep -v "HTTP_STATUS:")
    
    print_status "HTTP Status: ${http_status}"
    print_status "Response: ${response_body}"
    
    if [[ "${http_status}" == "201" ]]; then
        print_success "Terraform client created successfully"
    elif [[ "${http_status}" == "409" ]]; then
        print_success "Terraform client already exists"
    else
        print_error "Failed to create Terraform client"
        echo "HTTP Status: ${http_status}"
        echo "Response: ${response_body}"
        exit 1
    fi
}

# Function to get client ID
get_client_id() {
    local admin_token="$1"
    local client_id="$2"
    
    local clients_response
    clients_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${admin_token}" \
        "${KEYCLOAK_URL}/admin/realms/${MASTER_REALM}/clients")
    
    local client_uuid
    client_uuid=$(echo "${clients_response}" | jq -r ".[] | select(.clientId == \"${client_id}\") | .id")
    
    if [[ "${client_uuid}" == "null" || -z "${client_uuid}" ]]; then
        print_error "Failed to get client ID for ${client_id}"
        echo "Available clients:"
        echo "${clients_response}" | jq -r '.[].clientId'
        exit 1
    fi

    echo "${client_uuid}"
}

# Function to get client secret
get_client_secret() {
    local admin_token="$1"
    local client_uuid="$2"
    
    local secret_response
    secret_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${admin_token}" \
        "${KEYCLOAK_URL}/admin/realms/${MASTER_REALM}/clients/${client_uuid}/client-secret")
    
    local client_secret
    client_secret=$(echo "${secret_response}" | jq -r '.value')
    
    if [[ "${client_secret}" == "null" || -z "${client_secret}" ]]; then
        print_error "Failed to get client secret"
        echo "Response: ${secret_response}"
        exit 1
    fi

    echo "${client_secret}"
}

# Function to assign realm management roles
assign_realm_management_roles() {
    local admin_token="$1"
    local client_uuid="$2"
    
    print_status "Assigning realm management roles..."
    
    # Get service account user ID with retry
    local service_account_id=""
    local max_retries=5
    local retry_count=0
    
    while [[ -z "${service_account_id}" || "${service_account_id}" == "null" ]] && [[ ${retry_count} -lt ${max_retries} ]]; do
        local attempt_num=$((retry_count + 1))
        print_status "Getting service account user ID (attempt ${attempt_num}/${max_retries})..."
        
        # Get fresh token for each attempt to avoid expiration issues
        local fresh_token
        fresh_token=$(curl -s -X POST \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "username=${ADMIN_USERNAME}&password=${ADMIN_PASSWORD}&grant_type=password&client_id=admin-cli" \
            "${KEYCLOAK_URL}/realms/${MASTER_REALM}/protocol/openid-connect/token" | jq -r '.access_token' || true)
        
        local service_account_response
        service_account_response=$(curl -s -X GET \
            -H "Authorization: Bearer ${fresh_token}" \
            "${KEYCLOAK_URL}/admin/realms/${MASTER_REALM}/clients/${client_uuid}/service-account-user")
        
        print_status "Service account response: ${service_account_response}"
        
        service_account_id=$(echo "${service_account_response}" | jq -r '.id')
        
        print_status "Service account ID: ${service_account_id}"
        
        if [[ "${service_account_id}" == "null" || -z "${service_account_id}" ]]; then
            retry_count=$((retry_count + 1))
            if [[ ${retry_count} -lt ${max_retries} ]]; then
                print_status "Service account not ready yet, waiting 2 seconds before retry..."
                sleep 2
            fi
        fi
    done
    
    if [[ "${service_account_id}" == "null" || -z "${service_account_id}" ]]; then
        print_error "Failed to get service account user ID after ${max_retries} attempts"
        echo "Response: ${service_account_response}"
        exit 1
    fi
    
        # The Terraform client only needs admin and create-realm roles in the master realm
    # It doesn't need realm-management client roles since it will create the congen realm
    # and then have full access to manage resources within that realm
    print_status "Terraform client only needs admin and create-realm roles in master realm"
    print_status "No realm-management client roles needed for realm creation"
    
    # Also assign admin role to ensure full privileges
    print_status "Assigning admin role to service account..."
    local admin_role_id
    admin_role_id=$(curl -s -X GET \
        -H "Authorization: Bearer ${admin_token}" \
        "${KEYCLOAK_URL}/admin/realms/${MASTER_REALM}/roles" | jq -r '.[] | select(.name == "admin") | .id' || true)
    
    if [[ "${admin_role_id}" != "null" && -n "${admin_role_id}" ]]; then
        if curl -s -X POST \
            -H "Authorization: Bearer ${admin_token}" \
            -H "Content-Type: application/json" \
            -d "[{\"id\":\"${admin_role_id}\",\"name\":\"admin\"}]" \
            "${KEYCLOAK_URL}/admin/realms/${MASTER_REALM}/users/${service_account_id}/role-mappings/realm" > /dev/null; then
            print_success "Admin role assigned to service account"
        else
            print_warning "Failed to assign admin role (may already be assigned)"
        fi
    else
        print_warning "Admin role not found"
    fi
    
    # Assign create-realm role for full admin privileges
    print_status "Assigning create-realm role to service account..."
    local create_realm_role_id
    create_realm_role_id=$(curl -s -X GET \
        -H "Authorization: Bearer ${admin_token}" \
        "${KEYCLOAK_URL}/admin/realms/${MASTER_REALM}/roles" | jq -r '.[] | select(.name == "create-realm") | .id' || true)
    
    if [[ "${create_realm_role_id}" != "null" && -n "${create_realm_role_id}" ]]; then
        if curl -s -X POST \
            -H "Authorization: Bearer ${admin_token}" \
            -H "Content-Type: application/json" \
            -d "[{\"id\":\"${create_realm_role_id}\",\"name\":\"create-realm\"}]" \
            "${KEYCLOAK_URL}/admin/realms/${MASTER_REALM}/users/${service_account_id}/role-mappings/realm" > /dev/null; then
            print_success "Create-realm role assigned to service account"
        else
            print_warning "Failed to assign create-realm role (may already be assigned)"
        fi
    else
        print_warning "Create-realm role not found"
    fi
}

# Function to generate Terraform provider configuration
generate_terraform_config() {
    local client_secret="$1"
    
    print_status "Updating Terraform variables file..."
    
    if [[ "${ENVIRONMENT}" == "local-persist" ]]; then
        local tfvars_file="terraform/environments/local/terraform.tfvars"
    else
        local tfvars_file="terraform/environments/${ENVIRONMENT}/terraform.tfvars"
    fi
    
    # Check if keycloak_client_secret already exists in the file
    if grep -q "^keycloak_client_secret" "${tfvars_file}" 2>/dev/null; then
        # Replace existing value
        print_status "Replacing existing keycloak_client_secret value..."
        # Use awk instead of sed to avoid newline issues
        if awk -v secret="${client_secret}" '/^keycloak_client_secret = / {print "keycloak_client_secret = \"" secret "\""; next} {print}' "${tfvars_file}" > "${tfvars_file}.tmp" 2>/dev/null && mv "${tfvars_file}.tmp" "${tfvars_file}"; then
            print_status "Successfully updated client secret using awk"
        else
            # Fallback to simple replacement if awk fails
            print_status "Using fallback method to update client secret..."
            if grep -v "^keycloak_client_secret" "${tfvars_file}" > "${tfvars_file}.tmp" && echo "keycloak_client_secret = \"${client_secret}\"" >> "${tfvars_file}.tmp" && mv "${tfvars_file}.tmp" "${tfvars_file}"; then
                print_status "Successfully updated client secret using fallback method"
            else
                print_error "Failed to update client secret"
                exit 1
            fi
        fi
        print_success "Updated existing keycloak_client_secret in ${tfvars_file}"
    else
        # Append new line at the end of the file
        print_status "Adding keycloak_client_secret to ${tfvars_file}..."
        {
            echo ""
            echo "# Keycloak client secret for Terraform provider"
            echo "keycloak_client_secret = \"${client_secret}\""
        } >> "${tfvars_file}"
        print_success "Added keycloak_client_secret to ${tfvars_file}"
    fi
    
    print_warning "⚠️  IMPORTANT: The client secret is now in ${tfvars_file}"
    print_warning "⚠️  Make sure to add ${tfvars_file} to .gitignore to prevent committing secrets"
}

# Main execution
main() {
    print_status "Starting Keycloak Terraform bootstrap..."
    
    # Display configuration
    print_status "Configuration:"
    print_status "  Keycloak URL: ${KEYCLOAK_URL}"
    print_status "  Environment: ${ENVIRONMENT}"
    print_status "  Master Realm: ${MASTER_REALM}"
    print_status "  Terraform Client ID: ${TERRAFORM_CLIENT_ID}"
    print_status "  Admin Username: ${ADMIN_USERNAME}"
    echo ""
    
    # Check if Keycloak is accessible
    print_status "Checking Keycloak accessibility..."
    if ! curl -s "${KEYCLOAK_URL}/realms/${MASTER_REALM}/.well-known/openid_configuration" > /dev/null; then
        print_error "Keycloak is not accessible at ${KEYCLOAK_URL}"
        print_error "Make sure Keycloak is running and port forwarding is set up:"
        print_error "kubectl port-forward -n congen service/keycloak 8080:8080"
        exit 1
    fi
    
    print_success "Keycloak is accessible"
    
    # Get admin token
    local admin_token
    if ! admin_token=$(get_admin_token); then
        print_error "Failed to get admin token"
        exit 1
    fi
    
    # Check if Terraform client already exists
    local client_check_result
    client_check_result=0
    check_terraform_client_exists "${admin_token}"
    client_check_result=$?
    
    if [[ ${client_check_result} -eq 0 ]]; then
        print_status "Terraform client already exists, skipping creation"
    else
        # Create Terraform client
        create_terraform_client "${admin_token}"
    fi
    
    # Get client UUID
    local client_uuid
    print_status "About to get client ID for ${TERRAFORM_CLIENT_ID}..."
    if ! client_uuid=$(get_client_id "${admin_token}" "${TERRAFORM_CLIENT_ID}"); then
        print_error "Failed to get client UUID"
        exit 1
    fi
    
    print_status "Got client UUID: ${client_uuid}"
    
    # Get client secret
    local client_secret
    print_status "About to get client secret for UUID: ${client_uuid}..."
    if ! client_secret=$(get_client_secret "${admin_token}" "${client_uuid}"); then
        print_error "Failed to get client secret"
        exit 1
    fi
    
    # Assign realm management roles
    assign_realm_management_roles "${admin_token}" "${client_uuid}"
    
    # Generate Terraform configuration
    generate_terraform_config "${client_secret}"
    
    print_success "Keycloak Terraform bootstrap completed successfully!"
    print_status "Next steps:"
    print_status "1. Review the generated terraform.tfvars file"
    print_status "2. Ensure terraform.tfvars is in your .gitignore"
    if [[ "${ENVIRONMENT}" == "local-persist" ]]; then
        print_status "3. Run 'terraform init' in terraform/environments/local"
    else
        print_status "3. Run 'terraform init' in terraform/environments/${ENVIRONMENT}"
    fi
    print_status "4. Run 'terraform apply' to create your Keycloak resources"
}

# Run main function
main "$@" 