--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Drop - SL Trap Bar Tall to Short (hands on hips)
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Drop - SL Trap Bar Tall to Short (hands on hips)', 'Begin the drill standing with a trap bar in hands on one leg.  Raise your heel up and stand on the ball of your foot.  Quickly drop down into a squat position landing on your heel.', 'plyometric', true, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Drop - SL Trap Bar Tall to Short (hands on hips)', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Drop - SL Trap Bar Tall to Short (hands on hips)', 'hamstrings');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Drop - SL Trap Bar Tall to Short (hands on hips)', 'trap bar');
