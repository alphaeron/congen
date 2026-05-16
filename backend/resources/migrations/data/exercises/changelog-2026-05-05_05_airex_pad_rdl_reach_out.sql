--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Airex pad RDL reach-out
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Airex pad RDL reach-out', 'Perform a single leg RDL on an Airex pad, reaching both arms out straight overhead as you do the RDL for added challenge.', 'core', true, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Airex pad RDL reach-out', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Airex pad RDL reach-out', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Airex pad RDL reach-out', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Airex pad RDL reach-out', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Airex pad RDL reach-out', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Airex pad RDL reach-out', 'obliques');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Airex pad RDL reach-out', 'calves');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Airex pad RDL reach-out', 'airex pad');
