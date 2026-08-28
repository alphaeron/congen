--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Sandbag Zercher Squat
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Zercher Squat',
  'Hold the sandbag in the elbows and squat down with good form.',
  'squat',
  false,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Zercher Squat', 'biceps'),
  ('Sandbag Zercher Squat', 'quadriceps'),
  ('Sandbag Zercher Squat', 'glutes'),
  ('Sandbag Zercher Squat', 'rectus abdominis'),
  ('Sandbag Zercher Squat', 'obliques');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Zercher Squat', 'sandbag');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Zercher Squat', 'squat', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Zercher Squat', 'squat', 'maximal_effort');
