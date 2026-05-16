--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Split Stance Jump
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Split Stance Jump', 'Start in a split stance position and explosively jump, switching leg positions in mid-air and landing in the opposite split stance.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Stance Jump', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Stance Jump', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Stance Jump', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Stance Jump', 'calves');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Split Stance Jump', 'dumbbells');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Split Stance Jump', 'plyometric', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Split Stance Jump', 'plyometric', 'maximal_effort');
