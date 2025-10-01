#!/bin/bash

# Script to start the Congen minikube deployment with persistent storage
# This script leverages the existing Gradle deployment system and adds persistent storage

set -e

# Configuration
MINIKUBE_PROFILE="congen"
NAMESPACE="congen"
DEFAULT_DATA_DIR="${HOME}/.congen/minikube-data"
POSTGRES_DATA_DIR="${DEFAULT_DATA_DIR}/postgres"

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

# Function to check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    if ! command -v minikube &> /dev/null; then
        print_error "minikube is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is not installed or not in PATH"
        exit 1
    fi
    
    if ! command -v curl &> /dev/null; then
        print_error "curl is not installed or not in PATH"
        exit 1
    fi
    
    if [ ! -f "gradlew" ]; then
        print_error "Gradle wrapper not found. Please run this script from the project root."
        exit 1
    fi
    
    print_success "All prerequisites are available"
}

# Function to create local data directories
setup_local_storage() {
    print_status "Setting up local storage directories..."
    
    # Create main data directory
    mkdir -p "${DEFAULT_DATA_DIR}"
    
    # Create PostgreSQL data directory
    mkdir -p "${POSTGRES_DATA_DIR}"
    
    # Set proper permissions for PostgreSQL
    chmod 755 "${POSTGRES_DATA_DIR}"
    
    print_success "Local storage directories created at: ${DEFAULT_DATA_DIR}"
}

# Function to create a custom PostgreSQL deployment with local storage
create_postgres_with_local_storage() {
    print_status "Creating PostgreSQL deployment with local storage..."
    
    # Create a custom PostgreSQL deployment that mounts local directory
    cat <<EOF | kubectl apply -f -
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres-local
  namespace: ${NAMESPACE}
  labels:
    app: postgres-local
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres-local
  template:
    metadata:
      labels:
        app: postgres-local
    spec:
      containers:
      - name: postgres
        image: postgres:15-alpine
        ports:
        - containerPort: 5432
        env:
        - name: POSTGRES_DB
          value: "congen"
        - name: POSTGRES_USER
          value: "postgres"
        - name: POSTGRES_PASSWORD
          value: "postgres"
        - name: PGDATA
          value: /var/lib/postgresql/data/pgdata
        volumeMounts:
        - name: postgres-local-storage
          mountPath: /var/lib/postgresql/data
        resources:
          requests:
            memory: "100Mi"
            cpu: "250m"
          limits:
            memory: "250Mi"
            cpu: "500m"
        livenessProbe:
          exec:
            command:
            - pg_isready
            - -U
            - postgres
          initialDelaySeconds: 30
          periodSeconds: 10
        readinessProbe:
          exec:
            command:
            - pg_isready
            - -U
            - postgres
          initialDelaySeconds: 5
          periodSeconds: 5
      volumes:
      - name: postgres-local-storage
        hostPath:
          path: ${POSTGRES_DATA_DIR}
          type: DirectoryOrCreate
EOF

    # Create service for the local PostgreSQL
    cat <<EOF | kubectl apply -f -
apiVersion: v1
kind: Service
metadata:
  name: postgres-local
  namespace: ${NAMESPACE}
  labels:
    app: postgres-local
spec:
  type: ClusterIP
  ports:
  - port: 5432
    targetPort: 5432
    protocol: TCP
    name: postgres
  selector:
    app: postgres-local
EOF

    print_success "PostgreSQL with local storage created"
}

# Function to wait for PostgreSQL to be ready
wait_for_postgres() {
    print_status "Waiting for PostgreSQL to be ready..."
    
    # Wait for deployment to be ready (this includes readiness probe)
    kubectl wait --for=condition=available --timeout=300s deployment/postgres-local -n "${NAMESPACE}"
    
    print_success "PostgreSQL is ready and accepting connections"
}

# Function to deploy the application using Gradle
deploy_application() {
    print_status "Deploying Congen application using Gradle..."
    
    # Use the existing Gradle deployment system
    ./gradlew deployAll -Penvironment=local
    
    print_success "Application deployed successfully using Gradle"
}

# Function to update backend configuration to use local PostgreSQL
update_backend_config() {
    print_status "Updating backend configuration to use local PostgreSQL..."
    
    # Patch the backend deployment to use local PostgreSQL
    kubectl patch deployment backend -n "${NAMESPACE}" --type='merge' -p='{
        "spec": {
            "template": {
                "spec": {
                    "containers": [{
                        "name": "backend",
                        "env": [
                            {"name": "PGHOST", "value": "postgres-local"},
                            {"name": "PGPORT", "value": "5432"},
                            {"name": "PGDATABASE", "value": "congen"},
                            {"name": "PGUSER", "value": "postgres"},
                            {"name": "PGPASSWORD", "value": "postgres"},
                            {"name": "PGSSLMODE", "value": "false"}
                        ]
                    }]
                }
            }
        }
    }'
    
    print_success "Backend configuration updated"
}

# Function to set up port forwarding
setup_port_forwarding() {
    print_status "Setting up port forwarding..."
    
    # Kill any existing port forwards
    pkill -f "kubectl port-forward.*${NAMESPACE}" || true
    
    # Start port forwards in background
    kubectl port-forward service/backend 8888:8888 -n "${NAMESPACE}" > /tmp/congen-backend-port-forward.log 2>&1 &
    echo $! > /tmp/congen-backend-port-forward.pid
    
    kubectl port-forward service/frontend 3000:3000 -n "${NAMESPACE}" > /tmp/congen-frontend-port-forward.log 2>&1 &
    echo $! > /tmp/congen-frontend-port-forward.pid
    
    kubectl port-forward service/keycloak 8080:8080 -n "${NAMESPACE}" > /tmp/congen-keycloak-port-forward.log 2>&1 &
    echo $! > /tmp/congen-keycloak-port-forward.pid
    
    kubectl port-forward service/postgres-local 5432:5432 -n "${NAMESPACE}" > /tmp/congen-postgres-port-forward.log 2>&1 &
    echo $! > /tmp/congen-postgres-port-forward.pid
    
    sleep 3
    print_success "Port forwarding established"
}

# Function to verify services are accessible
verify_services() {
    print_status "Verifying services are accessible..."
    
    # Quick health checks
    curl -s http://localhost:8888/actuator/health >/dev/null 2>&1 && print_success "Backend: http://localhost:8888" || print_warning "Backend not ready"
    curl -s http://localhost:3000 >/dev/null 2>&1 && print_success "Frontend: http://localhost:3000" || print_warning "Frontend not ready"
    curl -s http://localhost:8080/realms/congen >/dev/null 2>&1 && print_success "Keycloak: http://localhost:8080" || print_warning "Keycloak not ready"
    nc -z localhost 5432 2>/dev/null && print_success "PostgreSQL: localhost:5432" || print_warning "PostgreSQL not ready"
}

# Function to display access information
display_access_info() {
    echo ""
    echo "🎉 Congen minikube deployment is ready!"
    echo ""
    echo "📱 Application Access:"
    echo "   Frontend:     http://localhost:3000"
    echo "   Backend API:  http://localhost:8888"
    echo "   Keycloak:     http://localhost:8080"
    echo "   PostgreSQL:   localhost:5432"
    echo ""
    echo "🔧 Management:"
    echo "   Stop services: ./scripts/stop-minikube-local.sh"
    echo "   View logs:     kubectl logs -f deployment/backend -n ${NAMESPACE}"
    echo "   Database:      psql -h localhost -p 5432 -U postgres -d congen"
    echo ""
    echo "💾 Data Storage:"
    echo "   PostgreSQL data: ${POSTGRES_DATA_DIR}"
    echo "   All data:        ${DEFAULT_DATA_DIR}"
    echo ""
    echo "📋 Port Forward PIDs:"
    echo "   Backend:   $(cat /tmp/congen-backend-port-forward.pid 2>/dev/null || echo 'N/A')"
    echo "   Frontend:  $(cat /tmp/congen-frontend-port-forward.pid 2>/dev/null || echo 'N/A')"
    echo "   Keycloak:  $(cat /tmp/congen-keycloak-port-forward.pid 2>/dev/null || echo 'N/A')"
    echo "   PostgreSQL: $(cat /tmp/congen-postgres-port-forward.pid 2>/dev/null || echo 'N/A')"
    echo ""
}

# Function to show help
show_help() {
    echo "Usage: $0 [OPTIONS]"
    echo ""
    echo "Start the Congen minikube deployment with persistent storage"
    echo ""
    echo "Options:"
    echo "  -d, --data-dir DIR    Data directory for persistent storage (default: ${DEFAULT_DATA_DIR})"
    echo "  --help, -h           Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0                                    # Use default data directory"
    echo "  $0 -d /path/to/custom/data           # Use custom data directory"
    echo "  $0 --data-dir /tmp/congen-data       # Use temporary data directory"
    echo ""
}

# Function to parse command line arguments
parse_arguments() {
    while [[ $# -gt 0 ]]; do
        case $1 in
            -d|--data-dir)
                DEFAULT_DATA_DIR="$2"
                POSTGRES_DATA_DIR="${DEFAULT_DATA_DIR}/postgres"
                shift 2
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
}

# Main execution
main() {
    echo "🚀 Starting Congen minikube deployment with persistent storage..."
    echo ""
    
    # Parse command line arguments
    parse_arguments "$@"
    
    # Display configuration
    print_status "Configuration:"
    print_status "  Data directory: ${DEFAULT_DATA_DIR}"
    print_status "  PostgreSQL data: ${POSTGRES_DATA_DIR}"
    echo ""
    
    # Execute steps
    check_prerequisites
    setup_local_storage
    create_postgres_with_local_storage
    wait_for_postgres
    deploy_application
    update_backend_config
    setup_port_forwarding
    verify_services
    display_access_info
    
    print_success "Congen minikube deployment completed successfully!"
}

# Run main function
main "$@"
