--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Ice Skaters
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Ice Skaters', 'Jump laterally from side to side, landing on one foot and then the other, mimicking ice skating movements.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ice Skaters', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ice Skaters', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ice Skaters', 'adductors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ice Skaters', 'calves');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Ice Skaters', 'plyometric', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Ice Skaters', 'plyometric', 'maximal_effort');
