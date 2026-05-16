--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Continuous Med Ball Chest Pass
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Continuous Med Ball Chest Pass', 'Perform med ball chest passes continuously close to a wall so you can catch the ball and repeat.', 'plyometric', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Continuous Med Ball Chest Pass', 'pec major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Continuous Med Ball Chest Pass', 'pec minor');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Continuous Med Ball Chest Pass', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Continuous Med Ball Chest Pass', 'triceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Continuous Med Ball Chest Pass', 'med ball');
