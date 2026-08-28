--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise 4 Way Neck
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('4 Way Neck', 'Perform neck exercises in four directions (flexion, extension, lateral flexion left and right) using resistance.', 'isolation', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('4 Way Neck', 'neck');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('4 Way Neck', 'iron neck');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('4 Way Neck', 'isolation', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('4 Way Neck', 'isolation', 'maximal_effort');
