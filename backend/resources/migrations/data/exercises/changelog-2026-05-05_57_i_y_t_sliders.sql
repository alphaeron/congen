--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise I/Y/T Sliders
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('I/Y/T Sliders', 'Perform the I/Y/T exercise using sliders while kneeling on the ground.', 'plyometric', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('I/Y/T Sliders', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('I/Y/T Sliders', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('I/Y/T Sliders', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('I/Y/T Sliders', 'rotator cuff');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('I/Y/T Sliders', 'rhomboids');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('I/Y/T Sliders', 'sliders');
