--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Bench Press
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Bench Press', 'Standard bench press', 'horizontal_push', false, true, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Bench Press', 'pec major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Bench Press', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Bench Press', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Bench Press', 'serratus anterior');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Bench Press', 'bench');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Bench Press', 'power bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Bench Press', 'horizontal_push', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Bench Press', 'horizontal_push', 'maximal_effort');
