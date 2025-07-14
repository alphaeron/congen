-- Initialize test database with required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Create test-specific schemas if needed
-- CREATE SCHEMA IF NOT EXISTS test_schema;

-- Set timezone to UTC for consistent testing
SET timezone = 'UTC'; 