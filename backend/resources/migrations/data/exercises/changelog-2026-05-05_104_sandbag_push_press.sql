--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Sandbag Push Press
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Push Press',
  'Dip the knees and use leg drive to press sandbag overhead.',
  'vertical_push',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Push Press', 'anterior deltoid'),
  ('Sandbag Push Press', 'lateral deltoid'),
  ('Sandbag Push Press', 'triceps'),
  ('Sandbag Push Press', 'quadriceps'),
  ('Sandbag Push Press', 'glutes'),
  ('Sandbag Push Press', 'rectus abdominis');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Push Press', 'sandbag');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Push Press', 'vertical_push', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Push Press', 'vertical_push', 'maximal_effort');
