--liquibase formatted sql

--changeset John Matty:gdpr_compliance labels:prod,test
--comment: Add comprehensive GDPR compliance infrastructure including user consent table, audit logging, and data retention policies

-- Create GDPR audit log table for compliance tracking
CREATE TABLE gdpr_audit_log (
  id BIGSERIAL PRIMARY KEY,
  keycloak_id VARCHAR(255), -- NULL indicates a system operation
  operation VARCHAR(100) NOT NULL, -- Type of operation (DATA_ACCESS, CONSENT_GIVEN, etc.)
  data_type VARCHAR(100) NOT NULL, -- Type of data involved
  performed_by VARCHAR(255), -- User who performed the operation
  timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  additional_info TEXT, -- Additional context or details
);

-- Create indexes for audit log queries
CREATE INDEX idx_gdpr_audit_log_keycloak_id ON gdpr_audit_log(keycloak_id);
CREATE INDEX idx_gdpr_audit_log_operation ON gdpr_audit_log(operation);
CREATE INDEX idx_gdpr_audit_log_timestamp ON gdpr_audit_log(timestamp);
CREATE INDEX idx_gdpr_audit_log_data_type ON gdpr_audit_log(data_type);

-- Create data retention policy table for GDPR compliance
CREATE TABLE data_retention_policy (
  id SERIAL PRIMARY KEY,
  data_type VARCHAR(100) NOT NULL UNIQUE,
  retention_period_days INTEGER NOT NULL,
  description TEXT,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Insert default retention policies
INSERT INTO data_retention_policy (data_type, retention_period_days, description) VALUES
('USER_PROFILE', 2555, 'User profile data retained for 7 years after account closure'),
('AUDIT_LOGS', 2555, 'Audit logs retained for 7 years for compliance'),
('EXERCISE_DATA', 1095, 'Exercise and fitness data retained for 3 years'),
('SESSION_LOGS', 365, 'Session and access logs retained for 1 year'),
('CONSENT_RECORDS', 2555, 'User consent records retained for 7 years for compliance');

-- Create dedicated user_consent table (separated from user table for proper GDPR data separation)
CREATE TABLE user_consent (
  keycloak_id VARCHAR(255) PRIMARY KEY,
  data_processing_consent BOOLEAN NOT NULL DEFAULT FALSE,
  consent_timestamp TIMESTAMP WITHOUT TIME ZONE,
  created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_consent_user FOREIGN KEY(keycloak_id) REFERENCES "user"(keycloak_id) ON DELETE CASCADE
);

-- Create indexes for user_consent operations
CREATE INDEX idx_user_consent_data_processing ON user_consent(data_processing_consent);
CREATE INDEX idx_user_consent_timestamp ON user_consent(consent_timestamp);
CREATE INDEX idx_user_consent_updated_at ON user_consent(updated_at);