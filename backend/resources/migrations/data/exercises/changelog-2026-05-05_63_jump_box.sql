--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Jump - Box
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Jump - Box', 'Facing the box with your feet shoulder width apart, bend your knees and push your hips back in a hinge motion. Jump off the balls of your feet, swinging your arms forward to launch yourself onto the top of the box. Land on the box gently, with your body and feet in their original position (knees bent, hips back). Jump or step off the box, returning to the starting position.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Box', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Box', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Box', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Box', 'calves');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Jump - Box', 'box');
