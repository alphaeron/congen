--liquibase formatted sql

--changeset alphaeron:9 labels:prod,test
--comment: Add user_weight_unit_preference table for tracking user weight unit preferences per exercise.

CREATE TABLE user_weight_unit_preference (
  user_id INTEGER NOT NULL,
  exercise_name VARCHAR(255) NOT NULL,
  preferred_unit VARCHAR(10) NOT NULL CHECK (preferred_unit IN ('KG', 'LBS')),
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, exercise_name),
  CONSTRAINT fk_user_weight_unit_preference_user FOREIGN KEY(user_id) REFERENCES "user"(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_weight_unit_preference_exercise FOREIGN KEY(exercise_name) REFERENCES exercise(name) ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_user_weight_unit_preference_user_id ON user_weight_unit_preference(user_id);
CREATE INDEX idx_user_weight_unit_preference_exercise_name ON user_weight_unit_preference(exercise_name);
CREATE INDEX idx_user_weight_unit_preference_unit ON user_weight_unit_preference(preferred_unit);

-- Add composite indexes for common query patterns
CREATE INDEX idx_user_weight_unit_preference_user_unit ON user_weight_unit_preference(user_id, preferred_unit); 