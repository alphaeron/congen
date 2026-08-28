--liquibase formatted sql

--changeset alphaeron:8 labels:prod,test
--comment: Remove exercise_rotation_history table as it is unused.

-- Drop indexes first
DROP INDEX IF EXISTS idx_exercise_rotation_history_user_exercise;
DROP INDEX IF EXISTS idx_exercise_rotation_history_exercise_name;
DROP INDEX IF EXISTS idx_exercise_rotation_history_user_id;

-- Drop the table (this will also drop the foreign key constraints)
DROP TABLE IF EXISTS exercise_rotation_history;
