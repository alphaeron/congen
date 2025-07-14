#!/bin/bash

# Kubernetes Integration Test Runner
# This script sets up the environment and runs integration tests against Kubernetes

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

# Function to check if Minikube is running
check_minikube() {
    if ! minikube status &> /dev/null; then
        print_warning "Minikube is not running. Starting Minikube..."
        minikube start --memory=8192 --cpus=4 --disk-size=20g
        MINIKUBE_DOCKER_ENV_OUTPUT="$(minikube docker-env)"
        eval "${MINIKUBE_DOCKER_ENV_OUTPUT}"
    else
        print_success "Minikube is running"
        MINIKUBE_DOCKER_ENV_OUTPUT="$(minikube docker-env)"
        eval "${MINIKUBE_DOCKER_ENV_OUTPUT}"
    fi
}

# Function to check if application is deployed
check_deployment() {
    if ! kubectl get pods -n congen &> /dev/null; then
        print_warning "Application is not deployed. Deploying..."
        ./scripts/deploy.sh local
    else
        print_success "Application is deployed"
    fi
}

# Function to wait for PostgreSQL to be ready
wait_for_postgres() {
    print_status "Waiting for PostgreSQL to be ready..."
    
    local max_attempts=30
    local attempt=1
    
    while [[ "${attempt}" -le "${max_attempts}" ]]; do
        if kubectl exec -n congen deployment/postgres -- pg_isready -U postgres &> /dev/null; then
            print_success "PostgreSQL is ready"
            return 0
        fi
        
        print_status "Attempt ${attempt}/${max_attempts}: PostgreSQL not ready yet..."
        sleep 2
        ((attempt++))
    done
    
    print_error "PostgreSQL failed to become ready after ${max_attempts} attempts"
    return 1
}

# Function to setup port forwarding
setup_port_forward() {
    print_status "Setting up port forwarding for PostgreSQL..."
    
    # Kill any existing port forward
    pkill -f "kubectl port-forward.*postgres" || true
    
    # Start port forward in background
    kubectl port-forward -n congen service/postgres 5432:5432 &
    local port_forward_pid=$!
    
    # Wait a moment for port forward to establish
    sleep 3
    
    # Check if port forward is working
    if ! nc -z localhost 5432; then
        print_error "Port forward failed to establish"
        kill "${port_forward_pid}" 2>/dev/null || true
        return 1
    fi
    
    print_success "Port forward established (PID: ${port_forward_pid})"
    echo "${port_forward_pid}" > /tmp/k8s-test-port-forward.pid
}

# Function to cleanup port forward
cleanup_port_forward() {
    if [[ -f /tmp/k8s-test-port-forward.pid ]]; then
        local pid
        pid=$(cat /tmp/k8s-test-port-forward.pid)
        print_status "Cleaning up port forward (PID: ${pid})..."
        kill "${pid}" 2>/dev/null || true
        rm -f /tmp/k8s-test-port-forward.pid
    fi
}

# Function to run tests
run_tests() {
    local test_type=$1
    
    case ${test_type} in
        "kubernetes")
            print_status "Running integration tests against Kubernetes..."
            ./gradlew kubernetesIntegrationTest
            ;;
        "containers")
            print_status "Running integration tests with TestContainers..."
            ./gradlew integrationTest
            ;;
        "all")
            print_status "Running all integration tests..."
            ./gradlew integrationTest
            ./gradlew kubernetesIntegrationTest
            ;;
        *)
            print_error "Unknown test type: ${test_type}"
            print_error "Use: kubernetes, containers, or all"
            exit 1
            ;;
    esac
}

# Function to show help
show_help() {
    echo "Usage: $0 [OPTIONS] [TEST_TYPE]"
    echo
    echo "Options:"
    echo "  --setup-only     Only setup the environment, don't run tests"
    echo "  --cleanup-only   Only cleanup the environment"
    echo "  --help           Show this help message"
    echo
    echo "Test Types:"
    echo "  kubernetes       Run tests against Kubernetes cluster"
    echo "  containers       Run tests with TestContainers"
    echo "  all              Run both test types"
    echo
    echo "Examples:"
    echo "  $0 kubernetes    # Run tests against Kubernetes"
    echo "  $0 containers    # Run tests with TestContainers"
    echo "  $0 all           # Run all integration tests"
    echo "  $0 --setup-only  # Only setup environment"
}

# Main script logic
main() {
    local test_type="kubernetes"
    local setup_only=false
    local cleanup_only=false
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --setup-only)
                setup_only=true
                shift
                ;;
            --cleanup-only)
                cleanup_only=true
                shift
                ;;
            --help|-h)
                show_help
                exit 0
                ;;
            kubernetes|containers|all)
                test_type="$1"
                shift
                ;;
            *)
                print_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    # Check prerequisites
    print_status "Checking prerequisites..."
    check_command "kubectl"
    check_command "docker"
    check_command "gradle"
    check_command "nc"
    
    if [[ "${cleanup_only}" = true ]]; then
        cleanup_port_forward
        print_success "Cleanup complete"
        exit 0
    fi
    
    if [[ "${test_type}" = "kubernetes" ]] || [[ "${test_type}" = "all" ]]; then
        # Setup Kubernetes environment
        check_minikube
        check_deployment
        wait_for_postgres
        setup_port_forward
        
        # Set trap to cleanup on exit
        trap cleanup_port_forward EXIT
    fi
    
    if [[ "${setup_only}" = true ]]; then
        print_success "Environment setup complete"
        print_status "You can now run tests manually:"
        print_status "  ./gradlew kubernetesIntegrationTest"
        exit 0
    fi
    
    # Run tests
    run_tests "${test_type}"
    
    print_success "Integration tests completed!"
}

# Run main function
main "$@" 