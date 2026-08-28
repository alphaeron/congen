--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Sandbag Squat
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Squat',
  'Hold the sandbag at chest or shoulder height and perform a squat.',
  'squat',
  false,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Squat', 'quadriceps'),
  ('Sandbag Squat', 'glutes'),
  ('Sandbag Squat', 'hamstrings'),
  ('Sandbag Squat', 'rectus abdominis'),
  ('Sandbag Squat', 'obliques');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Squat', 'sandbag');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Squat', 'squat', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Squat', 'squat', 'maximal_effort');
