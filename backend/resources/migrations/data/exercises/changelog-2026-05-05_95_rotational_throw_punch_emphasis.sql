--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Rotational Throw (Punch Emphasis)
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Rotational Throw (Punch Emphasis)', 'Stand with feet shoulder-width apart, holding a medicine ball. Rotate your torso and explosively throw the ball forward with a punching motion, emphasizing power and rotation.', 'plyometric', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Rotational Throw (Punch Emphasis)', 'obliques');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Rotational Throw (Punch Emphasis)', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Rotational Throw (Punch Emphasis)', 'serratus anterior');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Rotational Throw (Punch Emphasis)', 'pec major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Rotational Throw (Punch Emphasis)', 'anterior deltoid');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Rotational Throw (Punch Emphasis)', 'med ball');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Rotational Throw (Punch Emphasis)', 'plyometric', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Rotational Throw (Punch Emphasis)', 'plyometric', 'maximal_effort');
