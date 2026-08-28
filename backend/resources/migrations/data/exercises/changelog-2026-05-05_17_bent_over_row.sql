--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Bent-Over Row
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Bent-Over Row', 'Hinge your hips and lean forward, grabbing the bar in front of you. Pull the bar straight up to your solar plexus. Lower the bar to the ground in a controlled fashion and repeat.', 'vertical_pull', true, true, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Bent-Over Row', 'rear deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Bent-Over Row', 'teres minor');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Bent-Over Row', 'teres major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Bent-Over Row', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Bent-Over Row', 'rotator cuff');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Bent-Over Row', 'biceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Bent-Over Row', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Bent-Over Row', 'rhomboids');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Bent-Over Row', 'power bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Bent-Over Row', 'vertical_pull', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Bent-Over Row', 'vertical_pull', 'maximal_effort');
