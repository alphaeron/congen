#!/bin/bash

# Unified Kubernetes Setup Script for Congen
# This script sets up the local Kubernetes environment for development and testing

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
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
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check if command exists
check_command() {
    if ! command -v "${1}" &> /dev/null; then
        print_error "${1} is not installed. Please install it first."
        exit 1
    fi
}

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    check_command "minikube"
    check_command "kubectl"
    check_command "docker"
    check_command "kustomize"
    
    # Optional tools
    if ! command -v "skaffold" &> /dev/null; then
        print_warning "Skaffold is not installed. Some features may not be available."
    fi
    
    if ! command -v "nc" &> /dev/null; then
        print_warning "netcat (nc) is not installed. Port forwarding checks may not work."
    fi
    
    print_success "All required prerequisites are installed"
}

# Function to start and configure minikube
setup_minikube() {
    print_status "Setting up Minikube..."
    
    if ! minikube status &> /dev/null; then
        print_status "Starting Minikube..."
        minikube start
        print_success "Minikube started successfully"
    else
        print_success "Minikube is already running"
    fi
    
    # Enable addons
    print_status "Enabling Minikube addons..."
    minikube addons enable ingress
    minikube addons enable metrics-server
    print_success "Minikube addons enabled"
}

# Function to create namespace
create_namespace() {
    print_status "Creating congen namespace..."
    KUBECTL_NAMESPACE_YAML_OUTPUT="$(kubectl create namespace congen --dry-run=client -o yaml)"
    echo "${KUBECTL_NAMESPACE_YAML_OUTPUT}" | kubectl apply -f -
    print_success "Namespace created"
}

# Function to build and deploy application
build_and_deploy() {
    print_status "Building Docker image with JIB..."
    ./gradlew jibDockerBuild
    
    print_status "Cleaning up existing resources..."
    kubectl delete job liquibase-migration -n congen --ignore-not-found=true
    
    print_status "Deploying to Kubernetes..."
    kubectl apply -k k8s/overlays/local
    print_success "Application deployed"
}

# Function to wait for resources to be ready
wait_for_ready() {
    print_status "Waiting for PostgreSQL to be ready..."
    
    local max_attempts=30
    local attempt=1
    
    while [[ "${attempt}" -le "${max_attempts}" ]]; do
        if kubectl exec -n congen deployment/postgres -- pg_isready -U postgres &> /dev/null; then
            print_success "PostgreSQL is ready"
            break
        fi
        
        print_status "Attempt ${attempt}/${max_attempts}: PostgreSQL not ready yet..."
        sleep 2
        ((attempt++))
    done
    
    if [[ "${attempt}" -gt "${max_attempts}" ]]; then
        print_error "PostgreSQL failed to become ready after ${max_attempts} attempts"
        exit 1
    fi
    
    print_status "Waiting for application to be ready..."
    kubectl wait --for=condition=ready pod -l app=congen -n congen --timeout=300s
    print_success "Application is ready"
}

# Function to setup port forwarding for testing
setup_test_port_forward() {
    print_status "Setting up port forwarding for testing..."
    
    # Kill any existing port forward
    pkill -f "kubectl port-forward.*postgres" || true
    
    # Start port forward in background
    kubectl port-forward -n congen service/postgres 5432:5432 > /tmp/k8s-port-forward.log 2>&1 &
    local port_forward_pid=$!
    
    # Wait for port forward to establish
    sleep 3
    
    # Check if port forward is working
    if command -v "nc" &> /dev/null && ! nc -z localhost 5432; then
        print_error "Port forward failed to establish"
        kill "${port_forward_pid}" 2>/dev/null || true
        exit 1
    fi
    
    print_success "Test port forward established (PID: ${port_forward_pid})"
    echo "${port_forward_pid}" > /tmp/k8s-test-port-forward.pid
}

# Function to cleanup test port forward
cleanup_test_port_forward() {
    if [[ -f /tmp/k8s-test-port-forward.pid ]]; then
        local pid
        pid=$(cat /tmp/k8s-test-port-forward.pid)
        print_status "Cleaning up test port forward (PID: ${pid})..."
        kill "${pid}" 2>/dev/null || true
        rm -f /tmp/k8s-test-port-forward.pid
    fi
    
    # Clean up log files
    rm -f /tmp/k8s-port-forward.log
    rm -f /tmp/k8s-port-forward-error.log
}

# Function to show environment information
show_environment_info() {
    print_status "Environment Information:"
    
    if minikube status &> /dev/null; then
        MINIKUBE_IP=$(minikube ip)
        print_success "Minikube is running"
        echo "  - Minikube IP: ${MINIKUBE_IP}"
        echo "  - Application: http://${MINIKUBE_IP}:30080"
        echo "  - Health check: http://${MINIKUBE_IP}:30080/actuator/health"
    else
        print_warning "Minikube is not running"
    fi
    
    if kubectl get pods -n congen &> /dev/null; then
        print_success "Application is deployed"
        echo "  - Pods:"
        # shellcheck disable=SC2312
        kubectl get pods -n congen --no-headers | while read -r line; do
            echo "    ${line}"
        done
    else
        print_warning "Application is not deployed"
    fi
    
    if command -v "nc" &> /dev/null && nc -z localhost 5432 2>/dev/null; then
        print_success "Test port forward is active (localhost:5432)"
    else
        print_warning "Test port forward is not active"
    fi
    
    echo ""
    print_status "Next steps:"
    echo "1. For localhost access: ./scripts/access-local-app.sh"
    echo "2. For development: ./gradlew skaffoldDev"
    echo "3. For testing: ./gradlew integrationTest"
    echo "4. For cleanup: $0 cleanup"
}

# Function to show help
show_help() {
    echo "Unified Kubernetes Setup Script for Congen"
    echo ""
    echo "Usage: $0 [ACTION] [OPTIONS]"
    echo ""
    echo "Actions:"
    echo "  setup     - Complete setup (prerequisites, minikube, deploy) [default]"
    echo "  deploy    - Deploy application only (starts minikube if needed)"
    echo "  status    - Show current environment status"
    echo "  cleanup   - Clean up test resources"
    echo "  help      - Show this help message"
    echo ""
    echo "Options for setup/deploy:"
    echo "  --test    - Enable test port forwarding (for integration tests)"
    echo ""
    echo "Examples:"
    echo "  $0                    # Complete setup"
    echo "  $0 setup              # Complete setup"
    echo "  $0 setup --test       # Setup with test port forwarding"
    echo "  $0 deploy             # Deploy application only"
    echo "  $0 deploy --test      # Deploy with test port forwarding"
    echo "  $0 status             # Check status"
    echo "  $0 cleanup            # Clean up"
}

# Main script logic
main() {
    local action=${1:-setup}
    local enable_test=false
    
    # Parse options
    shift
    while [[ $# -gt 0 ]]; do
        case $1 in
            --test)
                enable_test=true
                shift
                ;;
            *)
                print_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    case ${action} in
        "setup"|"")
            print_status "Setting up complete Kubernetes environment..."
            check_prerequisites
            setup_minikube
            create_namespace
            build_and_deploy
            wait_for_ready
            
            if [[ "${enable_test}" == "true" ]]; then
                setup_test_port_forward
                print_success "Kubernetes environment setup complete with test port forwarding!"
            else
                print_success "Kubernetes environment setup complete!"
            fi
            
            show_environment_info
            ;;
        "deploy")
            print_status "Deploying application..."
            check_prerequisites
            setup_minikube  # Always ensure minikube is running
            create_namespace
            build_and_deploy
            wait_for_ready
            
            if [[ "${enable_test}" == "true" ]]; then
                setup_test_port_forward
                print_success "Application deployment complete with test port forwarding!"
            else
                print_success "Application deployment complete!"
            fi
            ;;
        "cleanup")
            print_status "Cleaning up test resources..."
            cleanup_test_port_forward
            print_success "Cleanup complete!"
            ;;
        "status")
            show_environment_info
            ;;
        "help"|"-h"|"--help")
            show_help
            ;;
        *)
            print_error "Unknown action: ${action}"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# Run main function
main "$@" 