--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Weighted Box Jumps
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Weighted Box Jumps', 'Perform box jumps while holding weights, starting from a static position without the pre-stretch countermovement for maximum power output.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Weighted Box Jumps', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Weighted Box Jumps', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Weighted Box Jumps', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Weighted Box Jumps', 'calves');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Weighted Box Jumps', 'box');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Weighted Box Jumps', 'dumbbells');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Weighted Box Jumps', 'plyometric', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Weighted Box Jumps', 'plyometric', 'maximal_effort');
