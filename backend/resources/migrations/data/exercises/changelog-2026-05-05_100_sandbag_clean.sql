--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Sandbag Clean
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Clean',
  'Lift the sandbag from the ground to the chest in one powerful motion.',
  'plyometric',
  false,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Clean', 'glutes'),
  ('Sandbag Clean', 'hip flexors'),
  ('Sandbag Clean', 'traps'),
  ('Sandbag Clean', 'biceps'),
  ('Sandbag Clean', 'rectus abdominis'),
  ('Sandbag Clean', 'obliques');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Clean', 'sandbag');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Clean', 'plyometric', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Clean', 'plyometric', 'maximal_effort');
