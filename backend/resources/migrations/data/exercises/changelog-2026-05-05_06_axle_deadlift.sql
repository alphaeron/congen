--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Axle Deadlift
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Axle Deadlift',
  'Pull a heavy implement from the ground. Axles require more grip strength due to thickness and shape.',
  'hinge',
  false,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Axle Deadlift', 'hamstrings'),
  ('Axle Deadlift', 'glutes'),
  ('Axle Deadlift', 'upper back'),
  ('Axle Deadlift', 'traps'),
  ('Axle Deadlift', 'forearms'),
  ('Axle Deadlift', 'rectus abdominis'),
  ('Axle Deadlift', 'obliques');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Axle Deadlift', 'axle');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Axle Deadlift', 'hinge', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Axle Deadlift', 'hinge', 'maximal_effort');
