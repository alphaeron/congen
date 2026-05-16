--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Overhead Press
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Overhead Press', 'Standard overhead press.', 'vertical_push', false, true, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead Press', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead Press', 'lateral deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead Press', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead Press', 'serratus anterior');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead Press', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead Press', 'obliques');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead Press', 'traps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Overhead Press', 'power bar');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Overhead Press', 'power rack');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Overhead Press', 'vertical_push', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Overhead Press', 'vertical_push', 'maximal_effort');
