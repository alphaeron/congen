--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Barbell Shrugs
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Barbell Shrugs',
  'Stand with your feet shoulder-width apart, holding a Barbell in front of you with an overhand grip, arms fully extended.  Keep your arms straight as you shrug your shoulders upward as high as possible.  Squeeze your traps at the top of the movement.  Lower the barbell back down under control.',
  'isolation',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Barbell Shrugs', 'traps'),
  ('Barbell Shrugs', 'rear deltoid'),
  ('Barbell Shrugs', 'rhomboids');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Barbell Shrugs', 'power bar');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Barbell Shrugs', 'isolation', 'dynamic_effort'),
  ('Barbell Shrugs', 'isolation', 'maximal_effort');

