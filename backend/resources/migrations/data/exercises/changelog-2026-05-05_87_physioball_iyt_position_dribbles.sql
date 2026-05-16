--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Physioball IYT Position Dribbles
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Physioball IYT Position Dribbles', 'With your arms on a stability ball, dribble your arms going from the I, to Y, to T position one each quickly, and repeat.', 'plyometric', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Physioball IYT Position Dribbles', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Physioball IYT Position Dribbles', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Physioball IYT Position Dribbles', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Physioball IYT Position Dribbles', 'rotator cuff');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Physioball IYT Position Dribbles', 'rhomboids');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Physioball IYT Position Dribbles', 'physioball');
