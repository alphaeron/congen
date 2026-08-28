--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Floor Press
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Floor Press', 'Bench press laying with your back on the ground.', 'horizontal_push', false, true, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Floor Press', 'pec major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Floor Press', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Floor Press', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Floor Press', 'serratus anterior');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Floor Press', 'power bar');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Floor Press', 'power rack');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Floor Press', 'horizontal_push', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Floor Press', 'horizontal_push', 'maximal_effort');
