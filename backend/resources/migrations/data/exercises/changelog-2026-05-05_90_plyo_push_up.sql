--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Plyo Push-Up
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Plyo Push-Up', 'Perform a push-up, except at the top of the motion throw yourself off the ground and clap your hands.', 'plyometric', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Plyo Push-Up', 'pec major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Plyo Push-Up', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Plyo Push-Up', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Plyo Push-Up', 'serratus anterior');
