--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Dumbbell Floor Press
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Floor Press',
  'Pressing from floor with dumbbells emphasizing triceps and chest.',
  'horizontal_push',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Floor Press', 'triceps'),
  ('Dumbbell Floor Press', 'pec major'),
  ('Dumbbell Floor Press', 'anterior deltoid');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Floor Press', 'dumbbells');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Dumbbell Floor Press', 'horizontal_push', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Dumbbell Floor Press', 'horizontal_push', 'maximal_effort');
