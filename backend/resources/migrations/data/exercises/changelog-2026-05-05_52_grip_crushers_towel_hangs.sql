--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Grip Crushers / Towel Hangs
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Grip Crushers / Towel Hangs',
  'Grip training using towels or static holds for grip endurance.',
  'isolation',
  true,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Grip Crushers / Towel Hangs', 'biceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Grip Crushers / Towel Hangs', 'pull-up bar');
