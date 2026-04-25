--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Straight Bar Triceps Push-Down
--rollback SELECT 1

INSERT INTO equipment (name, description) VALUES ('Straight Bar Handle', 'Straight bar handle for cable tower.')
ON CONFLICT (name) DO NOTHING;

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Straight Bar Triceps Push-Down',
  'Engage your core and pin your shoulder blades back.  Keep your elbows close to your body throughout the movement.  Take a slight forward lean at the hips.  Exhale and push the bar down until your arms are fully extended.  Focus on using only your triceps to push the weight down, keeping your shoulders and upper arms still.',
  'isolation',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Straight Bar Triceps Push-Down', 'triceps');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Straight Bar Triceps Push-Down', 'Straight Bar Handle');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Straight Bar Triceps Push-Down', 'isolation', 'dynamic_effort'),
  ('Straight Bar Triceps Push-Down', 'isolation', 'maximal_effort');

