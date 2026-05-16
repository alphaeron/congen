--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Hip Thrusts
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Hip Thrusts', 'Lie on your back with your feet on the ground and thrust your hips up, focusing on glute activation and strength.', 'hinge', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Hip Thrusts', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Hip Thrusts', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Hip Thrusts', 'rectus abdominis');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Hip Thrusts', 'hinge', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Hip Thrusts', 'hinge', 'maximal_effort');
