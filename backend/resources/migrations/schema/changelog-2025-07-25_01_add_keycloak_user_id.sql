--liquibase formatted sql

--changeset John Matty:13 labels:prod,test
--comment: Add keycloak_user_id column to user table for Keycloak integration.

ALTER TABLE "user" ADD COLUMN keycloak_user_id VARCHAR(255) NOT NULL;

-- Add unique constraint to ensure one-to-one mapping between Keycloak users and application users
CREATE UNIQUE INDEX idx_user_keycloak_user_id ON "user"(keycloak_user_id) WHERE keycloak_user_id IS NOT NULL;

-- Add index for better performance when querying by Keycloak user ID
CREATE INDEX idx_user_keycloak_user_id_lookup ON "user"(keycloak_user_id);
