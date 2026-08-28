--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Inverted Row
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Inverted Row', 'Start by grabbing a handle in front of you, and hang off it with your feet on a box in front of you.  Pull your chest up to the handle, and control your descent back down.', 'horizontal_pull', true, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Inverted Row', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Inverted Row', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Inverted Row', 'rhomboids');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Inverted Row', 'rotator cuff');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Inverted Row', 'teres minor');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Inverted Row', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Inverted Row', 'rear deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Inverted Row', 'biceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Inverted Row', 'trx');
