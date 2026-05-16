--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Hanging Leg Raises
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Hanging Leg Raises', 'Hang from a pull-up bar and raise your legs up to parallel or higher, focusing on core strength and control.', 'core', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Hanging Leg Raises', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Hanging Leg Raises', 'hip flexors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Hanging Leg Raises', 'obliques');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Hanging Leg Raises', 'pull-up bar');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Hanging Leg Raises', 'core', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Hanging Leg Raises', 'core', 'maximal_effort');
