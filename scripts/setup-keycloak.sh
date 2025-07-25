#!/bin/bash

# Keycloak setup script for Congen application
# This script configures Keycloak with the necessary realm, clients, and users

set -e

# Configuration
KEYCLOAK_URL="http://localhost:8081"
ADMIN_USERNAME="admin"
ADMIN_PASSWORD="admin"
REALM_NAME="congen"
BACKEND_CLIENT_ID="congen-backend"
FRONTEND_CLIENT_ID="congen-frontend"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${GREEN}Setting up Keycloak for Congen application...${NC}"

# Wait for Keycloak to be ready
echo -e "${YELLOW}Waiting for Keycloak to be ready...${NC}"
until curl -s "${KEYCLOAK_URL}/health" > /dev/null 2>&1; do
    echo "Waiting for Keycloak..."
    sleep 5
done

# Get admin token
echo -e "${YELLOW}Getting admin token...${NC}"
ADMIN_TOKEN=$(curl -s -X POST "${KEYCLOAK_URL}/realms/master/protocol/openid-connect/token" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=${ADMIN_USERNAME}" \
    -d "password=${ADMIN_PASSWORD}" \
    -d "grant_type=password" \
    -d "client_id=admin-cli" | jq -r '.access_token')

if [ "$ADMIN_TOKEN" = "null" ] || [ -z "$ADMIN_TOKEN" ]; then
    echo -e "${RED}Failed to get admin token. Check Keycloak credentials.${NC}"
    exit 1
fi

# Create realm
echo -e "${YELLOW}Creating realm '${REALM_NAME}'...${NC}"
curl -s -X POST "${KEYCLOAK_URL}/admin/realms" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{
        \"realm\": \"${REALM_NAME}\",
        \"enabled\": true,
        \"displayName\": \"Congen\",
        \"displayNameHtml\": \"<div class=\"kc-logo-text\"><span>Congen</span></div>\"
    }"

# Create backend client
echo -e "${YELLOW}Creating backend client...${NC}"
curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/clients" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{
        \"clientId\": \"${BACKEND_CLIENT_ID}\",
        \"enabled\": true,
        \"publicClient\": false,
        \"standardFlowEnabled\": false,
        \"directAccessGrantsEnabled\": false,
        \"serviceAccountsEnabled\": true,
        \"authorizationServicesEnabled\": false
    }"

# Create frontend client
echo -e "${YELLOW}Creating frontend client...${NC}"
curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/clients" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{
        \"clientId\": \"${FRONTEND_CLIENT_ID}\",
        \"enabled\": true,
        \"publicClient\": true,
        \"standardFlowEnabled\": true,
        \"directAccessGrantsEnabled\": false,
        \"serviceAccountsEnabled\": false,
        \"authorizationServicesEnabled\": false,
        \"redirectUris\": [\"http://localhost:3000/*\", \"http://localhost:3000\"],
        \"webOrigins\": [\"http://localhost:3000\"]
    }"

# Create roles
echo -e "${YELLOW}Creating roles...${NC}"
curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/roles" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"user\",
        \"description\": \"Regular user role\"
    }"

curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/roles" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"admin\",
        \"description\": \"Administrator role\"
    }"

curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/roles" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"service\",
        \"description\": \"Service account role\"
    }"

# Create test user
echo -e "${YELLOW}Creating test user...${NC}"
curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "{
        \"username\": \"testuser\",
        \"email\": \"test@example.com\",
        \"firstName\": \"Test\",
        \"lastName\": \"User\",
        \"enabled\": true,
        \"emailVerified\": true,
        \"credentials\": [{
            \"type\": \"password\",
            \"value\": \"password\",
            \"temporary\": false
        }]
    }"

# Get user ID and assign role
USER_ID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users?username=testuser" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq -r '.[0].id')

USER_ROLE_ID=$(curl -s -X GET "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/roles/user" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq -r '.id')

curl -s -X POST "${KEYCLOAK_URL}/admin/realms/${REALM_NAME}/users/${USER_ID}/role-mappings/realm" \
    -H "Authorization: Bearer ${ADMIN_TOKEN}" \
    -H "Content-Type: application/json" \
    -d "[{\"id\": \"${USER_ROLE_ID}\", \"name\": \"user\"}]"

echo -e "${GREEN}Keycloak setup completed successfully!${NC}"
echo -e "${GREEN}Test user credentials:${NC}"
echo -e "  Username: testuser"
echo -e "  Password: password"
echo -e "${GREEN}Keycloak admin console: ${KEYCLOAK_URL}${NC}" 