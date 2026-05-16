--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add exercise Chest-Supported Dumbbell Row
--rollback SELECT 1

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES ('Chest-Supported Dumbbell Row', 'Start with your chest on an inclined bench and 2 dumbbells in either hand off to the side.  Squeeze your shoulderblades together as you pull the dumbbells straight up, keeping a neutral spine.', 'horizontal_pull', true, true, true);

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chest-Supported Dumbbell Row', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chest-Supported Dumbbell Row', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chest-Supported Dumbbell Row', 'rotator cuff');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chest-Supported Dumbbell Row', 'teres minor');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chest-Supported Dumbbell Row', 'teres major');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chest-Supported Dumbbell Row', 'rhomboids');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chest-Supported Dumbbell Row', 'rear deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Chest-Supported Dumbbell Row', 'biceps');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Chest-Supported Dumbbell Row', 'adjustable bench');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Chest-Supported Dumbbell Row', 'dumbbells');
