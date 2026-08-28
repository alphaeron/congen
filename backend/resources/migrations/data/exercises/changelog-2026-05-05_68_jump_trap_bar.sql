--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Jump - Trap Bar
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Jump - Trap Bar', 'Begin squatting down feet shoulders width apart grabbing a trap bar (like you are about to start a deadlift).  Explosively lift the bar up like doing a deadlift, but instead of stopping at lockout jump upwards with the weight.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Trap Bar', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Trap Bar', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Trap Bar', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Trap Bar', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Trap Bar', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Trap Bar', 'calves');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Jump - Trap Bar', 'trap bar');
