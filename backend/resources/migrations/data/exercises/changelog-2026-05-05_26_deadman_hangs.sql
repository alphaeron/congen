--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Deadman Hangs
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Deadman Hangs', 'Hang from a pull-up bar in a relaxed position, focusing on grip strength and shoulder mobility.', 'isolation', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Deadman Hangs', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Deadman Hangs', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Deadman Hangs', 'biceps');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Deadman Hangs', 'isolation', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Deadman Hangs', 'isolation', 'maximal_effort');
