--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Jump - Hurdle
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Jump - Hurdle', 'Stand facing collapsible hurdles or barriers. Squat down and jump over hurdle with feet together using a double arm swing. Upon landing, immediately jump over next hurdle.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Hurdle', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Hurdle', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Hurdle', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Hurdle', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Hurdle', 'hip flexors');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Jump - Hurdle', 'hurdle');
