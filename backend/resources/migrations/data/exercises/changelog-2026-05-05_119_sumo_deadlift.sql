--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Sumo Deadlift
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Sumo Deadlift', 'Start with the feed about twice shoulders distance apart.  Grab the bar with both hands closer to the center of the bar about shoulders distance apart, and deadlift from here.', 'hinge', false, false, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sumo Deadlift', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sumo Deadlift', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sumo Deadlift', 'adductors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sumo Deadlift', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sumo Deadlift', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sumo Deadlift', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sumo Deadlift', 'erector spinae');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Sumo Deadlift', 'power bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sumo Deadlift', 'hinge', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sumo Deadlift', 'hinge', 'maximal_effort');
