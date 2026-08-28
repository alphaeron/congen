--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Sandbag Deadlift
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Deadlift',
  'Lift the sandbag from the ground by hugging or using handles.',
  'hinge',
  false,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Deadlift', 'hamstrings'),
  ('Sandbag Deadlift', 'glutes'),
  ('Sandbag Deadlift', 'upper back'),
  ('Sandbag Deadlift', 'forearms'),
  ('Sandbag Deadlift', 'rectus abdominis'),
  ('Sandbag Deadlift', 'obliques');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Deadlift', 'sandbag');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Deadlift', 'hinge', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Deadlift', 'hinge', 'maximal_effort');
