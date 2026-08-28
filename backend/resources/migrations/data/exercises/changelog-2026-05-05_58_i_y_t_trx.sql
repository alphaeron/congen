--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise I/Y/T TRX
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('I/Y/T TRX', 'Perform the I/Y/T exercise using a TRX.', 'plyometric', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('I/Y/T TRX', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('I/Y/T TRX', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('I/Y/T TRX', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('I/Y/T TRX', 'rotator cuff');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('I/Y/T TRX', 'rhomboids');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('I/Y/T TRX', 'trx');
