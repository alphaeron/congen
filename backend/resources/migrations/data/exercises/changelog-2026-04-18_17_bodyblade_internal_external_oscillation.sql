--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise BodyBlade Internal/External Oscillation
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'BodyBlade Internal/External Oscillation',
  'Hold the body blade with your elbow flexed to 90 degrees in front of your body (perpendicular to your body).  Oscillate the BodyBlade away from and inside of the line that was your starting position while keeping your elbow tight to your torso.',
  'isolation',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('BodyBlade Internal/External Oscillation', 'rotator cuff');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('BodyBlade Internal/External Oscillation', 'bodyblade');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('BodyBlade Internal/External Oscillation', 'isolation', 'dynamic_effort'),
  ('BodyBlade Internal/External Oscillation', 'isolation', 'maximal_effort');
