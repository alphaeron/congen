--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Overhead Kettlebell Snatch
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Overhead Kettlebell Snatch', 'Start with a kettlebell in one hand, feet spread about 1.5 times shoulder distance with the kettlebell between your legs, bent over slightly.  Swing the kettlebell up with an explosive hip extension and snatch it above your head.  Swing the kettlebell back between your legs down and repeat.', 'hinge', true, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead Kettlebell Snatch', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead Kettlebell Snatch', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead Kettlebell Snatch', 'hip flexors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead Kettlebell Snatch', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead Kettlebell Snatch', 'adductors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead Kettlebell Snatch', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Overhead Kettlebell Snatch', 'lateral deltoid');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Overhead Kettlebell Snatch', 'kettlebell');
