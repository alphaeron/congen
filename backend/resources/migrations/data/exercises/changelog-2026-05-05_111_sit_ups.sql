--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Sit Ups
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Sit Ups', 'Perform traditional sit-ups, focusing on proper form and core strength.', 'core', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sit Ups', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sit Ups', 'hip flexors');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sit Ups', 'core', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sit Ups', 'core', 'maximal_effort');
