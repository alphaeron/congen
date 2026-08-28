--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Triangle Bar Low Row
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Triangle Bar Low Row',
  'Pull the handle towards your body, keeping your elbows close to your ribs, until the handle is just in front of your chest.  Slowly extend your arms to return to start.',
  'horizontal_pull',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Triangle Bar Low Row', 'lats'),
  ('Triangle Bar Low Row', 'rear deltoid'),
  ('Triangle Bar Low Row', 'rhomboids');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Triangle Bar Low Row', 'Triangle Bar Handle');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Triangle Bar Low Row', 'horizontal_pull', 'dynamic_effort'),
  ('Triangle Bar Low Row', 'horizontal_pull', 'maximal_effort');
