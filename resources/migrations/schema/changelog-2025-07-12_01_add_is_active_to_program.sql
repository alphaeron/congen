--liquibase formatted sql

--changeset John Matty:5 labels:prod,test
--comment: Add is_active column to program table to track active programs per user

ALTER TABLE program ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;

-- Add index for better performance when querying active programs
CREATE INDEX idx_program_user_active ON program(user_id, is_active);

-- Add unique constraint to ensure only one active program per user
CREATE UNIQUE INDEX idx_program_user_active_unique ON program(user_id) WHERE is_active = TRUE; 