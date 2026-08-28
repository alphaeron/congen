--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Dumbbell Biceps Curl
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Biceps Curl',
  'Classic dumbbell curl targeting biceps.',
  'isolation',
  true,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Biceps Curl', 'biceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Biceps Curl', 'dumbbells');
