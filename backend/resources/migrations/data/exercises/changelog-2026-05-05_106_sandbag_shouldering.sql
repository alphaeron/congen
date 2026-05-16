--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Sandbag Shouldering
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Shouldering',
  'Clean the sandbag to one shoulder and balance it; switch sides.',
  'plyometric',
  true,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Shouldering', 'traps'),
  ('Sandbag Shouldering', 'anterior deltoid'),
  ('Sandbag Shouldering', 'lateral deltoid'),
  ('Sandbag Shouldering', 'obliques'),
  ('Sandbag Shouldering', 'rectus abdominis'),
  ('Sandbag Shouldering', 'glutes');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Shouldering', 'sandbag');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Shouldering', 'plyometric', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Shouldering', 'plyometric', 'maximal_effort');
