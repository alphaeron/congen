--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Zercher Carry
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Zercher Carry',
  'Carry an object in the crooks of your elbows for time or distance.',
  'carry',
  false,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Zercher Carry', 'biceps'),
  ('Zercher Carry', 'upper back'),
  ('Zercher Carry', 'rectus abdominis'),
  ('Zercher Carry', 'obliques'),
  ('Zercher Carry', 'quadriceps'),
  ('Zercher Carry', 'glutes');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Zercher Carry', 'power bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Zercher Carry', 'carry', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Zercher Carry', 'carry', 'maximal_effort');
