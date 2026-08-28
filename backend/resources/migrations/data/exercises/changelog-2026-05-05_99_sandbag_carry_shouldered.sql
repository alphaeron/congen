--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Sandbag Carry Shouldered
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Carry Shouldered',
  'Carry the sandbag on one shoulder while walking.',
  'carry',
  true,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Carry Shouldered', 'rectus abdominis'),
  ('Sandbag Carry Shouldered', 'obliques'),
  ('Sandbag Carry Shouldered', 'glutes'),
  ('Sandbag Carry Shouldered', 'upper back'),
  ('Sandbag Carry Shouldered', 'traps');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Carry Shouldered', 'sandbag');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Carry Shouldered', 'carry', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Carry Shouldered', 'carry', 'maximal_effort');
