--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Dumbbell Shrug
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Shrug',
  'Lift dumbbells by raising your shoulders up toward your ears, targeting the trapezius muscles.',
  'isolation',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Shrug', 'traps'),
  ('Dumbbell Shrug', 'rhomboids');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Shrug', 'dumbbells');
