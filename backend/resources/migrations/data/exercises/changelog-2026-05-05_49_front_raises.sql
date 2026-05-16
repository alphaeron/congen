--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Front Raises
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Front Raises',
  'Lift dumbbells or weights in front of your body to shoulder height.',
  'vertical_push',
  true,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Front Raises', 'anterior deltoid'),
  ('Front Raises', 'serratus anterior');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Front Raises', 'dumbbells');
