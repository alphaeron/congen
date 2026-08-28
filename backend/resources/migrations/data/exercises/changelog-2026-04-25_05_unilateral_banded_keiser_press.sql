--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Unilateral Banded Keiser Press
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Unilateral Banded Keiser Press',
  'Using a resistance band with a handle, anchor it so that it’s at shoulder height.  Put the band on one end of a broomstick.  Facing away from the anchor, hold the broomstick. Then, step forward until there is tension in the band.  With your your chest up, press the broomstick forward (similar to a bench press motion).  Then, slowly return to the starting position.',
  'isolation',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Unilateral Banded Keiser Press', 'triceps'),
  ('Unilateral Banded Keiser Press', 'obliques'),
  ('Unilateral Banded Keiser Press', 'serratus anterior');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Unilateral Banded Keiser Press', 'bands');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Unilateral Banded Keiser Press', 'isolation', 'dynamic_effort'),
  ('Unilateral Banded Keiser Press', 'isolation', 'maximal_effort');

