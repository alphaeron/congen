--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Valslide Hamstring Curls
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Valslide Hamstring Curls', 'Lie on your back with your feet on Valslides and perform hamstring curls, focusing on proper form and muscle activation.', 'isolation', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Valslide Hamstring Curls', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Valslide Hamstring Curls', 'glutes');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Valslide Hamstring Curls', 'valslides');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Valslide Hamstring Curls', 'isolation', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Valslide Hamstring Curls', 'isolation', 'maximal_effort');
