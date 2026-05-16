--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Step-Up
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Step-Up', 'Start with the right foot.  Step the right foot up on a box in front of you.  Try to keep the left foot up on the heel to avoid cheating.  Use the right leg to raise yourself off the ground.  Once the right leg is straight, bring the left knee up.  Return the left foot back.  Use your right foot to lower yourself to the ground, touching the left heel to the ground.', 'lunge', true, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Step-Up', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Step-Up', 'adductors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Step-Up', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Step-Up', 'glutes');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Step-Up', 'box');
