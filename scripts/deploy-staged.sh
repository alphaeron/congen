#!/bin/bash

# Staged Deployment Script
# This script handles the complete staged deployment process:
# 1. Deploy Keycloak infrastructure
# 2. Bootstrap Keycloak with Terraform
# 3. Update Kubernetes secrets with Terraform outputs
# 4. Deploy Congen application components

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Default configuration
ENVIRONMENT=""
KEYCLOAK_URL=""
STAGE=""

# Function to display usage
usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Perform staged deployment of Congen application.

OPTIONS:
    -e, --environment ENV   Environment name (REQUIRED: local, staging, production)
    -u, --keycloak-url URL  Keycloak URL for bootstrap (default: http://localhost:8080)
    --stage STAGE           Deploy specific stage only (1, 2, 3, 4, 5, 6)
    -h, --help              Show this help message

EXAMPLES:
    $0 -e local                    # Full deployment for local environment
    $0 -e staging -u https://keycloak.staging.example.com  # Staging deployment
    $0 -e local --stage 1          # Deploy only Stage 1 (Infrastructure)
    $0 -e local --stage 2          # Deploy only Stage 2 (Secrets)
    $0 -e local --stage 4          # Deploy only Stage 4 (Terraform and Secrets Update)

EOF
}

# Parse command line arguments
while getopts "e:u:h-:" opt; do
    case ${opt} in
        e) ENVIRONMENT="${OPTARG}" ;;
        u) KEYCLOAK_URL="${OPTARG}" ;;
        h) usage; exit 0 ;;
        -)
            case "${OPTARG}" in
                environment) ENVIRONMENT="${!OPTIND}"; OPTIND=$((OPTIND + 1)) ;;
                keycloak-url) KEYCLOAK_URL="${!OPTIND}"; OPTIND=$((OPTIND + 1)) ;;
                stage) STAGE="${!OPTIND}"; OPTIND=$((OPTIND + 1)) ;;
                help) usage; exit 0 ;;
                *) echo "Unknown option --${OPTARG}" >&2; usage; exit 1 ;;
            esac ;;
        *) echo "Unknown option -${opt}" >&2; usage; exit 1 ;;
    esac
done

# Set default Keycloak URL if not provided
if [[ -z "${KEYCLOAK_URL}" ]]; then
    case "${ENVIRONMENT}" in
        local) KEYCLOAK_URL="http://localhost:8080" ;;
        staging) KEYCLOAK_URL="https://keycloak.staging.congen.com" ;;
        production) KEYCLOAK_URL="https://keycloak.congen.com" ;;
        *) echo "Unknown environment: ${ENVIRONMENT}" >&2; exit 1 ;;
    esac
fi

# Validate required arguments
if [[ -z "${ENVIRONMENT}" ]]; then
    echo -e "${RED}[ERROR]${NC} Environment is required. Use -e or --environment to specify it (local, staging, production)."
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

print_step() {
    echo ""
    echo -e "${BLUE}=== STEP $1: $2 ===${NC}"
    echo ""
}

# Function to check if kubectl is available
check_kubectl() {
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is not installed or not in PATH"
        exit 1
    fi
    print_success "kubectl is available"
}

# Function to check if terraform is available
check_terraform() {
    if ! command -v terraform &> /dev/null; then
        print_error "terraform is not installed or not in PATH"
        exit 1
    fi
    print_success "terraform is available"
}

# Function to deploy infrastructure
deploy_infrastructure() {
    print_step "1" "Deploying Infrastructure"
    
    print_status "Deploying infrastructure components..."
    if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-1"; then
        print_success "Infrastructure deployed"
    else
        print_error "Failed to deploy infrastructure"
        exit 1
    fi
    
    # Wait for PostgreSQL if it exists in this environment
    if kubectl get deployment postgres -n congen 2>/dev/null; then
        print_status "Waiting for PostgreSQL to be ready..."
        if kubectl wait --for=condition=ready pod -l app=postgres -n congen --timeout=300s; then
            print_success "PostgreSQL is ready"
        else
            print_error "PostgreSQL failed to become ready"
            exit 1
        fi
    fi
}

# Function to deploy secrets
deploy_secrets() {
    print_step "2" "Deploying Secrets"
    
    print_status "Deploying secrets to Kubernetes..."
    if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-2"; then
        print_success "Secrets deployed"
    else
        print_error "Failed to deploy secrets"
        exit 1
    fi
}

# Function to deploy Keycloak infrastructure
deploy_keycloak_infrastructure() {
    print_step "3" "Deploying Keycloak Infrastructure"
    
    print_status "Deploying Keycloak to Kubernetes..."
    if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-3"; then
        print_success "Keycloak infrastructure deployed"
    else
        print_error "Failed to deploy Keycloak infrastructure"
        exit 1
    fi
    
    print_status "Waiting for Keycloak to be ready..."
    if kubectl wait --for=condition=ready pod -l app=keycloak -n congen --timeout=300s; then
        print_success "Keycloak is ready"
    else
        print_error "Keycloak failed to become ready"
        exit 1
    fi
}

# Function to bootstrap Keycloak
bootstrap_keycloak() {
    print_status "Setting up port forwarding for Keycloak..."
    # Start port forwarding in background
    kubectl port-forward -n congen service/keycloak 8080:8080 &
    local port_forward_pid=$!
    
    # Wait a moment for port forwarding to establish
    sleep 5
    
    print_status "Bootstrapping Keycloak (will skip if Terraform client already exists)..."
    if ./scripts/setup-keycloak-terraform.sh -u "${KEYCLOAK_URL}" -e "${ENVIRONMENT}"; then
        print_success "Keycloak bootstrap completed"
    else
        print_error "Keycloak bootstrap failed"
        kill "${port_forward_pid}" 2>/dev/null || true
        exit 1
    fi
    
    # Stop port forwarding
    kill "${port_forward_pid}" 2>/dev/null || true
}

# Function to apply Terraform
apply_terraform() {
    print_step "4" "Bootstrapping Keycloak and Applying Terraform Configuration"
    
    # Bootstrap Keycloak first
    print_status "Bootstrapping Keycloak for Terraform..."
    bootstrap_keycloak
    
    local terraform_dir="terraform/environments/${ENVIRONMENT}"
    
    print_status "Initializing Terraform..."
    cd "${terraform_dir}"
    if terraform init; then
        print_success "Terraform initialized"
    else
        print_error "Terraform initialization failed"
        cd - > /dev/null
        exit 1
    fi
    
    print_status "Checking for Terraform changes..."
    local plan_output
    plan_output=$(terraform plan -detailed-exitcode 2>&1)
    local plan_exit_code=$?
    
    if [[ ${plan_exit_code} -eq 0 ]]; then
        print_success "No Terraform changes detected - infrastructure is up to date"
        cd - > /dev/null
        # Set a flag to indicate no Terraform changes were applied
        export TERRAFORM_NO_CHANGES=true
        return
    elif [[ ${plan_exit_code} -eq 1 ]]; then
        print_error "Terraform plan failed"
        echo "Plan output: ${plan_output}"
        cd - > /dev/null
        exit 1
    elif [[ ${plan_exit_code} -eq 2 ]]; then
        print_status "Terraform changes detected, applying configuration..."
        if terraform apply -auto-approve; then
            print_success "Terraform applied successfully"
            # Set a flag to indicate Terraform changes were applied
            export TERRAFORM_NO_CHANGES=false
        else
            print_error "Terraform apply failed"
            cd - > /dev/null
            exit 1
        fi
    else
        print_error "Unexpected Terraform plan exit code: ${plan_exit_code}"
        echo "Plan output: ${plan_output}"
        cd - > /dev/null
        exit 1
    fi
    
    cd - > /dev/null
}

# Function to update Kubernetes secrets
update_secrets() {
    print_step "5" "Updating Kubernetes Secrets"
    
    # Check if Terraform was up to date (no changes applied)
    if [[ "${TERRAFORM_NO_CHANGES:-false}" == "true" ]]; then
        print_status "Terraform was up to date - skipping secrets update"
        print_success "Secrets are already current"
        return
    fi
    
    print_status "Updating Kubernetes secrets with Terraform outputs..."
    if ./scripts/update-k8s-secrets.sh -e "${ENVIRONMENT}"; then
        print_success "Kubernetes secrets updated"
    else
        print_error "Failed to update Kubernetes secrets"
        exit 1
    fi
}

# Function to deploy application components
deploy_applications() {
    print_step "5" "Deploying Application Components"
    
    print_status "Deploying Congen application components..."
    if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-4"; then
        print_success "Application components deployed"
    else
        print_error "Failed to deploy application components"
        exit 1
    fi
    
    print_status "Waiting for applications to be ready..."
    if kubectl wait --for=condition=ready pod -l app=backend -n congen --timeout=300s; then
        print_success "Backend is ready"
    else
        print_warning "Backend may still be starting up"
    fi
    
    if kubectl wait --for=condition=ready pod -l app=frontend -n congen --timeout=300s; then
        print_success "Frontend is ready"
    else
        print_warning "Frontend may still be starting up"
    fi
}

# Function to deploy ingress
deploy_ingress() {
    print_step "6" "Deploying Ingress"
    
    # Check if stage-5 directory exists for this environment
    if [[ -d "k8s/overlays/${ENVIRONMENT}/stage-5" ]]; then
        print_status "Deploying ingress with environment-specific configuration..."
        if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-5"; then
            print_success "Ingress deployed"
        else
            print_error "Failed to deploy ingress"
            exit 1
        fi
    else
        print_status "No stage-5 directory found for ${ENVIRONMENT}, skipping ingress deployment"
    fi
}

# Function to deploy Horizontal Pod Autoscaler
deploy_hpa() {
    print_step "6" "Deploying Horizontal Pod Autoscaler"
    
    # Check if stage-6 directory exists for this environment
    if [[ -d "k8s/overlays/${ENVIRONMENT}/stage-6" ]]; then
        print_status "Deploying HPA with environment-specific configuration..."
        if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-6"; then
            print_success "HPA deployed"
        else
            print_error "Failed to deploy HPA"
            exit 1
        fi
    else
        print_status "No stage-6 directory found for ${ENVIRONMENT}, skipping HPA deployment"
    fi
}

# Function to display final status
show_final_status() {
    print_step "7" "Deployment Summary"
    
    print_status "Deployment completed successfully!"
    print_status "Environment: ${ENVIRONMENT}"
    print_status "Keycloak URL: ${KEYCLOAK_URL}"
    
    echo ""
    print_status "Available services:"
    kubectl get services -n congen
    
    echo ""
    print_status "Pod status:"
    kubectl get pods -n congen
    
    echo ""
    print_status "Next steps:"
    print_status "1. Verify all pods are running: kubectl get pods -n congen"
    print_status "2. Check application logs: kubectl logs -n congen -l app=backend"
    print_status "3. Access the application through your ingress or port-forward"
}

# Main execution
main() {
    print_status "Starting staged deployment for environment: ${ENVIRONMENT}"
    
    # Display configuration
    print_status "Configuration:"
    print_status "  Environment: ${ENVIRONMENT}"
    print_status "  Keycloak URL: ${KEYCLOAK_URL}"
    if [[ -n "${STAGE}" ]]; then
        print_status "  Stage: ${STAGE} (stage-specific deployment)"
    else
        print_status "  Stage: All stages (full deployment)"
    fi
    echo ""
    
    # Check prerequisites
    print_status "Checking prerequisites..."
    check_kubectl
    check_terraform
    
    # Execute deployment steps based on stage
    if [[ -n "${STAGE}" ]]; then
        case "${STAGE}" in
            1)
                print_status "Deploying Stage 1: Infrastructure only"
                deploy_infrastructure
                ;;
            2)
                print_status "Deploying Stage 2: Secrets only"
                deploy_secrets
                ;;
            3)
                print_status "Deploying Stage 3: Keycloak Infrastructure only"
                deploy_keycloak_infrastructure
                ;;
            4)
                print_status "Deploying Stage 4: Terraform and Secrets Update only"
                apply_terraform
                update_secrets
                ;;
            5)
                print_status "Deploying Stage 5: Application Components only"
                deploy_applications
                ;;
            6)
                print_status "Deploying Stage 6: Horizontal Pod Autoscaler only"
                deploy_hpa
                ;;
            *)
                print_error "Invalid stage: ${STAGE}. Must be 1, 2, 3, 4, 5, or 6."
                exit 1
                ;;
        esac
    else
        # Full deployment (all stages)
        deploy_infrastructure
        deploy_secrets
        deploy_keycloak_infrastructure
        apply_terraform
        update_secrets
        deploy_applications
        deploy_hpa
    fi
    
    show_final_status
}

# Run main function
main "$@" 