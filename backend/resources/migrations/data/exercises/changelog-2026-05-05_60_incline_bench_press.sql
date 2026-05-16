--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Incline Bench Press
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Incline Bench Press', 'Bench press with an upward incline.', 'horizontal_push', false, true, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Incline Bench Press', 'pec major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Incline Bench Press', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Incline Bench Press', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Incline Bench Press', 'serratus anterior');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Incline Bench Press', 'adjustable bench');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Incline Bench Press', 'power bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Incline Bench Press', 'horizontal_push', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Incline Bench Press', 'horizontal_push', 'maximal_effort');
