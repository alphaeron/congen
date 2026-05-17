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
MOUNT_DIRECTORY=""

# Function to display usage
usage() {
    cat << EOF
Usage: $0 [OPTIONS]

Perform staged deployment of Congen application.

OPTIONS:
    -e, --environment ENV   Environment name (REQUIRED: local, local-persist, staging, production)
    -u, --keycloak-url URL  Keycloak URL for bootstrap (default: http://localhost:8080)
    --stage STAGE           Deploy specific stage only (1-10)
    -m, --mount-dir DIR     Mount directory for persistent storage (local-persist only)
    -h, --help              Show this help message

EXAMPLES:
    $0 -e local                    # Full deployment for local environment
    $0 -e local-persist -m /path/to/data  # Local deployment with persistent storage
    $0 -e staging -u https://keycloak.staging.example.com  # Staging deployment
    $0 -e local --stage 1          # Deploy only Stage 1 (Namespace)
    $0 -e local --stage 2          # Deploy only Stage 2 (Service Account)
    $0 -e local --stage 5          # Deploy only Stage 5 (Database Migrations)

EOF
}

# Parse command line arguments
while getopts "e:u:m:h-:" opt; do
    case ${opt} in
        e) ENVIRONMENT="${OPTARG}" ;;
        u) KEYCLOAK_URL="${OPTARG}" ;;
        m) MOUNT_DIRECTORY="${OPTARG}" ;;
        h) usage; exit 0 ;;
        -)
            case "${OPTARG}" in
                environment) ENVIRONMENT="${!OPTIND}"; OPTIND=$((OPTIND + 1)) ;;
                keycloak-url) KEYCLOAK_URL="${!OPTIND}"; OPTIND=$((OPTIND + 1)) ;;
                mount-dir) MOUNT_DIRECTORY="${!OPTIND}"; OPTIND=$((OPTIND + 1)) ;;
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
        local-persist) KEYCLOAK_URL="http://localhost:8080" ;;
        staging) KEYCLOAK_URL="https://keycloak.staging.congen.com" ;;
        production) KEYCLOAK_URL="https://keycloak.congen.com" ;;
        *) echo "Unknown environment: ${ENVIRONMENT}" >&2; exit 1 ;;
    esac
fi

# Validate required arguments
if [[ -z "${ENVIRONMENT}" ]]; then
    echo -e "${RED}[ERROR]${NC} Environment is required. Use -e or --environment to specify it (local, local-persist, staging, production)."
    usage
    exit 1
fi

# Validate mount directory for local-persist environment
if [[ "${ENVIRONMENT}" == "local-persist" && -z "${MOUNT_DIRECTORY}" ]]; then
    echo -e "${RED}[ERROR]${NC} Mount directory is required for local-persist environment. Use -m or --mount-dir to specify it."
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

# Function to setup minikube mount for local-persist
setup_minikube_mount() {
    if [[ "${ENVIRONMENT}" == "local-persist" && -n "${MOUNT_DIRECTORY}" ]]; then
        print_status "Setting up minikube mount for local-persist..."
        
        # Check if the mount is already active by looking for the process
        if ! pgrep -f "minikube mount.*${MOUNT_DIRECTORY}" > /dev/null; then
            print_status "Mounting ${MOUNT_DIRECTORY} to /host${MOUNT_DIRECTORY} in minikube with uid=999, gid=999..."
            # Start the mount in the background with correct ownership
            nohup minikube mount "${MOUNT_DIRECTORY}:/host${MOUNT_DIRECTORY}" --uid=999 --gid=999 > /dev/null 2>&1 &
            sleep 2  # Give it a moment to establish the mount
            print_success "Minikube mount established with correct ownership"
        else
            print_success "Minikube mount already active"
        fi
    fi
}

# Function to generate postgres mount patch for local-persist
generate_postgres_mount_patch() {
    if [[ "${ENVIRONMENT}" == "local-persist" && -n "${MOUNT_DIRECTORY}" ]]; then
        print_status "Generating postgres mount patch for local-persist..."
        
        # Create the postgres deployment with hostPath mount
        # Note: The directory will be mounted using minikube mount command
        cat > "k8s/overlays/local-persist/stage-5/postgres-deployment.yaml" << EOF
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: congen
  labels:
    app: postgres
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      securityContext:
        runAsNonRoot: true
        runAsUser: 999
        fsGroup: 999
      containers:
      - name: postgres
        image: postgres:15-alpine
        securityContext:
          runAsNonRoot: true
          runAsUser: 999
          allowPrivilegeEscalation: false
          capabilities:
            drop:
            - ALL
        ports:
        - containerPort: 5432
        env:
        - name: POSTGRES_DB
          valueFrom:
            secretKeyRef:
              name: congen-secret
              key: PGDATABASE
        - name: POSTGRES_USER
          valueFrom:
            secretKeyRef:
              name: congen-secret
              key: PGUSER
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: congen-secret
              key: PGPASSWORD
        - name: PGDATA
          value: /var/lib/postgresql/data/pgdata
        - name: POSTGRES_INITDB_ARGS
          value: "--auth-host=scram-sha-256 --auth-local=trust"
        volumeMounts:
        - name: postgres-storage
          mountPath: /var/lib/postgresql/data
        - name: postgres-config
          mountPath: /etc/postgresql/postgresql.conf
          subPath: postgresql.conf
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
      - name: postgres-storage
        hostPath:
          path: /host${MOUNT_DIRECTORY}
          type: Directory
      - name: postgres-config
        configMap:
          name: postgres-config
EOF
        
        print_success "Postgres deployment generated: k8s/overlays/local-persist/stage-5/postgres-deployment.yaml"
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
    
    # Setup minikube mount for local-persist environment
    setup_minikube_mount
    
    # Generate postgres mount patch for local-persist environment
    generate_postgres_mount_patch

    print_status "Generating migrations ConfigMap from Liquibase files..."
    if ! ./gradlew :backend:createMigrationsConfigMap -q; then
        print_error "Failed to generate migrations ConfigMap"
        exit 1
    fi
    
    print_status "Deploying database migrations to Kubernetes..."
    if kubectl apply -k "k8s/overlays/${ENVIRONMENT}/stage-5" --server-side --force-conflicts --field-manager=congen-deploy; then
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

# Function to process and display jq-extracted list items
# Usage: process_jq_list <json_response_var> <jq_expression> <error_message>
# Example: process_jq_list "${realms_response}" '.[].realm' "(none or error reading realms)"
process_jq_list() {
    local json_response="$1"
    local jq_expression="$2"
    local error_message="$3"
    
    local jq_output
    jq_output=$(echo "${json_response}" | jq -r "${jq_expression}" 2>/dev/null || true)
    if [[ -n "${jq_output}" ]]; then
        echo "${jq_output}" | while read -r item; do
            if [[ -n "${item}" ]]; then
                print_status "  - ${item}"
            fi
        done || print_status "  ${error_message}"
    else
        print_status "  ${error_message}"
    fi
}

# Function to extract a value from a tfvars file
# Usage: get_tfvars_value <file_path> <key_pattern> <default_value>
# Example: get_tfvars_value "${tfvars_file}" "^admin_username" "admin"
# Returns: The extracted value or the default value if not found
get_tfvars_value() {
    local file_path="$1"
    local key_pattern="$2"
    local default_value="$3"
    
    if [[ ! -f "${file_path}" ]]; then
        echo "${default_value}"
        return
    fi
    
    local grep_output
    grep_output=$(grep "${key_pattern}" "${file_path}" 2>/dev/null || true)
    if [[ -n "${grep_output}" ]]; then
        local cut_output
        cut_output=$(echo "${grep_output}" | cut -d'=' -f2 2>/dev/null || true)
        if [[ -n "${cut_output}" ]]; then
            local trimmed_value
            trimmed_value=$(echo "${cut_output}" | tr -d ' "' 2>/dev/null || echo "${default_value}")
            if [[ -n "${trimmed_value}" ]]; then
                echo "${trimmed_value}"
            else
                echo "${default_value}"
            fi
        else
            echo "${default_value}"
        fi
    else
        echo "${default_value}"
    fi
}

# Function to log existing Keycloak resources
log_existing_keycloak_resources() {
    print_status "Querying Keycloak for existing resources..."
    
    # Get admin credentials
    local admin_username
    local admin_password
    local tfvars_file
    
    if [[ "${ENVIRONMENT}" == "local-persist" ]]; then
        tfvars_file="terraform/environments/local/keycloak/terraform.tfvars"
    else
        tfvars_file="terraform/environments/${ENVIRONMENT}/keycloak/terraform.tfvars"
    fi
    
    # Get admin credentials - for local environments, prioritize Kubernetes secret
    if [[ "${ENVIRONMENT}" == "local" || "${ENVIRONMENT}" == "local-persist" ]]; then
        if command -v kubectl &> /dev/null; then
            base64_flag="-d"
            uname_output=$(uname 2>/dev/null || true)
            if [[ "${uname_output}" == "Darwin" ]]; then
                base64_flag="-D"
            fi
            admin_username=$(kubectl get secret keycloak-secret -n congen -o jsonpath='{.data.KC_BOOTSTRAP_ADMIN_USERNAME}' 2>/dev/null | base64 "${base64_flag}" 2>/dev/null || true)
            admin_password=$(kubectl get secret keycloak-secret -n congen -o jsonpath='{.data.KC_BOOTSTRAP_ADMIN_PASSWORD}' 2>/dev/null | base64 "${base64_flag}" 2>/dev/null || true)
        fi
    fi
    
    # Fallback to tfvars if not from K8s
    if [[ -z "${admin_username}" && -f "${tfvars_file}" ]]; then
        admin_username=$(get_tfvars_value "${tfvars_file}" "^admin_username" "admin")
    fi
    if [[ -z "${admin_username}" ]]; then
        admin_username="admin"
    fi
    
    if [[ -z "${admin_password}" && -f "${tfvars_file}" ]]; then
        admin_password=$(get_tfvars_value "${tfvars_file}" "^admin_password" "")
    fi
    
    if [[ -z "${admin_password}" ]]; then
        print_warning "Cannot get admin password, skipping Keycloak resource logging"
        return
    fi
    
    # Get admin token
    local token_response
    token_response=$(curl -s -X POST \
        -H "Content-Type: application/x-www-form-urlencoded" \
        -d "username=${admin_username}&password=${admin_password}&grant_type=password&client_id=admin-cli" \
        "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" 2>/dev/null || echo "")
    
    local access_token
    access_token=$(echo "${token_response}" | jq -r '.access_token' 2>/dev/null || echo "")
    
    if [[ "${access_token}" == "null" || -z "${access_token}" ]]; then
        print_warning "Failed to get admin token, skipping Keycloak resource logging"
        return
    fi
    
    # List all realms
    print_status "Listing existing realms in Keycloak..."
    local realms_response
    realms_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${access_token}" \
        -H "Content-Type: application/json" \
        "${KEYCLOAK_URL}/admin/realms" 2>/dev/null || echo "[]")
    
    print_status "Existing realms:"
    process_jq_list "${realms_response}" '.[].realm' "(none or error reading realms)"
    
    # List roles in congen realm
    print_status "Listing existing roles in 'congen' realm..."
    local roles_response
    roles_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${access_token}" \
        -H "Content-Type: application/json" \
        "${KEYCLOAK_URL}/admin/realms/congen/roles" 2>/dev/null || echo "[]")
    
    print_status "Existing roles in 'congen' realm:"
    process_jq_list "${roles_response}" '.[].name' "(none or error reading roles)"
    
    # List clients in congen realm
    print_status "Listing existing clients in 'congen' realm..."
    local clients_response
    clients_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${access_token}" \
        -H "Content-Type: application/json" \
        "${KEYCLOAK_URL}/admin/realms/congen/clients" 2>/dev/null || echo "[]")
    
    print_status "Existing clients in 'congen' realm:"
    process_jq_list "${clients_response}" '.[].clientId' "(none or error reading clients)"
    
    # List users in congen realm
    print_status "Listing existing users in 'congen' realm..."
    local users_response
    users_response=$(curl -s -X GET \
        -H "Authorization: Bearer ${access_token}" \
        -H "Content-Type: application/json" \
        "${KEYCLOAK_URL}/admin/realms/congen/users" 2>/dev/null || echo "[]")
    
    print_status "Existing users in 'congen' realm:"
    process_jq_list "${users_response}" '.[] | "\(.username) (\(.email // "no email"))"' "(none or error reading users)"
}

# Function to import a Keycloak resource into Terraform state
# Usage: import_keycloak_resource <state_pattern> <terraform_resource> <import_id> <resource_name>
#   state_pattern: Pattern to check in terraform state list (e.g., "module.keycloak.keycloak_role.admin_role")
#   terraform_resource: Full Terraform resource path (e.g., "module.keycloak.keycloak_role.admin_role")
#   import_id: Import ID (e.g., "${realm_name}/${resource_id}")
#   resource_name: Human-readable name for logging (e.g., "admin role")
import_keycloak_resource() {
    local state_pattern="$1"
    local terraform_resource="$2"
    local import_id="$3"
    local resource_name="$4"
    
    terraform_state_list=$(terraform state list 2>/dev/null || true)
    if ! echo "${terraform_state_list}" | grep -q "${state_pattern}"; then
        if [[ -n "${import_id}" && "${import_id}" != "null" ]]; then
            print_status "Importing ${resource_name}..."
            local import_output
            if import_output=$(terraform import "${terraform_resource}" "${import_id}" 2>&1); then
                print_success "Imported ${resource_name}"
            else
                print_warning "${resource_name} import failed: ${import_output}"
            fi
        fi
    fi
}

# Function to apply Terraform
apply_terraform() {
    print_step "7" "Bootstrapping Keycloak and Applying Terraform Configuration"
    
    # Bootstrap Keycloak first
    print_status "Bootstrapping Keycloak for Terraform..."
    bootstrap_keycloak
    
    # Log existing Keycloak resources for debugging
    log_existing_keycloak_resources
    
    if [[ "${ENVIRONMENT}" == "local-persist" ]]; then
        local terraform_dir="terraform/environments/local/keycloak"
    else
        local terraform_dir="terraform/environments/${ENVIRONMENT}/keycloak"
    fi
    
    print_status "Initializing Terraform..."
    cd "${terraform_dir}" || exit
    if terraform init; then
        print_success "Terraform initialized"
    else
        print_error "Terraform initialization failed"
        cd - > /dev/null || exit
        exit 1
    fi
    
    # Check what's in Terraform state
    print_status "Checking Terraform state for existing resources..."
    local realm_name
    realm_name=$(get_tfvars_value "terraform.tfvars" 'realm_name\s*=' "congen")
    if [[ -z "${realm_name}" ]]; then
        realm_name="congen"
    fi
    
    print_status "Resources in Terraform state:"
    terraform_state_list=$(terraform state list 2>/dev/null || true)
    if echo "${terraform_state_list}" | grep -q "module.keycloak.keycloak_realm.congen"; then
        print_status "  ✓ Realm 'congen' is in state"
    else
        print_status "  ✗ Realm 'congen' is NOT in state"
    fi
    
    if echo "${terraform_state_list}" | grep -q "module.keycloak.keycloak_role.admin_role"; then
        print_status "  ✓ Role 'admin' is in state"
    else
        print_status "  ✗ Role 'admin' is NOT in state"
    fi
    
    if echo "${terraform_state_list}" | grep -q "module.keycloak.keycloak_role.service_role"; then
        print_status "  ✓ Role 'service' is in state"
    else
        print_status "  ✗ Role 'service' is NOT in state"
    fi
    
    if echo "${terraform_state_list}" | grep -q "module.keycloak.keycloak_openid_client.backend_client"; then
        print_status "  ✓ Client 'congen-backend' is in state"
    else
        print_status "  ✗ Client 'congen-backend' is NOT in state"
    fi
    
    if echo "${terraform_state_list}" | grep -q "module.keycloak.keycloak_openid_client.frontend_client"; then
        print_status "  ✓ Client 'congen-frontend' is in state"
    else
        print_status "  ✗ Client 'congen-frontend' is NOT in state"
    fi
    
    # Import realm if not in state (simple case - no UUID lookup needed)
    terraform_state_list=$(terraform state list 2>/dev/null || true)
    if ! echo "${terraform_state_list}" | grep -q "module.keycloak.keycloak_realm.congen"; then
        print_status "Importing realm '${realm_name}' into Terraform state..."
        local import_output
        if import_output=$(terraform import "module.keycloak.keycloak_realm.congen" "${realm_name}" 2>&1); then
            print_success "Successfully imported realm"
        else
            print_warning "Realm import failed: ${import_output}"
        fi
    fi
    
    # Auto-import all existing resources from Keycloak
    print_status "Auto-importing existing resources from Keycloak..."
    local tfvars_file
    if [[ "${ENVIRONMENT}" == "local-persist" ]]; then
        tfvars_file="terraform/environments/local/keycloak/terraform.tfvars"
    else
        tfvars_file="terraform/environments/${ENVIRONMENT}/keycloak/terraform.tfvars"
    fi
    
    local admin_username=""
    local admin_password=""
    
    if [[ -f "${tfvars_file}" ]]; then
        admin_username=$(get_tfvars_value "${tfvars_file}" "^admin_username" "")
        admin_password=$(get_tfvars_value "${tfvars_file}" "^admin_password" "")
    fi
    
    if [[ "${ENVIRONMENT}" == "local" || "${ENVIRONMENT}" == "local-persist" ]]; then
        if command -v kubectl &> /dev/null; then
            base64_flag="-d"
            uname_output=$(uname 2>/dev/null || true)
            if [[ "${uname_output}" == "Darwin" ]]; then
                base64_flag="-D"
            fi
            if [[ -z "${admin_username}" ]]; then
                admin_username=$(kubectl get secret keycloak-secret -n congen -o jsonpath='{.data.KC_BOOTSTRAP_ADMIN_USERNAME}' 2>/dev/null | base64 "${base64_flag}" 2>/dev/null || true)
            fi
            if [[ -z "${admin_password}" ]]; then
                admin_password=$(kubectl get secret keycloak-secret -n congen -o jsonpath='{.data.KC_BOOTSTRAP_ADMIN_PASSWORD}' 2>/dev/null | base64 "${base64_flag}" 2>/dev/null || true)
            fi
        fi
    fi
    
    if [[ -n "${admin_password}" ]]; then
        local token_response
        token_response=$(curl -s -X POST \
            -H "Content-Type: application/x-www-form-urlencoded" \
            -d "username=${admin_username}&password=${admin_password}&grant_type=password&client_id=admin-cli" \
            "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" 2>/dev/null || echo "")
        local access_token
        access_token=$(echo "${token_response}" | jq -r '.access_token' 2>/dev/null || echo "")
        
        if [[ -n "${access_token}" && "${access_token}" != "null" ]]; then
            print_status "Successfully obtained admin token for auto-import"
            
            local roles_response
            roles_response=$(curl -s -X GET \
                -H "Authorization: Bearer ${access_token}" \
                -H "Content-Type: application/json" \
                "${KEYCLOAK_URL}/admin/realms/${realm_name}/roles" 2>/dev/null || echo "[]")
            
            local clients_response
            clients_response=$(curl -s -X GET \
                -H "Authorization: Bearer ${access_token}" \
                -H "Content-Type: application/json" \
                "${KEYCLOAK_URL}/admin/realms/${realm_name}/clients" 2>/dev/null || echo "[]")
            
            local users_response
            users_response=$(curl -s -X GET \
                -H "Authorization: Bearer ${access_token}" \
                -H "Content-Type: application/json" \
                "${KEYCLOAK_URL}/admin/realms/${realm_name}/users" 2>/dev/null || echo "[]")
            
            local backend_client_id="congen-backend"
            local frontend_client_id="congen-frontend"
            if [[ -f "${tfvars_file}" ]]; then
                local tfvars_backend_id
                local tfvars_frontend_id
                tfvars_backend_id=$(get_tfvars_value "${tfvars_file}" "^backend_client_id" "")
                tfvars_frontend_id=$(get_tfvars_value "${tfvars_file}" "^frontend_client_id" "")
                if [[ -n "${tfvars_backend_id}" ]]; then
                    backend_client_id="${tfvars_backend_id}"
                fi
                if [[ -n "${tfvars_frontend_id}" ]]; then
                    frontend_client_id="${tfvars_frontend_id}"
                fi
            fi
            
            local admin_role_id
            admin_role_id=$(echo "${roles_response}" | jq -r ".[] | select(.name == \"admin\") | .id" 2>/dev/null || echo "")
            if [[ -n "${admin_role_id}" && "${admin_role_id}" != "null" ]]; then
                import_keycloak_resource \
                    "module.keycloak.keycloak_role.admin_role" \
                    "module.keycloak.keycloak_role.admin_role" \
                    "${realm_name}/${admin_role_id}" \
                    "admin role"
            fi
            
            local service_role_id
            service_role_id=$(echo "${roles_response}" | jq -r ".[] | select(.name == \"service\") | .id" 2>/dev/null || echo "")
            if [[ -n "${service_role_id}" && "${service_role_id}" != "null" ]]; then
                import_keycloak_resource \
                    "module.keycloak.keycloak_role.service_role" \
                    "module.keycloak.keycloak_role.service_role" \
                    "${realm_name}/${service_role_id}" \
                    "service role"
            fi
            
            local backend_client_uuid
            backend_client_uuid=$(echo "${clients_response}" | jq -r ".[] | select(.clientId == \"${backend_client_id}\") | .id" 2>/dev/null || echo "")
            if [[ -n "${backend_client_uuid}" && "${backend_client_uuid}" != "null" ]]; then
                import_keycloak_resource \
                    "module.keycloak.keycloak_openid_client.backend_client" \
                    "module.keycloak.keycloak_openid_client.backend_client" \
                    "${realm_name}/${backend_client_uuid}" \
                    "backend client"
            fi
            
            local frontend_client_uuid=""
            frontend_client_uuid=$(echo "${clients_response}" | jq -r ".[] | select(.clientId == \"${frontend_client_id}\") | .id" 2>/dev/null || echo "")
            if [[ -n "${frontend_client_uuid}" && "${frontend_client_uuid}" != "null" ]]; then
                import_keycloak_resource \
                    "module.keycloak.keycloak_openid_client.frontend_client" \
                    "module.keycloak.keycloak_openid_client.frontend_client" \
                    "${realm_name}/${frontend_client_uuid}" \
                    "frontend client"
                
                terraform_state_list=$(terraform state list 2>/dev/null || true)
                if ! echo "${terraform_state_list}" | grep -q "module.keycloak.keycloak_openid_audience_protocol_mapper.frontend_to_backend_audience_mapper"; then
                    local mappers_response
                    mappers_response=$(curl -s -X GET \
                        -H "Authorization: Bearer ${access_token}" \
                        -H "Content-Type: application/json" \
                        "${KEYCLOAK_URL}/admin/realms/${realm_name}/clients/${frontend_client_uuid}/protocol-mappers/models" 2>/dev/null || echo "[]")
                    
                    local mapper_id
                    mapper_id=$(echo "${mappers_response}" | jq -r ".[] | select(.name == \"frontend-to-backend-audience-mapper\") | .id" 2>/dev/null || echo "")
                    if [[ -n "${mapper_id}" && "${mapper_id}" != "null" ]]; then
                        import_keycloak_resource \
                            "module.keycloak.keycloak_openid_audience_protocol_mapper.frontend_to_backend_audience_mapper" \
                            "module.keycloak.keycloak_openid_audience_protocol_mapper.frontend_to_backend_audience_mapper" \
                            "${realm_name}/client/${frontend_client_uuid}/${mapper_id}" \
                            "protocol mapper"
                    fi
                fi
            fi
            
            local admin_email="admin@congen.com"
            if [[ -f "${tfvars_file}" ]]; then
                local tfvars_email
                tfvars_email=$(get_tfvars_value "${tfvars_file}" "^admin_email" "")
                if [[ -n "${tfvars_email}" ]]; then
                    admin_email="${tfvars_email}"
                fi
            fi
            
            local user_uuid
            user_uuid=$(echo "${users_response}" | jq -r ".[] | select(.email == \"${admin_email}\") | .id" 2>/dev/null || echo "")
            if [[ -n "${user_uuid}" && "${user_uuid}" != "null" ]]; then
                import_keycloak_resource \
                    "module.keycloak.keycloak_user.admin_user" \
                    "module.keycloak.keycloak_user.admin_user" \
                    "${realm_name}/${user_uuid}" \
                    "admin user"
            fi
        else
            print_warning "Could not get admin token for auto-import, will proceed without importing existing resources"
        fi
    else
        print_warning "Could not get admin password for auto-import, will proceed without importing existing resources"
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

        local apply_output
        apply_output=$(terraform apply -auto-approve 2>&1)
        local apply_exit_code=$?
        
        if [[ ${apply_exit_code} -eq 0 ]]; then
            print_success "Terraform applied successfully"
            export TERRAFORM_NO_CHANGES=false
        elif echo "${apply_output}" | grep -q "409 Conflict"; then
            print_error "Terraform apply failed: Resources already exist in Keycloak but not in Terraform state"
            print_error "This typically happens when Terraform state was lost. You need to either:"
            print_error "1. Manually import existing resources using: terraform import <resource> <id>"
            print_error "2. Or delete the existing resources from Keycloak and let Terraform recreate them"
            print_error ""
            print_error "Failed resources:"
            echo "${apply_output}" | grep -A 2 "409 Conflict" || true
            cd - > /dev/null || exit
            exit 1
        else
            print_error "Terraform apply failed"
            echo "${apply_output}"
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
        if kubectl get pod keycloak-0 -n congen -o jsonpath='{.status.phase}' 2>/dev/null | grep -q "Running" || true; then
            # Use a temporary port-forward to test if Keycloak is responding
            local temp_port=$((8080 + keycloak_attempt % 100 + 1000))  # Use different port each attempt
            kubectl port-forward -n congen service/keycloak "${temp_port}":8080 > /dev/null 2>&1 &
            local temp_pid=$!
            sleep 2
            
            # Test if Keycloak is responding
            if curl -s --connect-timeout 3 "http://localhost:${temp_port}/realms/master/" > /dev/null 2>&1; then
                kill "${temp_pid}" 2>/dev/null || true
                print_success "Keycloak is fully ready"
                break
            fi
            
            kill "${temp_pid}" 2>/dev/null || true
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