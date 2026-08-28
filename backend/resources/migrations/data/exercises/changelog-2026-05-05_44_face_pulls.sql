--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Face Pulls
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Face Pulls',
  'Pulling band or cable toward face to work upper back and shoulder health.',
  'horizontal_pull',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Face Pulls', 'rear deltoid'),
  ('Face Pulls', 'rotator cuff'),
  ('Face Pulls', 'rhomboids');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Face Pulls', 'bands');
