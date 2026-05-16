--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Kettlebell Clean & Press
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Kettlebell Clean & Press',
  'Clean a kettlebell to rack and press it overhead.',
  'plyometric',
  true,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Kettlebell Clean & Press', 'anterior deltoid'),
  ('Kettlebell Clean & Press', 'lateral deltoid'),
  ('Kettlebell Clean & Press', 'traps'),
  ('Kettlebell Clean & Press', 'lats'),
  ('Kettlebell Clean & Press', 'glutes'),
  ('Kettlebell Clean & Press', 'hamstrings'),
  ('Kettlebell Clean & Press', 'quadriceps'),
  ('Kettlebell Clean & Press', 'rectus abdominis');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Kettlebell Clean & Press', 'kettlebell');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Kettlebell Clean & Press', 'plyometric', 'dynamic_effort');
