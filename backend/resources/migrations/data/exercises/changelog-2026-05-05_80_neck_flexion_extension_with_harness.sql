--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Neck Flexion Extension With Harness
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Neck Flexion Extension With Harness', 'Use a neck harness to perform flexion and extension exercises, focusing on neck strength and stability.', 'isolation', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Neck Flexion Extension With Harness', 'neck');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Neck Flexion Extension With Harness', 'iron neck');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Neck Flexion Extension With Harness', 'isolation', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Neck Flexion Extension With Harness', 'isolation', 'maximal_effort');
