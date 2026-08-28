--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Decline Sit Up
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Decline Sit Up', 'Perform sit-ups on a decline bench, focusing on proper form and core strength.', 'core', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Decline Sit Up', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Decline Sit Up', 'hip flexors');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Decline Sit Up', 'adjustable bench');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Decline Sit Up', 'core', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Decline Sit Up', 'core', 'maximal_effort');
