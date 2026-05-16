--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Medicine Ball Slam
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Medicine Ball Slam', 'Start with a med ball in both hands.  Raise the ball overhead as you extend your legs and raise up on your toes. Slam the med ball to the ground from the overhead position.  Gather the med ball and then repeat.', 'plyometric', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Medicine Ball Slam', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Medicine Ball Slam', 'lateral deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Medicine Ball Slam', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Medicine Ball Slam', 'biceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Medicine Ball Slam', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Medicine Ball Slam', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Medicine Ball Slam', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Medicine Ball Slam', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Medicine Ball Slam', 'quadriceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Medicine Ball Slam', 'med ball');
