--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Sandbag Get-Up
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Get-Up',
  'Start on the ground holding sandbag, stand up with it held overhead or on shoulder.',
  'core',
  false,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Get-Up', 'rectus abdominis'),
  ('Sandbag Get-Up', 'obliques'),
  ('Sandbag Get-Up', 'quadriceps'),
  ('Sandbag Get-Up', 'anterior deltoid'),
  ('Sandbag Get-Up', 'hip flexors'),
  ('Sandbag Get-Up', 'glutes');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Get-Up', 'sandbag');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Get-Up', 'core', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Get-Up', 'core', 'maximal_effort');
