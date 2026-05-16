--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Pull-Ups
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Pull-Ups',
  'Standard pull-up with overhand grip targeting lats and upper back.',
  'vertical_pull',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Pull-Ups', 'lats'),
  ('Pull-Ups', 'rhomboids'),
  ('Pull-Ups', 'traps'),
  ('Pull-Ups', 'rear deltoid'),
  ('Pull-Ups', 'biceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Pull-Ups', 'pull-up bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Pull-Ups', 'vertical_pull', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Pull-Ups', 'vertical_pull', 'maximal_effort');
