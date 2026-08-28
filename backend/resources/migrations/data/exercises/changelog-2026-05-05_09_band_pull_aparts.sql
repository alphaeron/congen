--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Band Pull Aparts
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Band Pull Aparts', 'Hold a resistance band in front of you and pull it apart horizontally, focusing on shoulder and upper back strength.', 'isolation', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Band Pull Aparts', 'rear deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Band Pull Aparts', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Band Pull Aparts', 'rhomboids');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Band Pull Aparts', 'bands');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Band Pull Aparts', 'isolation', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Band Pull Aparts', 'isolation', 'maximal_effort');
