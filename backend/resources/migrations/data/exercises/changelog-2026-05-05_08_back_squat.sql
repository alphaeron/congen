--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Back Squat
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Back Squat', 'Start with the bar in a back rack position, and squat from here, keeping the torso upright.', 'squat', false, false, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Back Squat', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Back Squat', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Back Squat', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Back Squat', 'adductors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Back Squat', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Back Squat', 'rectus abdominis');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Back Squat', 'power bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Back Squat', 'squat', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Back Squat', 'squat', 'maximal_effort');
