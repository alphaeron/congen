--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Farmers Carry
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Farmers Carry',
  'Walk while holding heavy weights in each hand, improving grip and stability.',
  'core',
  false,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Farmers Carry', 'biceps'),
  ('Farmers Carry', 'traps'),
  ('Farmers Carry', 'anterior deltoid'),
  ('Farmers Carry', 'lateral deltoid'),
  ('Farmers Carry', 'erector spinae'),
  ('Farmers Carry', 'obliques');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Farmers Carry', 'dumbbells');
