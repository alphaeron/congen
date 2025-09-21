--liquibase formatted sql

--changeset John Matty:15 labels:prod,test
--comment: Add age, weight, and height columns to user table for physical attributes tracking.

-- Add age column (encrypted string, stores encrypted integer values)
ALTER TABLE "user" ADD COLUMN age VARCHAR(255);

-- Add weight column (encrypted string, stores encrypted integer values)
ALTER TABLE "user" ADD COLUMN weight VARCHAR(255);

-- Add height column (encrypted string, stores encrypted integer values)
ALTER TABLE "user" ADD COLUMN height VARCHAR(255);

--rollback ALTER TABLE "user" DROP COLUMN IF EXISTS age;
--rollback ALTER TABLE "user" DROP COLUMN IF EXISTS weight;
--rollback ALTER TABLE "user" DROP COLUMN IF EXISTS height;
