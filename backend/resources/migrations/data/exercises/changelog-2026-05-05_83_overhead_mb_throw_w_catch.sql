--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Overhead MB Throw w/ Catch
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Overhead MB Throw w/ Catch', 'Start with a med ball in both hands.  Pull the med ball into your chest, and then throw it overhead against the wall in front of you.  Catch it as it falls back down.  Repeat', 'plyometric', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch', 'lateral deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch', 'traps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Overhead MB Throw w/ Catch', 'med ball');
