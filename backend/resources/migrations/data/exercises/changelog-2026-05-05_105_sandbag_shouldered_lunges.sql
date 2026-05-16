--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Sandbag Shouldered Lunges
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Shouldered Lunges',
  'Place sandbag on one shoulder and perform walking or stationary lunges.',
  'squat',
  true,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Shouldered Lunges', 'quadriceps'),
  ('Sandbag Shouldered Lunges', 'glutes'),
  ('Sandbag Shouldered Lunges', 'hamstrings'),
  ('Sandbag Shouldered Lunges', 'rectus abdominis'),
  ('Sandbag Shouldered Lunges', 'obliques'),
  ('Sandbag Shouldered Lunges', 'traps');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Shouldered Lunges', 'sandbag');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Shouldered Lunges', 'squat', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Shouldered Lunges', 'squat', 'maximal_effort');
