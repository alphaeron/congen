--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Double Incline Flye
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Double Incline Flye',
  'Chest flye movement performed on an inclined bench with dumbbells.',
  'horizontal_push',
  false,
  true,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Double Incline Flye', 'pec major'),
  ('Double Incline Flye', 'anterior deltoid'),
  ('Double Incline Flye', 'serratus anterior');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Double Incline Flye', 'dumbbells');
