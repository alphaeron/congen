--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Farmers Walk
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Farmers Walk',
  'Pick up a heavy implement in each hand and walk quickly while maintaining posture and grip.',
  'carry',
  false,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Farmers Walk', 'forearms'),
  ('Farmers Walk', 'traps'),
  ('Farmers Walk', 'upper back'),
  ('Farmers Walk', 'glutes'),
  ('Farmers Walk', 'calves'),
  ('Farmers Walk', 'rectus abdominis'),
  ('Farmers Walk', 'obliques');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Farmers Walk', 'dumbbells');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Farmers Walk', 'carry', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Farmers Walk', 'carry', 'maximal_effort');
