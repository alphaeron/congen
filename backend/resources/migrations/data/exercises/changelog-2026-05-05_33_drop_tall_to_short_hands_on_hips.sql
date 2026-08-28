--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Drop - Tall to Short (hands on hips)
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Drop - Tall to Short (hands on hips)', 'Begin the drill standing in an athletic posture.  Lift your arms overhead as you raise heels up and stand on the balls of your feet.  Quickly drop down into a squat position throwing the arms back and landing on your heels.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Drop - Tall to Short (hands on hips)', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Drop - Tall to Short (hands on hips)', 'hamstrings');
