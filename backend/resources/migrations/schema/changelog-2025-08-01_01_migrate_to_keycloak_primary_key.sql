--liquibase formatted sql

--changeset John Matty:14 labels:prod,test
--comment: Migrate user table to use Keycloak ID as primary key and update all foreign key relationships.

-- Step 1: Add new keycloak_id column to user table (will become the new primary key)
ALTER TABLE "user" ADD COLUMN keycloak_id VARCHAR(255);

-- Step 2: Populate keycloak_id with existing keycloak_user_id values
UPDATE "user" SET keycloak_id = keycloak_user_id WHERE keycloak_user_id IS NOT NULL;

-- Step 3: Make keycloak_id NOT NULL after population
ALTER TABLE "user" ALTER COLUMN keycloak_id SET NOT NULL;

-- Step 4: Add unique constraint on keycloak_id
CREATE UNIQUE INDEX idx_user_keycloak_id_unique ON "user"(keycloak_id);

-- Step 5: Add new foreign key columns to all related tables
ALTER TABLE user_equipment ADD COLUMN user_keycloak_id VARCHAR(255);
ALTER TABLE user_program_preferences ADD COLUMN user_keycloak_id VARCHAR(255);
ALTER TABLE user_exercise_preference ADD COLUMN user_keycloak_id VARCHAR(255);
ALTER TABLE user_one_rep_max ADD COLUMN user_keycloak_id VARCHAR(255);
ALTER TABLE program ADD COLUMN user_keycloak_id VARCHAR(255);
ALTER TABLE exercise_rotation_history ADD COLUMN user_keycloak_id VARCHAR(255);
ALTER TABLE user_weak_muscle ADD COLUMN user_keycloak_id VARCHAR(255);
ALTER TABLE user_weight_unit_preference ADD COLUMN user_keycloak_id VARCHAR(255);

-- Step 6: Populate new foreign key columns with Keycloak IDs
UPDATE user_equipment SET user_keycloak_id = u.keycloak_id 
FROM "user" u WHERE user_equipment.user_id = u.id;

UPDATE user_program_preferences SET user_keycloak_id = u.keycloak_id 
FROM "user" u WHERE user_program_preferences.user_id = u.id;

UPDATE user_exercise_preference SET user_keycloak_id = u.keycloak_id 
FROM "user" u WHERE user_exercise_preference.user_id = u.id;

UPDATE user_one_rep_max SET user_keycloak_id = u.keycloak_id 
FROM "user" u WHERE user_one_rep_max.user_id = u.id;

UPDATE program SET user_keycloak_id = u.keycloak_id 
FROM "user" u WHERE program.user_id = u.id;

UPDATE exercise_rotation_history SET user_keycloak_id = u.keycloak_id 
FROM "user" u WHERE exercise_rotation_history.user_id = u.id;

UPDATE user_weak_muscle SET user_keycloak_id = u.keycloak_id 
FROM "user" u WHERE user_weak_muscle.user_id = u.id;

UPDATE user_weight_unit_preference SET user_keycloak_id = u.keycloak_id 
FROM "user" u WHERE user_weight_unit_preference.user_id = u.id;

-- Step 7: Make new foreign key columns NOT NULL
ALTER TABLE user_equipment ALTER COLUMN user_keycloak_id SET NOT NULL;
ALTER TABLE user_program_preferences ALTER COLUMN user_keycloak_id SET NOT NULL;
ALTER TABLE user_exercise_preference ALTER COLUMN user_keycloak_id SET NOT NULL;
ALTER TABLE user_one_rep_max ALTER COLUMN user_keycloak_id SET NOT NULL;
ALTER TABLE program ALTER COLUMN user_keycloak_id SET NOT NULL;
ALTER TABLE exercise_rotation_history ALTER COLUMN user_keycloak_id SET NOT NULL;
ALTER TABLE user_weak_muscle ALTER COLUMN user_keycloak_id SET NOT NULL;
ALTER TABLE user_weight_unit_preference ALTER COLUMN user_keycloak_id SET NOT NULL;

-- Step 8: Drop old foreign key constraints
ALTER TABLE user_equipment DROP CONSTRAINT fk_user_equipment_user;
ALTER TABLE user_program_preferences DROP CONSTRAINT fk_user_program_preferences_user;
ALTER TABLE user_exercise_preference DROP CONSTRAINT fk_user_exercise_preference_user;
ALTER TABLE user_one_rep_max DROP CONSTRAINT fk_user_one_rep_max_user;
ALTER TABLE program DROP CONSTRAINT fk_program_user;
ALTER TABLE exercise_rotation_history DROP CONSTRAINT fk_exercise_rotation_history_user;
ALTER TABLE user_weak_muscle DROP CONSTRAINT fk_user_weak_muscle_user;
ALTER TABLE user_weight_unit_preference DROP CONSTRAINT fk_user_weight_unit_preference_user;

-- Step 9: Drop old user_id columns
ALTER TABLE user_equipment DROP COLUMN user_id;
ALTER TABLE user_program_preferences DROP COLUMN user_id;
ALTER TABLE user_exercise_preference DROP COLUMN user_id;
ALTER TABLE user_one_rep_max DROP COLUMN user_id;
ALTER TABLE program DROP COLUMN user_id;
ALTER TABLE exercise_rotation_history DROP COLUMN user_id;
ALTER TABLE user_weak_muscle DROP COLUMN user_id;
ALTER TABLE user_weight_unit_preference DROP COLUMN user_id;

-- Step 10: Rename new columns to standard naming
ALTER TABLE user_equipment RENAME COLUMN user_keycloak_id TO user_id;
ALTER TABLE user_program_preferences RENAME COLUMN user_keycloak_id TO user_id;
ALTER TABLE user_exercise_preference RENAME COLUMN user_keycloak_id TO user_id;
ALTER TABLE user_one_rep_max RENAME COLUMN user_keycloak_id TO user_id;
ALTER TABLE program RENAME COLUMN user_keycloak_id TO user_id;
ALTER TABLE exercise_rotation_history RENAME COLUMN user_keycloak_id TO user_id;
ALTER TABLE user_weak_muscle RENAME COLUMN user_keycloak_id TO user_id;
ALTER TABLE user_weight_unit_preference RENAME COLUMN user_keycloak_id TO user_id;

-- Step 11: Drop old primary key and id column from user table
ALTER TABLE "user" DROP CONSTRAINT user_pkey;
ALTER TABLE "user" DROP COLUMN id;

-- Step 12: Make keycloak_id the new primary key
ALTER TABLE "user" ADD PRIMARY KEY (keycloak_id);

-- Step 13: Add new foreign key constraints
ALTER TABLE user_equipment ADD CONSTRAINT fk_user_equipment_user 
    FOREIGN KEY(user_id) REFERENCES "user"(keycloak_id) ON DELETE CASCADE;

ALTER TABLE user_program_preferences ADD CONSTRAINT fk_user_program_preferences_user 
    FOREIGN KEY(user_id) REFERENCES "user"(keycloak_id) ON DELETE CASCADE;

ALTER TABLE user_exercise_preference ADD CONSTRAINT fk_user_exercise_preference_user 
    FOREIGN KEY(user_id) REFERENCES "user"(keycloak_id) ON DELETE CASCADE;

ALTER TABLE user_one_rep_max ADD CONSTRAINT fk_user_one_rep_max_user 
    FOREIGN KEY(user_id) REFERENCES "user"(keycloak_id) ON DELETE CASCADE;

ALTER TABLE program ADD CONSTRAINT fk_program_user 
    FOREIGN KEY(user_id) REFERENCES "user"(keycloak_id) ON DELETE CASCADE;

ALTER TABLE exercise_rotation_history ADD CONSTRAINT fk_exercise_rotation_history_user 
    FOREIGN KEY(user_id) REFERENCES "user"(keycloak_id) ON DELETE CASCADE;

ALTER TABLE user_weak_muscle ADD CONSTRAINT fk_user_weak_muscle_user 
    FOREIGN KEY(user_id) REFERENCES "user"(keycloak_id) ON DELETE CASCADE;

ALTER TABLE user_weight_unit_preference ADD CONSTRAINT fk_user_weight_unit_preference_user 
    FOREIGN KEY(user_id) REFERENCES "user"(keycloak_id) ON DELETE CASCADE;

-- Step 13.5: Add back the primary key constraint for user_weight_unit_preference
ALTER TABLE user_weight_unit_preference ADD PRIMARY KEY (user_id, exercise_name);

-- Step 14: Drop old keycloak_user_id column and its index
DROP INDEX IF EXISTS idx_user_keycloak_user_id;
DROP INDEX IF EXISTS idx_user_keycloak_user_id_lookup;
ALTER TABLE "user" DROP COLUMN keycloak_user_id;

-- Step 15: Update indexes to use new column types
DROP INDEX IF EXISTS idx_user_equipment_user_id;
DROP INDEX IF EXISTS idx_user_exercise_preference_user_id;
DROP INDEX IF EXISTS idx_user_one_rep_max_user_id;
DROP INDEX IF EXISTS idx_program_user_id;
DROP INDEX IF EXISTS idx_exercise_rotation_history_user_id;
DROP INDEX IF EXISTS idx_user_weak_muscle_user_id;

CREATE INDEX idx_user_equipment_user_id ON user_equipment(user_id);
CREATE INDEX idx_user_exercise_preference_user_id ON user_exercise_preference(user_id);
CREATE INDEX idx_user_one_rep_max_user_id ON user_one_rep_max(user_id);
CREATE INDEX idx_program_user_id ON program(user_id);
CREATE INDEX idx_exercise_rotation_history_user_id ON exercise_rotation_history(user_id);
CREATE INDEX idx_user_weak_muscle_user_id ON user_weak_muscle(user_id);

-- Step 16: Update composite indexes
DROP INDEX IF EXISTS idx_user_exercise_preference_user_avoid;
DROP INDEX IF EXISTS idx_exercise_rotation_history_user_exercise;

CREATE INDEX idx_user_exercise_preference_user_avoid ON user_exercise_preference(user_id, should_avoid);
CREATE INDEX idx_exercise_rotation_history_user_exercise ON exercise_rotation_history(user_id, exercise_name); 