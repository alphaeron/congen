--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Jump - Pogo
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Jump - Pogo', 'Begin the drill in an athletic posture with the feet hip/shoulder-width apart. Raise heels up and stand on the balls of your feet. Quickly bounce up and down on the balls of your feet. Don’t let your heels touch the ground.', 'plyometric', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Pogo', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Pogo', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Jump - Pogo', 'hamstrings');
