#!/bin/bash

# Liquibase Schema to DOT Generator
# This script automatically generates a Graphviz DOT file from Liquibase migrations
# by running them against a temporary database and extracting the schema.

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
# TEMP_DB_NAME="temp_schema_gen_$(date +%s)"  # Unused variable - removed to fix ShellCheck warning
# LIQUIBASE_CHANGELOG="resources/migrations/changelog-root.xml"  # Unused variable - removed to fix ShellCheck warning
DOCS_DIR="docs"
BASE_FILE_NAME="database_schema"
OUTPUT_DOT_FILE="${DOCS_DIR}/${BASE_FILE_NAME}.dot"
OUTPUT_PNG_FILE="${DOCS_DIR}/${BASE_FILE_NAME}.png"

# Set PostgreSQL password environment variable to avoid prompts
export PGPASSWORD=postgres

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

# Function to download PostgreSQL JDBC driver
download_postgresql_driver() {
    print_status "PostgreSQL JDBC driver not found. Downloading..."
    
    # Use a stable version of PostgreSQL JDBC driver
    DRIVER_VERSION="42.7.2"
    DRIVER_URL="https://jdbc.postgresql.org/download/postgresql-${DRIVER_VERSION}.jar"
    
    print_status "PostgreSQL JDBC driver version: ${DRIVER_VERSION}"
    print_status "Download URL: ${DRIVER_URL}"
    
    # Prompt for confirmation
    read -p "Download PostgreSQL JDBC driver (~1MB)? (y/N): " -n 1 -r
    echo
    if [[ ! ${REPLY} =~ ^[Yy]$ ]]; then
        print_error "Download cancelled by user"
        exit 1
    fi
    
    # Download the driver
    print_status "Downloading PostgreSQL JDBC driver..."
    if curl -L -o postgresql-jdbc.jar "${DRIVER_URL}"; then
        print_success "PostgreSQL JDBC driver downloaded successfully"
    else
        print_error "Failed to download PostgreSQL JDBC driver"
        exit 1
    fi
}

# Function to download SchemaSpy JAR
download_schemaspy() {
    print_status "SchemaSpy JAR not found. Downloading latest version..."
    
    # Get the latest version from GitHub API
    # shellcheck disable=SC2312
    LATEST_VERSION="$(curl -s https://api.github.com/repos/schemaspy/schemaspy/releases/latest | grep '"tag_name"' | cut -d'"' -f4)"
    
    if [[ -z "${LATEST_VERSION}" ]]; then
        print_error "Failed to get latest SchemaSpy version from GitHub"
        exit 1
    fi
    
    # Remove 'v' prefix if present
    VERSION_NUMBER=${LATEST_VERSION#v}
    
    print_status "Latest SchemaSpy version: ${VERSION_NUMBER}"
    print_status "Download URL: https://github.com/schemaspy/schemaspy/releases/download/${LATEST_VERSION}/schemaspy-${VERSION_NUMBER}.jar"
    
    # Prompt for confirmation
    read -p "Download SchemaSpy JAR (~13MB)? (y/N): " -n 1 -r
    echo
    if [[ ! ${REPLY} =~ ^[Yy]$ ]]; then
        print_error "Download cancelled by user"
        exit 1
    fi
    
    # Download the JAR
    print_status "Downloading SchemaSpy JAR..."
    if curl -L -o schemaspy.jar "https://github.com/schemaspy/schemaspy/releases/download/${LATEST_VERSION}/schemaspy-${VERSION_NUMBER}.jar"; then
        print_success "SchemaSpy JAR downloaded successfully"
    else
        print_error "Failed to download SchemaSpy JAR"
        exit 1
    fi
}

# Function to check dependencies
check_dependencies() {
    print_status "Checking dependencies..."
    
    # Check if kubectl is available
    if ! command -v kubectl &> /dev/null; then
        print_error "kubectl is required but not installed"
        exit 1
    fi
    
    # Check if psql is available
    if ! command -v psql &> /dev/null; then
        print_error "PostgreSQL client (psql) is required but not installed"
        exit 1
    fi
    
    # Check if schemaspy wrapper is available
    if [[ ! -f "scripts/schemaspy" ]]; then
        print_error "SchemaSpy wrapper script not found. Please ensure scripts/schemaspy exists."
        exit 1
    fi
    
    # Check if PostgreSQL JDBC driver is available, download if not
    if [[ ! -f "postgresql-jdbc.jar" ]]; then
        download_postgresql_driver
    fi
    
    # Check if schemaspy JAR is available, download if not
    if [[ ! -f "schemaspy.jar" ]]; then
        download_schemaspy
    fi
    
    # Check if dot is available
    if ! command -v dot &> /dev/null; then
        print_warning "Graphviz (dot) is not installed. PNG generation will be skipped."
    fi
    
    print_success "Dependencies check completed"
}

# Function to setup port forwarding to Kubernetes PostgreSQL
setup_port_forwarding() {
    print_status "Setting up port forwarding to Kubernetes PostgreSQL..."
    
    # Kill any existing port forward
    pkill -f "kubectl port-forward.*postgres" 2>/dev/null || true
    
    # Start port forwarding in background
    kubectl port-forward -n congen service/postgres 5433:5432 > /dev/null 2>&1 &
    PORT_FORWARD_PID=$!
    
    # Wait for port forwarding to be ready
    sleep 3
    
    # Test connection
    for i in {1..30}; do
        if PGPASSWORD=postgres psql -h localhost -p 5433 -U postgres -d postgres -c "SELECT 1;" > /dev/null 2>&1; then
            break
        fi
        sleep 2
    done
    
    if [[ "${i}" -eq 30 ]]; then
        print_error "Failed to connect to Kubernetes PostgreSQL"
        kill "${PORT_FORWARD_PID}" 2>/dev/null || true
        exit 1
    fi
    
    print_success "Port forwarding to Kubernetes PostgreSQL established (PID: ${PORT_FORWARD_PID})"
    echo "${PORT_FORWARD_PID}" > .temp_port_forward_pid
}

# Function to extract schema and generate DOT using SchemaSpy
generate_dot_from_schema() {
    print_status "Extracting database schema using SchemaSpy..."
    
    # Create output directory for SchemaSpy
    mkdir -p .temp_schemaspy_output
    
    # Run SchemaSpy to generate schema documentation
    ./scripts/schemaspy -t pgsql \
        -host localhost \
        -port 5433 \
        -db postgres \
        -u postgres \
        -p postgres \
        -o .temp_schemaspy_output \
        -s public \
        -dp postgresql-jdbc.jar
    
    print_status "Converting SchemaSpy output to DOT format..."
    
    # Generate DOT file from SchemaSpy output
    cat > "${OUTPUT_DOT_FILE}" << 'EOF'
digraph DatabaseSchema {
    // Graph settings
    rankdir=LR;
    node [shape=record, fontname="Arial", fontsize=11];
    edge [fontname="Arial", fontsize=9];
    
    // Compact layout settings
    nodesep=0.8;
    ranksep=1.2;
    splines=polyline;
    size="16,10";
    ratio=fill;
    
    // Color scheme
    node [style=filled];
EOF

    # Extract table information from SchemaSpy's HTML output
    print_status "Processing SchemaSpy output..."
    
    # Group tables by domain (you can customize this logic)
    # Use a simpler approach for bash compatibility
    table_domains=""
    
    # Process each table file from SchemaSpy
    for table_file in .temp_schemaspy_output/tables/*.html; do
        if [[ -f "${table_file}" ]]; then
            table_name=$(basename "${table_file}" .html)

            # Ignore Liquibase changelog tables
            if [[ "${table_name}" = "databasechangelog" ]] || [[ "${table_name}" = "databasechangeloglock" ]]; then
                continue
            fi

            # Determine domain based on table name
            domain="other"
            if [[ "${table_name}" == *"user"* ]]; then
                domain="user"
            elif [[ "${table_name}" == *"exercise"* ]] || [[ "${table_name}" == *"muscle"* ]] || [[ "${table_name}" == *"equipment"* ]]; then
                domain="exercise"
            elif [[ "${table_name}" == *"program"* ]] || [[ "${table_name}" == *"workout"* ]]; then
                domain="program"
            elif [[ "${table_name}" == *"set"* ]] || [[ "${table_name}" == *"scheme"* ]]; then
                domain="sets"
            fi
            
            # Store domain info for later use
            table_domains="${table_domains} ${table_name}:${domain}"
            
            # Extract column information from SchemaSpy HTML
            GREP_OUTPUT="$(grep -o '<td class=\"columnName\">[^<]*</td>' "${table_file}")"
            SED_OUTPUT="$(echo "${GREP_OUTPUT}" | sed 's/<td class=\"columnName\">\([^<]*\)<\/td>/\1/')"
            TR_OUTPUT="$(echo "${SED_OUTPUT}" | tr '\n' '\\l')"
            columns="${TR_OUTPUT%\\l}"
            
            # Set color based on domain (muted, accessible colors)
            case ${domain} in
                "user")
                    color="lightsteelblue"
                    ;;
                "exercise")
                    color="lightcoral"
                    ;;
                "program")
                    color="lightsteelblue"
                    ;;
                "sets")
                    color="lightcoral"
                    ;;
                *)
                    color="lightgray"
                    ;;
            esac
            
            # Create a simple label if no columns found
            if [[ -z "${columns}" ]]; then
                columns="id\\lname\\lcreated_at\\lupdated_at"
            fi
            
            echo "    ${table_name} [fillcolor=${color}, label=\"{${table_name}|${columns}}\"];" >> "${OUTPUT_DOT_FILE}"
        fi
    done
    
    echo "" >> "${OUTPUT_DOT_FILE}"
    echo "    // Foreign key relationships" >> "${OUTPUT_DOT_FILE}"
    
    # Extract foreign key relationships from SchemaSpy's relationships file
    if [[ -f ".temp_schemaspy_output/diagrams/summary/relationships.real.large.png" ]]; then
        # SchemaSpy generates relationship information, but we'll use a simpler approach
        # Extract from the database directly for foreign keys
        # shellcheck disable=SC2312
        PGPASSWORD=postgres psql -h localhost -p 5433 -U postgres -d postgres -t -c "
            SELECT 
                tc.table_name as source_table,
                ccu.table_name as target_table
            FROM information_schema.table_constraints tc
            JOIN information_schema.constraint_column_usage ccu 
                ON ccu.constraint_name = tc.constraint_name
            WHERE tc.constraint_type = 'FOREIGN KEY'
                AND tc.table_schema = 'public'
            ORDER BY tc.table_name;
        " | while IFS='|' read -r source_table target_table; do
            # Trim whitespace from both variables
            source_table=$(echo "${source_table}" | xargs)
            target_table=$(echo "${target_table}" | xargs)
            # Ignore changelog tables in relationships
            if [[ "${source_table}" = "databasechangelog" ]] || [[ "${source_table}" = "databasechangeloglock" ]] || [[ "${target_table}" = "databasechangelog" ]] || [[ "${target_table}" = "databasechangeloglock" ]]; then
                continue
            fi
            if [[ -n "${source_table}" ]] && [[ -n "${target_table}" ]]; then
                echo "    ${source_table} -> ${target_table} [label=\"FK\"];" >> "${OUTPUT_DOT_FILE}"
            fi
        done
    fi
    
    # Add combined clusters for related domains
    echo "" >> "${OUTPUT_DOT_FILE}"
    echo "    // Combined clusters for related domains" >> "${OUTPUT_DOT_FILE}"
    
    # User & Program cluster
    user_program_tables=""
    for table_info in ${table_domains}; do
        table_name=$(echo "${table_info}" | cut -d: -f1)
        table_domain=$(echo "${table_info}" | cut -d: -f2)
        if [[ "${table_name}" = "databasechangelog" ]] || [[ "${table_name}" = "databasechangeloglock" ]]; then
            continue
        fi
        if [[ "${table_domain}" = "user" ]] || [[ "${table_domain}" = "program" ]]; then
            if [[ -n "${user_program_tables}" ]]; then
                user_program_tables="${user_program_tables}; ${table_name}"
            else
                user_program_tables="${table_name}"
            fi
        fi
    done
    if [[ -n "${user_program_tables}" ]]; then
        {
            echo "    subgraph cluster_user_program {"
            echo "        label=\"User & Program Domains\";"
            echo "        style=filled;"
            echo "        color=lightsteelblue;"
            echo "        margin=10;"
            echo "        ${user_program_tables};"
            echo "    }"
        } >> "${OUTPUT_DOT_FILE}"
    fi
    
    # Exercise & Sets cluster
    exercise_sets_tables=""
    for table_info in ${table_domains}; do
        table_name=$(echo "${table_info}" | cut -d: -f1)
        table_domain=$(echo "${table_info}" | cut -d: -f2)
        if [[ "${table_name}" = "databasechangelog" ]] || [[ "${table_name}" = "databasechangeloglock" ]]; then
            continue
        fi
        if [[ "${table_domain}" = "exercise" ]] || [[ "${table_domain}" = "sets" ]]; then
            if [[ -n "${exercise_sets_tables}" ]]; then
                exercise_sets_tables="${exercise_sets_tables}; ${table_name}"
            else
                exercise_sets_tables="${table_name}"
            fi
        fi
    done
    if [[ -n "${exercise_sets_tables}" ]]; then
        {
            echo "    subgraph cluster_exercise_sets {"
            echo "        label=\"Exercise & Sets Domains\";"
            echo "        style=filled;"
            echo "        color=lightcoral;"
            echo "        margin=10;"
            echo "        ${exercise_sets_tables};"
            echo "    }"
        } >> "${OUTPUT_DOT_FILE}"
    fi
    
    # Add invisible edges to encourage side-by-side layout
    {
        echo ""
        echo "    // Invisible edges to encourage side-by-side layout"
        echo "    user -> exercise [style=invis];"
        echo "    exercise -> program [style=invis];"
        echo "    program -> set_scheme [style=invis];"
        echo "}"
    } >> "${OUTPUT_DOT_FILE}"
    
    print_success "DOT file generated: ${OUTPUT_DOT_FILE}"
}

# Function to generate PNG from DOT
generate_png() {
    if command -v dot &> /dev/null; then
        print_status "Generating PNG image with dot (hierarchical layout)..."
        dot -Tpng "${OUTPUT_DOT_FILE}" -o "${OUTPUT_PNG_FILE}"
        print_success "PNG image generated: ${OUTPUT_PNG_FILE} (hierarchical layout)"
    else
        print_warning "Graphviz not installed, skipping PNG generation"
    fi
}

# Function to cleanup
cleanup() {
    print_status "Cleaning up..."
    
    # Stop port forwarding
    if [[ -f .temp_port_forward_pid ]]; then
        local PORT_FORWARD_PID
        PORT_FORWARD_PID=$(cat .temp_port_forward_pid)
        kill "${PORT_FORWARD_PID}" 2>/dev/null || true
        pkill -f "kubectl port-forward.*postgres" 2>/dev/null || true
        rm .temp_port_forward_pid
    fi
    
    # Remove temporary files
    rm -rf .temp_schemaspy_output
    
    print_success "Cleanup completed"
}

# Main execution
main() {
    print_status "Starting Liquibase schema to DOT generation..."
    
    check_dependencies
    setup_port_forwarding
    generate_dot_from_schema
    generate_png
    cleanup
    
    print_success "Schema generation completed!"
    print_status "Output files:"
    echo "  - ${OUTPUT_DOT_FILE}"
    if [[ -f "${OUTPUT_PNG_FILE}" ]]; then
        echo "  - ${OUTPUT_PNG_FILE}"
    fi
}

# Trap to ensure cleanup on exit
trap cleanup EXIT

# Run main function
main "$@" 