--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise BodyBlade Front Oscillation
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'BodyBlade Front Oscillation',
  'Hold the body blade with your palm down straight out in front of your body.  Oscillate the BodyBlade up and down while keeping your arm straight.',
  'isolation',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('BodyBlade Front Oscillation', 'anterior deltoid');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('BodyBlade Front Oscillation', 'bodyblade');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('BodyBlade Front Oscillation', 'isolation', 'dynamic_effort'),
  ('BodyBlade Front Oscillation', 'isolation', 'maximal_effort');

