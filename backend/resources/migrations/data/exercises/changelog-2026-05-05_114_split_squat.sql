--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Split Squat
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Split Squat', 'Start with the weight on the back, and then reach one foot behind you and place it on the toe.  Squat from here, using the front leg primarily, and the rear leg for balance.', 'squat', true, false, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Squat', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Squat', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Squat', 'adductors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Squat', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Squat', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Squat', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Squat', 'hip flexors');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Split Squat', 'power bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Split Squat', 'squat', 'maximal_effort');
