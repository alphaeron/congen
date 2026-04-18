--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Palms Up Banded Pull Apart
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Palms Up Banded Pull Apart',
  'Hold a band with your palms facing up and raise them to just below shoulder height. Pull the band apart until the middle of the band touches your chest and slowly return back to starting position.',
  'isolation',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Palms Up Banded Pull Apart', 'rear deltoid'),
  ('Palms Up Banded Pull Apart', 'rotator cuff'),
  ('Palms Up Banded Pull Apart', 'rhomboids');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Palms Up Banded Pull Apart', 'bands');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Palms Up Banded Pull Apart', 'isolation', 'dynamic_effort'),
  ('Palms Up Banded Pull Apart', 'isolation', 'maximal_effort');

