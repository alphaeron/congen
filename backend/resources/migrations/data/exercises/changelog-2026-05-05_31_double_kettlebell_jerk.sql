--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Double Kettlebell Jerk
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Double Kettlebell Jerk',
  'Explosive overhead pressing of two kettlebells from rack via dip and drive.',
  'plyometric',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Double Kettlebell Jerk', 'anterior deltoid'),
  ('Double Kettlebell Jerk', 'lateral deltoid'),
  ('Double Kettlebell Jerk', 'triceps'),
  ('Double Kettlebell Jerk', 'glutes'),
  ('Double Kettlebell Jerk', 'hamstrings'),
  ('Double Kettlebell Jerk', 'quadriceps'),
  ('Double Kettlebell Jerk', 'rectus abdominis');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Double Kettlebell Jerk', 'kettlebell');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Double Kettlebell Jerk', 'plyometric', 'dynamic_effort');
