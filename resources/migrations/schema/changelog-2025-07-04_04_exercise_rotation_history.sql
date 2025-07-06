--liquibase formatted sql

--changeset John Matty:4 labels:prod,test
--comment: Add exercise_rotation_history table for tracking exercise usage in workout programs.

CREATE TABLE exercise_rotation_history (
  id BIGSERIAL PRIMARY KEY NOT NULL,
  user_id BIGINT NOT NULL,
  exercise_name VARCHAR(255) NOT NULL,
  is_accessory BOOLEAN NOT NULL,
  used_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_exercise_rotation_history_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
  CONSTRAINT fk_exercise_rotation_history_exercise FOREIGN KEY (exercise_name) REFERENCES exercise(name) ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_exercise_rotation_history_user_id ON exercise_rotation_history(user_id);
CREATE INDEX idx_exercise_rotation_history_exercise_name ON exercise_rotation_history(exercise_name);

-- Add composite indexes for common query patterns
CREATE INDEX idx_exercise_rotation_history_user_exercise ON exercise_rotation_history(user_id, exercise_name);