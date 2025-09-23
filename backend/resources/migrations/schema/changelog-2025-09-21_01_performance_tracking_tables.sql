--liquibase formatted sql

--changeset John Matty:16 labels:prod,test
--comment: Add performance tracking tables for gamified fitness metrics and weekly test protocol.

-- Create user_performance_metrics table for storing daily wearable and subjective data
CREATE TABLE user_performance_metrics (
  keycloak_id VARCHAR(255) PRIMARY KEY NOT NULL,
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

-- Create user_weekly_test table for tracking weekly test protocol
CREATE TABLE user_weekly_test (
  keycloak_id VARCHAR(255) NOT NULL,
  week_start_timestamp TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  vertical_jump_status VARCHAR(20) NOT NULL CHECK (vertical_jump_status IN ('PENDING', 'COMPLETED', 'SKIPPED')),
  vertical_jump_result NUMERIC(6,2) CHECK (vertical_jump_result >= 0),
  hr_recovery_status VARCHAR(20) NOT NULL CHECK (hr_recovery_status IN ('PENDING', 'COMPLETED', 'SKIPPED')),
  hr_recovery_result INTEGER CHECK (hr_recovery_result >= 0),
  reflex_status VARCHAR(20) NOT NULL CHECK (reflex_status IN ('PENDING', 'COMPLETED', 'SKIPPED')),
  reflex_result INTEGER CHECK (reflex_result >= 0),
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (keycloak_id, week_start_timestamp),
  CONSTRAINT fk_user_weekly_test_user FOREIGN KEY(keycloak_id) REFERENCES "user"(keycloak_id) ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_user_performance_metrics_keycloak_id ON user_performance_metrics(keycloak_id);
CREATE INDEX idx_user_performance_metrics_updated_at ON user_performance_metrics(updated_at);

CREATE INDEX idx_user_performance_scores_keycloak_id ON user_performance_scores(keycloak_id);
CREATE INDEX idx_user_performance_scores_level ON user_performance_scores(level);
CREATE INDEX idx_user_performance_scores_updated_at ON user_performance_scores(updated_at);

CREATE INDEX idx_user_weekly_test_keycloak_id ON user_weekly_test(keycloak_id);
CREATE INDEX idx_user_weekly_test_week_start_timestamp ON user_weekly_test(week_start_timestamp);
CREATE INDEX idx_user_weekly_test_updated_at ON user_weekly_test(updated_at);

-- Add composite indexes for common query patterns
CREATE INDEX idx_user_weekly_test_user_week ON user_weekly_test(keycloak_id, week_start_timestamp);

--rollback DROP TABLE IF EXISTS user_weekly_test;
--rollback DROP TABLE IF EXISTS user_performance_scores;
--rollback DROP TABLE IF EXISTS user_performance_metrics;
