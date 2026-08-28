--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Plate Holds (fingertips)
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Plate Holds (fingertips)', 'Hold weight plates using only your fingertips, focusing on grip strength and forearm development.', 'isolation', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Plate Holds (fingertips)', 'forearms');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Plate Holds (fingertips)', 'weight plate');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Plate Holds (fingertips)', 'isolation', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Plate Holds (fingertips)', 'isolation', 'maximal_effort');
