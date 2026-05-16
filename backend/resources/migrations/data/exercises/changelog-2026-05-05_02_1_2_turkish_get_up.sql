--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise 1/2 Turkish Get Up
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('1/2 Turkish Get Up', 'Perform the first half of a Turkish get-up, focusing on shoulder stability and core strength.', 'core', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('1/2 Turkish Get Up', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('1/2 Turkish Get Up', 'obliques');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('1/2 Turkish Get Up', 'anterior deltoid');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('1/2 Turkish Get Up', 'core', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('1/2 Turkish Get Up', 'core', 'maximal_effort');
