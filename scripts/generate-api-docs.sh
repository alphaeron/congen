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

# Function to create docs directory structure
create_docs_structure() {
    print_status "Creating documentation directory structure..."

    mkdir -p "${DOCS_DIR}"
    mkdir -p "${SWAGGER_UI_DIR}"

    print_success "Documentation directory structure created"
}

# Function to start the application in background
start_application() {
    print_status "Starting application for documentation generation..."

    # Build the application
    print_status "Building application..."
    ./gradlew build -x test -x integrationTest

    # Start the application in background
    print_status "Starting application..."
    ./gradlew bootRun > /dev/null 2>&1 &
    APP_PID=$!

    # Wait for application to start
    print_status "Waiting for application to start..."
    sleep 30

    # Check if application is running
    if ! curl -s http://localhost:8080/actuator/health > /dev/null; then
        print_error "Application failed to start"
        kill "${APP_PID}" 2>/dev/null || true
        exit 1
    fi

    print_success "Application started successfully (PID: ${APP_PID})"
}

# Function to generate OpenAPI JSON
generate_openapi_json() {
    print_status "Generating OpenAPI JSON specification..."

    if curl -s http://localhost:8080/api-docs > "${OPENAPI_JSON_FILE}"; then
        print_success "OpenAPI JSON generated: ${OPENAPI_JSON_FILE}"
    else
        print_error "Failed to generate OpenAPI JSON"
        return 1
    fi
}

# Function to generate OpenAPI YAML
generate_openapi_yaml() {
    print_status "Generating OpenAPI YAML specification..."

    if curl -s http://localhost:8080/api-docs.yaml > "${OPENAPI_YAML_FILE}"; then
        print_success "OpenAPI YAML generated: ${OPENAPI_YAML_FILE}"
    else
        print_warning "OpenAPI YAML generation failed (endpoint may not be available)"
    fi
}

# Function to generate markdown documentation
generate_markdown_docs() {
    print_status "Generating markdown documentation..."

    cat > "${API_DOCS_FILE}" << 'EOF'
# Congen API Documentation

This documentation is automatically generated from the Spring Boot application source code.

## Overview

The Congen API provides endpoints for managing workout programs, exercises, users, and preferences using the conjugate method.

## Interactive Documentation

- **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **OpenAPI JSON**: [api-docs.json](api-docs.json)
- **OpenAPI YAML**: [api-docs.yaml](api-docs.yaml)

## API Endpoints

### User Management

#### Create User
- **POST** `/user/`
- **Description**: Creates a new user profile
- **Request Body**: User object with name, age, height, weight
- **Response**: Created user with ID

#### Get User
- **GET** `/user/{id}`
- **Description**: Retrieves a user by ID
- **Parameters**: `id` (path parameter)
- **Response**: User object

#### Get All Users
- **GET** `/user/`
- **Description**: Retrieves all users
- **Response**: Array of user objects

#### Update User
- **PUT** `/user/{id}`
- **Description**: Updates an existing user
- **Parameters**: `id` (path parameter)
- **Request Body**: Updated user object
- **Response**: Updated user object

#### Delete User
- **DELETE** `/user/{id}`
- **Description**: Deletes a user
- **Parameters**: `id` (path parameter)
- **Response**: Deletion confirmation

### Program Management

#### Create Program
- **POST** `/program/`
- **Description**: Creates a new workout program
- **Request Body**: Program object
- **Response**: Created program with ID

#### Get Program
- **GET** `/program/{id}`
- **Description**: Retrieves a program by ID
- **Parameters**: `id` (path parameter)
- **Response**: Program object

#### Get All Programs
- **GET** `/program/`
- **Description**: Retrieves all programs
- **Response**: Array of program objects

#### Update Program
- **PUT** `/program/{id}`
- **Description**: Updates an existing program
- **Parameters**: `id` (path parameter)
- **Request Body**: Updated program object
- **Response**: Updated program object

#### Delete Program
- **DELETE** `/program/{id}`
- **Description**: Deletes a program
- **Parameters**: `id` (path parameter)
- **Response**: Deletion confirmation

### Exercise Management

#### Create Exercise
- **POST** `/exercise/`
- **Description**: Creates a new exercise
- **Request Body**: Exercise object
- **Response**: Created exercise with ID

#### Get Exercise
- **GET** `/exercise/{id}`
- **Description**: Retrieves an exercise by ID
- **Parameters**: `id` (path parameter)
- **Response**: Exercise object

#### Get All Exercises
- **GET** `/exercise/`
- **Description**: Retrieves all exercises
- **Response**: Array of exercise objects

#### Update Exercise
- **PUT** `/exercise/{id}`
- **Description**: Updates an existing exercise
- **Parameters**: `id` (path parameter)
- **Request Body**: Updated exercise object
- **Response**: Updated exercise object

#### Delete Exercise
- **DELETE** `/exercise/{id}`
- **Description**: Deletes an exercise
- **Parameters**: `id` (path parameter)
- **Response**: Deletion confirmation

### User Program Preferences

#### Create User Program Preferences
- **POST** `/user-program-preferences/`
- **Description**: Creates user program preferences
- **Request Body**: UserProgramPreferences object
- **Response**: Created preferences with ID

#### Get User Program Preferences
- **GET** `/user-program-preferences/{userId}`
- **Description**: Retrieves user program preferences
- **Parameters**: `userId` (path parameter)
- **Response**: UserProgramPreferences object

#### Update User Program Preferences
- **PUT** `/user-program-preferences/{id}`
- **Description**: Updates user program preferences
- **Parameters**: `id` (path parameter)
- **Request Body**: Updated preferences object
- **Response**: Updated preferences object

#### Delete User Program Preferences
- **DELETE** `/user-program-preferences/{id}`
- **Description**: Deletes user program preferences
- **Parameters**: `id` (path parameter)
- **Response**: Deletion confirmation

## Data Models

### User
```json
{
  "id": 1,
  "name": "John Doe",
  "age": 30,
  "height": 175.5,
  "weight": 80.0,
  "created_at": "2024-01-01T00:00:00Z",
  "updated_at": "2024-01-01T00:00:00Z"
}
```

### Program
```json
{
  "id": 1,
  "name": "Conjugate Powerlifting Program",
  "description": "A comprehensive powerlifting program using the conjugate method",
  "created_at": "2024-01-01T00:00:00Z",
  "updated_at": "2024-01-01T00:00:00Z"
}
```

### Exercise
```json
{
  "id": 1,
  "name": "Bench Press",
  "description": "Compound upper body pushing exercise",
  "created_at": "2024-01-01T00:00:00Z",
  "updated_at": "2024-01-01T00:00:00Z"
}
```

### UserProgramPreferences
```json
{
  "id": 1,
  "user_id": 1,
  "program_days_per_week": 3,
  "session_time_length": 60,
  "created_at": "2024-01-01T00:00:00Z",
  "updated_at": "2024-01-01T00:00:00Z"
}
```

## Error Responses

### Validation Error (422)
```json
{
  "error": "Validation failed",
  "message": "User age must be between 1 and 150, got: 0",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### Not Found Error (404)
```json
{
  "error": "Not Found",
  "message": "User not found",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

### Internal Server Error (500)
```json
{
  "error": "Internal Server Error",
  "message": "An unexpected error occurred",
  "timestamp": "2024-01-01T00:00:00Z"
}
```

## Validation Rules

### User Validation
- **Name**: Required, non-empty string, max 255 characters
- **Age**: Required, integer between 1 and 150
- **Height**: Required, decimal between 0.01 and 300.0 cm
- **Weight**: Required, decimal between 0.01 and 1000.0 kg

### Program Validation
- **Name**: Required, non-empty string, max 255 characters
- **Description**: Optional string, max 1000 characters

### Exercise Validation
- **Name**: Required, non-empty string, max 255 characters
- **Description**: Optional string, max 1000 characters

### UserProgramPreferences Validation
- **User ID**: Required, must reference existing user
- **Program Days Per Week**: Required, must be 2, 3, or 4
- **Session Time Length**: Required, integer between 15 and 300 minutes

## Rate Limiting

API requests are rate-limited to ensure fair usage. Please implement appropriate retry logic in your applications.

## Authentication

Currently, the API does not require authentication. All endpoints are publicly accessible.

---

*This documentation was automatically generated on $(date)*
EOF

    print_success "Markdown documentation generated: ${API_DOCS_FILE}"
}

# Function to create a simple index file
create_index_file() {
    print_status "Creating documentation index..."

    cat > "${DOCS_DIR}/README.md" << 'EOF'
# Congen API Documentation

Welcome to the Congen API documentation. This directory contains automatically generated documentation for the Conjugate Workout Generator API.

## Files

- **[API Documentation](API_DOCUMENTATION.md)** - Comprehensive API reference
- **[OpenAPI JSON](openapi.json)** - OpenAPI specification in JSON format
- **[OpenAPI YAML](openapi.yaml)** - OpenAPI specification in YAML format

## Interactive Documentation

When the application is running, you can access the interactive Swagger UI at:
- http://localhost:8080/swagger-ui.html

## Regenerating Documentation

To regenerate this documentation, run:

```bash
./scripts/generate-api-docs.sh
```

This script will:
1. Start the application
2. Generate OpenAPI specifications
3. Create markdown documentation
4. Stop the application

## Manual Generation

If you prefer to generate documentation manually:

1. Start the application: `./gradlew bootRun`
2. Access the OpenAPI JSON: http://localhost:8080/api-docs
3. Access the Swagger UI: http://localhost:8080/swagger-ui.html

---

*Documentation generated on $(date)*
EOF

    print_success "Documentation index created: ${DOCS_DIR}/README.md"
}

# Function to stop the application
stop_application() {
    if [[ -n "${APP_PID}" ]]; then
        print_status "Stopping application (PID: ${APP_PID})..."
        kill "${APP_PID}" 2>/dev/null || true
        sleep 5

        # Check if process is still running
        if kill -0 "${APP_PID}" 2>/dev/null; then
            print_warning "Application did not stop gracefully, forcing termination..."
            kill -9 "${APP_PID}" 2>/dev/null || true
        fi

        print_success "Application stopped"
    fi
}

# Function to cleanup on exit
cleanup() {
    stop_application
}

# Set up cleanup trap
trap cleanup EXIT

# Main execution
main() {
    print_status "Starting API documentation generation..."

    check_dependencies
    create_docs_structure
    start_application

    # Generate documentation
    generate_openapi_json
    generate_openapi_yaml
    generate_markdown_docs
    create_index_file

    print_success "API documentation generation completed!"
    print_status "Documentation files created in: ${DOCS_DIR}"
    print_status "Interactive documentation available at: http://localhost:8080/swagger-ui.html"
}

# Run main function
main "$@"
