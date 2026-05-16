--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Dips
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dips',
  'Bodyweight triceps exercise performed on parallel bars or bench.',
  'vertical_push',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dips', 'triceps'),
  ('Dips', 'anterior deltoid'),
  ('Dips', 'pec major');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dips', 'dip bars');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Dips', 'vertical_push', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Dips', 'vertical_push', 'maximal_effort');
