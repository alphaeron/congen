--liquibase formatted sql

--changeset John Matty:1 labels:prod,test
--comment: Add strongman and sandbag exercises to expand exercise variety
--rollback SELECT 1

-- Add new equipment that doesn't exist yet
INSERT INTO equipment (name, description) VALUES ('tire', 'Heavy tire used for flipping exercises and strongman training.')
ON CONFLICT (name) DO NOTHING;

INSERT INTO equipment (name, description) VALUES ('axle', 'Thick bar used for strongman exercises, requiring more grip strength than standard bars.')
ON CONFLICT (name) DO NOTHING;

INSERT INTO equipment (name, description) VALUES ('rope', 'Heavy rope or chain used for dragging exercises in strongman training.')
ON CONFLICT (name) DO NOTHING;

-- STRONGMAN EXERCISES

-- Farmers Walk (different from existing Farmers Carry - this is the classic strongman version)
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Farmers Walk',
  'Pick up a heavy implement in each hand and walk quickly while maintaining posture and grip.',
  'carry',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Farmers Walk', 'forearms'),
  ('Farmers Walk', 'traps'),
  ('Farmers Walk', 'upper back'),
  ('Farmers Walk', 'glutes'),
  ('Farmers Walk', 'calves'),
  ('Farmers Walk', 'rectus abdominis'),
  ('Farmers Walk', 'obliques');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Farmers Walk', 'dumbbells');

-- Axle Press
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Axle Press',
  'Press a thick axle bar. The thick grip taxes the forearms and clean is harder due to bar size.',
  'vertical_push',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Axle Press', 'forearms'),
  ('Axle Press', 'anterior deltoid'),
  ('Axle Press', 'lateral deltoid'),
  ('Axle Press', 'triceps'),
  ('Axle Press', 'upper back'),
  ('Axle Press', 'glutes'),
  ('Axle Press', 'rectus abdominis');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Axle Press', 'axle');

-- Sandbag Carry / Load
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Carry Load',
  'Pick up sandbag from ground and either carry (bear hug or shoulder) or load it to platform or over bar.',
  'carry',
  false,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Carry Load', 'rectus abdominis'),
  ('Sandbag Carry Load', 'obliques'),
  ('Sandbag Carry Load', 'anterior deltoid'),
  ('Sandbag Carry Load', 'glutes'),
  ('Sandbag Carry Load', 'upper back'),
  ('Sandbag Carry Load', 'forearms');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Carry Load', 'sandbag');

-- Tire Flip
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Tire Flip',
  'Squat down and grip underside of a heavy tire. Drive through legs and extend to flip the tire end over end.',
  'hinge',
  false,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Tire Flip', 'glutes'),
  ('Tire Flip', 'quadriceps'),
  ('Tire Flip', 'hamstrings'),
  ('Tire Flip', 'pec major'),
  ('Tire Flip', 'triceps'),
  ('Tire Flip', 'rectus abdominis'),
  ('Tire Flip', 'obliques'),
  ('Tire Flip', 'forearms');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Tire Flip', 'tire');

-- Axle Deadlift
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Axle Deadlift',
  'Pull a heavy implement from the ground. Axles require more grip strength due to thickness and shape.',
  'hinge',
  false,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Axle Deadlift', 'hamstrings'),
  ('Axle Deadlift', 'glutes'),
  ('Axle Deadlift', 'upper back'),
  ('Axle Deadlift', 'traps'),
  ('Axle Deadlift', 'forearms'),
  ('Axle Deadlift', 'rectus abdominis'),
  ('Axle Deadlift', 'obliques');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Axle Deadlift', 'axle');

-- Rope Drag / Pull
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Rope Drag/ Pull',
  'Pull a rope or chain across a distance, often with a backward lean.',
  'hinge',
  false,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Rope Drag/ Pull', 'quadriceps'),
  ('Rope Drag/ Pull', 'calves'),
  ('Rope Drag/ Pull', 'glutes'),
  ('Rope Drag/ Pull', 'rectus abdominis'),
  ('Rope Drag/ Pull', 'obliques'),
  ('Rope Drag/ Pull', 'upper back'),
  ('Rope Drag/ Pull', 'forearms');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Rope Drag/ Pull', 'rope');

-- Zercher Carry
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Zercher Carry',
  'Carry an object in the crooks of your elbows for time or distance.',
  'carry',
  false,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Zercher Carry', 'biceps'),
  ('Zercher Carry', 'upper back'),
  ('Zercher Carry', 'rectus abdominis'),
  ('Zercher Carry', 'obliques'),
  ('Zercher Carry', 'quadriceps'),
  ('Zercher Carry', 'glutes');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Zercher Carry', 'power bar');

-- SANDBAG EXERCISES

-- Sandbag Clean
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Clean',
  'Lift the sandbag from the ground to the chest in one powerful motion.',
  'plyometric',
  false,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Clean', 'glutes'),
  ('Sandbag Clean', 'hip flexors'),
  ('Sandbag Clean', 'traps'),
  ('Sandbag Clean', 'biceps'),
  ('Sandbag Clean', 'rectus abdominis'),
  ('Sandbag Clean', 'obliques');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Clean', 'sandbag');

-- Sandbag Squat
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Squat',
  'Hold the sandbag at chest or shoulder height and perform a squat.',
  'squat',
  false,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Squat', 'quadriceps'),
  ('Sandbag Squat', 'glutes'),
  ('Sandbag Squat', 'hamstrings'),
  ('Sandbag Squat', 'rectus abdominis'),
  ('Sandbag Squat', 'obliques');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Squat', 'sandbag');

-- Sandbag Over Shoulder
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Over Shoulder',
  'Clean and throw the sandbag over one shoulder; reset and repeat.',
  'plyometric',
  true,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Over Shoulder', 'glutes'),
  ('Sandbag Over Shoulder', 'hamstrings'),
  ('Sandbag Over Shoulder', 'upper back'),
  ('Sandbag Over Shoulder', 'triceps'),
  ('Sandbag Over Shoulder', 'rectus abdominis'),
  ('Sandbag Over Shoulder', 'obliques');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Over Shoulder', 'sandbag');

-- Sandbag Shouldering
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Shouldering',
  'Clean the sandbag to one shoulder and balance it; switch sides.',
  'plyometric',
  true,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Shouldering', 'traps'),
  ('Sandbag Shouldering', 'anterior deltoid'),
  ('Sandbag Shouldering', 'lateral deltoid'),
  ('Sandbag Shouldering', 'obliques'),
  ('Sandbag Shouldering', 'rectus abdominis'),
  ('Sandbag Shouldering', 'glutes');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Shouldering', 'sandbag');

-- Sandbag Carry (Shouldered)
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Carry Shouldered',
  'Carry the sandbag on one shoulder while walking.',
  'carry',
  true,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Carry Shouldered', 'rectus abdominis'),
  ('Sandbag Carry Shouldered', 'obliques'),
  ('Sandbag Carry Shouldered', 'glutes'),
  ('Sandbag Carry Shouldered', 'upper back'),
  ('Sandbag Carry Shouldered', 'traps');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Carry Shouldered', 'sandbag');

-- Sandbag Get-Up
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Get-Up',
  'Start on the ground holding sandbag, stand up with it held overhead or on shoulder.',
  'core',
  false,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Get-Up', 'rectus abdominis'),
  ('Sandbag Get-Up', 'obliques'),
  ('Sandbag Get-Up', 'quadriceps'),
  ('Sandbag Get-Up', 'anterior deltoid'),
  ('Sandbag Get-Up', 'hip flexors'),
  ('Sandbag Get-Up', 'glutes');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Get-Up', 'sandbag');

-- Sandbag Push Press
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Push Press',
  'Dip the knees and use leg drive to press sandbag overhead.',
  'vertical_push',
  false,
  true,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Push Press', 'anterior deltoid'),
  ('Sandbag Push Press', 'lateral deltoid'),
  ('Sandbag Push Press', 'triceps'),
  ('Sandbag Push Press', 'quadriceps'),
  ('Sandbag Push Press', 'glutes'),
  ('Sandbag Push Press', 'rectus abdominis');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Push Press', 'sandbag');

-- Sandbag Deadlift
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Deadlift',
  'Lift the sandbag from the ground by hugging or using handles.',
  'hinge',
  false,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Deadlift', 'hamstrings'),
  ('Sandbag Deadlift', 'glutes'),
  ('Sandbag Deadlift', 'upper back'),
  ('Sandbag Deadlift', 'forearms'),
  ('Sandbag Deadlift', 'rectus abdominis'),
  ('Sandbag Deadlift', 'obliques');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Deadlift', 'sandbag');

-- Sandbag Thruster
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Thruster',
  'Perform a squat with sandbag, then press overhead in one motion.',
  'plyometric',
  false,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Thruster', 'quadriceps'),
  ('Sandbag Thruster', 'glutes'),
  ('Sandbag Thruster', 'hamstrings'),
  ('Sandbag Thruster', 'anterior deltoid'),
  ('Sandbag Thruster', 'lateral deltoid'),
  ('Sandbag Thruster', 'triceps'),
  ('Sandbag Thruster', 'rectus abdominis');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Thruster', 'sandbag');

-- Sandbag Shouldered Lunges
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Shouldered Lunges',
  'Place sandbag on one shoulder and perform walking or stationary lunges.',
  'squat',
  true,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Shouldered Lunges', 'quadriceps'),
  ('Sandbag Shouldered Lunges', 'glutes'),
  ('Sandbag Shouldered Lunges', 'hamstrings'),
  ('Sandbag Shouldered Lunges', 'rectus abdominis'),
  ('Sandbag Shouldered Lunges', 'obliques'),
  ('Sandbag Shouldered Lunges', 'traps');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Shouldered Lunges', 'sandbag');

-- Sandbag Zercher Squat
INSERT INTO exercise (name, description, movement_type, is_unilateral, is_upper, is_accessory)
VALUES (
  'Sandbag Zercher Squat',
  'Hold the sandbag in the elbows and squat down with good form.',
  'squat',
  false,
  false,
  true
);
INSERT INTO exercise_muscle (exercise_name, muscle_name) VALUES
  ('Sandbag Zercher Squat', 'biceps'),
  ('Sandbag Zercher Squat', 'quadriceps'),
  ('Sandbag Zercher Squat', 'glutes'),
  ('Sandbag Zercher Squat', 'rectus abdominis'),
  ('Sandbag Zercher Squat', 'obliques');
INSERT INTO exercise_equipment (exercise_name, equipment_name)
VALUES ('Sandbag Zercher Squat', 'sandbag');

-- Add exercise_workout_type entries for exercises that could be used in dynamic_effort or maximal_effort workouts

-- Strongman exercises suitable for dynamic effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Farmers Walk', 'carry', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Farmers Walk', 'carry', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Axle Press', 'vertical_push', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Axle Press', 'vertical_push', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Carry Load', 'carry', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Carry Load', 'carry', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Tire Flip', 'hinge', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Tire Flip', 'hinge', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Axle Deadlift', 'hinge', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Axle Deadlift', 'hinge', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Rope Drag/ Pull', 'hinge', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Rope Drag/ Pull', 'hinge', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Zercher Carry', 'carry', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Zercher Carry', 'carry', 'maximal_effort');

-- Sandbag exercises suitable for dynamic effort
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Clean', 'plyometric', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Clean', 'plyometric', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Squat', 'squat', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Squat', 'squat', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Over Shoulder', 'plyometric', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Over Shoulder', 'plyometric', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Shouldering', 'plyometric', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Shouldering', 'plyometric', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Carry Shouldered', 'carry', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Carry Shouldered', 'carry', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Get-Up', 'core', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Get-Up', 'core', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Push Press', 'vertical_push', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Push Press', 'vertical_push', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Deadlift', 'hinge', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Deadlift', 'hinge', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Thruster', 'plyometric', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Thruster', 'plyometric', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Shouldered Lunges', 'squat', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Shouldered Lunges', 'squat', 'maximal_effort');

INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Zercher Squat', 'squat', 'dynamic_effort');
INSERT INTO exercise_workout_type (exercise_name, movement_type, workout_type) VALUES ('Sandbag Zercher Squat', 'squat', 'maximal_effort');
