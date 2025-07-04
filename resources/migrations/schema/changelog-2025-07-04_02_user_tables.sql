--liquibase formatted sql

--changeset John Matty:3 labels:prod,test
--comment: Add user, user_equipment, user_program_preferences, and user_exercise_preference tables for user-specific workout generation.

CREATE SEQUENCE congen.user_id_seq;

CREATE TABLE "user" (
  id SERIAL DEFAULT nextval('congen.user_id_seq') PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  age INTEGER NOT NULL CHECK (age > 0 AND age <= 150),
  height NUMERIC(5,2) NOT NULL CHECK (height > 0 AND height <= 300), -- in cm
  weight NUMERIC(6,2) NOT NULL CHECK (weight > 0 AND weight <= 1000), -- in kg
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE user_equipment (
  user_id INTEGER NOT NULL,
  equipment_name VARCHAR(255) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, equipment_name),
  CONSTRAINT fk_user_equipment_user FOREIGN KEY(user_id) REFERENCES "user"(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_equipment_equipment FOREIGN KEY(equipment_name) REFERENCES equipment(name) ON DELETE CASCADE
);

CREATE TABLE user_program_preferences (
  user_id INTEGER PRIMARY KEY,
  program_days_per_week INTEGER NOT NULL CHECK (program_days_per_week >= 1 AND program_days_per_week <= 7),
  session_time_length_in_minutes INTEGER NOT NULL CHECK (session_time_length_in_minutes >= 15 AND session_time_length_in_minutes <= 300),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_user_program_preferences_user FOREIGN KEY(user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE TABLE user_exercise_preference (
  user_id INTEGER NOT NULL,
  exercise_name VARCHAR(255) NOT NULL,
  should_avoid BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (user_id, exercise_name),
  CONSTRAINT fk_user_exercise_preference_user FOREIGN KEY(user_id) REFERENCES "user"(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_exercise_preference_exercise FOREIGN KEY(exercise_name) REFERENCES exercise(name) ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_user_name ON "user"(name);
CREATE INDEX idx_user_equipment_user_id ON user_equipment(user_id);
CREATE INDEX idx_user_equipment_equipment_name ON user_equipment(equipment_name);
CREATE INDEX idx_user_exercise_preference_user_id ON user_exercise_preference(user_id);
CREATE INDEX idx_user_exercise_preference_exercise_name ON user_exercise_preference(exercise_name);
CREATE INDEX idx_user_exercise_preference_should_avoid ON user_exercise_preference(should_avoid);

-- Add composite indexes for common query patterns
CREATE INDEX idx_user_exercise_preference_user_avoid ON user_exercise_preference(user_id, should_avoid); 