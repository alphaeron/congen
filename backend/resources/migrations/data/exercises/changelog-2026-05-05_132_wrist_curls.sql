--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Wrist Curls
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Wrist Curls',
  'Flexion/extension of wrists to strengthen forearm muscles.',
  'isolation',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Wrist Curls', 'biceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Wrist Curls', 'dumbbells');
