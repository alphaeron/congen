--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Supine Med Ball Draw and Throw
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Supine Med Ball Draw and Throw', 'Laying on your back, do a med ball chest pass, then catch the ball as it falls down and repeat.', 'plyometric', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Supine Med Ball Draw and Throw', 'pec major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Supine Med Ball Draw and Throw', 'pec minor');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Supine Med Ball Draw and Throw', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Supine Med Ball Draw and Throw', 'triceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Supine Med Ball Draw and Throw', 'med ball');
