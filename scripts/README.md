# Scripts Directory

This directory contains utility scripts for the Congen project.

## Scripts

### `start-minikube-local.sh`

**Purpose**: Start the Congen minikube deployment with persistent storage for local development.

**Prerequisites**:
- [minikube](https://minikube.sigs.k8s.io/docs/start/) installed and configured
- [kubectl](https://kubernetes.io/docs/tasks/tools/) installed
- [curl](https://curl.se/) for health checks
- [netcat](https://nmap.org/ncat/) for port checking
- Gradle wrapper (`gradlew`) in project root

**What it does**:

1. **Checks prerequisites** - Validates all required tools are available
2. **Creates persistent storage** - Sets up local data directories for PostgreSQL
3. **Deploys PostgreSQL** - Creates custom PostgreSQL deployment with hostPath mounting
4. **Deploys application** - Uses `./gradlew deployAll -Penvironment=local` for full deployment
5. **Updates backend config** - Patches backend to use local PostgreSQL
6. **Sets up port forwarding** - Makes all services accessible on localhost
7. **Verifies services** - Quick health checks for all services

**Usage**:

```bash
# Use default data directory (~/.congen/minikube-data)
./scripts/start-minikube-local.sh

# Use custom data directory
./scripts/start-minikube-local.sh -d /path/to/custom/data

# Use temporary data directory
./scripts/start-minikube-local.sh --data-dir /tmp/congen-data

# Show help
./scripts/start-minikube-local.sh --help
```

**Command Line Options**:
- `-d, --data-dir DIR`: Data directory for persistent storage (default: ~/.congen/minikube-data)
- `--help, -h`: Show help message

**Service Access**:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8888
- Keycloak: http://localhost:8080
- PostgreSQL: localhost:5432

**Data Storage**:
- PostgreSQL data: `{data-dir}/postgres/`
- All data persists between restarts
- Default location: `~/.congen/minikube-data/`

### `stop-minikube-local.sh`

**Purpose**: Stop the Congen minikube deployment and clean up port forwarding.

**Prerequisites**:
- [kubectl](https://kubernetes.io/docs/tasks/tools/) installed (for deployment cleanup)

**What it does**:

1. **Stops port forwarding** - Kills all port forward processes and cleans up PID files
2. **Stops deployments** - Deletes all application deployments and services
3. **Preserves data** - Data remains in the directory used when starting
4. **Optionally stops minikube** - Can stop minikube to save system resources

**Usage**:

```bash
# Stop application and port forwarding, keep minikube running
./scripts/stop-minikube-local.sh

# Stop everything including minikube (saves system resources)
./scripts/stop-minikube-local.sh --stop-minikube

# Show help
./scripts/stop-minikube-local.sh --help
```

**Command Line Options**:
- `--stop-minikube`: Also stop the minikube profile (saves resources)
- `--help, -h`: Show help message

**Data Preservation**:
- Data is preserved in the directory used when starting the application
- To start fresh, manually delete the data directory before restarting

**Integration**:
- Works with the existing Gradle deployment system
- Complements `./gradlew deployAll -Penvironment=local`
- Can be used alongside manual Gradle deployments

### `update-k8s-secrets.sh`

**Purpose**: Updates Kubernetes secrets with real values from Terraform outputs, replacing dummy values deployed in Stage 2.

**Prerequisites**:
- Terraform has been applied in the target environment
- Kubernetes cluster is accessible
- `kubectl` is configured and working
- `terraform` command is available
- Stage 2 (Secrets) has been deployed (secrets must exist in Kubernetes)

**What it does**:

1. **Extracts Terraform outputs** from the environment-specific Terraform directory
2. **Updates congen-secret** with Keycloak client secrets and service account passwords
3. **Updates keycloak-secret** with backend client secrets and service account passwords
4. **Handles missing outputs gracefully** - only updates secrets for which Terraform outputs exist
5. **Base64 encodes values** automatically for Kubernetes secret format

**Supported Terraform outputs**:
- `backend_client_secret` - Keycloak backend client secret
- `service_account_password` - Keycloak service account password

**Usage**:

```bash
# Update secrets for local environment
./scripts/update-k8s-secrets.sh -e local

# Update secrets for staging environment
./scripts/update-k8s-secrets.sh -e staging

# Update secrets for production environment
./scripts/update-k8s-secrets.sh -e production

# Use custom Terraform directory
./scripts/update-k8s-secrets.sh -e local -t /custom/terraform/path

# Show help
./scripts/update-k8s-secrets.sh -h
```

**Command Line Options**:
- `-e, --environment ENV`: Environment name (REQUIRED: local, staging, production)
- `-t, --terraform-dir DIR`: Terraform directory (default: terraform/environments/{environment})
- `-h, --help`: Show help message

**Security Notes**:
- Only updates secrets that exist in Kubernetes
- Fails if required secrets don't exist (ensures proper deployment order)
- All values are base64 encoded before being stored in Kubernetes
- Terraform outputs are marked as sensitive and not logged

**Integration**:
- Called automatically during Stage 4 of the deployment process
- Can be run manually to update secrets after Terraform changes
- Works with the staged deployment approach for secret management
- Requires Stage 2 to be deployed first (secrets must exist before updating)
- **Optimization**: Automatically skipped if Terraform was up to date (no changes applied)

### `setup-keycloak-terraform.sh`

**Purpose**: Bootstraps Keycloak with the necessary client credentials grant for Terraform to manage Keycloak resources.

**Prerequisites**:
- Keycloak running and accessible at `http://localhost:8080`
- Port forwarding active: `kubectl port-forward -n congen service/keycloak 8080:8080`
- `curl` and `jq` installed on the system
- Admin access to Keycloak (default: admin/admin)

**What it does**:

1. **Creates a Terraform client** in the master realm with the following configuration:
   - Client ID: `terraform`
   - Access Type: `confidential`
   - Standard Flow Enabled: `OFF`
   - Direct Access Grants Enabled: `OFF`
   - Service Accounts Enabled: `ON`
   - Client Authenticator Type: `client-secret`

2. **Assigns realm management roles** to the service account:
   - `view-realm`
   - `manage-users`
   - `view-users`
   - `view-clients`
   - `manage-clients`

3. **Updates Terraform variables file**:
   - Updates `terraform/environments/{environment}/terraform.tfvars` with the client secret
   - If the file doesn't exist, creates it with the client secret
   - If the file exists, updates the existing `keycloak_client_secret` value
   - Preserves any existing variables in the file

**Usage**:

```bash
# Make the script executable (first time only)
chmod +x scripts/setup-keycloak-terraform.sh

# Run with Keycloak URL and environment (required) - will prompt for credentials
./scripts/setup-keycloak-terraform.sh -u http://localhost:8080 -e local

# Run with custom Keycloak URL and environment - will prompt for credentials
./scripts/setup-keycloak-terraform.sh -u https://keycloak-staging.example.com -e staging

# Run with all credentials provided
./scripts/setup-keycloak-terraform.sh -u http://localhost:8080 -e local -a admin -p mypassword

# Run for production environment
./scripts/setup-keycloak-terraform.sh -u https://keycloak.example.com -e production -a admin -p mypassword

# Show help
./scripts/setup-keycloak-terraform.sh -h
```

**Command Line Options**:
- `-u, --url URL`: Keycloak server URL (REQUIRED)
- `-e, --environment ENV`: Environment name (REQUIRED: local, staging, production)
- `-r, --realm REALM`: Master realm name (default: master)
- `-c, --client-id ID`: Terraform client ID (default: terraform)
- `-a, --admin-user USER`: Admin username (will prompt if not provided)
- `-p, --admin-pass PASS`: Admin password (will prompt if not provided)
- `-h, --help`: Show help message

**Output**:
- Creates the Terraform client in Keycloak
- Updates `terraform/environments/{environment}/terraform.tfvars` with the client secret
- Preserves existing variables in the terraform.tfvars file

**Security Notes**:
- The `terraform.tfvars` file contains the client secret and should never be committed to version control
- Ensure `terraform/environments/*/terraform.tfvars` is in your `.gitignore` file
- The client secret is used for Terraform to authenticate with Keycloak's admin API
- Each environment (local, staging, production) has its own terraform.tfvars file

**Troubleshooting**:

If the script fails with authentication errors:
1. Ensure Keycloak is running and accessible
2. Verify port forwarding is active: `kubectl port-forward -n congen service/keycloak 8080:8080`
3. Check that the admin credentials are correct (default: admin/admin)

If the script fails with API errors:
1. Ensure you have admin privileges in Keycloak
2. Check that the master realm is accessible
3. Verify the Keycloak version is compatible (tested with Keycloak 26.x)

**Next Steps**:
After running the script:
1. Review the updated `terraform.tfvars` file
2. Ensure `terraform.tfvars` is in your `.gitignore` file
3. Run `terraform init` in `terraform/environments/{environment}`
4. Run `terraform apply` to create your Keycloak resources

**References**:
- [Keycloak Terraform Provider Documentation](https://registry.terraform.io/providers/keycloak/keycloak/latest/docs)
- [Client Credentials Grant Setup](https://registry.terraform.io/providers/keycloak/keycloak/latest/docs#client-credentials-grant-setup-recommended) 