#!/bin/bash

# Comprehensive Deployment Script for Congen
# This script handles the complete deployment workflow

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
    if ! command -v $1 &> /dev/null; then
        print_error "$1 is not installed. Please install it first."
        exit 1
    fi
}

# Function to check if Minikube is running
check_minikube() {
    if ! minikube status &> /dev/null; then
        print_warning "Minikube is not running. Starting Minikube..."
        minikube start --memory=8192 --cpus=4 --disk-size=20g
        eval $(minikube docker-env)
    else
        print_success "Minikube is running"
        eval $(minikube docker-env)
    fi
}

# Function to deploy to local environment
deploy_local() {
    print_status "Deploying to local environment..."
    
    # Build the application
    print_status "Building application..."
    ./gradlew clean build
    
    # Build Docker image
    print_status "Building Docker image..."
    ./gradlew buildDockerImage
    
    # Deploy to Kubernetes
    print_status "Deploying to Kubernetes..."
    ./gradlew deployToLocal
    
    print_success "Local deployment complete!"
    print_status "Access the application at: http://$(minikube ip):30080"
}

# Function to deploy to staging
deploy_staging() {
    print_status "Deploying to staging environment..."
    
    # Build the application
    print_status "Building application..."
    ./gradlew clean build
    
    # Build Docker image with staging tag
    print_status "Building Docker image for staging..."
    docker build -t congen:staging .
    
    # Deploy to staging
    print_status "Deploying to staging Kubernetes..."
    kubectl apply -k k8s/overlays/staging
    
    print_success "Staging deployment complete!"
}

# Function to deploy to production
deploy_production() {
    print_status "Deploying to production environment..."
    
    # Confirm production deployment
    read -p "Are you sure you want to deploy to production? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_warning "Production deployment cancelled"
        exit 0
    fi
    
    # Build the application
    print_status "Building application..."
    ./gradlew clean build
    
    # Build Docker image with production tag
    print_status "Building Docker image for production..."
    docker build -t congen:production .
    
    # Deploy to production
    print_status "Deploying to production Kubernetes..."
    kubectl apply -k k8s/overlays/production
    
    print_success "Production deployment complete!"
}

# Function to clean up
cleanup() {
    print_status "Cleaning up deployment..."
    
    case $1 in
        "local")
            kubectl delete -k k8s/overlays/local --ignore-not-found=true
            ;;
        "staging")
            kubectl delete -k k8s/overlays/staging --ignore-not-found=true
            ;;
        "production")
            kubectl delete -k k8s/overlays/production --ignore-not-found=true
            ;;
        "all")
            kubectl delete namespace congen --ignore-not-found=true
            ;;
        *)
            print_error "Invalid environment. Use: local, staging, production, or all"
            exit 1
            ;;
    esac
    
    print_success "Cleanup complete!"
}

# Function to show status
show_status() {
    print_status "Checking deployment status..."
    
    echo
    print_status "Pods:"
    kubectl get pods -n congen
    
    echo
    print_status "Services:"
    kubectl get services -n congen
    
    echo
    print_status "ConfigMaps:"
    kubectl get configmaps -n congen
    
    echo
    print_status "Secrets:"
    kubectl get secrets -n congen
}

# Function to show logs
show_logs() {
    local pod_name=$1
    
    if [ -z "$pod_name" ]; then
        print_status "Showing logs for congen pod..."
        kubectl logs -n congen -l app=congen -f
    else
        print_status "Showing logs for pod: $pod_name"
        kubectl logs -n congen $pod_name -f
    fi
}

# Function to show help
show_help() {
    echo "Usage: $0 [COMMAND] [OPTIONS]"
    echo
    echo "Commands:"
    echo "  local       Deploy to local Minikube environment"
    echo "  staging     Deploy to staging environment"
    echo "  production  Deploy to production environment"
    echo "  cleanup     Clean up deployment"
    echo "  status      Show deployment status"
    echo "  logs        Show application logs"
    echo "  help        Show this help message"
    echo
    echo "Options:"
    echo "  --env       Environment for cleanup (local|staging|production|all)"
    echo "  --pod       Pod name for logs"
    echo
    echo "Examples:"
    echo "  $0 local                    # Deploy to local environment"
    echo "  $0 staging                  # Deploy to staging environment"
    echo "  $0 production               # Deploy to production environment"
    echo "  $0 cleanup --env local      # Clean up local deployment"
    echo "  $0 status                   # Show deployment status"
    echo "  $0 logs --pod congen-xyz    # Show logs for specific pod"
}

# Main script logic
main() {
    # Check prerequisites
    print_status "Checking prerequisites..."
    check_command "kubectl"
    check_command "docker"
    check_command "gradle"
    
    case $1 in
        "local")
            check_minikube
            deploy_local
            ;;
        "staging")
            deploy_staging
            ;;
        "production")
            deploy_production
            ;;
        "cleanup")
            cleanup $2
            ;;
        "status")
            show_status
            ;;
        "logs")
            show_logs $2
            ;;
        "help"|"--help"|"-h")
            show_help
            ;;
        "")
            show_help
            ;;
        *)
            print_error "Unknown command: $1"
            show_help
            exit 1
            ;;
    esac
}

# Parse command line arguments
ENV=""
POD_NAME=""

while [[ $# -gt 0 ]]; do
    case $1 in
        --env)
            ENV="$2"
            shift 2
            ;;
        --pod)
            POD_NAME="$2"
            shift 2
            ;;
        *)
            COMMAND="$1"
            shift
            ;;
    esac
done

# Run main function
main "$COMMAND" "$ENV" "$POD_NAME" 