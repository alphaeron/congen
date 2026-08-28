--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Broad Jump (weighted)
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Broad Jump (weighted)', 'Perform a standing broad jump while holding weights, focusing on explosive power and proper landing mechanics.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Broad Jump (weighted)', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Broad Jump (weighted)', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Broad Jump (weighted)', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Broad Jump (weighted)', 'calves');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Broad Jump (weighted)', 'dumbbells');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Broad Jump (weighted)', 'plyometric', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Broad Jump (weighted)', 'plyometric', 'maximal_effort');
