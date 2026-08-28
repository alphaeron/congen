--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Side-Lying External Rotation
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Side-Lying External Rotation',
  'Lie down on your side and hold a dumbbell with the top arm. Keep the elbow tucked in on the side of your body and raise the weight into maximum external rotation (within tolerable limits). Slowly lower the weight until it rests in front of your belly and repeat.',
  'isolation',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Side-Lying External Rotation', 'rear deltoid'),
  ('Side-Lying External Rotation', 'rotator cuff'),
  ('Side-Lying External Rotation', 'rhomboids');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Side-Lying External Rotation', 'dumbbells');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Side-Lying External Rotation', 'isolation', 'dynamic_effort'),
  ('Side-Lying External Rotation', 'isolation', 'maximal_effort');

