#!/bin/bash

# PostgreSQL Schema to DOT Generator (Custom, no external tools)
# Generates a detailed Graphviz DOT file and PNG image from a PostgreSQL schema using psql.
# Excludes databasechangelog and databasechangeloglock tables.

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DOCS_DIR="docs"
BASE_FILE_NAME="database_schema"
OUTPUT_DOT_FILE="${DOCS_DIR}/${BASE_FILE_NAME}.dot"
OUTPUT_PNG_FILE="${DOCS_DIR}/${BASE_FILE_NAME}.png"
EXCLUDED="'databasechangelog','databasechangeloglock'"

# Ensure docs directory exists
mkdir -p "${DOCS_DIR}"

# Set PGPASSWORD for non-interactive psql
export PGPASSWORD=postgres

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}
print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}
print_error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

# Start port-forwarding to Kubernetes Postgres
start_port_forward() {
    print_status "Starting port-forward to Kubernetes Postgres (localhost:5432)..."
    kubectl port-forward -n congen service/postgres 5432:5432 > /dev/null 2>&1 &
    PORT_FORWARD_PID=$!
    # Wait for port to be available
    for _ in {1..30}; do
        if psql -h localhost -p 5432 -U postgres -d postgres -c "SELECT 1;" > /dev/null 2>&1; then
            print_success "Port-forward established (PID: ${PORT_FORWARD_PID})"
            return 0
        fi
        sleep 1
    done
    print_error "Failed to establish port-forward to Postgres."
    kill "${PORT_FORWARD_PID}" 2>/dev/null || true
    exit 1
}

# Cleanup function to kill port-forward and remove temp files
cleanup() {
    print_status "Cleaning up..."
    if [[ -n "${PORT_FORWARD_PID}" ]]; then
        kill "${PORT_FORWARD_PID}" 2>/dev/null || true
    fi
    rm -f tables_columns.txt foreign_keys.txt
    print_success "Cleanup completed"
}

trap cleanup EXIT

start_port_forward

print_status "Querying tables and columns..."
psql -h localhost -p 5432 -U postgres -d postgres -Atc "
SELECT table_name, column_name, data_type
FROM information_schema.columns
WHERE table_schema = 'public'
  AND table_name NOT IN (${EXCLUDED})
ORDER BY table_name, ordinal_position;" > tables_columns.txt

print_status "Querying foreign keys..."
psql -h localhost -p 5432 -U postgres -d postgres -Atc "
SELECT
  tc.table_name AS source_table,
  kcu.column_name AS source_column,
  ccu.table_name AS target_table,
  ccu.column_name AS target_column
FROM
  information_schema.table_constraints AS tc
  JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
  JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
  AND tc.table_schema = 'public'
  AND tc.table_name NOT IN (${EXCLUDED})
  AND ccu.table_name NOT IN (${EXCLUDED})
ORDER BY tc.table_name, kcu.column_name;" > foreign_keys.txt

print_status "Generating DOT file..."
{
  echo "digraph G {"
  echo "  graph [rankdir=LR, fontsize=10];"
  echo "  node [shape=plaintext];"
} > "${OUTPUT_DOT_FILE}"

# Build table nodes with detailed columns

{
  awk -F'|' '
  {
    t = $1;
    c = $2;
    d = $3;
    if (t != last_table && last_table != "") {
      print "    </table>>];";
    }
    if (t != last_table) {
      if (last_table != "") print "";
      printf "  \"%s\" [label=<\n    <table border=\"1\" cellborder=\"0\" cellspacing=\"0\" cellpadding=\"4\">\n      <tr><td bgcolor=\"#E9C2F2\" colspan=\"2\"><b>%s</b></td></tr>\n", t, t
    }
    printf "      <tr><td align=\"left\">%s</td><td align=\"left\"><font color=\"#888888\">%s</font></td></tr>\n", c, d
    last_table = t;
  }
  END {
    if (last_table != "") print "    </table>>];";
  }' tables_columns.txt
  echo ""
  # Add foreign key edges
  awk -F'|' '{printf "  \"%s\" -> \"%s\" [label=\"%s → %s\", fontsize=8, color=gray];\n", $1, $3, $2, $4}' foreign_keys.txt
  echo "}"
} >> "${OUTPUT_DOT_FILE}"

print_success "DOT file generated: ${OUTPUT_DOT_FILE}"

# Generate PNG from DOT
if command -v dot > /dev/null; then
    print_status "Generating PNG image with dot (Graphviz)..."
    dot -Tpng "${OUTPUT_DOT_FILE}" -o "${OUTPUT_PNG_FILE}"
    print_success "PNG image generated: ${OUTPUT_PNG_FILE}"
else
    print_error "Graphviz 'dot' not found. Please install Graphviz to generate PNG."
fi

# Clean up port-forward and temp files
cleanup

print_success "Schema generation completed!" 