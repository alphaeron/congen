--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Dumbbell Skull Crushers
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Skull Crushers',
  'Lying triceps extension with dumbbells.',
  'horizontal_push',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Skull Crushers', 'triceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Skull Crushers', 'dumbbells');
