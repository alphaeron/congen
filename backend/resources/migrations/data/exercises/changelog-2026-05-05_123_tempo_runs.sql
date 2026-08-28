--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Tempo Runs
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Tempo Runs', 'Perform running drills at various tempos, focusing on speed, power, and proper running mechanics.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Tempo Runs', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Tempo Runs', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Tempo Runs', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Tempo Runs', 'calves');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Tempo Runs', 'plyometric', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Tempo Runs', 'plyometric', 'maximal_effort');
