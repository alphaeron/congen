--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Suitcase Carry
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Suitcase Carry', 'Hold a heavy weight in one hand and walk, focusing on core stability and unilateral strength.', 'carry', true, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Suitcase Carry', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Suitcase Carry', 'obliques');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Suitcase Carry', 'traps');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Suitcase Carry', 'carry', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Suitcase Carry', 'carry', 'maximal_effort');
