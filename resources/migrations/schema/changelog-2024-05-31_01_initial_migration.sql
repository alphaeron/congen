--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Initial changeset for browsing exercise data.
-- Fake rollback - the initial changelog does not make sense to roll back
--rollback SELECT 1

CREATE TABLE exercise (
  name VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  movement_type VARCHAR(50) NOT NULL,
  is_unilateral BOOLEAN NOT NULL DEFAULT FALSE,
  is_upper BOOLEAN NOT NULL DEFAULT FALSE,
  is_accessory BOOLEAN NOT NULL DEFAULT FALSE,
  PRIMARY KEY (name)
);

CREATE TABLE muscle (
  name VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  PRIMARY KEY (name)
);

CREATE TABLE equipment (
  name VARCHAR(255) NOT NULL,
  description TEXT NOT NULL,
  PRIMARY KEY (name)
);

CREATE TABLE exercise_muscle (
  exercise_name VARCHAR(255) NOT NULL,
  muscle_name VARCHAR(255) NOT NULL,
  PRIMARY KEY (exercise_name, muscle_name),
  CONSTRAINT fk_exercise_muscle_exercise FOREIGN KEY(exercise_name) REFERENCES exercise(name) ON DELETE CASCADE,
  CONSTRAINT fk_exercise_muscle_muscle FOREIGN KEY(muscle_name) REFERENCES muscle(name) ON DELETE CASCADE
);

CREATE TABLE exercise_equipment (
  exercise_name VARCHAR(255) NOT NULL,
  equipment_name VARCHAR(255) NOT NULL,
  PRIMARY KEY (exercise_name, equipment_name),
  CONSTRAINT fk_exercise_equipment_exercise FOREIGN KEY(exercise_name) REFERENCES exercise(name) ON DELETE CASCADE,
  CONSTRAINT fk_exercise_equipment_equipment FOREIGN KEY(equipment_name) REFERENCES equipment(name) ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_exercise_movement_type ON exercise(movement_type);
CREATE INDEX idx_exercise_is_unilateral ON exercise(is_unilateral);
CREATE INDEX idx_exercise_is_upper ON exercise(is_upper);
CREATE INDEX idx_exercise_is_accessory ON exercise(is_accessory);
CREATE INDEX idx_exercise_muscle_muscle_name ON exercise_muscle(muscle_name);
CREATE INDEX idx_exercise_equipment_equipment_name ON exercise_equipment(equipment_name);

-- Add composite indexes for common query patterns
CREATE INDEX idx_exercise_movement_accessory ON exercise(movement_type, is_accessory);
CREATE INDEX idx_exercise_upper_unilateral ON exercise(is_upper, is_unilateral);
