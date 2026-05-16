--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Continuous Banded Bench Press
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Continuous Banded Bench Press', 'Perform a banded bench press as quickly as posssible.', 'plyometric', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Continuous Banded Bench Press', 'pec major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Continuous Banded Bench Press', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Continuous Banded Bench Press', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Continuous Banded Bench Press', 'serratus anterior');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Continuous Banded Bench Press', 'bench');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Continuous Banded Bench Press', 'bands');
