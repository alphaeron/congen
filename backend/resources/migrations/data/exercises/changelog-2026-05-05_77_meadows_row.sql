--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Meadows Row
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Meadows Row',
  'Landmine-supported one-arm row, chest at an angle.',
  'horizontal_pull',
  true,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Meadows Row', 'lats'),
  ('Meadows Row', 'rhomboids'),
  ('Meadows Row', 'rear deltoid');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Meadows Row', 'landmine');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Meadows Row', 'horizontal_pull', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Meadows Row', 'horizontal_pull', 'maximal_effort');
