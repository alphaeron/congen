--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Dumbbell Lateral Raise
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Lateral Raise',
  'Stand with feet shoulder-width apart, knees slightly bent, and a slight forward lean from the hips to maximize lateral deltoid engagement.  Hold dumbbells in front of your hips or at your sides with a neutral grip (palms facing inward), and a slight, fixed bend in the elbows.  Exhale and lift the dumbbells out to the sides, leading with the elbows, until your arms are roughly parallel to the floor.  Inhale as you slowly lower the dumbbells back to the starting position under control.',
  'isolation',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Lateral Raise', 'lateral deltoid'),
  ('Dumbbell Lateral Raise', 'serratus anterior'),
  ('Dumbbell Lateral Raise', 'traps');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Dumbbell Lateral Raise', 'dumbbells');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Dumbbell Lateral Raise', 'isolation', 'dynamic_effort'),
  ('Dumbbell Lateral Raise', 'isolation', 'maximal_effort');

