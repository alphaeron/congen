--liquibase formatted sql

--changeset alphaeron:2 labels:prod,test
--comment: Add exercise_workout_type relationship for dynamic_effort and maximal_effort workouts, with movement_type.

CREATE TABLE exercise_workout_type (
  exercise_name VARCHAR(255) NOT NULL,
  movement_type VARCHAR(50) NOT NULL,
  workout_type VARCHAR(20) NOT NULL CHECK (workout_type IN ('dynamic_effort', 'maximal_effort')),
  PRIMARY KEY (exercise_name, movement_type, workout_type),
  CONSTRAINT fk_exercise_workout_type_exercise FOREIGN KEY(exercise_name) REFERENCES exercise(name) ON DELETE CASCADE
);

-- Add indexes for better performance
CREATE INDEX idx_exercise_workout_type_workout_type ON exercise_workout_type(workout_type);
CREATE INDEX idx_exercise_workout_type_movement_type ON exercise_workout_type(movement_type);
CREATE INDEX idx_exercise_workout_type_movement_workout ON exercise_workout_type(movement_type, workout_type); 