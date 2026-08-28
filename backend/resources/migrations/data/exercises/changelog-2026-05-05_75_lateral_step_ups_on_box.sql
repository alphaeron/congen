--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Lateral Step Ups On Box
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Lateral Step Ups On Box', 'Step up onto a box laterally, focusing on explosive power and proper form.', 'plyometric', true, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Lateral Step Ups On Box', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Lateral Step Ups On Box', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Lateral Step Ups On Box', 'calves');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Lateral Step Ups On Box', 'box');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Lateral Step Ups On Box', 'plyometric', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Lateral Step Ups On Box', 'plyometric', 'maximal_effort');
