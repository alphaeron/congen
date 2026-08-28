--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Tire Flip
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Tire Flip',
  'Squat down and grip underside of a heavy tire. Drive through legs and extend to flip the tire end over end.',
  'hinge',
  false,
  false,
  true
);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Tire Flip', 'glutes'),
  ('Tire Flip', 'quadriceps'),
  ('Tire Flip', 'hamstrings'),
  ('Tire Flip', 'pec major'),
  ('Tire Flip', 'triceps'),
  ('Tire Flip', 'rectus abdominis'),
  ('Tire Flip', 'obliques'),
  ('Tire Flip', 'forearms');

INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Tire Flip', 'tire');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Tire Flip', 'hinge', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Tire Flip', 'hinge', 'maximal_effort');
