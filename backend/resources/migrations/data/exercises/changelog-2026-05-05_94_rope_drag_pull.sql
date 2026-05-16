--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Rope Drag/ Pull
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Rope Drag/ Pull',
  'Pull a rope or chain across a distance, often with a backward lean.',
  'hinge',
  false,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Rope Drag/ Pull', 'quadriceps'),
  ('Rope Drag/ Pull', 'calves'),
  ('Rope Drag/ Pull', 'glutes'),
  ('Rope Drag/ Pull', 'rectus abdominis'),
  ('Rope Drag/ Pull', 'obliques'),
  ('Rope Drag/ Pull', 'upper back'),
  ('Rope Drag/ Pull', 'forearms');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Rope Drag/ Pull', 'rope');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Rope Drag/ Pull', 'hinge', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Rope Drag/ Pull', 'hinge', 'maximal_effort');
