--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Trap Bar Shrugs
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Trap Bar Shrugs',
  'Stand inside a trap bar (also known as a hex bar) with your feet hip-width apart.  Grasp the handles using a pronated grip.  Keep your arms straight as you shrug your shoulders upward as high as possible.  Squeeze your traps at the top of the movement.  Lower the trap bar back down under control.',
  'isolation',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Trap Bar Shrugs', 'traps'),
  ('Trap Bar Shrugs', 'rear deltoid'),
  ('Trap Bar Shrugs', 'rhomboids');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Trap Bar Shrugs', 'trap bar');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Trap Bar Shrugs', 'isolation', 'dynamic_effort'),
  ('Trap Bar Shrugs', 'isolation', 'maximal_effort');

