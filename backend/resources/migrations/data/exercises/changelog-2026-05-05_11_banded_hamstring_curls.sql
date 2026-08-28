--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Banded Hamstring Curls
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Banded Hamstring Curls', 'Perform hamstring curls using a resistance band, focusing on proper form and muscle activation.', 'isolation', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Hamstring Curls', 'hamstrings');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Banded Hamstring Curls', 'bands');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Hamstring Curls', 'isolation', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Hamstring Curls', 'isolation', 'maximal_effort');
