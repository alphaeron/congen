--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Palloff Press
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Palloff Press', 'Stand sideways to a cable machine and perform a press movement, focusing on anti-rotation and core stability.', 'core', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Palloff Press', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Palloff Press', 'obliques');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Palloff Press', 'core', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Palloff Press', 'core', 'maximal_effort');
