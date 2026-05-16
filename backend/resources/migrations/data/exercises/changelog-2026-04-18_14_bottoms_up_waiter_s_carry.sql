--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Bottoms Up Waiter's Carry
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Bottoms Up Waiters Carry',
  'Curl the bell in the bottom-up position, with your elbow bent at 90 degrees, your forearm vertical, your wrist neutral, and your hands stacked in line with the bell.  The handle should rest firmly in the fleshy part of your palm. Then, hold on tight.  Brace your core, glutes, and lats by lowering your rib cage and avoiding overarching your lower back.  Walk slowly, keeping your hips level and torso upright.',
  'isolation',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Bottoms Up Waiters Carry', 'obliques'),
  ('Bottoms Up Waiters Carry', 'rotator cuff');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Bottoms Up Waiters Carry', 'kettlebell');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Bottoms Up Waiters Carry', 'isolation', 'dynamic_effort'),
  ('Bottoms Up Waiters Carry', 'isolation', 'maximal_effort');
