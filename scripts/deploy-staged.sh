#!/bin/bash

# Staged Deployment Script
# This script handles the complete staged deployment process.

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
    --stage STAGE           Deploy specific stage only (1-10)
    -h, --help              Show this help message

EXAMPLES:
    $0 -e local                    # Full deployment for local environment
    $0 -e staging -u https://keycloak.staging.example.com  # Staging deployment
    $0 -e local --stage 1          # Deploy only Stage 1 (Namespace)
    $0 -e local --stage 2          # Deploy only Stage 2 (Service Account)
    $0 -e local --stage 5          # Deploy only Stage 5 (Database Migrations)

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

# Function to deploy namespace
deploy_namespace() {
    print_step "1" "Deploying Namespace"
    
    print_status "Deploying namespace..."
    if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-1"; then
        print_success "Namespace deployed"
    else
        print_error "Failed to deploy namespace"
        exit 1
    fi
}

# Function to deploy service account
deploy_service_account() {
    print_step "2" "Deploying Service Account"
    
    print_status "Deploying service account..."
    if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-2"; then
        print_success "Service account deployed"
    else
        print_error "Failed to deploy service account"
        exit 1
    fi
}

# Function to deploy infrastructure
deploy_infrastructure() {
    print_step "3" "Deploying Infrastructure"
    
    print_status "Deploying infrastructure components..."
    if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-3"; then
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
    print_step "4" "Deploying Secrets"
    
    print_status "Deploying secrets to Kubernetes..."
    if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-4"; then
        print_success "Secrets deployed"
    else
        print_error "Failed to deploy secrets"
        exit 1
    fi
}

# Function to deploy database migrations
deploy_database_migrations() {
    print_step "5" "Deploying Database Migrations"
    
    print_status "Deploying database migrations to Kubernetes..."
    if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-5"; then
        print_success "Database migrations deployed"
    else
        print_error "Failed to deploy database migrations"
        exit 1
    fi
    
    print_status "Waiting for migration service to be ready..."
    if kubectl wait --for=condition=ready pod -l app=migration-service -n congen --timeout=300s; then
        print_success "Migration service is ready"
    else
        print_warning "Migration service may still be starting up"
    fi
}

# Function to deploy Keycloak infrastructure
deploy_keycloak_infrastructure() {
    print_step "6" "Deploying Keycloak Infrastructure"
    
    print_status "Deploying Keycloak to Kubernetes..."
    if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-6"; then
        print_success "Keycloak infrastructure deployed"
    else
        print_error "Failed to deploy Keycloak infrastructure"
        exit 1
    fi
    
    print_status "Waiting for PostgreSQL to be ready..."
    if kubectl wait --for=condition=ready pod -l app=postgres -n congen --timeout=300s; then
        print_success "PostgreSQL is ready"
    else
        print_error "PostgreSQL failed to become ready"
        kubectl get pods -n congen
        kubectl describe pods -n congen -l app=postgres
        exit 1
    fi
    
    print_status "Waiting for Keycloak pods to be created and ready..."
    
    # First wait for pods to be created
    local max_attempts=60
    local attempt=0
    while [[ ${attempt} -lt ${max_attempts} ]]; do
        local pod_count
        pod_count=$(kubectl get pods -n congen -l app=keycloak --no-headers 2>/dev/null | wc -l || true)
        
        if [[ ${pod_count} -gt 0 ]]; then
            print_status "Keycloak pods found, waiting for them to be ready..."
            break
        fi
        
        attempt=$((attempt + 1))
        print_status "Waiting for Keycloak pods to be created (attempt ${attempt}/${max_attempts})..."
        sleep 5
    done
    
    if [[ ${attempt} -eq ${max_attempts} ]]; then
        print_error "Keycloak pods failed to be created"
        kubectl get pods -n congen
        exit 1
    fi
    
    # Now wait for pods to be ready
    if kubectl wait --for=condition=ready pod -l app=keycloak -n congen --timeout=300s; then
        print_success "Keycloak is ready"
    else
        print_error "Keycloak failed to become ready"
        kubectl get pods -n congen
        kubectl describe pods -n congen -l app=keycloak
        exit 1
    fi
}

# Function to bootstrap Keycloak
bootstrap_keycloak() {
    print_status "Bootstrapping Keycloak (will skip if Terraform client already exists)..."
    if ./scripts/setup-keycloak-terraform.sh -u "${KEYCLOAK_URL}" -e "${ENVIRONMENT}"; then
        print_success "Keycloak bootstrap completed"
    else
        print_error "Keycloak bootstrap failed"
        exit 1
    fi
}

# Function to apply Terraform
apply_terraform() {
    print_step "7" "Bootstrapping Keycloak and Applying Terraform Configuration"
    
    # Bootstrap Keycloak first
    print_status "Bootstrapping Keycloak for Terraform..."
    bootstrap_keycloak
    
    local terraform_dir="terraform/environments/${ENVIRONMENT}"
    
    print_status "Initializing Terraform..."
    cd "${terraform_dir}" || exit
    if terraform init; then
        print_success "Terraform initialized"
    else
        print_error "Terraform initialization failed"
        cd - > /dev/null || exit
        exit 1
    fi
    
    print_status "Checking for Terraform changes..."
    local plan_output
    print_status "Running terraform plan..."
    
    plan_output=$(terraform plan -detailed-exitcode 2>&1)
    local plan_exit_code=$?
    
    print_status "Terraform plan completed with exit code: ${plan_exit_code}"
    print_status "Plan output length: ${#plan_output} characters"
    
    if [[ ${plan_exit_code} -eq 0 ]]; then
        print_success "No Terraform changes detected - infrastructure is up to date"
        cd - > /dev/null || exit
        # Set a flag to indicate no Terraform changes were applied
        export TERRAFORM_NO_CHANGES=true
        return
    elif [[ ${plan_exit_code} -eq 1 ]]; then
        print_error "Terraform plan failed"
        echo "Plan output: ${plan_output}"
        cd - > /dev/null || exit
        exit 1
    elif [[ ${plan_exit_code} -eq 2 ]]; then
        print_status "Terraform changes detected, applying configuration..."
        print_status "This may take several minutes. Please wait..."
        
        # Run terraform apply with progress indication
        print_status "Running terraform apply..."

        if terraform apply -auto-approve; then
            print_success "Terraform applied successfully"
            # Set a flag to indicate Terraform changes were applied
            export TERRAFORM_NO_CHANGES=false
        else
            print_error "Terraform apply failed"
            cd - > /dev/null || exit
            exit 1
        fi
    else
        print_error "Unexpected Terraform plan exit code: ${plan_exit_code}"
        echo "Plan output: ${plan_output}"
        cd - > /dev/null || exit
        exit 1
    fi
    
    cd - > /dev/null || exit
}

# Function to update Kubernetes secrets
update_secrets() {
    print_step "6" "Updating Kubernetes Secrets"
    
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
    print_step "8" "Deploying Application Components"
    
    print_status "Deploying Congen application components..."
    if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-7"; then
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

# Function to deploy networking
deploy_networking() {
    print_step "9" "Deploying Networking"
    
    # Check if stage-8 directory exists for this environment
    if [[ -d "k8s/overlays/${ENVIRONMENT}/stage-8" ]]; then
        print_status "Deploying networking with environment-specific configuration..."
        if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-8"; then
            print_success "Networking deployed"
        else
            print_error "Failed to deploy networking"
            exit 1
        fi
    else
        print_status "No stage-9 directory found for ${ENVIRONMENT}, skipping networking deployment"
    fi
}

# Function to deploy Horizontal Pod Autoscaler
deploy_hpa() {
    print_step "10" "Deploying Horizontal Pod Autoscaler"
    
    # Check if stage-9 directory exists for this environment
    if [[ -d "k8s/overlays/${ENVIRONMENT}/stage-9" ]]; then
        print_status "Deploying HPA with environment-specific configuration..."
        if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-9"; then
            print_success "HPA deployed"
        else
            print_error "Failed to deploy HPA"
            exit 1
        fi
    else
        print_status "No stage-10 directory found for ${ENVIRONMENT}, skipping HPA deployment"
    fi
}

# Function to set up port forwarding
setup_port_forwarding() {
    print_status "Setting up port forwarding for Keycloak..."
    
    # First, ensure Keycloak is fully ready before attempting port forwarding
    print_status "Ensuring Keycloak is fully ready before port forwarding..."
    local max_keycloak_attempts=60  # 2 minutes total
    local keycloak_attempt=0
    while [[ ${keycloak_attempt} -lt ${max_keycloak_attempts} ]]; do
        # Check if Keycloak pod is ready and running
        if kubectl get pod keycloak-0 -n congen -o jsonpath='{.status.phase}' 2>/dev/null | grep -q "Running"; then
            # Use a temporary port-forward to test if Keycloak is responding
            local temp_port=$((8080 + keycloak_attempt % 100 + 1000))  # Use different port each attempt
            kubectl port-forward -n congen service/keycloak ${temp_port}:8080 > /dev/null 2>&1 &
            local temp_pid=$!
            sleep 2
            
            # Test if Keycloak is responding
            if curl -s --connect-timeout 3 "http://localhost:${temp_port}/realms/master/" > /dev/null 2>&1; then
                kill ${temp_pid} 2>/dev/null || true
                print_success "Keycloak is fully ready"
                break
            fi
            
            kill ${temp_pid} 2>/dev/null || true
        fi
        keycloak_attempt=$((keycloak_attempt + 1))
        if [[ $((keycloak_attempt % 10)) -eq 0 ]]; then
            print_status "Waiting for Keycloak to be fully ready (attempt ${keycloak_attempt}/${max_keycloak_attempts})..."
        fi
        sleep 2
    done
    
    if [[ ${keycloak_attempt} -eq ${max_keycloak_attempts} ]]; then
        print_error "Keycloak failed to become fully ready after ${max_keycloak_attempts} attempts"
        kubectl get pods -n congen -l app=keycloak
        kubectl logs -n congen keycloak-0 --tail=20
        exit 1
    fi
    
    # Kill any existing port-forward processes
    pkill -f "kubectl port-forward.*keycloak.*8080" 2>/dev/null || true
    sleep 2
    
    # Start port forwarding in background
    kubectl port-forward -n congen service/keycloak 8080:8080 > /dev/null 2>&1 &
    export PORT_FORWARD_PID=$!
    
    # Wait a moment for the port forwarding to start
    sleep 3
    
    # Wait for port forwarding to establish and verify it's working
    local max_attempts=30  # Reduced since Keycloak is already ready
    local attempt=0
    while [[ ${attempt} -lt ${max_attempts} ]]; do
        if curl -s --connect-timeout 5 "http://localhost:8080/realms/master/" > /dev/null 2>&1; then
            print_success "Port forwarding established and Keycloak is accessible"
            break
        fi
        attempt=$((attempt + 1))
        if [[ $((attempt % 5)) -eq 0 ]]; then
            print_status "Waiting for port forwarding to establish (attempt ${attempt}/${max_attempts})..."
        fi
        sleep 2
    done
    
    if [[ ${attempt} -eq ${max_attempts} ]]; then
        print_error "Failed to establish port forwarding after ${max_attempts} attempts"
        print_status "Keycloak pod status:"
        kubectl get pods -n congen -l app=keycloak
        print_status "Keycloak logs:"
        kubectl logs -n congen keycloak-0 --tail=10
        kill "${PORT_FORWARD_PID}" 2>/dev/null || true
        exit 1
    fi
}

# Function to cleanup resources
cleanup() {
    # Clean up port forwarding if it's still running
    if [[ -n "${PORT_FORWARD_PID}" ]]; then
        print_status "Cleaning up port forwarding..."
        kill "${PORT_FORWARD_PID}" 2>/dev/null || true
    fi
    
    # Kill any remaining port-forward processes
    pkill -f "kubectl port-forward.*keycloak.*8080" 2>/dev/null || true
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
    
    # Set up port forwarding for stages that need it
    if [[ -n "${STAGE}" ]]; then
        case "${STAGE}" in
            7)
                print_status "Setting up port forwarding for Terraform operations..."
                setup_port_forwarding
                ;;
            *)
                # No port forwarding needed for other stages
                ;;
        esac
    fi
    
    # Execute deployment steps based on stage
    if [[ -n "${STAGE}" ]]; then
        case "${STAGE}" in
            1)
                print_status "Deploying Stage 1: Namespace only"
                deploy_namespace
                ;;
            2)
                print_status "Deploying Stage 2: Service Account only"
                deploy_service_account
                ;;
            3)
                print_status "Deploying Stage 3: Infrastructure only"
                deploy_infrastructure
                ;;
            4)
                print_status "Deploying Stage 4: Secrets only"
                deploy_secrets
                ;;
            5)
                print_status "Deploying Stage 5: Database Migrations only"
                deploy_database_migrations
                ;;
            6)
                print_status "Deploying Stage 6: Keycloak Infrastructure only"
                deploy_keycloak_infrastructure
                ;;
            7)
                print_status "Deploying Stage 7: Terraform and Secrets Update only"
                apply_terraform
                update_secrets
                ;;
            8)
                print_status "Deploying Stage 8: Application Components only"
                deploy_applications
                ;;
            9)
                print_status "Deploying Stage 9: Networking only"
                deploy_networking
                ;;
            10)
                print_status "Deploying Stage 10: Horizontal Pod Autoscaler only"
                deploy_hpa
                ;;
            *)
                print_error "Invalid stage: ${STAGE}. Must be 1-10."
                cleanup
                exit 1
                ;;
        esac
        show_final_status
    else
        # Full deployment (all stages)
        deploy_namespace
        deploy_service_account
        deploy_infrastructure
        deploy_secrets
        deploy_database_migrations
        deploy_keycloak_infrastructure
        
        # Set up port forwarding before Terraform operations
        print_status "Setting up port forwarding for Terraform operations..."
        setup_port_forwarding
        
        apply_terraform
        update_secrets
        deploy_applications
        deploy_networking
        deploy_hpa
    fi
    
    show_final_status
    cleanup
}

# Run main function
main "$@" 