--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Banded Kettlebell Swing
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Banded Kettlebell Swing', 'Perform kettlebell swings while wearing a resistance band around your waist, focusing on explosive power and proper form.', 'hinge', false, false, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Kettlebell Swing', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Kettlebell Swing', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Kettlebell Swing', 'rectus abdominis');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Banded Kettlebell Swing', 'kettlebell');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Banded Kettlebell Swing', 'bands');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Kettlebell Swing', 'hinge', 'dynamic_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Kettlebell Swing', 'hinge', 'maximal_effort');
