--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Front High Knees
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Front High Knees', 'Run in place, bringing your knees up to waist level, focusing on explosive movement and proper form.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Front High Knees', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Front High Knees', 'hip flexors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Front High Knees', 'calves');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Front High Knees', 'plyometric', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Front High Knees', 'plyometric', 'maximal_effort');
