#!/bin/bash

# Script to stop the Congen minikube deployment and clean up port forwarding
# This script stops port forwarding and optionally stops minikube

set -e

# Configuration
MINIKUBE_PROFILE="congen"
NAMESPACE="congen"

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

# Function to stop port forwarding
stop_port_forwarding() {
    print_status "Stopping port forwarding..."
    
    # Kill all port forward processes and clean up
    pkill -f "kubectl port-forward.*${NAMESPACE}" || true
    rm -f /tmp/congen-*-port-forward.pid /tmp/congen-*-port-forward.log
    
    print_success "Port forwarding stopped"
}

# Function to stop the application deployments
stop_application() {
    print_status "Stopping application deployments..."
    
    # Delete the custom PostgreSQL deployment with local storage
    kubectl delete deployment postgres-local -n "${NAMESPACE}" --ignore-not-found=true
    kubectl delete service postgres-local -n "${NAMESPACE}" --ignore-not-found=true
    
    # Delete the main application deployments (deployed by Gradle)
    kubectl delete deployment backend frontend keycloak -n "${NAMESPACE}" --ignore-not-found=true
    kubectl delete service backend frontend keycloak -n "${NAMESPACE}" --ignore-not-found=true
    
    # Delete other resources
    kubectl delete configmap backend-config postgres-config -n "${NAMESPACE}" --ignore-not-found=true
    kubectl delete secret congen-secret keycloak-secret -n "${NAMESPACE}" --ignore-not-found=true
    
    # Delete jobs (like migration jobs)
    kubectl delete job -l app=migration-service -n "${NAMESPACE}" --ignore-not-found=true
    
    print_success "Application deployments stopped"
}

# Function to stop minikube (optional)
stop_minikube() {
    local stop_minikube_flag="$1"
    
    if [ "$stop_minikube_flag" = "--stop-minikube" ]; then
        print_status "Stopping minikube profile '${MINIKUBE_PROFILE}'..."
        
        if minikube status -p "${MINIKUBE_PROFILE}" >/dev/null 2>&1; then
            minikube stop -p "${MINIKUBE_PROFILE}"
            print_success "Minikube profile '${MINIKUBE_PROFILE}' stopped"
        else
            print_warning "Minikube profile '${MINIKUBE_PROFILE}' is not running"
        fi
    else
        print_status "Minikube profile '${MINIKUBE_PROFILE}' is still running"
        print_status "To stop minikube, run: $0 --stop-minikube"
    fi
}

# Function to display cleanup information
display_cleanup_info() {
    echo ""
    echo "🧹 Cleanup completed!"
    echo ""
    echo "📋 What was stopped:"
    echo "   ✅ Port forwarding processes"
    echo "   ✅ Application deployments"
    echo "   ✅ Services and configurations"
    echo ""
    echo "💾 Data preservation:"
    echo "   📁 Data is preserved in the directory used when starting the application"
    echo ""
    echo "🔄 To restart:"
    echo "   ./scripts/start-minikube-local.sh"
    echo ""
    echo "🛑 To stop minikube completely:"
    echo "   $0 --stop-minikube"
    echo ""
    echo "📚 Alternative deployment methods:"
    echo "   ./gradlew deployAll -Penvironment=local    # Full deployment"
    echo "   ./gradlew deployStage -Pstage=1 -Penvironment=local  # Stage-specific"
    echo ""
}

# Function to show help
show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Stop the Congen minikube deployment and clean up port forwarding"
    echo ""
    echo "Options:"
    echo "  --stop-minikube       Also stop the minikube profile (saves resources)"
    echo "  --help, -h            Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                 # Stop application and port forwarding, keep minikube running"
    echo "  $0 --stop-minikube # Stop everything including minikube"
    echo ""
}

# Main execution
main() {
    local stop_minikube_flag=""
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --stop-minikube)
                stop_minikube_flag="--stop-minikube"
                shift
                ;;
            --help|-h)
                show_help
                exit 0
                ;;
            *)
                print_error "Unknown option: $1"
                show_help
                exit 1
                ;;
        esac
    done
    
    echo "🛑 Stopping Congen minikube deployment..."
    echo ""
    
    # Execute cleanup steps
    stop_port_forwarding
    stop_application
    stop_minikube "$stop_minikube_flag"
    display_cleanup_info
    
    print_success "Congen minikube deployment stopped successfully!"
}

# Run main function
main "$@"
