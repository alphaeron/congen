--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Banded Bench Press
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Banded Bench Press', 'Standard bench press', 'horizontal_push', false, true, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Bench Press', 'pec major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Bench Press', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Bench Press', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Bench Press', 'serratus anterior');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Banded Bench Press', 'power bar');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Banded Bench Press', 'bands');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Bench Press', 'horizontal_push', 'dynamic_effort');
