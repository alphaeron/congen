--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise U-Bar Low Row
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'U-Bar Low Row',
  'Pull the handle towards your body, keeping your elbows close to your ribs, until the handle is just in front of your chest.  Slowly extend your arms to return to start.',
  'horizontal_pull',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('U-Bar Low Row', 'lats'),
  ('U-Bar Low Row', 'rear deltoid'),
  ('U-Bar Low Row', 'rhomboids');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('U-Bar Low Row', 'U-Bar Handle');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('U-Bar Low Row', 'horizontal_pull', 'dynamic_effort'),
  ('U-Bar Low Row', 'horizontal_pull', 'maximal_effort');
