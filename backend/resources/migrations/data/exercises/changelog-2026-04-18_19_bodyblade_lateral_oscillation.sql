--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Bodyblade Lateral Oscillation
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Bodyblade Lateral Oscillation',
  'Hold the body blade with your palm down straight out to your side.  Oscillate the BodyBlade up and down while keeping your arm straight.',
  'isolation',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Bodyblade Lateral Oscillation', 'lateral deltoid');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Bodyblade Lateral Oscillation', 'bodyblade');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Bodyblade Lateral Oscillation', 'isolation', 'dynamic_effort'),
  ('Bodyblade Lateral Oscillation', 'isolation', 'maximal_effort');

