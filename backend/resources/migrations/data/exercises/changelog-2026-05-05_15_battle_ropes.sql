--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Battle Ropes
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Battle Ropes',
  'Alternate or double-wave motion using heavy ropes for arm and shoulder conditioning.',
  'plyometric',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Battle Ropes', 'anterior deltoid'),
  ('Battle Ropes', 'lateral deltoid'),
  ('Battle Ropes', 'pec major'),
  ('Battle Ropes', 'traps'),
  ('Battle Ropes', 'biceps'),
  ('Battle Ropes', 'obliques');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Battle Ropes', 'battle rope');
