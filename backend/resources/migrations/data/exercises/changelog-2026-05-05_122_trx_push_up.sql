--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise TRX Push-Up
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('TRX Push-Up', 'Perform a push-up while supporting yourself with TRX straps.', 'horizontal_push', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('TRX Push-Up', 'pec major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('TRX Push-Up', 'pec minor');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('TRX Push-Up', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('TRX Push-Up', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('TRX Push-Up', 'serratus anterior');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('TRX Push-Up', 'rectus abdominis');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('TRX Push-Up', 'trx');
