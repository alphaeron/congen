--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Plank
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Plank', 'Hold a plank position with your body in a straight line from head to heels, focusing on core stability and endurance.', 'core', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Plank', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Plank', 'obliques');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Plank', 'erector spinae');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Plank', 'core', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Plank', 'core', 'maximal_effort');
