--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Thumbs Down Battle Rope
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Thumbs Down Battle Rope', 'Hold battle ropes with thumbs down and perform various wave patterns, focusing on upper body strength and conditioning.', 'isolation', false, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Down Battle Rope', 'pec major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Down Battle Rope', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Down Battle Rope', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Down Battle Rope', 'lats');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Thumbs Down Battle Rope', 'battle rope');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Thumbs Down Battle Rope', 'isolation', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Thumbs Down Battle Rope', 'isolation', 'maximal_effort');
