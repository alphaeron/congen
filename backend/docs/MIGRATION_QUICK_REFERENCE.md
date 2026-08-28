# Migration Quick Reference

This is a quick reference guide for common database migration operations in the Congen application.

## Quick Commands

### Build and Deploy with Migrations
```bash
# Complete workflow
./gradlew clean build createMigrationsConfigMap deployToLocal

# Or use the deployment script
./scripts/deploy.sh local
```

### Check Migration Status
```bash
# Check job status
kubectl get jobs -n congen

# View migration logs
kubectl logs job/liquibase-migration -n congen

# Check database migration history
kubectl exec -n congen deployment/postgres -- psql -U postgres -d postgres -c "SELECT ID, AUTHOR, FILENAME, DATEEXECUTED FROM DATABASECHANGELOG ORDER BY DATEEXECUTED DESC;"
```

## Creating New Migrations

### 1. Create Migration File
```bash
# Create new schema migration
touch resources/migrations/schema/changelog-$(date +%Y-%m-%d)_01_description.sql

# Create new data migration
touch resources/migrations/data/changelog-$(date +%Y-%m-%d)_01_description.sql
```

### 2. Migration File Template
```sql
--liquibase formatted sql

--changeset author:your-name id:unique-id
--comment: Brief description of the change

-- Your SQL changes here
CREATE TABLE example_table (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--rollback DROP TABLE example_table;
```

### 3. Deploy Migration
```bash
# Create ConfigMap and deploy
./gradlew createMigrationsConfigMap deployToLocal
```

## Troubleshooting

### Migration Job Failed
```bash
# Check job status
kubectl get jobs -n congen

# View error logs
kubectl logs job/liquibase-migration -n congen

# Check database connectivity
kubectl exec -n congen deployment/postgres -- pg_isready -U postgres
```

### Database Lock Issues
```bash
# Check for locks
kubectl exec -n congen deployment/postgres -- psql -U postgres -d postgres -c "SELECT * FROM DATABASECHANGELOGLOCK;"

# Clear lock (use with caution)
kubectl exec -n congen deployment/postgres -- psql -U postgres -d postgres -c "DELETE FROM DATABASECHANGELOGLOCK;"
```

### ConfigMap Issues
```bash
# Check ConfigMap contents
kubectl describe configmap migrations-config -n congen

# Verify migration files
kubectl get configmap migrations-config -n congen -o yaml
```

## Monitoring

### Migration History
```bash
# View recent migrations
kubectl exec -n congen deployment/postgres -- psql -U postgres -d postgres -c "
SELECT 
    ID,
    AUTHOR,
    FILENAME,
    DATEEXECUTED,
    DESCRIPTION
FROM DATABASECHANGELOG 
ORDER BY DATEEXECUTED DESC 
LIMIT 10;"
```

### Migration Status
```bash
# Check what migrations are pending
kubectl exec -n congen deployment/postgres -- psql -U postgres -d postgres -c "
SELECT 
    ID,
    AUTHOR,
    FILENAME,
    EXECTYPE
FROM DATABASECHANGELOG 
WHERE EXECTYPE = 'EXECUTED'
ORDER BY DATEEXECUTED DESC;"
```

## Environment-Specific

### Local Development
```bash
# Deploy with test data
./gradlew deployToLocal

# Access database
kubectl port-forward -n congen service/postgres 5432:5432
psql -h localhost -p 5432 -U postgres -d postgres
```

### Staging
```bash
# Deploy to staging
kubectl apply -k k8s/overlays/staging

# Check staging migrations
kubectl logs job/liquibase-migration -n congen
```

### Production
```bash
# Deploy to production
kubectl apply -k k8s/overlays/production

# Monitor production migrations
kubectl logs job/liquibase-migration -n congen -f
```

## Rollback Operations

### Check Rollback Capability
```bash
# Check if rollback is possible
kubectl exec -n congen deployment/postgres -- psql -U postgres -d postgres -c "
SELECT 
    ID,
    AUTHOR,
    FILENAME,
    ROLLBACK
FROM DATABASECHANGELOG 
WHERE ROLLBACK IS NOT NULL
ORDER BY DATEEXECUTED DESC;"
```

### Manual Rollback
```bash
# Connect to database
kubectl port-forward -n congen service/postgres 5432:5432
psql -h localhost -p 5432 -U postgres -d postgres

# Execute rollback SQL manually
-- (Execute the rollback SQL from the ROLLBACK column)
```

## Common Patterns

### Adding a New Table
```sql
--liquibase formatted sql

--changeset author:developer id:add-new-table
--comment: Add new table for feature X

CREATE TABLE new_feature (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--rollback DROP TABLE new_feature;
```

### Adding a Column
```sql
--liquibase formatted sql

--changeset author:developer id:add-column-to-table
--comment: Add new column to existing table

ALTER TABLE existing_table ADD COLUMN new_column VARCHAR(100);

--rollback ALTER TABLE existing_table DROP COLUMN new_column;
```

### Adding Data
```sql
--liquibase formatted sql

--changeset author:developer id:insert-reference-data
--comment: Insert reference data for new feature

INSERT INTO reference_table (name, description) VALUES 
('Option 1', 'First option'),
('Option 2', 'Second option');

--rollback DELETE FROM reference_table WHERE name IN ('Option 1', 'Option 2');
```

## Important Notes

- **Always test migrations in development first**
- **Include rollback statements when possible**
- **Use descriptive file names and comments**
- **Check migration logs after deployment**
- **Never modify existing migration files in production**

## Full Documentation

For complete documentation, see [DATABASE_MIGRATIONS.md](DATABASE_MIGRATIONS.md). 