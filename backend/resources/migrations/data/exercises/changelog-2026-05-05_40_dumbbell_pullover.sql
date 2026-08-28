--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Dumbbell Pullover
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Pullover',
  'Lying movement bringing dumbbell from behind head to over chest, targeting chest and lats.',
  'horizontal_pull',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Pullover', 'pec major'),
  ('Dumbbell Pullover', 'lats'),
  ('Dumbbell Pullover', 'serratus anterior');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Pullover', 'dumbbells');
