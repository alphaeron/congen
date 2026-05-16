--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Upright Row
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Upright Row', 'Start with the bar in an overhand grip in front of you. Lift the bar up along your body to your shoulders keeping the elbows up and back. Lower the bar to the starting position.', 'vertical_pull', true, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Upright Row', 'lateral deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Upright Row', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Upright Row', 'biceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Upright Row', 'power bar');
