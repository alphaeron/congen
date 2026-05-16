--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise GHR
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('GHR', 'Glute/Ham Raise machine.', 'core', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('GHR', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('GHR', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('GHR', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('GHR', 'erector spinae');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('GHR', 'ghr');
