--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add more powerlifting exercises with proper accessory marking for lats targeting
--rollback SELECT 1

-- Add new equipment that doesn't exist yet
INSERT INTO equipment (name, description) VALUES ('dip bars', 'Parallel bars used for performing dips and other bodyweight exercises.');

-- Kettlebell Waiter's Walk
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Kettlebell Waiters Walk',
  'Hold a kettlebell overhead with arm fully extended and walk while keeping shoulders level and core braced.',
  'core',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Kettlebell Waiters Walk', 'anterior deltoid'),
  ('Kettlebell Waiters Walk', 'lateral deltoid'),
  ('Kettlebell Waiters Walk', 'rear deltoid'),
  ('Kettlebell Waiters Walk', 'traps'),
  ('Kettlebell Waiters Walk', 'rhomboids'),
  ('Kettlebell Waiters Walk', 'rotator cuff'),
  ('Kettlebell Waiters Walk', 'serratus anterior'),
  ('Kettlebell Waiters Walk', 'obliques'),
  ('Kettlebell Waiters Walk', 'erector spinae');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Kettlebell Waiters Walk', 'kettlebell');

-- Weighted Pull-Up (including towel/gi variations)
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Weighted Pull-Up (towel/gi variation)',
  'Perform pull-up adding weight or using towel/gi to increase grip and challenge.',
  'vertical_pull',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Weighted Pull-Up (towel/gi variation)', 'lats'),
  ('Weighted Pull-Up (towel/gi variation)', 'biceps'),
  ('Weighted Pull-Up (towel/gi variation)', 'rhomboids'),
  ('Weighted Pull-Up (towel/gi variation)', 'traps');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Weighted Pull-Up (towel/gi variation)', 'pull-up bar');

-- Dumbbell Floor Press
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Floor Press',
  'Pressing from floor with dumbbells emphasizing triceps and chest.',
  'horizontal_push',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Floor Press', 'triceps'),
  ('Dumbbell Floor Press', 'pec major'),
  ('Dumbbell Floor Press', 'anterior deltoid');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Floor Press', 'dumbbells');

-- Dumbbell Skull Crushers / French Press
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Skull Crushers',
  'Lying triceps extension with dumbbells.',
  'horizontal_push',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Skull Crushers', 'triceps');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Skull Crushers', 'dumbbells');

-- Standing Overhead Dumbbell Triceps Extension
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Standing Overhead Dumbbell Triceps Extension',
  'Single or two-arm overhead triceps extension with dumbbell.',
  'vertical_push',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Standing Overhead Dumbbell Triceps Extension', 'triceps');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Standing Overhead Dumbbell Triceps Extension', 'dumbbells');

-- Dumbbell Triceps Kickbacks
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Triceps Kickbacks',
  'Bent-over triceps extension performed with dumbbells.',
  'horizontal_push',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Triceps Kickbacks', 'triceps');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Triceps Kickbacks', 'dumbbells');

-- Kettlebell Clean & Press
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Kettlebell Clean & Press',
  'Clean a kettlebell to rack and press it overhead.',
  'plyometric',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Kettlebell Clean & Press', 'anterior deltoid'),
  ('Kettlebell Clean & Press', 'lateral deltoid'),
  ('Kettlebell Clean & Press', 'traps'),
  ('Kettlebell Clean & Press', 'lats'),
  ('Kettlebell Clean & Press', 'glutes'),
  ('Kettlebell Clean & Press', 'hamstrings'),
  ('Kettlebell Clean & Press', 'quadriceps'),
  ('Kettlebell Clean & Press', 'rectus abdominis');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Kettlebell Clean & Press', 'kettlebell');

-- Double Kettlebell Jerk
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Double Kettlebell Jerk',
  'Explosive overhead pressing of two kettlebells from rack via dip and drive.',
  'plyometric',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Double Kettlebell Jerk', 'anterior deltoid'),
  ('Double Kettlebell Jerk', 'lateral deltoid'),
  ('Double Kettlebell Jerk', 'triceps'),
  ('Double Kettlebell Jerk', 'glutes'),
  ('Double Kettlebell Jerk', 'hamstrings'),
  ('Double Kettlebell Jerk', 'quadriceps'),
  ('Double Kettlebell Jerk', 'rectus abdominis');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Double Kettlebell Jerk', 'kettlebell');

-- Dumbbell Row (One-Arm)
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'One‑Arm Dumbbell Row',
  'Bent-over rowing motion with a single dumbbell.',
  'horizontal_pull',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('One‑Arm Dumbbell Row', 'lats'),
  ('One‑Arm Dumbbell Row', 'rhomboids'),
  ('One‑Arm Dumbbell Row', 'traps'),
  ('One‑Arm Dumbbell Row', 'rear deltoid'),
  ('One‑Arm Dumbbell Row', 'biceps');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('One‑Arm Dumbbell Row', 'dumbbells');

-- Meadows Row
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Meadows Row',
  'Landmine-supported one-arm row, chest at an angle.',
  'horizontal_pull',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Meadows Row', 'lats'),
  ('Meadows Row', 'rhomboids'),
  ('Meadows Row', 'rear deltoid'),
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Meadows Row', 'landmine');

-- Face Pulls (Band variation)
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Face Pulls',
  'Pulling band or cable toward face to work upper back and shoulder health.',
  'horizontal_pull',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Face Pulls', 'rear deltoid'),
  ('Face Pulls', 'rotator cuff'),
  ('Face Pulls', 'rhomboids'),
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Face Pulls', 'bands');

-- Dumbbell Biceps Curls
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Biceps Curl',
  'Classic dumbbell curl targeting biceps.',
  'isolation',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Biceps Curl', 'biceps');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Biceps Curl', 'dumbbells');

-- Hammer Curl (Variation of Biceps)
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Hammer Curl',
  'Neutral-grip dumbbell curl targeting brachialis and biceps.',
  'isolation',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Hammer Curl', 'biceps');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Hammer Curl', 'dumbbells');

-- Wrist Curls / Reverse Wrist Curls
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Wrist Curls',
  'Flexion/extension of wrists to strengthen forearm muscles.',
  'isolation',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Wrist Curls', 'biceps');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Wrist Curls', 'dumbbells');

-- Grip Crushers & Towel Hangs
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Grip Crushers / Towel Hangs',
  'Grip training using towels or static holds for grip endurance.',
  'isolation',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Grip Crushers / Towel Hangs', 'biceps');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Grip Crushers / Towel Hangs', 'pull-up bar');

-- Farmer's Carries
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Farmers Carry',
  'Walk while holding heavy weights in each hand, improving grip and stability.',
  'core',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Farmers Carry', 'biceps'),
  ('Farmers Carry', 'traps'),
  ('Farmers Carry', 'anterior deltoid'),
  ('Farmers Carry', 'lateral deltoid'),
  ('Farmers Carry', 'erector spinae'),
  ('Farmers Carry', 'obliques');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Farmers Carry', 'dumbbells');

-- Battle Ropes
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Battle Ropes',
  'Alternate or double-wave motion using heavy ropes for arm and shoulder conditioning.',
  'plyometric',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Battle Ropes', 'anterior deltoid'),
  ('Battle Ropes', 'lateral deltoid'),
  ('Battle Ropes', 'pec major'),
  ('Battle Ropes', 'traps'),
  ('Battle Ropes', 'biceps'),
  ('Battle Ropes', 'obliques');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Battle Ropes', 'battle rope');

-- Bulgarian Bag Movements
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Bulgarian Bag Rotational Work',
  'Dynamic rotational swinging patterns using a Bulgarian bag.',
  'plyometric',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Bulgarian Bag Rotational Work', 'anterior deltoid'),
  ('Bulgarian Bag Rotational Work', 'lateral deltoid'),
  ('Bulgarian Bag Rotational Work', 'biceps'),
  ('Bulgarian Bag Rotational Work', 'obliques'),
  ('Bulgarian Bag Rotational Work', 'lats');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Bulgarian Bag Rotational Work', 'bands');

-- Pull-Ups (different from existing Chin-Up)
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Pull-Ups',
  'Standard pull-up with overhand grip targeting lats and upper back.',
  'vertical_pull',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Pull-Ups', 'lats'),
  ('Pull-Ups', 'rhomboids'),
  ('Pull-Ups', 'traps'),
  ('Pull-Ups', 'rear deltoid'),
  ('Pull-Ups', 'biceps');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Pull-Ups', 'pull-up bar');

-- Dips
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dips',
  'Bodyweight triceps exercise performed on parallel bars or bench.',
  'vertical_push',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dips', 'triceps'),
  ('Dips', 'anterior deltoid'),
  ('Dips', 'pec major');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dips', 'dip bars');

-- Front Raises
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Front Raises',
  'Lift dumbbells or weights in front of your body to shoulder height.',
  'vertical_push',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Front Raises', 'anterior deltoid'),
  ('Front Raises', 'serratus anterior');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Front Raises', 'dumbbells');

-- Dumbbell Arm Bar
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Arm Bar',
  'Lie on your side holding a dumbbell and rotate your arm in a controlled arc to improve shoulder mobility and stability.',
  'isolation',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Arm Bar', 'anterior deltoid'),
  ('Dumbbell Arm Bar', 'lateral deltoid'),
  ('Dumbbell Arm Bar', 'rear deltoid'),
  ('Dumbbell Arm Bar', 'rotator cuff'),
  ('Dumbbell Arm Bar', 'serratus anterior');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Arm Bar', 'dumbbells');

-- Dumbbell Shrug
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Shrug',
  'Lift dumbbells by raising your shoulders up toward your ears, targeting the trapezius muscles.',
  'isolation',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Shrug', 'traps'),
  ('Dumbbell Shrug', 'rhomboids');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Shrug', 'dumbbells');

-- Dumbbell Cuban Press
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Cuban Press',
  'External rotation movement followed by overhead press, targeting rotator cuff and deltoids.',
  'vertical_push',
  true,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Cuban Press', 'rotator cuff'),
  ('Dumbbell Cuban Press', 'rear deltoid'),
  ('Dumbbell Cuban Press', 'lateral deltoid'),
  ('Dumbbell Cuban Press', 'anterior deltoid');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Cuban Press', 'dumbbells');

-- Double Incline Flye
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Double Incline Flye',
  'Chest flye movement performed on an inclined bench with dumbbells.',
  'horizontal_push',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Double Incline Flye', 'pec major'),
  ('Double Incline Flye', 'anterior deltoid'),
  ('Double Incline Flye', 'serratus anterior');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Double Incline Flye', 'dumbbells');

-- Dumbbell Pullover
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Dumbbell Pullover',
  'Lying movement bringing dumbbell from behind head to over chest, targeting chest and lats.',
  'horizontal_pull',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Dumbbell Pullover', 'pec major'),
  ('Dumbbell Pullover', 'lats'),
  ('Dumbbell Pullover', 'serratus anterior');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Dumbbell Pullover', 'dumbbells');

-- Add exercise_workout_type entries for exercises that could be used in dynamic_effort or maximal_effort workouts
-- Pull-Ups can be used for dynamic effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Pull-Ups', 'vertical_pull', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Pull-Ups', 'vertical_pull', 'maximal_effort');

-- Weighted Pull-Up can be used for both dynamic and maximal effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Weighted Pull-Up (towel/gi variation)', 'vertical_pull', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Weighted Pull-Up (towel/gi variation)', 'vertical_pull', 'maximal_effort');

-- Dips can be used for dynamic effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Dips', 'vertical_push', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Dips', 'vertical_push', 'maximal_effort');

-- Dumbbell Floor Press can be used for both dynamic and maximal effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Dumbbell Floor Press', 'horizontal_push', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Dumbbell Floor Press', 'horizontal_push', 'maximal_effort');

-- Kettlebell Clean & Press can be used for dynamic effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Kettlebell Clean & Press', 'plyometric', 'dynamic_effort');

-- Double Kettlebell Jerk can be used for dynamic effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Double Kettlebell Jerk', 'plyometric', 'dynamic_effort');

-- One-Arm Dumbbell Row can be used for dynamic effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('One‑Arm Dumbbell Row', 'horizontal_pull', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('One‑Arm Dumbbell Row', 'horizontal_pull', 'maximal_effort');

-- Meadows Row can be used for dynamic effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Meadows Row', 'horizontal_pull', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Meadows Row', 'horizontal_pull', 'maximal_effort');
