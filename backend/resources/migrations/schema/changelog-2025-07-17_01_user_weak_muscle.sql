--liquibase formatted sql

--changeset John Matty:12 labels:prod,test
--comment: Add user_weak_muscle table for tracking user weak muscle groups.

CREATE TABLE user_weak_muscle (
  user_id INTEGER NOT NULL,
  muscle_name VARCHAR(255) NOT NULL,
  created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, muscle_name),
  CONSTRAINT fk_user_weak_muscle_user FOREIGN KEY(user_id) REFERENCES "user"(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_weak_muscle_muscle FOREIGN KEY(muscle_name) REFERENCES muscle(name) ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_user_weak_muscle_user_id ON user_weak_muscle(user_id);
CREATE INDEX idx_user_weak_muscle_muscle_name ON user_weak_muscle(muscle_name); 