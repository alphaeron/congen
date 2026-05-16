--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Ab Wheel
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Ab Wheel', 'Ab wheel tool.', 'core', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ab Wheel', 'teres major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ab Wheel', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ab Wheel', 'pec major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ab Wheel', 'serratus anterior');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ab Wheel', 'hip flexors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ab Wheel', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ab Wheel', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ab Wheel', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ab Wheel', 'obliques');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Ab Wheel', 'ab wheel');
