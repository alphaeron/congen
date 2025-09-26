--liquibase formatted sql

--changeset John Matty:16 labels:prod,test
--comment: Add performance tracking tables for gamified fitness metrics and weekly test protocol.

-- Create user_performance_metrics table for storing daily wearable and subjective data
-- Modified to support historical data with composite primary key
CREATE TABLE user_performance_metrics (
  id SERIAL PRIMARY KEY,
  keycloak_id VARCHAR(255) NOT NULL,
  vo2_max NUMERIC(6,2) CHECK (vo2_max >= 0),
  strain NUMERIC(4,2) CHECK (strain >= 0 AND strain <= 21),
  recovery NUMERIC(5,2) CHECK (recovery >= 0 AND recovery <= 100),
  hrv NUMERIC(6,2) CHECK (hrv >= 0),
  sleep_score NUMERIC(5,2) CHECK (sleep_score >= 0 AND sleep_score <= 100),
  rem_sleep_minutes NUMERIC(6,2) CHECK (rem_sleep_minutes >= 0),
  deep_sleep_minutes NUMERIC(6,2) CHECK (deep_sleep_minutes >= 0),
  subjective_tiredness INTEGER CHECK (subjective_tiredness >= 1 AND subjective_tiredness <= 5),
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_performance_metrics_user FOREIGN KEY(keycloak_id) REFERENCES "user"(keycloak_id) ON DELETE CASCADE
);

-- Create user_performance_scores table for storing calculated scores
CREATE TABLE user_performance_scores (
  keycloak_id VARCHAR(255) PRIMARY KEY NOT NULL,
  explosiveness_score NUMERIC(5,2) CHECK (explosiveness_score >= 0 AND explosiveness_score <= 100),
  aerobic_capacity_score NUMERIC(5,2) CHECK (aerobic_capacity_score >= 0 AND aerobic_capacity_score <= 100),
  recovery_score NUMERIC(5,2) CHECK (recovery_score >= 0 AND recovery_score <= 100),
  reaction_time_score NUMERIC(5,2) CHECK (reaction_time_score >= 0 AND reaction_time_score <= 100),
  mobility_score NUMERIC(5,2) CHECK (mobility_score >= 0 AND mobility_score <= 100),
  level INTEGER NOT NULL CHECK (level >= 1 AND level <= 100),
  hp NUMERIC(5,2) NOT NULL CHECK (hp >= 0 AND hp <= 100),
  hp_loss NUMERIC(5,2) NOT NULL CHECK (hp_loss >= 0 AND hp_loss <= 100),
  mp NUMERIC(5,2) NOT NULL CHECK (mp >= 0 AND mp <= 100),
  mp_loss NUMERIC(5,2) NOT NULL CHECK (mp_loss >= 0 AND mp_loss <= 100),
  fatigue NUMERIC(5,2) NOT NULL CHECK (fatigue >= 0 AND fatigue <= 100),
  fatigue_loss NUMERIC(5,2) NOT NULL CHECK (fatigue_loss >= 0 AND fatigue_loss <= 100),
  skills TEXT[], -- Array of skill strings
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_performance_scores_user FOREIGN KEY(keycloak_id) REFERENCES "user"(keycloak_id) ON DELETE CASCADE
);

-- Create test_protocol_config table for storing test protocol definitions
CREATE TABLE test_protocol_config (
  test_name VARCHAR(50) PRIMARY KEY NOT NULL,
  display_name VARCHAR(100) NOT NULL,
  description TEXT NOT NULL,
  unit VARCHAR(20) NOT NULL,
  icon_name VARCHAR(50) NOT NULL,
  is_required BOOLEAN NOT NULL DEFAULT true,
  display_order INTEGER NOT NULL DEFAULT 0,
  radar_chart_color VARCHAR(7) NOT NULL DEFAULT '#00bcd4',
  radar_chart_enabled BOOLEAN NOT NULL DEFAULT true,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Create user_test_results table for tracking individual test results
CREATE TABLE user_test_results (
  id SERIAL PRIMARY KEY,
  keycloak_id VARCHAR(255) NOT NULL,
  week_start_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  test_name VARCHAR(50) NOT NULL,
  status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'COMPLETED', 'SKIPPED')),
  result_value NUMERIC(10,2),
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_test_results_user FOREIGN KEY(keycloak_id) REFERENCES "user"(keycloak_id) ON DELETE CASCADE,
  CONSTRAINT fk_user_test_results_test FOREIGN KEY(test_name) REFERENCES test_protocol_config(test_name) ON DELETE CASCADE,
  UNIQUE(keycloak_id, week_start_timestamp, test_name)
);

-- Add indexes for better performance
CREATE INDEX idx_user_performance_metrics_keycloak_id ON user_performance_metrics(keycloak_id);
CREATE INDEX idx_user_performance_metrics_created_at ON user_performance_metrics(created_at);
CREATE INDEX idx_user_performance_metrics_updated_at ON user_performance_metrics(updated_at);
-- Composite index for range queries
CREATE INDEX idx_user_performance_metrics_user_date ON user_performance_metrics(keycloak_id, created_at);

CREATE INDEX idx_user_performance_scores_keycloak_id ON user_performance_scores(keycloak_id);
CREATE INDEX idx_user_performance_scores_level ON user_performance_scores(level);
CREATE INDEX idx_user_performance_scores_updated_at ON user_performance_scores(updated_at);

CREATE INDEX idx_test_protocol_config_test_name ON test_protocol_config(test_name);
CREATE INDEX idx_test_protocol_config_is_required ON test_protocol_config(is_required);
CREATE INDEX idx_test_protocol_config_display_order ON test_protocol_config(display_order);
CREATE INDEX idx_test_protocol_config_radar_enabled ON test_protocol_config(radar_chart_enabled);

CREATE INDEX idx_user_test_results_keycloak_id ON user_test_results(keycloak_id);
CREATE INDEX idx_user_test_results_week_start ON user_test_results(week_start_timestamp);
CREATE INDEX idx_user_test_results_test_name ON user_test_results(test_name);
CREATE INDEX idx_user_test_results_status ON user_test_results(status);
CREATE INDEX idx_user_test_results_updated_at ON user_test_results(updated_at);

-- Add composite indexes for common query patterns
CREATE INDEX idx_user_test_results_user_week ON user_test_results(keycloak_id, week_start_timestamp);
CREATE INDEX idx_user_test_results_user_test ON user_test_results(keycloak_id, test_name);
CREATE INDEX idx_user_test_results_week_test ON user_test_results(week_start_timestamp, test_name);


--rollback DROP TABLE IF EXISTS user_test_results;
--rollback DROP TABLE IF EXISTS user_performance_scores;
--rollback DROP TABLE IF EXISTS user_performance_metrics;
--rollback DROP TABLE IF EXISTS test_protocol_config;
