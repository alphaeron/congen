--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Axle Press
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Axle Press',
  'Press a thick axle bar. The thick grip taxes the forearms and clean is harder due to bar size.',
  'vertical_push',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Axle Press', 'forearms'),
  ('Axle Press', 'anterior deltoid'),
  ('Axle Press', 'lateral deltoid'),
  ('Axle Press', 'triceps'),
  ('Axle Press', 'upper back'),
  ('Axle Press', 'glutes'),
  ('Axle Press', 'rectus abdominis');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Axle Press', 'axle');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Axle Press', 'vertical_push', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Axle Press', 'vertical_push', 'maximal_effort');
