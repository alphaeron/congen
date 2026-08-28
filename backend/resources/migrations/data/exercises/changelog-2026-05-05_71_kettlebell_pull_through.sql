--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Kettlebell Pull Through
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Kettlebell Pull Through', 'Stand with your feet shoulder-width apart and a kettlebell between your legs. Hinge at the hips and pull the kettlebell through your legs, focusing on hip hinge mechanics.', 'hinge', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Kettlebell Pull Through', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Kettlebell Pull Through', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Kettlebell Pull Through', 'rectus abdominis');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Kettlebell Pull Through', 'kettlebell');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Kettlebell Pull Through', 'hinge', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Kettlebell Pull Through', 'hinge', 'maximal_effort');
