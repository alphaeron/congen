--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Thumbs Up Battle Rope
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Thumbs Up Battle Rope', 'Hold battle ropes with thumbs up and perform various wave patterns, focusing on upper body strength and conditioning.', 'isolation', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Up Battle Rope', 'pec major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Up Battle Rope', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Up Battle Rope', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Up Battle Rope', 'lats');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Thumbs Up Battle Rope', 'battle rope');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Thumbs Up Battle Rope', 'isolation', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Thumbs Up Battle Rope', 'isolation', 'maximal_effort');
