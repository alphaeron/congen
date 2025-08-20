# GDPR Compliance Guide

This document outlines the GDPR compliance features implemented in the Congen application and provides guidance for administrators and developers.

## Overview

The Congen application implements comprehensive GDPR compliance features to protect user privacy and ensure data protection rights. All personal data is encrypted at rest and in transit, with detailed audit logging and user consent management.

## GDPR Rights Implemented

### 1. Right to Information (Articles 13-14)
- **Privacy Policy Endpoint**: `/api/v1/gdpr/privacy_policy`
- **Transparent Data Processing**: Clear information about what data is collected and why
- **Contact Information**: Data controller contact details provided

### 2. Right of Access (Article 15)
- **Consent Status Check**: `/api/v1/gdpr/consent` (GET)
- **Data Export**: `/api/v1/gdpr/export` (GET) - provides all personal data

### 3. Right to Rectification (Article 16)
- **Profile Updates**: Users can update their information via existing profile endpoints
- **Data Accuracy**: Validation ensures data accuracy and completeness

### 4. Right to Erasure (Article 17)
- **Complete Data Deletion**: `/api/v1/gdpr/delete_all_data` (DELETE)
- **Cascading Deletion**: All related data automatically deleted
- **Audit Trail**: Deletion operations fully logged

### 5. Right to Data Portability (Article 20)
- **JSON Export**: Structured, machine-readable data export
- **Complete Dataset**: All personal data included in export

### 6. Right to Object (Article 21)
- **Consent Withdrawal**: `/api/v1/gdpr/consent` (POST with consent=false)
- **Processing Stop**: Data processing stops when consent withdrawn

## Consent Management

### Account Creation and Implied Consent

The application implements a two-tier consent approach that complies with GDPR requirements:

#### 1. Implied Consent During Account Creation
When users create an account through Keycloak registration, they implicitly consent to:
- **Account Creation**: Processing necessary for account setup and authentication
- **Basic Service Provision**: Essential data processing for service delivery
- **Legal Basis**: Contract performance (GDPR Article 6.1.b)

#### 2. Explicit Consent for Full Service
After account creation, users automatically receive explicit consent for:
- **Personalized Features**: Advanced workout generation and fitness tracking
- **Data Processing**: All data processing activities beyond basic service provision
- **Legal Basis**: Consent (GDPR Article 6.1.a)

### Consent Implementation

- **Automatic Consent Creation**: When a user profile is created, a consent record is automatically created with `data_processing_consent = true`
- **Consent Withdrawal**: Users can withdraw consent at any time via `/api/v1/gdpr/consent` endpoint
- **Processing Restrictions**: When consent is withdrawn, data processing stops for non-essential operations
- **Audit Trail**: All consent changes are logged with timestamps

### Legal Basis Documentation

The privacy policy clearly documents the legal basis for data processing:

1. **Contract Performance (Article 6.1.b)**: Account creation and basic service provision
2. **Consent (Article 6.1.a)**: Additional data processing for personalized features  
3. **Legitimate Interest (Article 6.1.f)**: Service improvement and security

This approach ensures GDPR compliance while simplifying the user experience by reducing friction during account creation.

## Data Protection Features

### Encryption
- **Algorithm**: AES-256-GCM (authenticated encryption)
- **Key Management**: Secure key storage via Kubernetes secrets
- **Field-Level Encryption**: Sensitive personal data encrypted in database
- **Unique IVs**: Each encryption operation uses unique initialization vector

### Data Classification
- **Highly Sensitive**: Name (encrypted)
- **Sensitive**: Age, height, weight (encrypted - health data under Article 9)
- **Non-Sensitive**: Keycloak ID (pseudonymized), timestamps

### Audit Logging
- **All Data Access**: Every access to personal data logged
- **Consent Changes**: All consent operations tracked with timestamps
- **Data Modifications**: Changes to personal data logged
- **Security Violations**: Unauthorized access attempts logged

## Database Security

### Encryption at Rest
Configure PostgreSQL with encryption at rest for production:

```sql
-- Enable transparent data encryption (TDE) in PostgreSQL
-- This should be configured at the database cluster level
ALTER SYSTEM SET ssl = on;
ALTER SYSTEM SET ssl_cert_file = '/path/to/server.crt';
ALTER SYSTEM SET ssl_key_file = '/path/to/server.key';
ALTER SYSTEM SET ssl_ca_file = '/path/to/ca.crt';
```

### Connection Security
- **SSL/TLS**: All database connections use SSL
- **Certificate Validation**: Proper certificate validation enabled
- **Connection Pooling**: Secure connection pooling with timeout limits

## Data Retention Policies & TTL

### Automated TTL System

The application implements comprehensive TTL (time-to-live) functionality for GDPR compliance:

- **Automated Cleanup**: Daily scheduled cleanup at 2:00 AM UTC
- **Policy-Based**: Retention periods configurable per data type
- **Database Functions**: PostgreSQL stored procedures for efficient cleanup
- **Audit Trail**: All cleanup operations logged for compliance

### Default Retention Periods

Configured in `data_retention_policy` table:

| Data Type | Retention Period | Description |
|-----------|------------------|-------------|
| User Profile | 7 years | User profile data retained after account closure |
| Audit Logs | 7 years | Compliance and security audit logs |
| Consent Records | 7 years | Consent history after withdrawal |
| Exercise Data | 3 years | Fitness and exercise data |
| Session Logs | 1 year | Access and session logs |

### TTL Management

#### PostgreSQL Functions
- `cleanup_expired_audit_logs()` - Cleans up old audit logs
- `cleanup_expired_consent_history()` - Cleans up old consent records  
- `cleanup_expired_data()` - Master cleanup function for all data types

#### API Endpoints
- `GET /api/v1/admin/data_retention/policies` - View retention policies
- `PUT /api/v1/admin/data_retention/policies` - Update retention periods
- `GET /api/v1/admin/data_retention/cleanup_estimate` - Preview cleanup impact
- `POST /api/v1/admin/data_retention/cleanup` - Manual cleanup trigger
- `GET /api/v1/admin/data_retention/status` - Current retention status

> **Note**: The `/api/v1` prefix is automatically added by the `WebConfig` to all controller endpoints.

#### Scheduling
```kotlin
@Scheduled(cron = "0 0 2 * * ?", zone = "UTC") // Daily at 2:00 AM UTC
fun performDailyDataCleanup()
```

## Key Management

### Development Environment
- Uses base64-encoded key in configuration
- Key stored in Kubernetes secrets
- **WARNING**: Development key is not secure for production

### Production Environment
1. **Generate Secure Key**:
   ```bash
   # Generate a new AES-256 key
   openssl rand -base64 32
   ```

2. **Key Rotation**:
   - Keys should be rotated every 90 days
   - Old keys must be retained to decrypt existing data
   - Implement key versioning for smooth rotation

3. **External Key Management**:
   - Consider using AWS KMS, Azure Key Vault, or HashiCorp Vault
   - Store keys separately from application
   - Implement key access auditing

## Deployment Configuration

### Environment Variables
Required environment variables for GDPR compliance:

```bash
# Encryption configuration
ENCRYPTION_KEY=<base64-encoded-32-byte-key>
GDPR_AUDIT_ENABLED=true
DATA_RETENTION_CHECK_ENABLED=true

# PostgreSQL SSL configuration
PGSSLMODE=require
PGSSLCERT=/path/to/client.crt
PGSSLKEY=/path/to/client.key
PGSSLROOTCERT=/path/to/ca.crt
```

### Kubernetes Secrets
Update production secrets with strong encryption keys:

```yaml
apiVersion: v1
kind: Secret
metadata:
  name: congen-secret
  namespace: congen
type: Opaque
data:
  ENCRYPTION_KEY: <base64-encoded-production-key>
  GDPR_AUDIT_ENABLED: dHJ1ZQ==  # true
  DATA_RETENTION_CHECK_ENABLED: dHJ1ZQ==  # true
```

## Monitoring and Compliance

### Audit Log Monitoring
- Monitor `gdpr_audit_log` table for all data operations
- Set up alerts for suspicious access patterns
- Regular review of data access logs

### Consent Monitoring
- Track consent withdrawal patterns
- Monitor consent history in `consent_history` table
- Ensure processing stops when consent withdrawn

### Data Retention Compliance
- Automated checks for data retention periods
- Regular cleanup of expired data
- Audit trail for all data deletion operations

## Testing GDPR Features

### Local Testing
1. **Start Application**:
   ```bash
   ./gradlew :backend:bootRun
   ```

2. **Test Encryption**:
   ```bash
   # Create user profile (data will be encrypted)
   curl -X POST "http://localhost:8888/api/v1/user" \
     -H "Authorization: Bearer $TOKEN" \
     -d "name=Test User&age=30&height=175&weight=70"
   ```

3. **Test GDPR Endpoints**:
   ```bash
   # Give consent
   curl -X POST "http://localhost:8888/api/v1/gdpr/consent?consent=true" \
     -H "Authorization: Bearer $TOKEN"

   # Export data
   curl -X GET "http://localhost:8888/api/v1/gdpr/export" \
     -H "Authorization: Bearer $TOKEN"

   # Delete all data
   curl -X DELETE "http://localhost:8888/api/v1/gdpr/delete_all_data?confirmation=DELETE_ALL_MY_DATA" \
     -H "Authorization: Bearer $TOKEN"
   ```

### Production Verification
1. **Verify Encryption**: Check database to ensure sensitive data is encrypted
2. **Audit Logs**: Confirm all operations are logged
3. **Consent Tracking**: Verify consent changes are recorded
4. **Data Export**: Test complete data export functionality
5. **Data Deletion**: Verify complete data removal

## Compliance Checklist

- [ ] **Encryption at Rest**: Database encryption configured
- [ ] **Encryption in Transit**: SSL/TLS for all connections
- [ ] **Field-Level Encryption**: Sensitive data encrypted
- [ ] **Audit Logging**: All data operations logged
- [ ] **Consent Management**: Proper consent tracking
- [ ] **Data Export**: Complete data portability
- [ ] **Data Deletion**: Secure data erasure
- [ ] **Key Management**: Secure key storage and rotation
- [ ] **Retention Policies**: Automated data retention compliance
- [ ] **Privacy Policy**: Transparent data processing information

## Support and Maintenance

### Regular Tasks
1. **Key Rotation**: Every 90 days
2. **Audit Log Review**: Weekly security review
3. **Consent Monitoring**: Monthly consent pattern analysis
4. **Data Retention**: Quarterly cleanup of expired data

### Emergency Procedures
1. **Data Breach Response**: Immediate audit log review and user notification
2. **Key Compromise**: Emergency key rotation procedure
3. **Consent Violations**: Immediate processing stop and user notification

For technical support or compliance questions, contact: privacy@congen.com
