--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Straight Arm Pullback
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Straight Arm Pullback',
  'The straight arm pullback exercise is used to strengthen the shoulder extensors. Loop a band around a stable band anchor or squat rack at the height of your hips. While keeping a straight arm (a slight bend is acceptable) pull the band back into shoulder extension. Return to start position to complete a repetition.',
  'isolation',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Straight Arm Pullback', 'teres minor'),
  ('Straight Arm Pullback', 'teres major'),
  ('Straight Arm Pullback', 'triceps');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Straight Arm Pullback', 'bands');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Straight Arm Pullback', 'isolation', 'dynamic_effort'),
  ('Straight Arm Pullback', 'isolation', 'maximal_effort');

