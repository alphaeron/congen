#!/bin/bash

# Update Kubernetes Secrets with Terraform Outputs
# This script extracts Terraform outputs and updates Kubernetes secrets

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default configuration
ENVIRONMENT=""
TERRAFORM_DIR=""

# Function to display usage
usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Update Kubernetes secrets with Terraform outputs.

OPTIONS:
    -e, --environment ENV   Environment name (REQUIRED: local, local-persist, staging, production)
    -t, --terraform-dir DIR Terraform directory (default: terraform/environments/{environment}/keycloak)
    -h, --help              Show this help message

EXAMPLES:
    $0 -e local                    # Update local environment secrets
    $0 --environment staging       # Update staging environment secrets
    $0 -e production -t /custom/path # Use custom terraform directory

EOF
}

# Parse command line arguments
while getopts "e:t:h-:" opt; do
    case ${opt} in
        e) ENVIRONMENT="${OPTARG}" ;;
        t) TERRAFORM_DIR="${OPTARG}" ;;
        h) usage; exit 0 ;;
        -)
            case "${OPTARG}" in
                environment) ENVIRONMENT="${!OPTIND}"; OPTIND=$((OPTIND + 1)) ;;
                terraform-dir) TERRAFORM_DIR="${!OPTIND}"; OPTIND=$((OPTIND + 1)) ;;
                help) usage; exit 0 ;;
                *) echo "Unknown option --${OPTARG}" >&2; usage; exit 1 ;;
            esac ;;
        *) echo "Unknown option -${opt}" >&2; usage; exit 1 ;;
    esac
done

# Set default terraform directory if not provided
if [[ -z "${TERRAFORM_DIR}" ]]; then
    if [[ "${ENVIRONMENT}" == "local-persist" ]]; then
        TERRAFORM_DIR="terraform/environments/local/keycloak"
    else
        TERRAFORM_DIR="terraform/environments/${ENVIRONMENT}/keycloak"
    fi
fi

# Validate required arguments
if [[ -z "${ENVIRONMENT}" ]]; then
    echo -e "${RED}[ERROR]${NC} Environment is required. Use -e or --environment to specify it (local, local-persist, staging, production)."
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

# Function to get Terraform output
get_terraform_output() {
    local output_name="$1"
    local terraform_dir="$2"

    cd "${terraform_dir}"
    local output_value
    output_value=$(terraform output -raw "${output_name}" 2>/dev/null || echo "")
    cd - > /dev/null

    if [[ -z "${output_value}" ]]; then
        exit 1
    fi

    echo "${output_value}"
}

# Function to update Kubernetes secret
update_k8s_secret() {
    local secret_name="$1"
    local key="$2"
    local value="$3"
    local namespace="$4"
    
    print_status "Updating Kubernetes secret: ${secret_name}.${key}"
    
    # Base64 encode the value
    local encoded_value
    encoded_value=$(echo -n "${value}" | base64)
    
    # Update the secret using kubectl patch
    if kubectl patch secret "${secret_name}" -n "${namespace}" --type='merge' -p="{\"data\":{\"${key}\":\"${encoded_value}\"}}" > /dev/null; then
        print_success "Updated ${secret_name}.${key}"
    else
        print_error "Failed to update ${secret_name}.${key}"
        exit 1
    fi
}

# Function to check if Kubernetes secret exists
secret_exists() {
    local secret_name="$1"
    local namespace="$2"
    
    kubectl get secret "${secret_name}" -n "${namespace}" > /dev/null 2>&1
}

# Function to verify Kubernetes secret exists
verify_secret_exists() {
    local secret_name="$1"
    local namespace="$2"
    
    local secret_check_result
    secret_check_result=0
    secret_exists "${secret_name}" "${namespace}"
    secret_check_result=$?
    
    if [[ ${secret_check_result} -ne 0 ]]; then
        print_error "Secret ${secret_name} does not exist in namespace ${namespace}"
        print_error "Ensure Stage 2 (Secrets) has been deployed before running this script"
        exit 1
    else
        print_status "Secret ${secret_name} exists"
    fi
}

# Main execution
main() {
    print_status "Starting Kubernetes secret update for environment: ${ENVIRONMENT}"
    
    # Display configuration
    print_status "Configuration:"
    print_status "  Environment: ${ENVIRONMENT}"
    print_status "  Terraform Directory: ${TERRAFORM_DIR}"
    echo ""
    
    # Check if terraform directory exists
    if [[ ! -d "${TERRAFORM_DIR}" ]]; then
        print_error "Terraform directory does not exist: ${TERRAFORM_DIR}"
        exit 1
    fi
    
    # Check if terraform has been initialized
    if [[ ! -f "${TERRAFORM_DIR}/terraform.tfstate" ]]; then
        print_error "Terraform has not been applied in ${TERRAFORM_DIR}"
        print_error "Run 'terraform init' and 'terraform apply' first"
        exit 1
    fi
    
    # Get Terraform outputs
    print_status "Getting Terraform outputs..."
    local backend_client_secret
    backend_client_secret=$(set -e; get_terraform_output "backend_client_secret" "${TERRAFORM_DIR}")
    
    local admin_username
    admin_username=$(set -e; get_terraform_output "admin_username" "${TERRAFORM_DIR}")
    
    local admin_password
    admin_password=$(set -e; get_terraform_output "admin_password" "${TERRAFORM_DIR}")
    
    # Get service account password from Terraform (if available)
    local service_account_password=""
    
    if cd "${TERRAFORM_DIR}" && terraform output -raw service_account_password 2>/dev/null; then
        service_account_password=$(set -e; get_terraform_output "service_account_password" "${TERRAFORM_DIR}")
    fi
    
    # Ensure Kubernetes namespace exists
    print_status "Ensuring namespace exists..."
    kubectl create namespace congen --dry-run=client -o yaml | kubectl apply -f - > /dev/null || true
    
    # Verify secrets exist
    verify_secret_exists "congen-secret" "congen"
    verify_secret_exists "keycloak-secret" "congen"
    
    # Update secrets with Terraform outputs
    print_status "Updating Kubernetes secrets..."
    update_k8s_secret "congen-secret" "KEYCLOAK_CLIENT_SECRET" "${backend_client_secret}" "congen"
    
    # Update keycloak-secret with backend client secret and admin credentials
    update_k8s_secret "keycloak-secret" "CONGEN_BACKEND_CLIENT_SECRET" "${backend_client_secret}" "congen"
    update_k8s_secret "keycloak-secret" "KC_BOOTSTRAP_ADMIN_USERNAME" "${admin_username}" "congen"
    update_k8s_secret "keycloak-secret" "KC_BOOTSTRAP_ADMIN_PASSWORD" "${admin_password}" "congen"
    
    # Update service account password if we have the value
    if [[ -n "${service_account_password}" ]]; then
        update_k8s_secret "congen-secret" "KEYCLOAK_SERVICE_ACCOUNT_PASSWORD" "${service_account_password}" "congen"
        update_k8s_secret "keycloak-secret" "CONGEN_SERVICE_ACCOUNT_PASSWORD" "${service_account_password}" "congen"
    fi
    
    print_success "Kubernetes secret update completed successfully!"
    print_status "Next steps:"
    print_status "1. Deploy stage 5 (Applications): kubectl apply -k k8s/overlays/${ENVIRONMENT}/stage-4-applications.yaml"
    print_status "2. Verify the deployment: kubectl get pods -n congen"
}

# Run main function
main "$@" 