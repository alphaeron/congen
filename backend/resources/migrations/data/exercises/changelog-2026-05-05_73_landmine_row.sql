--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Landmine Row
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Landmine Row', 'Start with the landmine between your legs, and hinge your hips and lean forward.  Grab the landmine in front of you, and row it up to your chest. Lower it back to the ground in a controlled fashion.', 'horizontal_pull', true, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Landmine Row', 'rear deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Landmine Row', 'biceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Landmine Row', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Landmine Row', 'rotator cuff');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Landmine Row', 'teres major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Landmine Row', 'teres minor');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Landmine Row', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Landmine Row', 'pec major');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Landmine Row', 'power bar');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Landmine Row', 'landmine');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Landmine Row', 'horizontal_pull', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Landmine Row', 'horizontal_pull', 'maximal_effort');
