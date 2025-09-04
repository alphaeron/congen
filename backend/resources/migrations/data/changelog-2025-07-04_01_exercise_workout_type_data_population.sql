--liquibase formatted sql

--changeset John Matty:2 labels:prod,test
--comment: Populate exercise_workout_type table with relationships based on movement_type and is_accessory.
-- Fake rollback - data population does not make sense to roll back
--rollback SELECT 1

-- Dynamic Effort exercises (typically compound movements that are not accessories)
-- Horizontal Push - Dynamic Effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Bench Press', 'horizontal_push', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Incline Bench Press', 'horizontal_push', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Floor Press', 'horizontal_push', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Bench Press', 'horizontal_push', 'dynamic_effort');

-- Vertical Push - Dynamic Effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Overhead Press', 'vertical_push', 'dynamic_effort');

-- Horizontal Pull - Dynamic Effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Bent-Over Row', 'vertical_pull', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Landmine Row', 'horizontal_pull', 'dynamic_effort');

-- Vertical Pull - Dynamic Effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Chin-Up', 'vertical_pull', 'dynamic_effort');

-- Squat - Dynamic Effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Safety Bar Squat', 'squat', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Front Squat', 'squat', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Back Squat', 'squat', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Zercher Squat', 'squat', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Safety Bar Squat', 'squat', 'dynamic_effort');

-- Hinge - Dynamic Effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Deadlift', 'hinge', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sumo Deadlift', 'hinge', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Trap Bar Deadlift', 'hinge', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Romanian Deadlift', 'hinge', 'dynamic_effort');

-- Maximal Effort exercises (typically compound movements that are not accessories)
-- Horizontal Push - Maximal Effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Bench Press', 'horizontal_push', 'maximal_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Incline Bench Press', 'horizontal_push', 'maximal_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Floor Press', 'horizontal_push', 'maximal_effort');

-- Vertical Push - Maximal Effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Overhead Press', 'vertical_push', 'maximal_effort');

-- Horizontal Pull - Maximal Effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Bent-Over Row', 'vertical_pull', 'maximal_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Landmine Row', 'horizontal_pull', 'maximal_effort');

-- Vertical Pull - Maximal Effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Chin-Up', 'vertical_pull', 'maximal_effort');

-- Squat - Maximal Effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Safety Bar Squat', 'squat', 'maximal_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Front Squat', 'squat', 'maximal_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Back Squat', 'squat', 'maximal_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Zercher Squat', 'squat', 'maximal_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Split Squat', 'squat', 'maximal_effort');

-- Hinge - Maximal Effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Deadlift', 'hinge', 'maximal_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sumo Deadlift', 'hinge', 'maximal_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Trap Bar Deadlift', 'hinge', 'maximal_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Romanian Deadlift', 'hinge', 'maximal_effort');

-- Add workout type assignments for the new exercises
-- Sled Drag - suitable for both workout types as it's a conditioning exercise
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sled Drag', 'hinge', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sled Drag', 'hinge', 'maximal_effort');

-- Dumbbell Bench Press - suitable for both workout types
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Dumbbell Bench Press', 'horizontal_push', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Dumbbell Bench Press', 'horizontal_push', 'maximal_effort');

-- Reverse Hyper - suitable for both workout types as it's an accessory
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Reverse Hyper', 'hinge', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Reverse Hyper', 'hinge', 'maximal_effort');
