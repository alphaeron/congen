--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Plank Rotations
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Plank Rotations',
  'Start in a plank position with your hands under your shoulders, core engaged, and feet shoulder-width apart or slightly wider.  Lift your left hand off the floor and rotate onto the sides of your feet, turning your hips and shoulders to face to the left so you’re in a side plank with your feet staggered. Reach your left arm toward the ceiling.  Slowly return to the starting position.  Repeat on the both sides, alternating throughout the set. Throughout the move, think about keeping both shoulder blades pulling back and down.',
  'core',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Plank Rotations', 'rectus abdominis'),
  ('Plank Rotations', 'rhomboids'),
  ('Plank Rotations', 'rotator cuff');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Plank Rotations', 'core', 'dynamic_effort'),
  ('Plank Rotations', 'core', 'maximal_effort');

