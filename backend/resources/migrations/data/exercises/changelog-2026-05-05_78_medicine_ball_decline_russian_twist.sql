--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Medicine Ball Decline Russian Twist
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Medicine Ball Decline Russian Twist', 'Perform Russian twists on a decline bench while holding a medicine ball, focusing on core strength and rotation.', 'core', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Medicine Ball Decline Russian Twist', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Medicine Ball Decline Russian Twist', 'obliques');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Medicine Ball Decline Russian Twist', 'med ball');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Medicine Ball Decline Russian Twist', 'adjustable bench');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Medicine Ball Decline Russian Twist', 'core', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Medicine Ball Decline Russian Twist', 'core', 'maximal_effort');
