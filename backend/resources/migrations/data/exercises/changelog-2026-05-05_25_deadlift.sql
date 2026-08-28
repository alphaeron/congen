--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Deadlift
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Deadlift', 'Classical deadlift.', 'hinge', false, false, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Deadlift', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Deadlift', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Deadlift', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Deadlift', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Deadlift', 'traps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Deadlift', 'power bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Deadlift', 'hinge', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Deadlift', 'hinge', 'maximal_effort');
