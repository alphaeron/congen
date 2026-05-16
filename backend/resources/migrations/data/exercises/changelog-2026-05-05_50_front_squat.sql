--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Front Squat
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Front Squat', 'Start with the bar in a front rack position, and squat from here, keeping the torso upright.', 'squat', false, false, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Front Squat', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Front Squat', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Front Squat', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Front Squat', 'adductors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Front Squat', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Front Squat', 'rectus abdominis');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Front Squat', 'power bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Front Squat', 'squat', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Front Squat', 'squat', 'maximal_effort');
