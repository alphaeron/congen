--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Weighted Pull-Up (towel/gi variation)
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Weighted Pull-Up (towel/gi variation)',
  'Perform pull-up adding weight or using towel/gi to increase grip and challenge.',
  'vertical_pull',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Weighted Pull-Up (towel/gi variation)', 'lats'),
  ('Weighted Pull-Up (towel/gi variation)', 'biceps'),
  ('Weighted Pull-Up (towel/gi variation)', 'rhomboids'),
  ('Weighted Pull-Up (towel/gi variation)', 'traps');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Weighted Pull-Up (towel/gi variation)', 'pull-up bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Weighted Pull-Up (towel/gi variation)', 'vertical_pull', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Weighted Pull-Up (towel/gi variation)', 'vertical_pull', 'maximal_effort');
