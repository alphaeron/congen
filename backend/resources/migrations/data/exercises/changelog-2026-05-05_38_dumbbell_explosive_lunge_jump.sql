--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Dumbbell Explosive Lunge Jump
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Dumbbell Explosive Lunge Jump', 'Perform a lunge with dumbbells, then explosively jump up, switching leg positions in mid-air.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Dumbbell Explosive Lunge Jump', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Dumbbell Explosive Lunge Jump', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Dumbbell Explosive Lunge Jump', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Dumbbell Explosive Lunge Jump', 'calves');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Dumbbell Explosive Lunge Jump', 'dumbbells');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Dumbbell Explosive Lunge Jump', 'plyometric', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Dumbbell Explosive Lunge Jump', 'plyometric', 'maximal_effort');
