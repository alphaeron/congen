--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Incline Dumbbell T Raise
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Incline Dumbbell T Raise',
  'Set an adjustable bench to 30 to 45 degrees. Lie face down with chest on the pad.  Hold a light dumbbell in each hand with arms hanging straight down.  Brace your core. Press chest into the pad.  Lift both arms straight out to the sides until they reach shoulder height (T position), palms facing down.  Keep your palms pointing down throughout.  Squeeze your shoulder blades together.  Lower under control.',
  'isolation',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Incline Dumbbell T Raise', 'rear deltoid'),
  ('Incline Dumbbell T Raise', 'rhomboids');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Incline Dumbbell T Raise', 'dumbbells');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Incline Dumbbell T Raise', 'isolation', 'dynamic_effort'),
  ('Incline Dumbbell T Raise', 'isolation', 'maximal_effort');
