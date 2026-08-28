--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add exercise Banded Safety Bar Squat
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Banded Safety Bar Squat', 'Squat with a safety bar.', 'squat', false, false, false);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Safety Bar Squat', 'quadriceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Safety Bar Squat', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Safety Bar Squat', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Safety Bar Squat', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Safety Bar Squat', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Safety Bar Squat', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Safety Bar Squat', 'traps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Banded Safety Bar Squat', 'safety squat bar');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Banded Safety Bar Squat', 'bands');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Safety Bar Squat', 'squat', 'dynamic_effort');
