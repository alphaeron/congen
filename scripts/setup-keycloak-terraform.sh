#!/bin/bash

# Keycloak Terraform Bootstrap Script
# This script sets up the necessary client credentials grant for Terraform to manage Keycloak

set -e

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
    -e, --environment ENV   Environment name (REQUIRED: local, staging, production)
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
    print_error "Environment is required. Use -e or --environment to specify it (local, staging, production)."
    usage
    exit 1
fi

# Validate environment value
if [[ "${ENVIRONMENT}" != "local" && "${ENVIRONMENT}" != "staging" && "${ENVIRONMENT}" != "production" ]]; then
    echo -e "${RED}[ERROR]${NC} Invalid environment: ${ENVIRONMENT}. Must be one of: local, staging, production"
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
    tfvars_file="terraform/environments/${ENVIRONMENT}/terraform.tfvars"
    if [[ -f "${tfvars_file}" ]]; then
        # Check if admin_username is defined in tfvars
        tfvars_username=$(grep "^admin_username" "${tfvars_file}" | cut -d'=' -f2 | tr -d ' "')
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
    tfvars_file="terraform/environments/${ENVIRONMENT}/terraform.tfvars"
    if [[ -f "${tfvars_file}" ]]; then
        # Check if admin_password is defined in tfvars
        tfvars_password=$(grep "^admin_password" "${tfvars_file}" | cut -d'=' -f2 | tr -d ' "')
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
    print_status "Getting admin token..."
    
    local token_response
    token_response=$(curl -s -X POST \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "username=${ADMIN_USERNAME}&password=${ADMIN_PASSWORD}&grant_type=password&client_id=admin-cli" \
        "${KEYCLOAK_URL}/realms/${MASTER_REALM}/protocol/openid-connect/token")
    
    local access_token
    access_token=$(echo "${token_response}" | jq -r '.access_token')
    
    if [[ "${access_token}" == "null" || -z "${access_token}" ]]; then
        print_error "Failed to get admin token"
        echo "Response: ${token_response}"
        exit 1
    fi
    
    print_success "Admin token obtained"
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
        "${KEYCLOAK_URL}/admin/realms/${MASTER_REALM}/clients?clientId=${TERRAFORM_CLIENT_ID}")
    
    local http_status
    http_status=$(echo "${check_response}" | grep "HTTP_STATUS:" | cut -d: -f2 || true)
    local response_body
    response_body=$(echo "${check_response}" | grep -v "HTTP_STATUS:")
    
    if [[ "${http_status}" == "200" ]]; then
        local client_count
        client_count=$(echo "${response_body}" | jq 'length')
        if [[ "${client_count}" -gt 0 ]]; then
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
        exit 1
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
    
    print_status "Getting client ID for ${client_id}..."
    
    local clients_response
    clients_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${admin_token}" \
        "${KEYCLOAK_URL}/admin/realms/${MASTER_REALM}/clients")
    
    print_status "Clients response: ${clients_response}"
    
    local client_uuid
    client_uuid=$(echo "${clients_response}" | jq -r ".[] | select(.clientId == \"${client_id}\") | .id")
    
    print_status "Client UUID: ${client_uuid}"
    
    if [[ "${client_uuid}" == "null" || -z "${client_uuid}" ]]; then
        print_error "Failed to get client ID for ${client_id}"
        echo "Available clients:"
        echo "${clients_response}" | jq -r '.[].clientId'
        exit 1
    fi
    
    print_success "Client ID obtained: ${client_uuid}"
    echo "${client_uuid}"
}

# Function to get client secret
get_client_secret() {
    local admin_token="$1"
    local client_uuid="$2"
    
    print_status "Getting client secret..."
    
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
    
    print_success "Client secret obtained"
    echo "${client_secret}"
}

# Function to assign realm management roles
assign_realm_management_roles() {
    local admin_token="$1"
    local client_uuid="$2"
    
    print_status "Assigning realm management roles..."
    
    # Get service account user ID
    local service_account_response
    service_account_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${admin_token}" \
        "${KEYCLOAK_URL}/admin/realms/${MASTER_REALM}/clients/${client_uuid}/service-account-user")
    
    local service_account_id
    service_account_id=$(echo "${service_account_response}" | jq -r '.id')
    
    if [[ "${service_account_id}" == "null" || -z "${service_account_id}" ]]; then
        print_error "Failed to get service account user ID"
        echo "Response: ${service_account_response}"
        exit 1
    fi
    
    # Get realm-management client ID
    local realm_management_id
    realm_management_id=$(set -e; get_client_id "${admin_token}" "realm-management")
    
    # Get available roles
    local roles_response
    roles_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${admin_token}" \
        "${KEYCLOAK_URL}/admin/realms/${MASTER_REALM}/clients/${realm_management_id}/roles")
    
    # Assign required roles
    local required_roles=("view-realm" "manage-users" "view-users" "view-clients" "manage-clients")
    
    for role in "${required_roles[@]}"; do
        print_status "Assigning role: ${role}"
        
        local role_id
        role_id=$(echo "${roles_response}" | jq -r ".[] | select(.name == \"${role}\") | .id")
        
        if [[ "${role_id}" != "null" && -n "${role_id}" ]]; then
            if curl -s -X POST \
                -H "Authorization: Bearer ${admin_token}" \
                -H "Content-Type: application/json" \
                -d "[{\"id\":\"${role_id}\",\"name\":\"${role}\"}]" \
                "${KEYCLOAK_URL}/admin/realms/${MASTER_REALM}/users/${service_account_id}/role-mappings/clients/${realm_management_id}" > /dev/null; then
                print_success "Role ${role} assigned"
            else
                print_warning "Failed to assign role ${role} (may already be assigned)"
            fi
        else
            print_warning "Role ${role} not found"
        fi
    done
}

# Function to generate Terraform provider configuration
generate_terraform_config() {
    local client_secret="$1"
    
    print_status "Updating Terraform variables file..."
    
    local tfvars_file="terraform/environments/${ENVIRONMENT}/terraform.tfvars"
    
    # Check if keycloak_client_secret already exists in the file
    if grep -q "^keycloak_client_secret" "${tfvars_file}" 2>/dev/null; then
        # Replace existing value
        print_status "Replacing existing keycloak_client_secret value..."
        sed -i.bak "s/^keycloak_client_secret = \".*\"/keycloak_client_secret = \"${client_secret}\"/" "${tfvars_file}"
        rm -f "${tfvars_file}.bak"
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
    admin_token=$(set -e; get_admin_token)
    
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
    client_uuid=$(set -e; get_client_id "${admin_token}" "${TERRAFORM_CLIENT_ID}")
    
    # Get client secret
    local client_secret
    client_secret=$(set -e; get_client_secret "${admin_token}" "${client_uuid}")
    
    # Assign realm management roles
    assign_realm_management_roles "${admin_token}" "${client_uuid}"
    
    # Generate Terraform configuration
    generate_terraform_config "${client_secret}"
    
    print_success "Keycloak Terraform bootstrap completed successfully!"
    print_status "Next steps:"
    print_status "1. Review the generated terraform.tfvars file"
    print_status "2. Ensure terraform.tfvars is in your .gitignore"
    print_status "3. Run 'terraform init' in terraform/environments/${ENVIRONMENT}"
    print_status "4. Run 'terraform apply' to create your Keycloak resources"
}

# Run main function
main "$@" 