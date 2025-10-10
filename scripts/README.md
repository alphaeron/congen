# Scripts Directory

This directory contains utility scripts for the Congen project.

## Scripts

### `deploy-staged.sh`

**Purpose**: Deploy the Congen application using staged deployment with support for persistent storage.

**Prerequisites**:
- [minikube](https://minikube.sigs.k8s.io/docs/start/) installed and configured
- [kubectl](https://kubernetes.io/docs/tasks/tools/) installed
- [terraform](https://terraform.io/) installed
- Gradle wrapper (`gradlew`) in project root

**What it does**:

1. **Deploys in stages** - Deploys application components in dependency order
2. **Supports persistent storage** - Can mount host directories for data persistence
3. **Manages Keycloak** - Sets up Keycloak authentication and authorization
4. **Updates secrets** - Dynamically updates Kubernetes secrets with Terraform outputs
5. **Validates deployment** - Ensures all components are properly deployed

**Usage**:

```bash
# Deploy to local environment
./scripts/deploy-staged.sh -e local

# Deploy to local environment with persistent storage
./scripts/deploy-staged.sh -e local-persist -m /path/to/data

# Deploy to staging environment
./scripts/deploy-staged.sh -e staging

# Deploy specific stage only
./scripts/deploy-staged.sh -e local --stage 5

# Show help
./scripts/deploy-staged.sh --help
```

**Command Line Options**:
- `-e, --environment ENV`: Environment name (REQUIRED: local, local-persist, staging, production)
- `-m, --mount-dir DIR`: Mount directory for persistent storage (local-persist only)
- `--stage STAGE`: Deploy specific stage only (1-10)
- `-u, --keycloak-url URL`: Keycloak URL for bootstrap (default: environment-specific)
- `--help, -h`: Show help message

**Service Access**:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8888
- Keycloak: http://localhost:8080
- PostgreSQL: localhost:5432

**Data Storage** (local-persist only):
- PostgreSQL data: Mounted from specified directory
- Data persists between deployments
- Use `-m /path/to/data` to specify storage location

**Integration**:
- Works with the existing Gradle deployment system
- Can be called directly or via `./gradlew deployAll -Penvironment=local-persist -PmountDir=/path/to/data`
- Supports all deployment stages for complete application setup

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
- `-e, --environment ENV`: Environment name (REQUIRED: local, local-persist, staging, production)
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
   - Updates `terraform/environments/{environment}/terraform.tfvars` with the client secret (local-persist uses local directory)
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
- `-e, --environment ENV`: Environment name (REQUIRED: local, local-persist, staging, production)
- `-r, --realm REALM`: Master realm name (default: master)
- `-c, --client-id ID`: Terraform client ID (default: terraform)
- `-a, --admin-user USER`: Admin username (will prompt if not provided)
- `-p, --admin-pass PASS`: Admin password (will prompt if not provided)
- `-h, --help`: Show help message

**Output**:
- Creates the Terraform client in Keycloak
- Updates `terraform/environments/{environment}/terraform.tfvars` with the client secret (local-persist uses local directory)
- Preserves existing variables in the terraform.tfvars file

**Security Notes**:
- The `terraform.tfvars` file contains the client secret and should never be committed to version control
- Ensure `terraform/environments/*/terraform.tfvars` is in your `.gitignore` file
- The client secret is used for Terraform to authenticate with Keycloak's admin API
- Each environment (local, local-persist, staging, production) has its own terraform.tfvars file
- Note: local-persist uses the same Terraform directory as local (terraform/environments/local)

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
3. Run `terraform init` in `terraform/environments/{environment}` (use `local` for local-persist)
4. Run `terraform apply` to create your Keycloak resources

**References**:
- [Keycloak Terraform Provider Documentation](https://registry.terraform.io/providers/keycloak/keycloak/latest/docs)
- [Client Credentials Grant Setup](https://registry.terraform.io/providers/keycloak/keycloak/latest/docs#client-credentials-grant-setup-recommended) 