--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Trap Bar Deadlift
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Trap Bar Deadlift', 'Deadlift with a Trap/Hex bar.', 'hinge', false, false, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Trap Bar Deadlift', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Trap Bar Deadlift', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Trap Bar Deadlift', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Trap Bar Deadlift', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Trap Bar Deadlift', 'traps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Trap Bar Deadlift', 'trap bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Trap Bar Deadlift', 'hinge', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Trap Bar Deadlift', 'hinge', 'maximal_effort');
