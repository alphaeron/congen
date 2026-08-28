--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Dumbbell Cuban Press
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Cuban Press',
  'External rotation movement followed by overhead press, targeting rotator cuff and deltoids.',
  'vertical_push',
  true,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Cuban Press', 'rotator cuff'),
  ('Dumbbell Cuban Press', 'rear deltoid'),
  ('Dumbbell Cuban Press', 'lateral deltoid'),
  ('Dumbbell Cuban Press', 'anterior deltoid');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Cuban Press', 'dumbbells');
