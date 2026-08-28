--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Banded Lateral Raise
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Banded Lateral Raise',
  'Stand with feet shoulder-width apart, knees slightly bent, and a slight forward lean from the hips to maximize lateral deltoid engagement.  Hold the band in one hand in front of your hips or at your side with a neutral grip (palms facing inward), and a slight, fixed bend in the elbows.  Exhale and raise your hand out to the side, leading with the elbows, until your arms are roughly parallel to the floor.  Inhale as you slowly lower your arm back to the starting position under control.',
  'isolation',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Banded Lateral Raise', 'lateral deltoid'),
  ('Banded Lateral Raise', 'serratus anterior'),
  ('Banded Lateral Raise', 'traps');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Banded Lateral Raise', 'bands');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Banded Lateral Raise', 'isolation', 'dynamic_effort'),
  ('Banded Lateral Raise', 'isolation', 'maximal_effort');

