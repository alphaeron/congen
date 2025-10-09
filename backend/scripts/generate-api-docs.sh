#!/bin/bash

# API Documentation Generator Script
# This script automatically generates API documentation from the Spring Boot application
# and saves it to the docs directory for version control.

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# Get the backend directory (parent of scripts directory)
BACKEND_DIR="$(dirname "$SCRIPT_DIR")"
# Set docs directory relative to backend directory
DOCS_DIR="${BACKEND_DIR}/docs"
API_DOCS_FILE="${DOCS_DIR}/API_DOCUMENTATION.md"
OPENAPI_JSON_FILE="${DOCS_DIR}/openapi.json"
OPENAPI_YAML_FILE="${DOCS_DIR}/openapi.yaml"
SWAGGER_UI_DIR="${DOCS_DIR}/swagger-ui"

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

# Function to check if required tools are installed
check_dependencies() {
    print_status "Checking dependencies..."

    # Check if curl is available
    if ! command -v curl &> /dev/null; then
        print_error "curl is required but not installed"
        exit 1
    fi

    # Check if jq is available (for JSON processing)
    if ! command -v jq &> /dev/null; then
        print_warning "jq is not installed. JSON processing will be limited."
    fi

    # Check if yq is available (for YAML processing)
    if ! command -v yq &> /dev/null; then
        print_warning "yq is not installed. YAML processing will be limited."
    fi

    print_success "Dependencies check completed"
}

# Function to check if Kubernetes is available
check_kubernetes() {
    if command -v kubectl &> /dev/null; then
        if kubectl get pods -n congen --no-headers &> /dev/null; then
            return 0
        fi
    fi
    return 1
}

# Function to setup port-forwarding
setup_port_forwarding() {
    local kubernetes_available
    check_kubernetes
    kubernetes_available=$?
    [[ ${kubernetes_available} -eq 0 ]] || return 0
    
    print_status "Setting up port-forwarding to Kubernetes service..."
    
    # Kill any existing port-forward processes
    pkill -f "kubectl port-forward" 2>/dev/null || true
    
    # Start port-forwarding in background
    kubectl port-forward -n congen service/backend 8888:8888 > /dev/null 2>&1 &
    PORT_FORWARD_PID=$!
    
    # Wait for port-forwarding to be ready
    sleep 5
    
    # Test if port-forwarding is working with retries
    local retry_count=0
    local max_retries=10
    
    while [[ ${retry_count} -lt ${max_retries} ]]; do
        if curl -s http://localhost:8888/api/v1/health/ > /dev/null; then
            print_success "Port-forwarding is working"
            return 0
        fi
        print_status "Waiting for application to be ready... (attempt ${retry_count}/${max_retries})"
        sleep 2
        retry_count=$((retry_count + 1))
    done
    
    print_warning "Port-forwarding established but application may not be ready yet"
}

# Function to cleanup port-forwarding
cleanup_port_forwarding() {
    if [[ -n "${PORT_FORWARD_PID}" ]]; then
        print_status "Cleaning up port-forwarding..."
        kill "${PORT_FORWARD_PID}" 2>/dev/null || true
        pkill -f "kubectl port-forward" 2>/dev/null || true
        print_success "Port-forwarding cleaned up"
    fi
}

# Function to check if application is running (either local or Kubernetes)
check_application_health() {
    local kubernetes_available
    check_kubernetes
    kubernetes_available=$?
    
    # Try to connect to the application with timeout
    if curl -s --max-time 10 http://localhost:8888/api/v1/health/ > /dev/null; then
        return 0
    else
        return 1
    fi
}

# Function to create docs directory structure
create_docs_structure() {
    print_status "Creating documentation directory structure..."

    mkdir -p "${DOCS_DIR}"
    mkdir -p "${SWAGGER_UI_DIR}"

    print_success "Documentation directory structure created"
}

# Function to check application availability
check_application() {
    print_status "Checking application availability..."

    local kubernetes_available
    check_kubernetes
    kubernetes_available=$?

    if [[ ${kubernetes_available} -eq 0 ]]; then
        print_success "Found Kubernetes deployment"
        
        # Wait for backend pods to be ready (same as deploy script)
        print_status "Waiting for backend pods to be ready..."
        if kubectl wait --for=condition=ready pod -l app=backend -n congen --timeout=300s; then
            print_success "Backend pods are ready"
        else
            print_warning "Backend pods may still be starting up, but continuing..."
        fi
        
        setup_port_forwarding
        
        # Wait for application to be fully ready with health checks
        print_status "Waiting for application to be fully ready..."
        local max_attempts=30
        local attempt=0
        
        while [[ ${attempt} -lt ${max_attempts} ]]; do
            if check_application_health; then
                print_success "Application is healthy via port-forwarding"
                return 0
            fi
            
            attempt=$((attempt + 1))
            print_status "Waiting for application to be ready... (attempt ${attempt}/${max_attempts})"
            sleep 5
        done
        
        print_warning "Application health check failed after ${max_attempts} attempts, but continuing with documentation generation..."
        return 0  # Continue anyway, the app might be starting up
    else
        print_warning "Kubernetes not available, checking local application..."
        local health_check_result
        check_application_health
        health_check_result=$?
        
        if [[ ${health_check_result} -eq 0 ]]; then
            print_success "Local application is healthy"
            return 0
        else
            print_error "Local application is not available"
            return 1
        fi
    fi
}

# Function to generate OpenAPI JSON
generate_openapi_json() {
    print_status "Generating OpenAPI JSON specification..."

    # Try the working endpoint with retries
    local endpoints=("/api/v1/api-docs")
    local retry_count=0
    local max_retries=5
    
    for endpoint in "${endpoints[@]}"; do
        print_status "Trying endpoint: http://localhost:8888${endpoint}"
        
        while [[ ${retry_count} -lt ${max_retries} ]]; do
            # Get HTTP status code and response
            local http_code
            http_code=$(curl -s -w "%{http_code}" --max-time 30 "http://localhost:8888${endpoint}" -o "${OPENAPI_JSON_FILE}")
            
            if [[ ${http_code} -eq 200 ]]; then
                # Check if the file contains valid JSON (not an error page)
                if [[ -s "${OPENAPI_JSON_FILE}" ]] && jq empty "${OPENAPI_JSON_FILE}" 2>/dev/null; then
                    print_success "OpenAPI JSON generated from ${endpoint}: ${OPENAPI_JSON_FILE}"
                    return 0
                else
                    print_warning "Received invalid JSON response from ${endpoint}, retrying... (attempt ${retry_count}/${max_retries})"
                    # Show first few lines for debugging
                    print_status "Response preview: $(head -3 "${OPENAPI_JSON_FILE}" 2>/dev/null || echo 'Empty file')"
                fi
            else
                print_warning "Failed to fetch OpenAPI JSON from ${endpoint} (HTTP ${http_code}), retrying... (attempt ${retry_count}/${max_retries})"
                # Show first few lines for debugging
                print_status "Response preview: $(head -3 "${OPENAPI_JSON_FILE}" 2>/dev/null || echo 'Empty file')"
            fi
            
            retry_count=$((retry_count + 1))
            sleep 5
        done
        
        retry_count=0  # Reset for next endpoint
    done
    
    print_error "Failed to generate OpenAPI JSON from any endpoint after ${max_retries} attempts each"
    print_status "Available endpoints to check:"
    print_status "  - http://localhost:8888/api/v1/api-docs"
    return 1
}

# Function to generate OpenAPI YAML
generate_openapi_yaml() {
    print_status "Generating OpenAPI YAML specification..."

    # Try the working endpoint with retries
    local endpoints=("/api/v1/api-docs.yaml")
    local retry_count=0
    local max_retries=3
    
    for endpoint in "${endpoints[@]}"; do
        print_status "Trying YAML endpoint: http://localhost:8888${endpoint}"
        
        while [[ ${retry_count} -lt ${max_retries} ]]; do
            # Get HTTP status code and response
            local http_code
            http_code=$(curl -s -w "%{http_code}" --max-time 30 "http://localhost:8888${endpoint}" -o "${OPENAPI_YAML_FILE}")
            
            if [[ ${http_code} -eq 200 ]]; then
                # Check if the file contains valid YAML (not an error page)
                if [[ -s "${OPENAPI_YAML_FILE}" ]] && ! grep -q "404\|500\|error" "${OPENAPI_YAML_FILE}"; then
                    print_success "OpenAPI YAML generated from ${endpoint}: ${OPENAPI_YAML_FILE}"
                    return 0
                else
                    print_warning "Received invalid YAML response from ${endpoint}, retrying... (attempt ${retry_count}/${max_retries})"
                    # Show first few lines for debugging
                    print_status "Response preview: $(head -3 "${OPENAPI_YAML_FILE}" 2>/dev/null || echo 'Empty file')"
                fi
            else
                print_warning "Failed to fetch OpenAPI YAML from ${endpoint} (HTTP ${http_code}), retrying... (attempt ${retry_count}/${max_retries})"
                # Show first few lines for debugging
                print_status "Response preview: $(head -3 "${OPENAPI_YAML_FILE}" 2>/dev/null || echo 'Empty file')"
            fi
            
            retry_count=$((retry_count + 1))
            sleep 3
        done
        
        retry_count=0  # Reset for next endpoint
    done
    
    print_warning "OpenAPI YAML generation failed from all endpoints (endpoint may not be available)"
}

# Function to generate markdown documentation from OpenAPI JSON
generate_markdown_docs() {
    print_status "Generating markdown documentation from OpenAPI specification..."

    # Check if jq is available for JSON processing
    if ! command -v jq &> /dev/null; then
        print_warning "jq not available, creating basic markdown documentation"
        cat > "${API_DOCS_FILE}" << 'EOF'
# Congen API Documentation

This documentation is automatically generated from the OpenAPI specification.

## Overview

The Congen API provides endpoints for managing workout programs, exercises, users, and preferences using the conjugate method.

## Interactive Documentation

- **Swagger UI**: [http://localhost:8888/api/v1/swagger-ui.html](http://localhost:8888/api/v1/swagger-ui.html)
- **OpenAPI JSON**: [openapi.json](openapi.json)
- **OpenAPI YAML**: [openapi.yaml](openapi.yaml)

## API Endpoints

For detailed endpoint documentation, please refer to the OpenAPI specification files or use the interactive Swagger UI.

## Data Models

For complete data model definitions, please refer to the OpenAPI specification files.

## Error Responses

The API uses standard HTTP status codes and returns error responses in JSON format.

---

*This documentation was automatically generated on $(date)*
EOF
        print_success "Basic markdown documentation generated: ${API_DOCS_FILE}"
        return
    fi

    # Generate comprehensive markdown from OpenAPI JSON
    cat > "${API_DOCS_FILE}" << 'EOF'
# Congen API Documentation

This documentation is automatically generated from the OpenAPI specification.

## Overview

EOF

    # Extract API info
    if [[ -f "${OPENAPI_JSON_FILE}" ]]; then
        # Get API title and description
        API_TITLE=$(jq -r '.info.title // "Congen API"' "${OPENAPI_JSON_FILE}")
        API_DESCRIPTION=$(jq -r '.info.description // "API for Conjugate Workout Generator"' "${OPENAPI_JSON_FILE}")
        API_VERSION=$(jq -r '.info.version // "1.0.0"' "${OPENAPI_JSON_FILE}")

        {
            echo "**${API_TITLE}** - ${API_DESCRIPTION}"
            echo "**Version:** ${API_VERSION}"
            echo ""
        } >> "${API_DOCS_FILE}"

        # Add interactive documentation links
        cat >> "${API_DOCS_FILE}" << 'EOF'
## Interactive Documentation

- **Swagger UI**: [http://localhost:8888/api/v1/swagger-ui.html](http://localhost:8888/api/v1/swagger-ui.html)
- **OpenAPI JSON**: [openapi.json](openapi.json)
- **OpenAPI YAML**: [openapi.yaml](openapi.yaml)

## API Endpoints

EOF

        # Extract and format endpoints
        jq -r '.paths | to_entries[] | "### \(.key)\n\n" + (.value | to_entries[] | "#### \(.key | ascii_upcase) \(.key)\n- **Path**: `\(.key)`\n- **Description**: \(.value.description // "No description available")\n")' "${OPENAPI_JSON_FILE}" >> "${API_DOCS_FILE}" 2>/dev/null || true

        # Add data models section
        {
            echo ""
            echo "## Data Models"
            echo ""
        } >> "${API_DOCS_FILE}"

        # Extract schemas
        if ! jq -r '.components.schemas | to_entries[] | "### \(.key)\n\n```json\n\(.value | tojson)\n```\n"' "${OPENAPI_JSON_FILE}" >> "${API_DOCS_FILE}" 2>/dev/null; then
            {
                echo "No schemas found in OpenAPI specification"
            } >> "${API_DOCS_FILE}"
        fi

        # Add error responses section
        {
            echo ""
            echo "## Error Responses"
            echo ""
            echo "The API uses standard HTTP status codes:"
            echo ""
        } >> "${API_DOCS_FILE}"
        {
            echo "- **400 Bad Request**: Invalid request format or parameters"
            echo "- **404 Not Found**: Resource not found"
            echo "- **422 Unprocessable Entity**: Validation errors"
            echo "- **500 Internal Server Error**: Server-side errors"
            echo ""
        } >> "${API_DOCS_FILE}"

        # Add footer
        {
            echo "---"
            echo "*This documentation was automatically generated from OpenAPI specification on $(date)*"
        } >> "${API_DOCS_FILE}" || true

        print_success "Comprehensive markdown documentation generated from OpenAPI JSON: ${API_DOCS_FILE}"
    else
        print_error "OpenAPI JSON file not found: ${OPENAPI_JSON_FILE}"
        return 1
    fi
}

# Function to create a simple index file
create_index_file() {
    print_status "Creating documentation index..."

    cat > "${DOCS_DIR}/README.md" << 'EOF'
# Congen API Documentation

This directory contains automatically generated API documentation for the Conjugate Workout Generator.

## Contents

- **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** - Complete API reference with endpoints, models, and examples
- **[openapi.json](openapi.json)** - OpenAPI 3.0 specification (JSON)
- **[openapi.yaml](openapi.yaml)** - OpenAPI 3.0 specification (YAML)
- **[swagger-ui/](swagger-ui/)** - Static Swagger UI files

## Quick Access

- **Interactive API Docs**: http://localhost:8888/api/v1/swagger-ui.html (when running)
- **OpenAPI JSON**: http://localhost:8888/api/v1/api-docs
- **OpenAPI YAML**: http://localhost:8888/api/v1/api-docs.yaml

## Regeneration

```bash
# Using Gradle (recommended)
./gradlew generateApiDocs

# Using script directly
./scripts/generate-api-docs.sh
```

---

*Generated: $(date)*
EOF

    print_success "Documentation index created: ${DOCS_DIR}/README.md"
}

# Function to cleanup on exit
cleanup() {
    cleanup_port_forwarding
}

# Set up cleanup trap
trap cleanup EXIT

# Main execution
main() {
    print_status "Starting API documentation generation..."

    check_dependencies
    create_docs_structure
    check_application

    # Generate documentation
    generate_openapi_json
    generate_openapi_yaml
    generate_markdown_docs
    create_index_file

    print_success "API documentation generation completed!"
    print_status "Documentation files created in: ${DOCS_DIR}"
    print_status "Interactive documentation available at: http://localhost:8888/api/v1/swagger-ui.html"
}

# Run main function
main "$@"
