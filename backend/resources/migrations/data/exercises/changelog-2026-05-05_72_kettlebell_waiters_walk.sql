--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Kettlebell Waiters Walk
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Kettlebell Waiters Walk',
  'Hold a kettlebell overhead with arm fully extended and walk while keeping shoulders level and core braced.',
  'core',
  true,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Kettlebell Waiters Walk', 'anterior deltoid'),
  ('Kettlebell Waiters Walk', 'lateral deltoid'),
  ('Kettlebell Waiters Walk', 'rear deltoid'),
  ('Kettlebell Waiters Walk', 'traps'),
  ('Kettlebell Waiters Walk', 'rhomboids'),
  ('Kettlebell Waiters Walk', 'rotator cuff'),
  ('Kettlebell Waiters Walk', 'serratus anterior'),
  ('Kettlebell Waiters Walk', 'obliques'),
  ('Kettlebell Waiters Walk', 'erector spinae');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Kettlebell Waiters Walk', 'kettlebell');
