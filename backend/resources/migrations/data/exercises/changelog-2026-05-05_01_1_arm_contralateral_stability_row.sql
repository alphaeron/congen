--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise 1-Arm Contralateral Stability Row
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('1-Arm Contralateral Stability Row', 'Start with your knees on a bosu ball or airex pad.  Place one arm on a physioball in front of you, and the other grabbing a dumbbell by your side.  Do a 1-arm dumbbell row as you keep yourself balanced with the other arm.  This challenges the stability in the shoulder of the arm keeping you balanced.', 'horizontal_pull', true, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('1-Arm Contralateral Stability Row', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('1-Arm Contralateral Stability Row', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('1-Arm Contralateral Stability Row', 'rear deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('1-Arm Contralateral Stability Row', 'hip flexors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('1-Arm Contralateral Stability Row', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('1-Arm Contralateral Stability Row', 'lateral deltoid');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('1-Arm Contralateral Stability Row', 'physioball');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('1-Arm Contralateral Stability Row', 'airex pad');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('1-Arm Contralateral Stability Row', 'dumbbells');
