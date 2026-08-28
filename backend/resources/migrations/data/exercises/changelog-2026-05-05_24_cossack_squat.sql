--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Cossack Squat
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Cossack Squat', 'Start with your feet together.  Step one foot off to the side a foots distance offset forward from the other in a wide stance.  Squat down on that leg as you keep the other leg straight.  As you raise yourself back up, step the feet together, then repeat on the other side.', 'lunge', true, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Cossack Squat', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Cossack Squat', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Cossack Squat', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Cossack Squat', 'adductors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Cossack Squat', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Cossack Squat', 'erector spinae');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Cossack Squat', 'dumbbells');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Cossack Squat', 'kettlebell');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Cossack Squat', 'power bar');
