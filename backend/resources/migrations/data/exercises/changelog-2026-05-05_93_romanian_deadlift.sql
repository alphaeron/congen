--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Romanian Deadlift
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Romanian Deadlift', 'Start with the bar in both hands at shin level, with the knees slightly bent.  From there, finish the deadlift, however when lowering the bar return to the RDL starting position rather than letting the bar touch the ground.', 'hinge', false, false, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Romanian Deadlift', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Romanian Deadlift', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Romanian Deadlift', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Romanian Deadlift', 'adductors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Romanian Deadlift', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Romanian Deadlift', 'traps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Romanian Deadlift', 'power bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Romanian Deadlift', 'hinge', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Romanian Deadlift', 'hinge', 'maximal_effort');
