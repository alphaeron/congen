--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Jump - Seated to Box
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Jump - Seated to Box', 'Start seated in front of a box.  Quickly stand up and then perform a box jump.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Seated to Box', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Seated to Box', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Seated to Box', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Seated to Box', 'calves');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Jump - Seated to Box', 'box');
