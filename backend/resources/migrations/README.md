# Database Migrations (Liquibase)

This directory contains all database migration files for the project, managed with [Liquibase](https://www.liquibase.org/). Migrations are organized to support schema evolution, data population, and environment-specific workflows.

## Directory Structure

- **changelog-root.xml**: The root changelog file. Includes all schema and data migrations.
- **liquibase.properties**: Liquibase configuration (changelog file, logging, etc).
- **schema/**: All schema migrations (DDL: tables, indexes, constraints, etc). Each file is a Liquibase-formatted SQL changelog.
- **data/**: Data migrations (DML: initial data population, reference data, etc).
- **classpath/**, **changelog/**: Reserved for advanced or future use.

## How to Add a Migration

1. **Schema changes**: Add a new file to `schema/` with a descriptive name and date, e.g. `changelog-YYYY-MM-DD_NN_description.sql`.
2. **Data changes**: Add a new file to `data/` with a similar naming convention.
3. **Format**: Use Liquibase-formatted SQL. Each changeset must have:
   - `--changeset author:id labels:prod,test`
   - `--comment: <description>`
   - `--rollback <rollback SQL>` (or a fake rollback if not possible)
4. **Update root changelog**: The `changelog-root.xml` automatically includes all files in `schema/` and `data/`.
5. **Test**: Run migrations locally and in CI before merging.

### Changeset ID Conventions

- **Author**: Use your full name (e.g., "alphaeron")
- **ID**: Use sequential numbers starting from 1 for each author
- **Labels**: Always use `labels:prod,test` for environment control
- **Comments**: Provide clear descriptions of what the changeset does

### Rollback Patterns

- **Schema changes**: Provide actual DROP/CREATE rollback statements when possible
- **Data changes**: Use `--rollback SELECT 1` for data population (cannot be safely rolled back)
- **Initial migrations**: Use `--rollback SELECT 1` for foundational changes

## How to Run Migrations

### Local Development
- Migrations are applied automatically on startup or via scripts
- Use `./gradlew createMigrationsConfigMap` to generate the Kubernetes ConfigMap
- Deploy with `./gradlew deployToLocal` or `./scripts/setup-kubernetes.sh`

### CI/CD Pipeline
- Migrations are validated and tested as part of the build
- The `createMigrationsConfigMap` task generates the ConfigMap from migration files
- Kubernetes deployment includes the Liquibase job that applies migrations

### Production Deployment
- Migrations are applied using the same changelog files via Kubernetes Job
- The `liquibase-migration` job runs before application deployment
- Uses environment-specific database credentials from Kubernetes secrets

## Kubernetes Integration

### ConfigMap Generation
The `createMigrationsConfigMap` Gradle task:
- Reads all files from `resources/migrations/`
- Creates a Kubernetes ConfigMap (`k8s/base/stage-6/migrations-configmap.yaml`)
- Includes all SQL, XML, and properties files
- Maintains directory structure for Liquibase

### Migration Job
The `liquibase-migration` Kubernetes Job:
- Uses the `sfat/liquibase:4.11.0` Docker image
- Mounts migration files from the ConfigMap
- Connects to PostgreSQL using environment variables
- Runs validation and update commands with proper labels

### Deployment Process
1. **ConfigMap Creation**: `./gradlew createMigrationsConfigMap`
2. **Database Deployment**: PostgreSQL is deployed first
3. **Migration Execution**: Liquibase job runs and applies pending migrations
4. **Application Deployment**: Application starts after successful migration

## Best Practices & Conventions

- **Naming**: Use clear, date-based filenames for ordering and traceability
- **Changeset labels**: Use `labels:prod,test` to control environment application
- **Rollbacks**: Always provide a rollback, or a fake rollback if not possible
- **Review**: All migrations should be reviewed for correctness and reversibility
- **Idempotency**: Write migrations to be safe to re-run if possible
- **Documentation**: Comment each changeset with a clear description
- **Sequential IDs**: Use sequential changeset IDs to avoid conflicts
- **Atomic Changes**: Keep each changeset focused on a single logical change

## Troubleshooting

### Common Issues
- **ConfigMap not updated**: Run `./gradlew createMigrationsConfigMap` before deployment
- **Migration job fails**: Check logs with `kubectl logs job/liquibase-migration -n congen`
- **Database connection issues**: Verify PostgreSQL is running and credentials are correct
- **Changeset conflicts**: Ensure changeset IDs are unique across all migration files

### Useful Commands
```bash
# Generate ConfigMap
./gradlew createMigrationsConfigMap

# Check migration job status
kubectl get jobs -n congen

# View migration logs
kubectl logs job/liquibase-migration -n congen

# Check applied migrations in database
kubectl exec -it deployment/postgres -n congen -- psql -U postgres -d postgres -c "SELECT * FROM DATABASECHANGELOG ORDER BY DATEEXECUTED DESC;"
```

## References
- [Liquibase SQL Format](https://docs.liquibase.com/concepts/formats/sql.html)
- [Liquibase Changelogs](https://docs.liquibase.com/concepts/changelogs.html)
- [Project changelog-root.xml](./changelog-root.xml)
- [Project liquibase.properties](./liquibase.properties)
- [Kubernetes Liquibase Job](../../k8s/base/liquibase-job.yaml)

---

For questions or to propose changes to migration practices, update this README and notify the team. 