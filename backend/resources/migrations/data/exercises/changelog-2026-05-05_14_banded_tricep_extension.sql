--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Banded Tricep Extension
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Banded Tricep Extension', 'Perform tricep extensions using a resistance band, focusing on proper form and muscle activation.', 'isolation', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Tricep Extension', 'triceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Banded Tricep Extension', 'bands');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Tricep Extension', 'isolation', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Tricep Extension', 'isolation', 'maximal_effort');
