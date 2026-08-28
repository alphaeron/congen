--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Reverse Hyper
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Reverse Hyper', 'Reverse hyperextension machine.', 'hinge', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Reverse Hyper', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Reverse Hyper', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Reverse Hyper', 'hamstrings');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Reverse Hyper', 'reverse hyper');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Reverse Hyper', 'hinge', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Reverse Hyper', 'hinge', 'maximal_effort');
