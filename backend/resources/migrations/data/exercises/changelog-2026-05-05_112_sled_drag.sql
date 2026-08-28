--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Sled Drag
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Sled Drag', 'Drag a weighted sled forward or backward to build lower body strength and conditioning.', 'hinge', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sled Drag', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sled Drag', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sled Drag', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sled Drag', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sled Drag', 'erector spinae');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Sled Drag', 'sled');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sled Drag', 'hinge', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sled Drag', 'hinge', 'maximal_effort');
