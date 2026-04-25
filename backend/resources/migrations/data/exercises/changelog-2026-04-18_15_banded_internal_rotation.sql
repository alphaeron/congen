--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Banded Internal Rotation
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Banded Internal Rotation',
  'Assume a standing position with your elbow flexed to 90 degrees while holding a band anchored to a sturdy object.  Rotate your hand in towards your body while keeping your elbow tight to your torso.  Slowly lower the band back to the starting position under control.',
  'isolation',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Banded Internal Rotation', 'rotator cuff');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES
  ('Banded Internal Rotation', 'bands');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES
  ('Banded Internal Rotation', 'isolation', 'dynamic_effort'),
  ('Banded Internal Rotation', 'isolation', 'maximal_effort');

