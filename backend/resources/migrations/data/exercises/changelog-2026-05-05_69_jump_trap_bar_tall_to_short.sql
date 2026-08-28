--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Jump - Trap Bar Tall to Short
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Jump - Trap Bar Tall to Short', 'Begin the drill in an athletic posture with the feet hip/shoulder-width apart with a trap bar in hands. Raise heels up and stand on the balls of your feet. Quickly drop down into a squat position landing on your heels.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Trap Bar Tall to Short', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Trap Bar Tall to Short', 'hamstrings');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Jump - Trap Bar Tall to Short', 'trap bar');
