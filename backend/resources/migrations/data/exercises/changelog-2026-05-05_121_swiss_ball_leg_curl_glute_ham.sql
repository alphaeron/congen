--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Swiss Ball Leg Curl Glute HAM
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Swiss Ball Leg Curl Glute HAM', 'Lie on your back with your feet on a Swiss ball and perform leg curls, focusing on hamstring and glute strength.', 'isolation', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Swiss Ball Leg Curl Glute HAM', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Swiss Ball Leg Curl Glute HAM', 'glutes');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Swiss Ball Leg Curl Glute HAM', 'physioball');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Swiss Ball Leg Curl Glute HAM', 'isolation', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Swiss Ball Leg Curl Glute HAM', 'isolation', 'maximal_effort');
