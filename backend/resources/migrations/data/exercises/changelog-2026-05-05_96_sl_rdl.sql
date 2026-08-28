--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise SL RDL
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('SL RDL', 'Single leg romanian deadlift.', 'hinge', true, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('SL RDL', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('SL RDL', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('SL RDL', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('SL RDL', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('SL RDL', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('SL RDL', 'obliques');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('SL RDL', 'calves');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('SL RDL', 'power bar');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('SL RDL', 'dumbbells');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('SL RDL', 'kettlebell');
