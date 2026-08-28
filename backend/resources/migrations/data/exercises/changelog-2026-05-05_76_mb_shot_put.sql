--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise MB Shot Put
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('MB Shot Put', 'Start with a med ball in one hand, you can use the other hand to support it.  Quickly pull the med ball to your shoulder, then throw the med ball from there in a shot put motion.', 'plyometric', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('MB Shot Put', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('MB Shot Put', 'lateral deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('MB Shot Put', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('MB Shot Put', 'adductors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('MB Shot Put', 'biceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('MB Shot Put', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('MB Shot Put', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('MB Shot Put', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('MB Shot Put', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('MB Shot Put', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('MB Shot Put', 'quadriceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('MB Shot Put', 'med ball');
