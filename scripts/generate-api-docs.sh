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
DOCS_DIR="docs"
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
    kubectl port-forward -n congen service/congen 8080:8080 > /dev/null 2>&1 &
    PORT_FORWARD_PID=$!
    
    # Wait for port-forwarding to be ready
    sleep 3
    
    # Test if port-forwarding is working
    if curl -s http://localhost:8080/api/v1/health/ > /dev/null; then
        print_success "Port-forwarding is working"
    else
        print_warning "Port-forwarding may not be working properly"
    fi
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
    
    if [[ ${kubernetes_available} -eq 0 ]]; then
        # Use port-forwarding for Kubernetes
        curl -s http://localhost:8080/api/v1/health/ > /dev/null
        return $?
    else
        curl -s http://localhost:8080/api/v1/health/ > /dev/null
        return $?
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
        setup_port_forwarding
        local health_check_result
        check_application_health
        health_check_result=$?
        
        if [[ ${health_check_result} -eq 0 ]]; then
            print_success "Application is healthy via port-forwarding"
            return 0
        else
            print_error "Application is not healthy via port-forwarding"
            return 1
        fi
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

    if curl -s http://localhost:8080/api/v1/api-docs > "${OPENAPI_JSON_FILE}"; then
        print_success "OpenAPI JSON generated: ${OPENAPI_JSON_FILE}"
    else
        print_error "Failed to generate OpenAPI JSON"
        return 1
    fi
}

# Function to generate OpenAPI YAML
generate_openapi_yaml() {
    print_status "Generating OpenAPI YAML specification..."

    if curl -s http://localhost:8080/api/v1/api-docs.yaml > "${OPENAPI_YAML_FILE}"; then
        print_success "OpenAPI YAML generated: ${OPENAPI_YAML_FILE}"
    else
        print_warning "OpenAPI YAML generation failed (endpoint may not be available)"
    fi
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

- **Swagger UI**: [http://localhost:8080/api/v1/swagger-ui.html](http://localhost:8080/api/v1/swagger-ui.html)
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

- **Swagger UI**: [http://localhost:8080/api/v1/swagger-ui.html](http://localhost:8080/api/v1/swagger-ui.html)
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

- **Interactive API Docs**: http://localhost:8080/api/v1/swagger-ui.html (when running)
- **OpenAPI JSON**: http://localhost:8080/api/v1/api-docs
- **OpenAPI YAML**: http://localhost:8080/api/v1/api-docs.yaml

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
    print_status "Interactive documentation available at: http://localhost:8080/api/v1/swagger-ui.html"
}

# Run main function
main "$@"
