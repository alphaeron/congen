--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add additional exercise data to expand the congen database.
-- Fake rollback - data population does not make sense to roll back
--rollback SELECT 1

-- Add new equipment if not already present
INSERT INTO equipment (name, description) VALUES ('valslides', 'Smooth plastic discs that slide on carpet or other surfaces for resistance training.')
ON CONFLICT (name) DO NOTHING;

INSERT INTO equipment (name, description) VALUES ('battle rope', 'Heavy ropes used for dynamic upper body and conditioning exercises.')
ON CONFLICT (name) DO NOTHING;

INSERT INTO equipment (name, description) VALUES ('weight plate', 'Circular weight plates used for various exercises and resistance training.')
ON CONFLICT (name) DO NOTHING;

INSERT INTO equipment (name, description) VALUES ('sandbag', 'Heavy bag filled with sand for functional strength training.')
ON CONFLICT (name) DO NOTHING;

-- Add new exercises
-- PLYOMETRIC EXERCISES
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Rotational Throw (Punch Emphasis)', 'Stand with feet shoulder-width apart, holding a medicine ball. Rotate your torso and explosively throw the ball forward with a punching motion, emphasizing power and rotation.', 'plyometric', false, true, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Weighted Box Jumps', 'Perform box jumps while holding weights, starting from a static position without the pre-stretch countermovement for maximum power output.', 'plyometric', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Broad Jump (weighted)', 'Perform a standing broad jump while holding weights, focusing on explosive power and proper landing mechanics.', 'plyometric', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Split Stance Jump', 'Start in a split stance position and explosively jump, switching leg positions in mid-air and landing in the opposite split stance.', 'plyometric', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Dumbbell Explosive Lunge Jump', 'Perform a lunge with dumbbells, then explosively jump up, switching leg positions in mid-air.', 'plyometric', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Lateral Step Ups On Box', 'Step up onto a box laterally, focusing on explosive power and proper form.', 'plyometric', true, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Lateral Single Leg Hops', 'Hop laterally on one leg, focusing on explosive power and stability.', 'plyometric', true, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Front High Knees', 'Run in place, bringing your knees up to waist level, focusing on explosive movement and proper form.', 'plyometric', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Ice Skaters', 'Jump laterally from side to side, landing on one foot and then the other, mimicking ice skating movements.', 'plyometric', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Tempo Runs', 'Perform running drills at various tempos, focusing on speed, power, and proper running mechanics.', 'plyometric', false, false, true);

-- JOINT INTEGRITY EXERCISES
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Band Pull Aparts', 'Hold a resistance band in front of you and pull it apart horizontally, focusing on shoulder and upper back strength.', 'isolation', false, true, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Hip Thrusts', 'Lie on your back with your feet on the ground and thrust your hips up, focusing on glute activation and strength.', 'hinge', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Banded Tricep Extension', 'Perform tricep extensions using a resistance band, focusing on proper form and muscle activation.', 'isolation', false, true, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Valslide Hamstring Curls', 'Lie on your back with your feet on Valslides and perform hamstring curls, focusing on proper form and muscle activation.', 'isolation', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Neck Flexion Extension With Harness', 'Use a neck harness to perform flexion and extension exercises, focusing on neck strength and stability.', 'isolation', false, true, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Plate Holds (fingertips)', 'Hold weight plates using only your fingertips, focusing on grip strength and forearm development.', 'isolation', false, true, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('4 Way Neck', 'Perform neck exercises in four directions (flexion, extension, lateral flexion left and right) using resistance.', 'isolation', false, true, true);

-- AUXILIARY EXERCISES
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Kettlebell Pull Through', 'Stand with your feet shoulder-width apart and a kettlebell between your legs. Hinge at the hips and pull the kettlebell through your legs, focusing on hip hinge mechanics.', 'hinge', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Hanging Leg Raises', 'Hang from a pull-up bar and raise your legs up to parallel or higher, focusing on core strength and control.', 'core', false, true, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Banded Kettlebell Swing', 'Perform kettlebell swings while wearing a resistance band around your waist, focusing on explosive power and proper form.', 'hinge', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Plank', 'Hold a plank position with your body in a straight line from head to heels, focusing on core stability and endurance.', 'core', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Banded Hamstring Curls', 'Perform hamstring curls using a resistance band, focusing on proper form and muscle activation.', 'isolation', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Suitcase Carry', 'Hold a heavy weight in one hand and walk, focusing on core stability and unilateral strength.', 'carry', true, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Thumbs Up Battle Rope', 'Hold battle ropes with thumbs up and perform various wave patterns, focusing on upper body strength and conditioning.', 'isolation', false, true, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Decline Sit Up', 'Perform sit-ups on a decline bench, focusing on proper form and core strength.', 'core', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Sit Ups', 'Perform traditional sit-ups, focusing on proper form and core strength.', 'core', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Decline Single Arm Sit Ups', 'Perform sit-ups on a decline bench while holding a weight in one hand, focusing on core strength and unilateral development.', 'core', true, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Medicine Ball Decline Russian Twist', 'Perform Russian twists on a decline bench while holding a medicine ball, focusing on core strength and rotation.', 'core', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Palloff Press', 'Stand sideways to a cable machine and perform a press movement, focusing on anti-rotation and core stability.', 'core', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Sled Marches', 'Push a sled while marching in place, focusing on lower body strength and conditioning.', 'hinge', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Thumbs Down Battle Rope', 'Hold battle ropes with thumbs down and perform various wave patterns, focusing on upper body strength and conditioning.', 'isolation', false, true, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Swiss Ball Leg Curl Glute HAM', 'Lie on your back with your feet on a Swiss ball and perform leg curls, focusing on hamstring and glute strength.', 'isolation', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('1/2 Turkish Get Up', 'Perform the first half of a Turkish get-up, focusing on shoulder stability and core strength.', 'core', false, false, true);

INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory) VALUES
('Deadman Hangs', 'Hang from a pull-up bar in a relaxed position, focusing on grip strength and shoulder mobility.', 'isolation', false, true, true);

-- Add muscle relationships for exercises
-- PLYOMETRIC EXERCISES
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Rotational Throw (Punch Emphasis)', 'obliques');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Rotational Throw (Punch Emphasis)', 'rectus abdominis');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Rotational Throw (Punch Emphasis)', 'serratus anterior');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Rotational Throw (Punch Emphasis)', 'pec major');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Rotational Throw (Punch Emphasis)', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Weighted Box Jumps', 'quadriceps');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Weighted Box Jumps', 'glutes');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Weighted Box Jumps', 'hamstrings');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Weighted Box Jumps', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Broad Jump (weighted)', 'quadriceps');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Broad Jump (weighted)', 'glutes');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Broad Jump (weighted)', 'hamstrings');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Broad Jump (weighted)', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Stance Jump', 'quadriceps');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Stance Jump', 'glutes');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Stance Jump', 'hamstrings');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Split Stance Jump', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Dumbbell Explosive Lunge Jump', 'quadriceps');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Dumbbell Explosive Lunge Jump', 'glutes');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Dumbbell Explosive Lunge Jump', 'hamstrings');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Dumbbell Explosive Lunge Jump', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Lateral Step Ups On Box', 'quadriceps');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Lateral Step Ups On Box', 'glutes');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Lateral Step Ups On Box', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Lateral Single Leg Hops', 'quadriceps');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Lateral Single Leg Hops', 'glutes');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Lateral Single Leg Hops', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Front High Knees', 'quadriceps');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Front High Knees', 'hip flexors');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Front High Knees', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ice Skaters', 'quadriceps');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ice Skaters', 'glutes');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ice Skaters', 'adductors');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Ice Skaters', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Tempo Runs', 'quadriceps');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Tempo Runs', 'glutes');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Tempo Runs', 'hamstrings');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Tempo Runs', 'calves');

-- JOINT INTEGRITY EXERCISES
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Band Pull Aparts', 'rear deltoid');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Band Pull Aparts', 'traps');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Band Pull Aparts', 'rhomboids');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Hip Thrusts', 'glutes');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Hip Thrusts', 'hamstrings');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Hip Thrusts', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Tricep Extension', 'triceps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Valslide Hamstring Curls', 'hamstrings');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Valslide Hamstring Curls', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Neck Flexion Extension With Harness', 'neck');

INSERT INTO muscle (name, description) VALUES ('forearms', 'The forearms are the muscles that extend the wrist and fingers. They are located on the front of the lower arm, between the elbow and the wrist.');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Plate Holds (fingertips)', 'forearms');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('4 Way Neck', 'neck');

-- AUXILIARY EXERCISES
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Kettlebell Pull Through', 'glutes');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Kettlebell Pull Through', 'hamstrings');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Kettlebell Pull Through', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Hanging Leg Raises', 'rectus abdominis');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Hanging Leg Raises', 'hip flexors');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Hanging Leg Raises', 'obliques');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Kettlebell Swing', 'glutes');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Kettlebell Swing', 'hamstrings');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Kettlebell Swing', 'rectus abdominis');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Plank', 'rectus abdominis');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Plank', 'obliques');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Plank', 'erector spinae');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Banded Hamstring Curls', 'hamstrings');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Suitcase Carry', 'rectus abdominis');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Suitcase Carry', 'obliques');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Suitcase Carry', 'traps');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Up Battle Rope', 'pec major');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Up Battle Rope', 'anterior deltoid');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Up Battle Rope', 'triceps');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Up Battle Rope', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Decline Sit Up', 'rectus abdominis');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Decline Sit Up', 'hip flexors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sit Ups', 'rectus abdominis');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sit Ups', 'hip flexors');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Decline Single Arm Sit Ups', 'rectus abdominis');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Decline Single Arm Sit Ups', 'hip flexors');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Decline Single Arm Sit Ups', 'obliques');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Medicine Ball Decline Russian Twist', 'rectus abdominis');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Medicine Ball Decline Russian Twist', 'obliques');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Palloff Press', 'rectus abdominis');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Palloff Press', 'obliques');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sled Marches', 'quadriceps');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sled Marches', 'glutes');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Sled Marches', 'calves');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Down Battle Rope', 'pec major');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Down Battle Rope', 'anterior deltoid');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Down Battle Rope', 'triceps');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Thumbs Down Battle Rope', 'lats');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Swiss Ball Leg Curl Glute HAM', 'hamstrings');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Swiss Ball Leg Curl Glute HAM', 'glutes');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('1/2 Turkish Get Up', 'rectus abdominis');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('1/2 Turkish Get Up', 'obliques');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('1/2 Turkish Get Up', 'anterior deltoid');

INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Deadman Hangs', 'lats');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Deadman Hangs', 'traps');
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES ('Deadman Hangs', 'biceps');

-- Add equipment relationships for exercises
-- PLYOMETRIC EXERCISES
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Rotational Throw (Punch Emphasis)', 'med ball');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Weighted Box Jumps', 'box');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Weighted Box Jumps', 'dumbbells');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Broad Jump (weighted)', 'dumbbells');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Split Stance Jump', 'dumbbells');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Dumbbell Explosive Lunge Jump', 'dumbbells');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Lateral Step Ups On Box', 'box');

-- JOINT INTEGRITY EXERCISES
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Band Pull Aparts', 'bands');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Banded Tricep Extension', 'bands');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Valslide Hamstring Curls', 'valslides');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Neck Flexion Extension With Harness', 'iron neck');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Plate Holds (fingertips)', 'weight plate');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('4 Way Neck', 'iron neck');

-- AUXILIARY EXERCISES
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Kettlebell Pull Through', 'kettlebell');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Hanging Leg Raises', 'pull-up bar');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Banded Kettlebell Swing', 'kettlebell');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Banded Kettlebell Swing', 'bands');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Banded Hamstring Curls', 'bands');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Thumbs Up Battle Rope', 'battle rope');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Decline Sit Up', 'adjustable bench');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Decline Single Arm Sit Ups', 'adjustable bench');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Decline Single Arm Sit Ups', 'dumbbells');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Medicine Ball Decline Russian Twist', 'med ball');
INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Medicine Ball Decline Russian Twist', 'adjustable bench');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Sled Marches', 'sled');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Thumbs Down Battle Rope', 'battle rope');

INSERT INTO exercise_equipment (exercise_name, equipment_name) VALUES ('Swiss Ball Leg Curl Glute HAM', 'physioball');

-- Add workout type assignments for exercises
-- Most exercises are suitable for both workout types as they are accessories
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Rotational Throw (Punch Emphasis)', 'plyometric', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Rotational Throw (Punch Emphasis)', 'plyometric', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Weighted Box Jumps', 'plyometric', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Weighted Box Jumps', 'plyometric', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Broad Jump (weighted)', 'plyometric', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Broad Jump (weighted)', 'plyometric', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Split Stance Jump', 'plyometric', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Split Stance Jump', 'plyometric', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Dumbbell Explosive Lunge Jump', 'plyometric', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Dumbbell Explosive Lunge Jump', 'plyometric', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Lateral Step Ups On Box', 'plyometric', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Lateral Step Ups On Box', 'plyometric', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Lateral Single Leg Hops', 'plyometric', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Lateral Single Leg Hops', 'plyometric', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Front High Knees', 'plyometric', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Front High Knees', 'plyometric', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Ice Skaters', 'plyometric', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Ice Skaters', 'plyometric', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Tempo Runs', 'plyometric', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Tempo Runs', 'plyometric', 'maximal_effort');

-- JOINT INTEGRITY EXERCISES
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Band Pull Aparts', 'isolation', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Band Pull Aparts', 'isolation', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Hip Thrusts', 'hinge', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Hip Thrusts', 'hinge', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Tricep Extension', 'isolation', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Tricep Extension', 'isolation', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Valslide Hamstring Curls', 'isolation', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Valslide Hamstring Curls', 'isolation', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Neck Flexion Extension With Harness', 'isolation', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Neck Flexion Extension With Harness', 'isolation', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Plate Holds (fingertips)', 'isolation', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Plate Holds (fingertips)', 'isolation', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('4 Way Neck', 'isolation', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('4 Way Neck', 'isolation', 'maximal_effort');

-- AUXILIARY EXERCISES
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Kettlebell Pull Through', 'hinge', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Kettlebell Pull Through', 'hinge', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Hanging Leg Raises', 'core', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Hanging Leg Raises', 'core', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Kettlebell Swing', 'hinge', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Kettlebell Swing', 'hinge', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Plank', 'core', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Plank', 'core', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Hamstring Curls', 'isolation', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Banded Hamstring Curls', 'isolation', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Suitcase Carry', 'carry', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Suitcase Carry', 'carry', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Thumbs Up Battle Rope', 'isolation', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Thumbs Up Battle Rope', 'isolation', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Decline Sit Up', 'core', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Decline Sit Up', 'core', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sit Ups', 'core', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sit Ups', 'core', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Decline Single Arm Sit Ups', 'core', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Decline Single Arm Sit Ups', 'core', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Medicine Ball Decline Russian Twist', 'core', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Medicine Ball Decline Russian Twist', 'core', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Palloff Press', 'core', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Palloff Press', 'core', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sled Marches', 'hinge', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sled Marches', 'hinge', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Thumbs Down Battle Rope', 'isolation', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Thumbs Down Battle Rope', 'isolation', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Swiss Ball Leg Curl Glute HAM', 'isolation', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Swiss Ball Leg Curl Glute HAM', 'isolation', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('1/2 Turkish Get Up', 'core', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('1/2 Turkish Get Up', 'core', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Deadman Hangs', 'isolation', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Deadman Hangs', 'isolation', 'maximal_effort');
