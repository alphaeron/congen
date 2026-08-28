--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Dumbbell Triceps Kickbacks
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Triceps Kickbacks',
  'Bent-over triceps extension performed with dumbbells.',
  'horizontal_push',
  true,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Triceps Kickbacks', 'triceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Triceps Kickbacks', 'dumbbells');
