--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Lateral Single Leg Hops
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Lateral Single Leg Hops', 'Hop laterally on one leg, focusing on explosive power and stability.', 'plyometric', true, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Lateral Single Leg Hops', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Lateral Single Leg Hops', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Lateral Single Leg Hops', 'calves');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Lateral Single Leg Hops', 'plyometric', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Lateral Single Leg Hops', 'plyometric', 'maximal_effort');
