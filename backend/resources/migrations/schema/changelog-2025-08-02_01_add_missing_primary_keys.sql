--liquibase formatted sql

--changeset alphaeron:1 labels:prod,test
--comment: Add missing primary key constraints for tables that were affected by the Keycloak migration

-- Add primary key constraint for user_one_rep_max table
-- This constraint is needed for the ON CONFLICT clause in upsert operations
ALTER TABLE user_one_rep_max ADD PRIMARY KEY (user_id, exercise_name); 