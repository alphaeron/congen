--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Forearm Wall Slides
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Forearm Wall Slides',
  'Face a wall about a foot away.  Bend your arms and put them on a foam roller on the wall, with the elbow located a few inches below your shoulder.  With your palms facing each other, slowly slide your elbows up the wall until your arms are straight.  Do not let your arms move closer to each other.',
  'isolation',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Forearm Wall Slides', 'serratus anterior'),
  ('Forearm Wall Slides', 'rhomboids');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Forearm Wall Slides', 'isolation', 'dynamic_effort'),
  ('Forearm Wall Slides', 'isolation', 'maximal_effort');

