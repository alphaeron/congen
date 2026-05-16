--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Sandbag Over Shoulder
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Over Shoulder',
  'Clean and throw the sandbag over one shoulder; reset and repeat.',
  'plyometric',
  true,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Over Shoulder', 'glutes'),
  ('Sandbag Over Shoulder', 'hamstrings'),
  ('Sandbag Over Shoulder', 'upper back'),
  ('Sandbag Over Shoulder', 'triceps'),
  ('Sandbag Over Shoulder', 'rectus abdominis'),
  ('Sandbag Over Shoulder', 'obliques');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Over Shoulder', 'sandbag');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Over Shoulder', 'plyometric', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Over Shoulder', 'plyometric', 'maximal_effort');
