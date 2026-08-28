--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Chin-Up
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Chin-Up', 'Standard chin-up.', 'vertical_pull', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chin-Up', 'biceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chin-Up', 'rear deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chin-Up', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chin-Up', 'rhomboids');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chin-Up', 'pec major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chin-Up', 'teres major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chin-Up', 'lats');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Chin-Up', 'pull-up bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Chin-Up', 'vertical_pull', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Chin-Up', 'vertical_pull', 'maximal_effort');
