--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise One‑Arm Dumbbell Row
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'One‑Arm Dumbbell Row',
  'Bent-over rowing motion with a single dumbbell.',
  'horizontal_pull',
  true,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('One‑Arm Dumbbell Row', 'lats'),
  ('One‑Arm Dumbbell Row', 'rhomboids'),
  ('One‑Arm Dumbbell Row', 'traps'),
  ('One‑Arm Dumbbell Row', 'rear deltoid'),
  ('One‑Arm Dumbbell Row', 'biceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('One‑Arm Dumbbell Row', 'dumbbells');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('One‑Arm Dumbbell Row', 'horizontal_pull', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('One‑Arm Dumbbell Row', 'horizontal_pull', 'maximal_effort');
