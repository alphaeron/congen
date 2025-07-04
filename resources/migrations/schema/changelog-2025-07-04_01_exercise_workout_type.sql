--liquibase formatted sql

--changeset John Matty:2 labels:prod,test
--comment: Add exercise_workout_type relationship for dynamic_effort and maximal_effort workouts, with movement_type.
CREATE TABLE exercise_workout_type (
  exercise_name VARCHAR NOT NULL,
  movement_type VARCHAR NOT NULL,
  workout_type VARCHAR NOT NULL CHECK (workout_type IN ('dynamic_effort', 'maximal_effort')),
  PRIMARY KEY (exercise_name, movement_type, workout_type),
  CONSTRAINT fk_exercise FOREIGN KEY(exercise_name) REFERENCES exercise(name)
); 