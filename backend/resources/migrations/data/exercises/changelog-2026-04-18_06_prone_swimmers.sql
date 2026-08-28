--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Prone Swimmers
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Prone Swimmers',
  'Exercise description: Lie face down on the floor with your arms extended straight overhead and legs fully extended.  Keep your forehead gently resting on the ground to maintain a neutral neck position.  Engage your core muscles to stabilize your torso throughout the movement.  Simultaneously lift your right arm and left leg off the ground, keeping them straight.  Lower them back to the starting position and immediately lift your left arm and right leg.  Continue alternating sides in a controlled manner, simulating a swimming motion.',
  'isolation',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Prone Swimmers', 'rear deltoid'),
  ('Prone Swimmers', 'teres minor'),
  ('Prone Swimmers', 'teres major');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Prone Swimmers', 'isolation', 'dynamic_effort'),
  ('Prone Swimmers', 'isolation', 'maximal_effort');

