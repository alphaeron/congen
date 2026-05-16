--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Standing Overhead Dumbbell Triceps Extension
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Standing Overhead Dumbbell Triceps Extension',
  'Single or two-arm overhead triceps extension with dumbbell.',
  'vertical_push',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Standing Overhead Dumbbell Triceps Extension', 'triceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Standing Overhead Dumbbell Triceps Extension', 'dumbbells');
