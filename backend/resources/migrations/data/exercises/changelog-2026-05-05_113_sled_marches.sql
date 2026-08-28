--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Sled Marches
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Sled Marches', 'Push a sled while marching in place, focusing on lower body strength and conditioning.', 'hinge', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sled Marches', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sled Marches', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sled Marches', 'calves');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Sled Marches', 'sled');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sled Marches', 'hinge', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sled Marches', 'hinge', 'maximal_effort');
