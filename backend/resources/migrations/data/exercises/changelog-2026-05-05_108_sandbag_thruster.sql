--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Sandbag Thruster
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Thruster',
  'Perform a squat with sandbag, then press overhead in one motion.',
  'plyometric',
  false,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Thruster', 'quadriceps'),
  ('Sandbag Thruster', 'glutes'),
  ('Sandbag Thruster', 'hamstrings'),
  ('Sandbag Thruster', 'anterior deltoid'),
  ('Sandbag Thruster', 'lateral deltoid'),
  ('Sandbag Thruster', 'triceps'),
  ('Sandbag Thruster', 'rectus abdominis');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Thruster', 'sandbag');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Thruster', 'plyometric', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Thruster', 'plyometric', 'maximal_effort');
