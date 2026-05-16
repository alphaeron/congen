--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Jump - Lateral + Medial Hurdle
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Jump - Lateral + Medial Hurdle', 'Start standing sideways by a hurdle.  Raise heels up and stand on the balls of your feet. Squat down, then explode up and to the side, jumping sideways over the hurdle.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Lateral + Medial Hurdle', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Lateral + Medial Hurdle', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Lateral + Medial Hurdle', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Lateral + Medial Hurdle', 'calves');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Jump - Lateral + Medial Hurdle', 'hurdle');
