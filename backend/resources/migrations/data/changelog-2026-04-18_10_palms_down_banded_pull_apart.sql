--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Palms Down Banded Pull Apart
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Palms Down Banded Pull Apart',
  'Hold a band with your palms facing down and raise them to just below shoulder height. Pull the band apart until the middle of the band touches your chest and slowly return back to starting position.',
  'isolation',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Palms Down Banded Pull Apart', 'rotator cuff'),
  ('Palms Down Banded Pull Apart', 'rhomboids'),
  ('Palms Down Banded Pull Apart', 'rear deltoid');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Palms Down Banded Pull Apart', 'bands');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Palms Down Banded Pull Apart', 'isolation', 'dynamic_effort'),
  ('Palms Down Banded Pull Apart', 'isolation', 'maximal_effort');

