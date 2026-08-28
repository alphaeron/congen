--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Scoop Toss
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Scoop Toss', 'Stand with your feet 6-12 inches wider then shoulder width apart and bend slightly at the knees, with a med ball in your hands. Raise the ball up over your head and then bring it down between your legs and scoop throw it at the wall in front of you. Step forward as you throw the ball.  Catch and repeat.', 'plyometric', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Scoop Toss', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Scoop Toss', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Scoop Toss', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Scoop Toss', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Scoop Toss', 'pec minor');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Scoop Toss', 'med ball');
