--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Zercher Squat
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Zercher Squat', 'Start with the bar in the crooks of the elbows, palms facing you.  Squat from here, keeping the torso upright.', 'squat', false, false, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Zercher Squat', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Zercher Squat', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Zercher Squat', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Zercher Squat', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Zercher Squat', 'obliques');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Zercher Squat', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Zercher Squat', 'upper back');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Zercher Squat', 'biceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Zercher Squat', 'power bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Zercher Squat', 'squat', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Zercher Squat', 'squat', 'maximal_effort');
