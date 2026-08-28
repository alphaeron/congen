--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Overhead MB Throw w/ Catch & Squat
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Overhead MB Throw w/ Catch & Squat', 'Start with a med ball in both hands.  Pull the med ball into your chest, and then throw it overhead against the wall in front of you.  Catch it as it falls back down.  Perform a squat as you catch the med ball.  Repeat.', 'plyometric', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch & Squat', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch & Squat', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch & Squat', 'lateral deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch & Squat', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch & Squat', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch & Squat', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch & Squat', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch & Squat', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead MB Throw w/ Catch & Squat', 'quadriceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Overhead MB Throw w/ Catch & Squat', 'med ball');
