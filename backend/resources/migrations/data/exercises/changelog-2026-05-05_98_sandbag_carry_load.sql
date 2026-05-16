--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Sandbag Carry Load
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Carry Load',
  'Pick up sandbag from ground and either carry (bear hug or shoulder) or load it to platform or over bar.',
  'carry',
  false,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Carry Load', 'rectus abdominis'),
  ('Sandbag Carry Load', 'obliques'),
  ('Sandbag Carry Load', 'anterior deltoid'),
  ('Sandbag Carry Load', 'glutes'),
  ('Sandbag Carry Load', 'upper back'),
  ('Sandbag Carry Load', 'forearms');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Carry Load', 'sandbag');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Carry Load', 'carry', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Carry Load', 'carry', 'maximal_effort');
