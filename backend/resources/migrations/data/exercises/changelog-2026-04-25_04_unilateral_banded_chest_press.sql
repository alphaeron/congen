--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Unilateral Banded Chest Press
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Unilateral Banded Chest Press',
  'Using a resistance band with a handle, anchor it so that it’s at shoulder height.  Facing away from the anchor, hold the handle. Then, step forward until there is tension in the band.  With your hand at the side of your chest and your chest up, press the band forward until your arm is straight.  Then, slowly return the band to its original position.',
  'isolation',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Unilateral Banded Chest Press', 'triceps'),
  ('Unilateral Banded Chest Press', 'obliques'),
  ('Unilateral Banded Chest Press', 'serratus anterior');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Unilateral Banded Chest Press', 'bands');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Unilateral Banded Chest Press', 'isolation', 'dynamic_effort'),
  ('Unilateral Banded Chest Press', 'isolation', 'maximal_effort');

