--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Rear Deltoid Row
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Rear Deltoid Row',
  'Start with a band gripped in your hand, and your arm across your chest.  Keeping your arm straight, pull the band until the arm is straight out at your side, focusing on using your rear shoulder to pull.',
  'horizontal_pull',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Rear Deltoid Row', 'rear deltoid');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Rear Deltoid Row', 'bands');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Rear Deltoid Row', 'horizontal_pull', 'dynamic_effort'),
  ('Rear Deltoid Row', 'horizontal_pull', 'maximal_effort');

