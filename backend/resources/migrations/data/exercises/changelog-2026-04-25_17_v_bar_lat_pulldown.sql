--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise V-Bar Lat Pulldown
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'V-Bar Lat Pulldown',
  'Grasp the bar with an overhand grip.  Pull the shoulder blades down and back, bringing the bar down to your upper chest.  Slowly return the bar to the starting position, resisting the urge to let the weight drop.  Keep your torso upright or with a slight 10–20 degree lean back, avoiding excessive leaning or momentum throughout the exercise.',
  'vertical_pull',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('V-Bar Lat Pulldown', 'lats');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('V-Bar Lat Pulldown', 'Lat Pulldown');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('V-Bar Lat Pulldown', 'vertical_pull', 'dynamic_effort'),
  ('V-Bar Lat Pulldown', 'vertical_pull', 'maximal_effort');

