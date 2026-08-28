--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Hammer Curl
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Hammer Curl',
  'Neutral-grip dumbbell curl targeting brachialis and biceps.',
  'isolation',
  true,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Hammer Curl', 'biceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Hammer Curl', 'dumbbells');
