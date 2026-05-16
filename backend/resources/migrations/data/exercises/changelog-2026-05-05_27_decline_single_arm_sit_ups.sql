--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Decline Single Arm Sit Ups
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Decline Single Arm Sit Ups', 'Perform sit-ups on a decline bench while holding a weight in one hand, focusing on core strength and unilateral development.', 'core', true, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Decline Single Arm Sit Ups', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Decline Single Arm Sit Ups', 'hip flexors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Decline Single Arm Sit Ups', 'obliques');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Decline Single Arm Sit Ups', 'adjustable bench');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Decline Single Arm Sit Ups', 'dumbbells');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Decline Single Arm Sit Ups', 'core', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Decline Single Arm Sit Ups', 'core', 'maximal_effort');
