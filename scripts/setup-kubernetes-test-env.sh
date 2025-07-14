#!/bin/bash

# Kubernetes Test Environment Setup Script
# This script is called from Gradle tasks to setup the Kubernetes environment

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
        
        # Enable addons
        minikube addons enable ingress
        minikube addons enable metrics-server
    else
        print_success "Minikube is already running"
    fi
    
    # Point shell to minikube's docker-daemon
    MINIKUBE_DOCKER_ENV_OUTPUT="$(minikube docker-env)"
    eval "${MINIKUBE_DOCKER_ENV_OUTPUT}"
}

# Function to create namespace
create_namespace() {
    print_status "Creating congen namespace..."
    KUBECTL_NAMESPACE_YAML_OUTPUT="$(kubectl create namespace congen --dry-run=client -o yaml)"
    echo "${KUBECTL_NAMESPACE_YAML_OUTPUT}" | kubectl apply -f -
}

# Function to build and deploy application
build_and_deploy() {
    print_status "Building Docker image..."
    docker build -t congen:latest .
    
    print_status "Deploying to Kubernetes..."
    kubectl apply -k k8s/overlays/local
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

# Function to setup port forwarding
setup_port_forward() {
    print_status "Setting up port forwarding..."
    
    # Kill any existing port forward
    pkill -f "kubectl port-forward.*postgres" || true
    
    # Start port forward in background
    kubectl port-forward -n congen service/postgres 5432:5432 > /tmp/k8s-port-forward.log 2>&1 &
    local port_forward_pid=$!
    
    # Wait for port forward to establish
    sleep 3
    
    # Check if port forward is working
    if ! nc -z localhost 5432; then
        print_error "Port forward failed to establish"
        kill "${port_forward_pid}" 2>/dev/null || true
        exit 1
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
    
    # Clean up log files
    rm -f /tmp/k8s-port-forward.log
    rm -f /tmp/k8s-port-forward-error.log
}

# Main script logic
main() {
    local action=$1
    
    case ${action} in
        "setup")
            print_status "Setting up Kubernetes test environment..."
            check_command "minikube"
            check_command "kubectl"
            check_command "docker"
            check_command "nc"
            
            check_minikube
            create_namespace
            build_and_deploy
            wait_for_ready
            setup_port_forward
            
            print_success "Kubernetes test environment setup complete!"
            ;;
        "cleanup")
            print_status "Cleaning up Kubernetes test environment..."
            cleanup_port_forward
            print_success "Cleanup complete!"
            ;;
        "status")
            print_status "Checking Kubernetes test environment status..."
            
            if minikube status &> /dev/null; then
                print_success "Minikube is running"
            else
                print_warning "Minikube is not running"
            fi
            
            if kubectl get pods -n congen &> /dev/null; then
                print_success "Application is deployed"
                kubectl get pods -n congen
            else
                print_warning "Application is not deployed"
            fi
            
            if nc -z localhost 5432; then
                print_success "Port forward is active"
            else
                print_warning "Port forward is not active"
            fi
            ;;
        *)
            print_error "Unknown action: ${action}"
            print_error "Use: setup, cleanup, or status"
            exit 1
            ;;
    esac
}

# Run main function
main "$@" 