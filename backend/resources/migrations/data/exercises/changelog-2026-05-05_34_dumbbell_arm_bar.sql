--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Dumbbell Arm Bar
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Arm Bar',
  'Lie on your side holding a dumbbell and rotate your arm in a controlled arc to improve shoulder mobility and stability.',
  'isolation',
  true,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Arm Bar', 'anterior deltoid'),
  ('Dumbbell Arm Bar', 'lateral deltoid'),
  ('Dumbbell Arm Bar', 'rear deltoid'),
  ('Dumbbell Arm Bar', 'rotator cuff'),
  ('Dumbbell Arm Bar', 'serratus anterior');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Arm Bar', 'dumbbells');
